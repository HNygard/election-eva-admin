package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.evote.exception.EvoteException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.ElectoralRollAuditEvent;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.manntall.ValgkortgrunnlagFactory;
import no.valg.eva.admin.configuration.domain.model.manntall.ValgkortgrunnlagRadCsvMapper;
import no.valg.eva.admin.configuration.domain.model.manntall.ValgkortgrunnlagStatistikk;
import no.valg.eva.admin.configuration.domain.model.manntall.ValgkortgrunnlagVelgerFilter;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.evote.util.EvoteProperties.NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER;
import static no.evote.util.EvoteProperties.NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER_DEFAULT;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.GenererValgkortgrunnlagJobbFerdig;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.GenererValgkortgrunnlagJobbStartet;
import static no.valg.eva.admin.common.auditlog.Outcome.GenericError;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;

/**
 * Tjenester relatert til generering av valgkortgrunnlag
 * 
 * DEV-NOTES:
 * - Denne tjenesten er på mange måter en eksport-funksjon, og slik sett passer den kanskje utenfor domenet?
 *   Det hadde kanskje vært riktigere om den lå i en integrasjonsmodul, eller en ports/adapters-sak
 */
public class ValgkortgrunnlagDomainService {

	private static final long MILLISEKUNDER_PER_SEKUND = 1000;
	private static final Logger LOG = Logger.getLogger(ValgkortgrunnlagDomainService.class); 
	
	private VoterRepository voterRepository;
	private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
	private ManntallsnummerDomainService manntallsnummerDomainService;
	private ReportingUnitRepository reportingUnitRepository;
	private PollingPlaceRepository pollingPlaceRepository;
	private ContestAreaRepository contestAreaRepository;
	private ContestRepository contestRepository;
	private AuditLogServiceBean auditLogServiceBean;
	
	@Inject
	public ValgkortgrunnlagDomainService(VoterRepository voterRepository,
										 BakgrunnsjobbDomainService bakgrunnsjobbDomainService,
										 ManntallsnummerDomainService manntallsnummerDomainService,
										 ReportingUnitRepository reportingUnitRepository,
										 PollingPlaceRepository pollingPlaceRepository,
										 ContestAreaRepository contestAreaRepository,
										 ContestRepository contestRepository,
										 AuditLogServiceBean auditLogServiceBean) {
		this.voterRepository = voterRepository;
		this.bakgrunnsjobbDomainService = bakgrunnsjobbDomainService;
		this.manntallsnummerDomainService = manntallsnummerDomainService;
		this.reportingUnitRepository = reportingUnitRepository;
		this.pollingPlaceRepository = pollingPlaceRepository;
		this.contestAreaRepository = contestAreaRepository;
		this.contestRepository = contestRepository;
		this.auditLogServiceBean = auditLogServiceBean;
	}

	public Path genererValgkortgrunnlag(UserData userData, boolean tillatVelgereIkkeTilknyttetValgdistrikt) {
		return genererValgkortgrunnlag(userData, genererFilnavn(), tillatVelgereIkkeTilknyttetValgdistrikt);
	}

	private Path genererFilnavn() {
		String folder = EvoteProperties.getProperty(NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER, NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER_DEFAULT);
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HHmmss");
		return Paths.get(folder + "/" + "valgkortgrunnlag_" + dateTimeFormatter.print(DateTime.now()) + ".csv");
	}

	//           P.t. ser det ut til at ca 64GB er nødvendig for å kjøre effektivt
	//           En fremtidig løsning på dette bør være at man bruker streams-APIet i nyere
	//           Dersom det skal gjøres forsøk med lavere stemmerettsalder er det mulig at noe av koden her trenger utvidelse
	Path genererValgkortgrunnlag(UserData userData, Path fil, boolean tillatVelgereIkkeTilknyttetValgdistrikt) {
		LOG.info("Generering av valgkortgrunnlag starter");
		leggTilAuditlogg(userData, fil, GenererValgkortgrunnlagJobbStartet, Success, null);

		ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk = new ValgkortgrunnlagStatistikk().initializeStatistics();
		Batch bakgrunnsJobb = lagBakgrunnsjobb(userData, fil);
		try {
			genererValgkortgrunnlagsfil(userData, fil, tillatVelgereIkkeTilknyttetValgdistrikt, valgkortgrunnlagStatistikk);
			oppdaterBakgrunnsjobb(userData, bakgrunnsJobb, BATCH_STATUS_COMPLETED_ID);
			leggTilAuditlogg(userData, fil, GenererValgkortgrunnlagJobbFerdig, Success, null);
			valgkortgrunnlagStatistikk.loggStatistikkForGenerering();
		} catch (Exception e) {
			oppdaterBakgrunnsjobb(userData, bakgrunnsJobb, BATCH_STATUS_FAILED_ID);
			leggTilAuditlogg(userData, fil, GenererValgkortgrunnlagJobbFerdig, GenericError, e.getMessage());
			LOG.error("Generering av valgkortgrunnlag feilet", e);
		}
		return fil;
	}

