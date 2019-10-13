package no.valg.eva.admin.valgnatt.application.service;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.i18n.MessageProvider;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.valgnatt.ValgnattAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.valgnatt.ValgnattAuditable;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.counting.port.adapter.service.valgnatt.ValgnattApi;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates.CandidatesReport;
import no.valg.eva.admin.valgnatt.domain.model.resultat.Resultatskjema;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata.ValgnattCandidateDomainService;
import no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata.ValgnattElectoralRollDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.oppgjørsskjema.OppgjørsskjemaDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.stemmeskjema.StemmeskjemaDomainService;
import no.valg.eva.admin.valgnatt.domain.service.valgnattrapport.RapporteringsstatusDomainService;
import no.valg.eva.admin.valgnatt.domain.service.valgnattrapport.ValgnattrapportDomainService;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;
import org.joda.time.DateTime;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.rbac.Accesses.Resultat_Konfigurasjon;
import static no.valg.eva.admin.common.rbac.Accesses.Resultat_Rapporter;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ValgnattReportService")
@Default
@Remote(ValgnattReportService.class)
@NoArgsConstructor
@Log4j
public class ValgnattReportApplicationService implements ValgnattReportService {
	
	private static final long serialVersionUID = 8384469799572317958L;

	@Inject
	private transient ValgnattElectoralRollDomainService valgnattElectoralRollDomainService;
	@Inject
	private transient ValgnattApi valgnattApi;
	@Inject
	private transient AuditLogServiceBean auditLogService;
	@Inject
	private transient MvElectionRepository mvElectionRepository;
	@Inject
	private transient MvAreaRepository mvAreaRepository;
	@Inject
	private transient BallotRepository ballotRepository;
	@Inject
	private transient CandidateRepository candidateRepository;
	@Inject
	private transient StemmeskjemaDomainService stemmeskjemaDomainService;
	@Inject
	private transient OppgjørsskjemaDomainService oppgjørsskjemaDomainService;
	@Inject
	private transient ValgnattrapportDomainService valgnattrapportDomainService;
	@Inject
	private transient ValgnattrapportRepository valgnattrapportRepository;
	@Inject
	private transient RapporteringsstatusDomainService rapporteringsstatusDomainService;

	@Override
	@Security(accesses = Resultat_Konfigurasjon, type = WRITE)
	public void exportGeographyAndVoters(UserData userData, ElectionPath contestElectionPath) {
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(contestElectionPath.tilValghierarkiSti());
		Valgnattrapport valgnattrapport = findValgnattrapport(mvElectionContest.getElection(), ReportType.GEOGRAFI_STEMMEBERETTIGEDE);
		uploadAndLog(userData, valgnattElectoralRollDomainService.findVotersAndReportingAreas(mvElectionContest), valgnattrapport);
	}

	@Override
	@Security(accesses = Resultat_Konfigurasjon, type = READ)
	public List<Valgnattrapportering> rapporteringerForGrunnlagsdata(UserData userData, ElectionPath electionPath) {
		electionPath.assertElectionLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		return ValgnattrapportMapper
				.toValgnattrapporteringList(valgnattrapportDomainService.grunnlagsdataRapporter(mvElection.getElection()));
	}

	@Override
	@SecurityNone
	public boolean kanFylketRapportere(UserData userData, ElectionPath contestPath) {
		return rapporteringsstatusDomainService.kanFylketRapportere(contestPath);
	}

	@Override
	@Security(accesses = Resultat_Konfigurasjon, type = WRITE)
	public void exportPartiesAndCandidates(UserData userData, ElectionPath electionPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		Set<Contest> contests = mvElection.getElection().getContests();
		boolean sametingsvalg = !mvElection.getElection().isSingleArea();
		CandidatesReport candidatesReport = new ValgnattCandidateDomainService(contests, sametingsvalg, ballotRepository, candidateRepository)
				.createCandidatesReport();
		Valgnattrapport ssbReport = findValgnattrapport(mvElection.getElection(), ReportType.PARTIER_OG_KANDIDATER);
		uploadAndLog(userData, candidatesReport, ssbReport);
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = READ)
	public long antallRapporterbare(UserData userData, ElectionPath contestElectionPath, AreaPath reportingAreaPath) {
		if (rapporteringsstatusDomainService.erValgPaaBydelsnivaa(contestElectionPath, reportingAreaPath)) {
			return 0;
		}
		
		if (rapporteringsstatusDomainService.brukStatusforOppgjorOgStemmeskjema(contestElectionPath, reportingAreaPath)) {
			return rapporteringsstatusDomainService.kanFylketRapportere(contestElectionPath)
					? rapporteringsstatusDomainService.antallRapporterbareOppgjorsskjema()
					: antallRapporterbareStemmeskjema(contestElectionPath, reportingAreaPath);
		}

		if (rapporteringsstatusDomainService.brukStatusForStemmeskjema(reportingAreaPath)) {
			return antallRapporterbareStemmeskjema(contestElectionPath, reportingAreaPath);
		}
		if (rapporteringsstatusDomainService.brukStatusForOppgjorsskjema(contestElectionPath, reportingAreaPath)) {
			return rapporteringsstatusDomainService.antallRapporterbareOppgjorsskjema();
		}
		return 0;
	}

