package no.evote.service.backendmock;

import lombok.Getter;
import no.evote.service.BatchServiceBean;
import no.evote.service.BinaryDataServiceBean;
import no.evote.service.CertificateService;
import no.evote.service.CryptoServiceBean;
import no.evote.service.ExportServiceBean;
import no.evote.service.LocaleTextServiceBean;
import no.evote.service.SigningKeyServiceBean;
import no.evote.service.TestService;
import no.evote.service.TestServiceEjb;
import no.evote.service.TranslationServiceBean;
import no.evote.service.configuration.AffiliationServiceBean;
import no.evote.service.configuration.AreaImportServiceBean;
import no.evote.service.configuration.BallotServiceBean;
import no.evote.service.configuration.BoroughServiceBean;
import no.evote.service.configuration.CandidateServiceBean;
import no.evote.service.configuration.ContestServiceBean;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.configuration.ElectionGroupServiceBean;
import no.evote.service.configuration.ElectionServiceBean;
import no.evote.service.configuration.FullElectoralRollImporter;
import no.evote.service.configuration.ImportElectoralRollService;
import no.evote.service.configuration.ImportElectoralRollServiceEjb;
import no.evote.service.configuration.IncrementalElectoralRollImporter;
import no.evote.service.configuration.LegacyListProposalService;
import no.evote.service.configuration.LegacyListProposalServiceEjb;
import no.evote.service.configuration.MvAreaServiceBean;
import no.evote.service.configuration.MvElectionServiceBean;
import no.evote.service.configuration.PartyServiceBean;
import no.evote.service.configuration.PollingDistrictServiceBean;
import no.evote.service.configuration.PollingStationServiceBean;
import no.evote.service.configuration.ProposerService;
import no.evote.service.configuration.ProposerServiceBean;
import no.evote.service.configuration.ProposerServiceEjb;
import no.evote.service.configuration.ReportCountCategoryServiceBean;
import no.evote.service.configuration.ReportingUnitServiceBean;
import no.evote.service.configuration.VoteCountCategoryServiceBean;
import no.evote.service.configuration.VoterServiceBean;
import no.evote.service.counting.CountingImportServiceBean;
import no.evote.service.counting.LegacyCountingServiceBean;
import no.evote.service.rbac.LegacyAccessServiceBean;
import no.evote.service.rbac.OperatorRoleServiceBean;
import no.evote.service.rbac.OperatorServiceBean;
import no.evote.service.rbac.RoleServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.evote.service.security.SystemPasswordStore;
import no.evote.service.util.ExportImportOperatorsServiceBean;
import no.evote.service.util.RoleExporterImporter;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.backend.common.repository.TextIdRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.backend.rbac.RBACAuthenticator;
import no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.configuration.application.ElectionApplicationService;
import no.valg.eva.admin.configuration.application.ElectionGroupApplicationService;
import no.valg.eva.admin.configuration.application.ElectionGroupMapper;
import no.valg.eva.admin.configuration.application.ManntallsnummerApplicationService;
import no.valg.eva.admin.configuration.application.MunicipalityApplicationService;
import no.valg.eva.admin.configuration.application.VoterImportBatchServiceLocalEjb;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.domain.service.MunicipalityDomainService;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.domain.service.VelgerDomainService;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import no.valg.eva.admin.configuration.repository.LegacyPollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionReportingUnitsRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.OpeningHoursRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import no.valg.eva.admin.configuration.repository.VoteCategoryRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.counting.application.CountingApplicationService;
import no.valg.eva.admin.counting.application.ModifiedBallotBatchApplicationService;
import no.valg.eva.admin.counting.domain.modifiedballots.ModifiedBallotDomainService;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindPreliminaryCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindProtocolCountService;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.domain.validation.GetCountsValidator;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.counting.repository.BallotCountRepository;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.CandidateVoteRepository;
import no.valg.eva.admin.counting.repository.CastBallotRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ElectionDayRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.rbac.domain.RoleAreaService;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.rbac.service.AccessServiceBean;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata.ValgnattElectoralRollDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk.ValgnattstatistikkDomainService;
import no.valg.eva.admin.valgnatt.repository.StemmegivningsstatistikkRepository;
import no.valg.eva.admin.voting.domain.electoralroll.EligibleVoterDomainService;
import no.valg.eva.admin.voting.domain.service.VotingRegistrationDomainService;
import no.valg.eva.admin.voting.repository.PagingVotingRepository;
import no.valg.eva.admin.voting.repository.VotingRejectionRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.repository.impl.DefaultVotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;

import javax.ejb.SessionContext;
import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * Holds correctly wired services, for use in repository tests.
 */
@SuppressWarnings("unused")
@Getter
public class BackendContainer {

    private final BackendWirer wirer;
	