	private void leggTilAuditlogg(UserData userData, Path fil, AuditEventTypes auditEventType, Outcome resultat, String detail) {
		auditLogServiceBean.addToAuditTrail(new ElectoralRollAuditEvent(userData, fil.toString(), auditEventType, resultat, detail));
	}

	private Batch lagBakgrunnsjobb(UserData userData, Path fil) {
		return bakgrunnsjobbDomainService.lagBakgrunnsjobb(userData, Jobbkategori.VALGKORTUNDERLAG, BATCH_STATUS_STARTED_ID, null, fil.toString());
	}

	private void genererValgkortgrunnlagsfil(UserData userData, Path fil, boolean tillatVelgereIkkeTilknyttetValgdistrikt,
                                             ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) throws IOException {
		ElectionEvent valghendelse = userData.electionEvent();
		List<Voter> velgere = hentVelgere(valghendelse, valgkortgrunnlagStatistikk);
		validerVelgere(velgere, tillatVelgereIkkeTilknyttetValgdistrikt, valgkortgrunnlagStatistikk);
		ValgkortgrunnlagFactory valgkortgrunnlagFactory = forberedGenerering(valghendelse);
		List<Voter> velgereSomSkalHaValgkort = filtrerVelgere(velgere, valgkortgrunnlagFactory, valgkortgrunnlagStatistikk);
		List<String> csv = genererValgkortCsv(velgereSomSkalHaValgkort, valgkortgrunnlagFactory, valgkortgrunnlagStatistikk);
		skrivTilFil(fil, velgere, csv, valgkortgrunnlagStatistikk);
	}

    private List<Voter> hentVelgere(ElectionEvent valghendelse, ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) {
		List<Voter> velgere = voterRepository.findVotersForValgkortgrunnlag(valghendelse);
		valgkortgrunnlagStatistikk.setAntallVelgereTotalt(velgere.size());
		LOG.debug("Uthenting av velgere fra databasen ferdig");
		return velgere;
	}

	private void validerVelgere(List<Voter> velgere, boolean tillatVelgereIkkeTilknyttetValgdistrikt, ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) {
		boolean valideringFeilet = false;

		for (Voter velger : velgere) {
			if (velger.getNumber() == null && !velger.isNotInElectoralRollAnymore()) {
				if (!tillatVelgereIkkeTilknyttetValgdistrikt) {
					LOG.error("Velger " + velger.getId() + " mangler manntallsnummer. Dette skal normalt ikke skje, og tyder på korrupte data. "
						+ "Dersom valghendelsen har velgere som ikke er tilknyttet et valgdistrikt (feks som i det ekstraordinære kommunestyrevalget i Færder 2017) "
						+ "kan du bruke funksjonen for å tillate velgere som ikke er tilknyttet valgdistrikt når du starter genereringen");
					valideringFeilet = true;
				}
				valgkortgrunnlagStatistikk.setAntallVelgereUtenManntallsnummer(valgkortgrunnlagStatistikk.getAntallVelgereUtenManntallsnummer() + 1);
			}
		}
		
		if (valideringFeilet) {
			throw new EvoteException("Validering av velgere feilet. Generering av valgkortgrunnlag kan ikke gjennomføres");
		}
	}