	private long antallRapporterbareStemmeskjema(ElectionPath contestElectionPath, AreaPath reportingAreaPath) {
		contestElectionPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(contestElectionPath.tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(reportingAreaPath);
		return valgnattrapportDomainService.antallRapporterbareStemmeskjemaRapporter(mvElection, mvArea.getMunicipality());
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = READ)
	public boolean altErRapportert(UserData userData, ElectionPath contestElectionPath, AreaPath reportingAreaPath) {
		if (rapporteringsstatusDomainService.brukStatusforOppgjorOgStemmeskjema(contestElectionPath, reportingAreaPath)) {
			return erAlleStemmeskjemaRapportert(contestElectionPath, reportingAreaPath)
					&& erAlleOppgjorsskjemaRapportert(userData, contestElectionPath, reportingAreaPath);
		}

		if (rapporteringsstatusDomainService.brukStatusForStemmeskjema(reportingAreaPath)) {
			return erAlleStemmeskjemaRapportert(contestElectionPath, reportingAreaPath);
		}
		return rapporteringsstatusDomainService.brukStatusForOppgjorsskjema(contestElectionPath, reportingAreaPath)
				&& erAlleOppgjorsskjemaRapportert(userData, contestElectionPath, reportingAreaPath);
	}

	private boolean erAlleOppgjorsskjemaRapportert(UserData userData, ElectionPath electionPath, AreaPath reportingAreaPath) {
		Valgnattrapportering valgnattrapportering = rapporteringerForOppgjorsskjema(userData, electionPath, reportingAreaPath);
		return valgnattrapportering.ferdigRapportert();
	}

	private boolean erAlleStemmeskjemaRapportert(ElectionPath electionPath, AreaPath reportingAreaPath) {
		electionPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(reportingAreaPath);
		return valgnattrapportDomainService.erAlleStemmeskjemaRapportert(mvElection, mvArea.getMunicipality());
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = READ)
	public List<Valgnattrapportering> rapporteringerForStemmeskjema(UserData userData, ElectionPath electionPath, AreaPath areaPath) {
		electionPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		return ValgnattrapportMapper.toSortedValgnattrapportingList(valgnattrapportDomainService.stemmeskjemaRapporter(mvElection, mvArea.getMunicipality()));
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = WRITE)
	public void rapporterStemmeskjema(UserData userData, ElectionPath contestPath, AreaPath areaPath, Valgnattrapportering valgnattrapportering) {
		contestPath.assertContestLevel();
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		MvArea reportForArea = mvAreaRepository.findSingleByPath(areaPath);
		Valgnattrapport valgnattrapport = valgnattrapportRepository.byPk(valgnattrapportering.getValgnattrapportPk());
		Resultatskjema stemmeskjema = stemmeskjemaDomainService.fraMøtebok(mvElectionContest, reportForArea);
		uploadAndLog(userData, stemmeskjema, valgnattrapport);
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = WRITE)
	public void rapporterOppgjørsskjema(UserData userData, ElectionPath contestPath, AreaPath areaPath, Valgnattrapportering valgnattrapportering) {
		contestPath.assertContestLevel();
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		MvArea reportForArea = mvAreaRepository.findSingleByPath(areaPath);
		Valgnattrapport valgnattrapport = valgnattrapportRepository.byPk(valgnattrapportering.getValgnattrapportPk());

		oppgjørsskjemaDomainService.fraMøtebokOgValgoppgjør(mvElectionContest, reportForArea)
				.forEach(oppgjørsskjema -> uploadAndLog(userData, oppgjørsskjema, valgnattrapport));

		valgnattrapportDomainService.markerStemmeskjemaRapportert(mvElectionContest);
	}

	@Override
	@Security(accesses = Resultat_Rapporter, type = READ)
	public Valgnattrapportering rapporteringerForOppgjorsskjema(UserData userData, ElectionPath electionPath, AreaPath areaPath) {
		electionPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		MvArea reportForArea = mvAreaRepository.findSingleByPath(areaPath);
		return ValgnattrapportMapper.toValgnattrapporteringList(singletonList(valgnattrapportDomainService.oppgjorsskjemaRapporter(mvElection, reportForArea)))
				.get(0);
	}

	private Valgnattrapport findValgnattrapport(Election election, ReportType reportType) {
		return valgnattrapportRepository.byElectionAndReportType(election, reportType);
	}

	private void uploadAndLog(UserData userData, ValgnattAuditable valgnattAuditable, Valgnattrapport valgnattrapport) {
		ValgnattAuditEvent valgnattAuditEvent = null;
		try {
			valgnattApi.upload(valgnattAuditable.toJson());
			valgnattrapport.oppdaterTilStatusOk();
			valgnattAuditEvent = makeValgnattAuditEvent(userData, valgnattAuditable, Outcome.Success);
		} catch (Exception e) {
			valgnattAuditEvent = makeValgnattAuditEvent(userData, valgnattAuditable, Outcome.GenericError);
			log.error(e.getMessage(), e);
			if (e.getClass() == BadRequestException.class) {
				String feilmelding = ((BadRequestException) e).getResponse().readEntity(String.class);
				feilmelding = feilmelding.substring(feilmelding.indexOf("<body>") + "<body>".length(), feilmelding.indexOf("</body>"));
				throw new EvoteException(new UserMessage(feilmelding));
			} else if (e.getClass() == ProcessingException.class || e.getClass() == NotFoundException.class) {
				String valgnattBaseUrl = EvoteProperties.getProperty(EvoteProperties.VALGNATT_BASE_URL);
				log.error("Kunne ikke koble til " + valgnattBaseUrl);
				Locale locale = userData.getJavaLocale();
				String melding = MessageProvider.get(locale, "@valgresultat.tilkoblingsfeil");
				throw new EvoteException(new UserMessage(melding));	
			} else {
				throw e;
			}
		} finally {
			auditLogService.addToAuditTrail(valgnattAuditEvent);
		}
	}

	private ValgnattAuditEvent makeValgnattAuditEvent(UserData userData, ValgnattAuditable valgnattAuditable, Outcome outcome) {
		return new ValgnattAuditEvent(userData, DateTime.now(), AuditEventTypes.GenerateReport, Process.COUNTING, outcome, valgnattAuditable);
	}

}
