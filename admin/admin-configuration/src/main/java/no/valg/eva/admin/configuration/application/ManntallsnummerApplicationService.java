package no.valg.eva.admin.configuration.application;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Generer_Mannntallsnummer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VOTER_NUMBER;
import static no.valg.eva.admin.util.TidtakingUtil.taTiden;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.service.cache.CacheInvalidate;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.service.ManntallsnummerDomainService;
import no.valg.eva.admin.configuration.domain.service.PapirmanntallDomainService;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.util.Service;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

@Stateless(name = "ManntallsnummerService")
@Remote(ManntallsnummerService.class)
public class ManntallsnummerApplicationService implements ManntallsnummerService {

	private static final Logger LOG = Logger.getLogger(ManntallsnummerApplicationService.class);
	private static final String FEIL_UNDER_GENERERING_AV_MANNTALLSNUMRE = "En feil oppstod under generering av manntallsnumre";
	protected static final int GENERERING_FEILET = -1;

	private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
	private ManntallsnummerDomainService manntallsnummerDomainService;
	private PapirmanntallDomainService papirmanntallDomainService;
	private VoterRepository voterRepository;

	@SuppressWarnings("unused")
	public ManntallsnummerApplicationService() {
		// CDI
	}

	@Inject
	public ManntallsnummerApplicationService(BakgrunnsjobbDomainService bakgrunnsjobbDomainService,
											 ManntallsnummerDomainService manntallsnummerDomainService,
											 PapirmanntallDomainService papirmanntallDomainService,
											 VoterRepository voterRepository) {
		this.bakgrunnsjobbDomainService = bakgrunnsjobbDomainService;
		this.manntallsnummerDomainService = manntallsnummerDomainService;
		this.papirmanntallDomainService = papirmanntallDomainService;
		this.voterRepository = voterRepository;
	}

	@Override
	@Security(accesses = { Aggregert_Manntall, Aggregert_Stemmegiving }, type = READ)
	public boolean erValgaarssifferGyldig(UserData userData, Manntallsnummer manntallsnummer) {
		return manntallsnummerDomainService.erValgaarssifferGyldig(manntallsnummer, userData.electionEvent());
	}

	@Override
	@Security(accesses = { Aggregert_Manntall, Aggregert_Stemmegiving }, type = READ)
	public Manntallsnummer beregnFulltManntallsnummer(UserData userData, Long kortManntallsnummer) {
		return manntallsnummerDomainService.beregnFulltManntallsnummer(kortManntallsnummer, userData.electionEvent());
	}

	@Override
	@Asynchronous
	@Security(accesses = Manntall_Generer_Mannntallsnummer, type = WRITE)
	@Transactional(REQUIRES_NEW)
	@CacheInvalidate(entityClass = ElectionEvent.class, entityParam = 1)
	public void genererManntallsnumre(UserData userData, Long electionEventPk) {
		kjoerSomBakgrunnsjobb(userData, () -> {
				genererManntallsnumreSideOgLinje(electionEventPk);
				regenererSideOgLinjeForRoder(userData, electionEventPk);
			});
	}

	private void kjoerSomBakgrunnsjobb(UserData userData, Service service) {
		Batch bakgrunnsjobb = lagBakgrunnsjobbForGenereringAvManntallsnumre(userData);
		try {
			service.execute();
			lagreBakgrunnsjobbSomFullfort(userData, bakgrunnsjobb);
		} catch (EvoteException e) {
			lagreBakgrunnsjobbSomFeilet(userData, bakgrunnsjobb, e);
			throw e;
		} catch (Exception e) {
			lagreBakgrunnsjobbSomFeilet(userData, bakgrunnsjobb, e);
			throw new EvoteException(FEIL_UNDER_GENERERING_AV_MANNTALLSNUMRE, e);
		}
	}

	private Batch lagBakgrunnsjobbForGenereringAvManntallsnumre(UserData userData) {
		if (bakgrunnsjobbDomainService.erManntallsnummergenereringStartetEllerFullfort(userData.electionEvent())) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0205_GENERERING_MANNTALLSNUMRE_ALLEREDE_STARTET_ELLER_FULLFORT, null);
		} else {
			return bakgrunnsjobbDomainService.lagBakgrunnsjobb(userData, VOTER_NUMBER, BATCH_STATUS_STARTED_ID, null, null);
		}
	}

	private void lagreBakgrunnsjobbSomFullfort(UserData userData, Batch bakgrunnsjobb) {
		bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, bakgrunnsjobb, BATCH_STATUS_COMPLETED_ID);
	}

	private void lagreBakgrunnsjobbSomFeilet(final UserData userData, final Batch batch, final Exception e) {
		LOG.error(FEIL_UNDER_GENERERING_AV_MANNTALLSNUMRE, e);
		bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, batch, BATCH_STATUS_FAILED_ID);
	}

	private void genererManntallsnumreSideOgLinje(Long electionEventPk) {
		taTiden(LOG, "Generering av manntallsnumre, side og linje", () -> {
			int mantallsnummergenreringStatus = voterRepository.genererManntallsnumre(electionEventPk);
			if (mantallsnummergenreringStatus == GENERERING_FEILET) {
				throw new EvoteException("Database-funksjon for generering av manntallsnumre feilet");
			}
		});
	}

	private void regenererSideOgLinjeForRoder(UserData userData, Long electionEventPk) {
		taTiden(LOG, "Regenerering av side og linje for kretser med roder", () -> {
			LOG.info("Starter regenerering av side og linje for roder");
			papirmanntallDomainService.regenererSideOgLinjeForRoder(userData, electionEventPk);
		});
	}

	@Override
	@Security(accesses = Manntall_Generer_Mannntallsnummer, type = READ)
	public ManntallsnummergenereringStatus hentManntallsnummergenereringStatus(UserData userData, ElectionEvent electionEvent) {
		if (!(electionEvent.getElectionEventStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA)) {
			return ManntallsnummergenereringStatus.VALGHENDELSE_LAAST;
		}
		if (!voterRepository.areVotersInElectionEvent(electionEvent.getPk())) {
			return ManntallsnummergenereringStatus.INGEN_VELGERE;
		}
		if (electionEvent.getElectoralRollCutOffDate() != null && electionEvent.getElectoralRollCutOffDate().isAfter(LocalDate.now())) {
			return ManntallsnummergenereringStatus.SKJARINGSDATO_I_FREMTIDEN;
		}
		if (bakgrunnsjobbDomainService.erManntallsnummergenereringStartetEllerFullfort(electionEvent)) {
			return ManntallsnummergenereringStatus.ALLEREDE_GENERERT;
		}
		return ManntallsnummergenereringStatus.OK;
	}
}