    private ValgkortgrunnlagFactory forberedGenerering(ElectionEvent valghendelse) {
		List<ReportingUnit> valgstyrer = reportingUnitRepository.findAlleValgstyrerIValghendelse(valghendelse);
		LOG.debug("Uthenting av valgstyrer ferdig");
		int valgaarssiffer = manntallsnummerDomainService.valgaarssifferForValghendelse(valghendelse);
		LOG.debug("Uthenting av valgårssiffer ferdig");
		List<PollingPlace> stemmestederMedAapningstider = pollingPlaceRepository.findPollingPlacesWithOpeningHours(valghendelse);
		LOG.debug("Uthenting av stemmesteder, åpningstider og valgdager ferdig");
		boolean harMultiomraadedistrikter = contestRepository.antallMultiomraadedistrikter(valghendelse) > 0;
		LOG.debug("Uthenting av info om multiområdedistrikter ferdig");
		List<ContestArea> valgdistriktsomraader = contestAreaRepository.finnForValghendelseMedValgdistrikt(valghendelse);
		LOG.debug("Uthenting av valgdistriktsområder ferdig");
		List<ReportingUnit> opptellingsvalgstyrer = reportingUnitRepository.finnOpptellingsvalgstyrer(valghendelse);
		LOG.debug("Uthenting av " + opptellingsvalgstyrer.size() + " opptellingsvalgstyrer ferdig");
		
		return new ValgkortgrunnlagFactory(valgstyrer, valgaarssiffer, stemmestederMedAapningstider, harMultiomraadedistrikter, valgdistriktsomraader,
			opptellingsvalgstyrer);
	}

    private List<Voter> filtrerVelgere(List<Voter> velgere, ValgkortgrunnlagFactory valgkortgrunnlagFactory, ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) {
        ValgkortgrunnlagVelgerFilter valgkortgrunnlagVelgerFilter = new ValgkortgrunnlagVelgerFilter(valgkortgrunnlagFactory, valgkortgrunnlagStatistikk);
        return valgkortgrunnlagVelgerFilter.filter(velgere);
    }

    private List<String> genererValgkortCsv(List<Voter> velgere, ValgkortgrunnlagFactory valgkortgrunnlagFactory,
                                            ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) {
		DateTime startKonvertering = DateTime.now();
		List<String> csv = velgere.stream()
			.map(valgkortgrunnlagFactory::tilValgkortgrunnlagRad)
			.map(ValgkortgrunnlagRadCsvMapper::tilCsv)
			.collect(Collectors.toList());
		LOG.debug("Generering av valgkortdata ferdig. " + varighetOgHastighet(startKonvertering, velgere.size()));
		valgkortgrunnlagStatistikk.setAntallVelgereEksportertTilValgkort(csv.size());
		return csv;
	}

	private String varighetOgHastighet(DateTime startTidspunkt, int antall) {
		Period periode = new Duration(startTidspunkt, DateTime.now()).toPeriod();
		long hastighet = (MILLISEKUNDER_PER_SEKUND * antall) / (periode.toStandardDuration().getMillis() + 1);
		return "Tid brukt: " + PeriodFormat.getDefault().print(periode) + ". Hastighet: " + hastighet + " rader per sekund";
	}

	private void skrivTilFil(Path fil, List<Voter> velgere, List<String> csv, ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) throws IOException {
		Files.write(fil, csv, StandardCharsets.UTF_8);
		LOG.debug("Valgkortdata er skrevet til fil " + fil.toString() + ". Total " + varighetOgHastighet(valgkortgrunnlagStatistikk.getStartJobb(), velgere.size()));
	}

	private void oppdaterBakgrunnsjobb(UserData userData, Batch bakgrunnsjobb, int jobbstatusId) {
		bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, bakgrunnsjobb, jobbstatusId);
	}

	public GenererValgkortgrunnlagStatus sjekkForutsetningerForGenerering(ElectionEvent electionEvent) {
		if (manntallErTomt(electionEvent)) {
			return GenererValgkortgrunnlagStatus.TOMT_MANNTALL;
		} else if (!erManntallsnummergenereringFullfortUtenFeil(electionEvent)) {
			return GenererValgkortgrunnlagStatus.MANNTALLSNUMRE_MANGLER;
		} else {
			return GenererValgkortgrunnlagStatus.OK;
		}
	}

	private boolean manntallErTomt(ElectionEvent electionEvent) {
		return !voterRepository.areVotersInElectionEvent(electionEvent.getPk());
	}

	private boolean erManntallsnummergenereringFullfortUtenFeil(ElectionEvent electionEvent) {
		return bakgrunnsjobbDomainService.erManntallsnummergenereringFullfortUtenFeil(electionEvent);
	}
}