    @Wired
    private AccessRepository accessRepository;
    @Wired
    private AccessServiceBean accessService;
    @Wired
    private AreaImportServiceBean areaImportService;
    @Wired
    private AffiliationServiceBean affiliationService;
    @Wired
    private AffiliationRepository affiliationRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
    @Wired
    private AntallStemmesedlerLagtTilSideRepository antallStemmesedlerLagtTilSideRepository;
    @Wired(impl = ManntallsnummerApplicationService.class)
    private ManntallsnummerApplicationService manntallsnummerApplicationService;
    @Wired
    private AuditLogServiceBean auditLogServiceBean;
    @Wired
    private BallotCountRepository ballotCountRepository;
    @Wired
    private BallotRejectionRepository ballotRejectionRepository;
    @Wired
    private BatchRepository batchRepository;
    @Wired
    private BatchServiceBean batchService;
    @Wired
    private BallotServiceBean ballotService;
    @Wired
    private BallotRepository ballotRepository;
    @Wired
    private BinaryDataRepository binaryDataRepository;
    @Wired
    private BinaryDataServiceBean binaryDataService;
    @Wired
    private BoroughRepository boroughRepository;
    @Wired
    private BoroughServiceBean boroughService;
    @Wired
    private CandidateRepository candidateRepository;
    @Wired
    private CandidateServiceBean candidateService;
    @Wired
    private CandidateVoteRepository candidateVoteRepository;
    @Wired
    private CastBallotRepository castBallotRepository;
    @Wired
    private CertificateService certificateService;
    @Wired
    private CountingCodeValueRepository countingCodeValueRepository;
    @Wired
    private CountryRepository countryRepository;
    @Wired
    private CountryServiceBean countryService;
    @Wired
    private CountyRepository countyRepository;
    @Wired
    private ElectionGroupMapper electionGroupMapper;
    @Wired
    private CountyServiceBean countyService;
    @Wired
    private ContestAreaRepository contestAreaRepository;
    @Wired
    private ContestRelAreaRepository contestRelAreaRepository;
    @Wired
    private ContestReportRepository contestReportRepository;
    @Wired
    private ContestRepository contestRepository;
    @Wired
    private ContestServiceBean contestServiceBean;
    @Wired
    private CountingImportServiceBean countingImportService;
    @Wired
    private CryptoServiceBean cryptoService;
    @Wired
    private ElectionDayRepository electionDayRepository;
    @Wired
    private ElectionGroupRepository electionGroupRepository;
    @Wired
    private ElectionGroupServiceBean electionGroupServiceBean;
    @Wired
    private ElectionEventRepository electionEventRepository;
    @Wired
    private ElectionEventDomainService electionEventService;
    @Wired
    private ElectionRepository electionRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private ElectionServiceBean electionServiceBean;
    @Wired
    private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;
    @Wired
    private EligibilityRepository eligibilityRepository;
    @Wired
    private EligibleVoterDomainService eligibleVoterDomainService;
    @Wired(impl = FakeEvent.class)
    private Event event;
    @Wired
    private ExportImportOperatorsServiceBean exportImportOperatorsService;
    @Wired
    private ExportServiceBean exportService;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private FindProtocolCountService findProtocolCountService;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private FindCountService findCountService;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private FindPreliminaryCountService findPreliminaryCountService;
    @Wired
    private FullElectoralRollImporter fullElectoralRollImporter;
    @Wired
    private GetCountsValidator getCountsValidator;
    @Wired
    private GenericTestRepository genericTestRepository;
    @Wired(impl = ImportElectoralRollServiceEjb.class)
    private ImportElectoralRollService importElectoralRollService;
    @Wired
    private IncrementalElectoralRollImporter incrementalElectoralRollImporter;
    @Wired
    private LegacyAccessServiceBean legacyAccessService;
    @Wired
    private LegacyCountingServiceBean legacyCountingService;
    @Wired
    private LegacyPollingDistrictRepository legacyPollingDistrictRepository;
    @Wired
    private LegacyUserDataServiceBean userDataService;
    @Wired(impl = LegacyListProposalServiceEjb.class)
    private LegacyListProposalService listProposalService;
    @Wired
    private LocaleRepository localeRepository;
    @Wired
    private LocaleTextRepository localeTextRepository;
    @Wired
    private LocaleTextServiceBean localeTextService;
    @Wired
    private ManualContestVotingRepository manualContestVotingRepository;
    @Wired
    private ModifiedBallotBatchRepository modifiedBallotBatchRepository;
    @Wired(impl = ModifiedBallotBatchApplicationService.class)
    private ModifiedBallotBatchService modifiedBallotBatchService;
    @Wired
    private ModifiedBallotDomainService modifiedBallotDomainService;
    @Wired
    private MunicipalityRepository municipalityRepository;
    @Wired
    private MunicipalityDomainService municipalityService;
    @Wired
    private MunicipalityApplicationService municipalityApplicationService;
    @Wired
    private MvAreaRepository mvAreaRepository;
    @Wired
    private MvAreaServiceBean mvAreaService;
    @Wired
    private MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository;
    @Wired
    private MvElectionRepository mvElectionRepository;
    @Wired
    private MvElectionServiceBean mvElectionService;
    @Wired(impl = CountingApplicationService.class)
    private CountingService countingService;
    @Wired
    private OpeningHoursRepository openingHoursRepository;
    @Wired
    private OperatorRepository operatorRepository;
    @Wired
    private OperatorRoleRepository operatorRoleRepository;
    @Wired
    private OperatorRoleServiceBean operatorRoleService;
    @Wired
    private OperatorServiceBean operatorService;
    @Wired
    private PartyCategoryRepository partyCategoryRepository;
    @Wired
    private PartyRepository partyRepository;
    @Wired
    private PartyServiceBean partyServiceBean;
    @Wired
    private PollingDistrictRepository pollingDistrictRepository;
    @Wired
    private PollingDistrictServiceBean pollingDistrictService;
    @Wired
    private PollingPlaceRepository pollingPlaceRepository;
    @Wired
    private PollingPlaceDomainService pollingPlaceDomainService;
    @Wired
    private PollingStationRepository pollingStationRepository;
    @Wired
    private PollingStationServiceBean pollingStationService;
    @Wired(impl = ProposerServiceEjb.class)
    private ProposerService proposerService;
    @Wired
    private ProposerServiceBean proposerServiceBean;
    @Wired
    private ProposerRepository proposerRepository;
    @Wired
    private RBACAuthenticator rbacAuthenticator;
    @Wired
    private ReportCountCategoryRepository reportCountCategoryRepository;
    @Wired
    private ReportCountCategoryServiceBean reportCountCategoryService;
    @Wired
    private ReportingUnitRepository reportingUnitRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private ReportingUnitDomainService reportingUnitDomainService;
    @Wired
    private ReportingUnitServiceBean reportingUnitService;
    @Wired
    private ResponsibleOfficerRepository responsibleOfficerRepository;
    @Wired
    private RoleAreaService roleAreaService;
    @Wired
    private RoleExporterImporter roleExporterImporter;
    @Wired
    private RoleServiceBean roleService;
    @Wired
    private RoleRepository roleRepository;
    @Wired(producer = MockSessionContextProducer.class)
    private SessionContext sessionContext;
    @Wired
    private SigningKeyRepository signingKeyRepository;
    @Wired
    private SigningKeyServiceBean signingKeyService;
    @Wired
    private SystemPasswordStore systemPasswordStore;
    @Wired(impl = TestServiceEjb.class)
    private TestService testService;
    @Wired
    private TextIdRepository textIdRepository;
    @Wired(producer = MockTransactionSynchronizationRegistryProducer.class)
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    @Wired
    private TranslationServiceBean translationService;
    @Wired
    private VelgerDomainService velgerDomainService;
    @Wired
    private VoteCategoryRepository voteCategoryRepository;
    @Wired
    private VoteCountRepository voteCountRepository;
    @Wired
    private VoteCountCategoryRepository voteCountCategoryRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private VoteCountService newVoteCountService;
    @Wired
    private VoterImportBatchRepository voterImportBatchRepository;
    @Wired
    private VoterImportBatchServiceLocalEjb voterImportBatchServiceLocalEjb;
    @Wired
    private VoterRepository voterRepository;
    @Wired
    private VoterServiceBean voterService;
    @Wired
    private VotingRejectionRepository votingRejectionRepository;
    @Wired
    private VoteCountCategoryServiceBean votingCountCategoryService;
    @Wired(impl = DefaultVotingRepository.class)
    private VotingRepository votingRepository;
    @Wired(impl = PagingVotingRepository.class)
    private PagingVotingRepository pagingVotingRepository;
    @Wired
    private no.valg.eva.admin.counting.repository.VotingRepository countingVotingRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private VotingServiceBean votingService;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private PartyMapper partyMapper;
    @Wired(impl = ElectionApplicationService.class)
    private ElectionService electionService;
    @Wired(impl = ElectionGroupApplicationService.class)
    private ElectionGroupService electionGroupService;
    @Wired
    private JasperReportServiceBean jasperReportServiceBean;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private VoteCountStatusendringTrigger voteCountStatusendringTrigger;
    @Wired
    private StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private ValgnattstatistikkDomainService valgnattstatistikkDomainService;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private ValgnattElectoralRollDomainService valgnattElectoralRollDomainService;
    @Wired
    private ValgnattElectoralRollRepository valgnattElectoralRollRepository;
    @Wired(producer = ConstructorInjectedBeanProducer.class)
    private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
    @Wired
    private VotingRegistrationDomainService votingRegistrationDomainService;

	private EntityManager em;

	/**
	 * Constructs the container.
	 * <p/>
	 * NOTE: Remember to initialize the container by invoking {@link #initServices()}.
	 *
	 * @param entityManager the entity manager to use in all services and repositories.
	 */
	public BackendContainer(final EntityManager entityManager) {
		this.wirer = new BackendWirer(this, entityManager);
		this.em = entityManager;
	}

	public BackendContainer(EntityManager entityManager, Object... preinitializedFields) throws ClassNotFoundException {
		this.wirer = new BackendWirer(this, entityManager, preinitializedFields);
		this.em = entityManager;
	}

	/**
	 * Initializes the container and wires the services. Must be invoked before accessing the services.
	 */
	public void initServices() {
		wirer.initServices();
	}
}
