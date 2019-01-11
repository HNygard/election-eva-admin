package no.evote.service.backendmock;

import no.evote.service.configuration.ContestServiceBean;
import no.evote.service.configuration.ElectionServiceBean;
import no.evote.service.configuration.MvAreaServiceBean;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.configuration.application.ElectionMapper;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.counting.domain.event.TellingEndrerStatus;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindPreliminaryCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindProtocolCountService;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ElectionDayRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata.ValgnattElectoralRollDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk.ValgnattstatistikkDomainService;
import no.valg.eva.admin.valgnatt.repository.StemmegivningsstatistikkRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Can be used in BackendContainer for wiring a bean or service which uses constructor injection and does not have a default constructor.
 */
public class ConstructorInjectedBeanProducer {

	// burde kanskje brukt constructor injection, men, men..
	@Inject
	private ContestServiceBean contestService;
	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ElectionMapper electionMapper;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private PartyCategoryRepository partyCategoryRepository;
	@Inject
	private MvAreaServiceBean mvAreaService;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private BallotRejectionRepository ballotRejectionRepository;
	@Inject
	private ElectionDayRepository electionDayRepository;
	@Inject
	private ManualContestVotingRepository manualContestVotingRepository;
	@Inject
	private no.valg.eva.admin.counting.repository.VotingRepository votingRepository2;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private ReportCountCategoryRepository reportCountCategoryRepository;
	@Inject
	private CountingCodeValueRepository countingCodeValueRepository;
	@Inject
	private BallotRepository ballotRepository;
	@Inject
	private ContestReportRepository contestReportRepository;
	@Inject
	private VoteCountRepository voteCountRepository;
	@Inject
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	@Inject
	private AntallStemmesedlerLagtTilSideRepository antallStemmesedlerLagtTilSideRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private Event<TellingEndrerStatus> tellingGodkjentEvent;
	@Inject
	private EligibilityRepository eligibilityRepository;
	@Inject
	private StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository;
	@Inject
	private ValgnattElectoralRollRepository valgnattElectoralRollRepository;
	@Inject
	private BatchRepository batchRepository;

	@Produces
	public AntallStemmesedlerLagtTilSideDomainService getAntallStemmesedlerLagtTilSideDomainService() {
		return new AntallStemmesedlerLagtTilSideDomainService(
				antallStemmesedlerLagtTilSideRepository, contestReportRepository, mvElectionRepository, mvAreaRepository);
	}

	@Produces
	public ElectionServiceBean getElectionServiceBean() {
		return new ElectionServiceBean(contestService, electionRepository, electionMapper, mvElectionRepository);
	}

	@Produces
	public ReportingUnitDomainService getReportingUnitDomainService() {
		return new ReportingUnitDomainService(reportingUnitRepository);
	}

	@Produces
	public PartyMapper getPartyMapper() {
		return new PartyMapper(partyCategoryRepository, localeTextRepository, mvAreaRepository);
	}

	@Produces
	public VotingServiceBean getVotingService() {
		return new VotingServiceBean(mvAreaService, pollingDistrictRepository, pollingPlaceRepository, votingRepository,
				voterRepository, eligibilityRepository, mvElectionRepository);
	}

	@Produces
	public FindCountService getFindFinalCountService() {
		return new FindCountService(getVoteCountService(), reportingUnitRepository, affiliationRepository, ballotRejectionRepository,
				getReportingUnitDomainService(), mvElectionRepository, antallStemmesedlerLagtTilSideDomainService);
	}

	@Produces
	public FindPreliminaryCountService getFindPreliminaryCountService() {
		return new FindPreliminaryCountService(getVoteCountService(), reportingUnitRepository, affiliationRepository, electionDayRepository,
				manualContestVotingRepository, votingRepository2, getReportingUnitDomainService(), antallStemmesedlerLagtTilSideDomainService);
	}

	@Produces
	public FindProtocolCountService getFindProtocolCountService() {
		return new FindProtocolCountService(mvAreaRepository, getVoteCountService(), reportingUnitRepository, votingRepository2, electionDayRepository,
				manualContestVotingRepository);
	}

	@Produces
	public VoteCountService getVoteCountService() {
		return new VoteCountService(
				reportingUnitRepository,
				reportCountCategoryRepository,
				votingRepository2,
				manualContestVotingRepository,
				countingCodeValueRepository,
				mvElectionRepository,
				mvAreaRepository,
				ballotRepository,
				ballotRejectionRepository,
				contestReportRepository,
				voteCountRepository,
				getReportingUnitDomainService(),
				getAntallStemmesedlerLagtTilSideDomainService(),
				getVoteCountGodkjentEventTrigger());
	}

	@Produces
	public VoteCountStatusendringTrigger getVoteCountGodkjentEventTrigger() {
		return new VoteCountStatusendringTrigger(tellingGodkjentEvent);
	}

	@Produces
	public ValgnattstatistikkDomainService getValgnattstatistikkDomainService() {
		return new ValgnattstatistikkDomainService(stemmegivningsstatistikkRepository, getRapporteringsområdeDomainService());
	}

	@Produces
	public RapporteringsområdeDomainService getRapporteringsområdeDomainService() {
		return new RapporteringsområdeDomainService(getVoteCountService(), mvAreaRepository);
	}

	@Produces
	public ValgnattElectoralRollDomainService getValgnattElectoralRollDomainService() {
		return new ValgnattElectoralRollDomainService(valgnattElectoralRollRepository, pollingDistrictRepository);
	}
	
	@Produces 
	public BakgrunnsjobbDomainService getBakgrunnsjobbDomainService() {
		return new BakgrunnsjobbDomainService(batchRepository);
	}

}
