package no.valg.eva.admin.counting.service.impl;

import no.evote.model.views.ContestRelArea;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ElectionTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.repository.BallotCountRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class BallotCountServiceImplTest extends AbstractJpaTestBase {

	private static final Integer VALID_VOTES_1 = 50;
	private VoteCount votingCount;
	private ContestReport contestReport;
	private Ballot ballot;
	private VoteCountRepository voteCountRepository;
	private ServiceBackedRBACTestFixture rbacTestFixture;
	private MvElectionRepository mvElectionRepository;
	private ContestRelAreaRepository contestRelAreaRepository;
	private VoteCountCategoryRepository votingCountCategoryService;
	private ElectionTestFixture electionTestFixture;
	private ReportingUnitRepository reportingUnitRepository;
	private CountingCodeValueRepository countingCodeValueRepository;
	private BallotCountRepository ballotCountRepository;
	private ContestRepository contestRepository;
	private ElectionRepository electionRepository;
	private ElectionGroupRepository electionGroupRepository;
	private ElectionEventRepository electionEventRepository;
	private ContestReportRepository contestReportRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();
		setupTransactionSynchronizationRegistry();
		electionTestFixture = new ElectionTestFixture(backend);
		electionTestFixture.init();
		voteCountRepository = backend.getVoteCountRepository();
		mvElectionRepository = backend.getMvElectionRepository();
		BallotRepository ballotRepository = backend.getBallotRepository();
		contestRelAreaRepository = backend.getContestRelAreaRepository();
		votingCountCategoryService = backend.getVoteCountCategoryRepository();
		reportingUnitRepository = backend.getReportingUnitRepository();
		countingCodeValueRepository = backend.getCountingCodeValueRepository();
		ballotCountRepository = backend.getBallotCountRepository();
		contestRepository = backend.getContestRepository();
		electionRepository = backend.getElectionRepository();
		electionGroupRepository = backend.getElectionGroupRepository();
		electionEventRepository = backend.getElectionEventRepository();
		contestReportRepository = backend.getContestReportRepository();
		genericTestRepository = backend.getGenericTestRepository();

		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
		ballot = ballotRepository.findBallotByPk(1L);

		MvAreaRepository mvAreaRepository = backend.getMvAreaRepository();
		MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01.0101.010100.0004"));

		initContestReport(mvArea);
		initVotingCount(mvArea);
	}

	private void initContestReport(final MvArea mvArea) {

		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from("200701.01.02.000101").tilValghierarkiSti());

		ContestRelArea contestRelArea = contestRelAreaRepository.findUnique(mvElection.getPk(), mvArea.getPk());
		ReportingUnit reportingUnit = reportingUnitRepository.getReportingUnit(contestRelArea);

		contestReport = new ContestReport();
		contestReport.setReportingUnit(reportingUnit);
		Contest contest = contestRepository
				.create(rbacTestFixture.getUserData(), electionTestFixture
						.buildContest(electionRepository
								.create(rbacTestFixture.getUserData(), electionTestFixture
										.buildElection(electionGroupRepository
												.create(rbacTestFixture.getUserData(), electionTestFixture
														.buildElectionGroup(electionEventRepository
																.create(rbacTestFixture.getUserData(), electionTestFixture.buildElectionEvent())))))));
		contestReport.setContest(contest);
		contestReport = contestReportRepository.create(rbacTestFixture.getUserData(), contestReport);
	}

	private void initVotingCount(final MvArea mvArea) {
		VoteCountCategory votingCountCategory = votingCountCategoryService.findById("VO");
		CountQualifier countQualifier = countingCodeValueRepository.findCountQualifierById("F");
		String id = "Test";
		
		Integer value = 5;
		
		VoteCountStatus voteCountStatus = getEntityManager().find(VoteCountStatus.class, 2L);

		votingCount = new VoteCount();
		votingCount.setContestReport(contestReport);
		votingCount.setPollingDistrict(mvArea.getPollingDistrict());
		votingCount.setMvArea(mvArea);
		votingCount.setVoteCountCategory(votingCountCategory);
		votingCount.setCountQualifier(countQualifier);
		votingCount.setManualCount(true);
		votingCount.setId(id);
		votingCount.setApprovedBallots(value);
		votingCount.setRejectedBallots(value);
		votingCount.setManualCount(true);
		votingCount.setVoteCountStatus(voteCountStatus);
		votingCount = genericTestRepository.createEntity(votingCount);
	}

	@Test
	public void testCreate() {
		BallotCount ballotCount = null;

		try {
			ballotCount = new BallotCount();
			ballotCount.setVoteCount(votingCount);
			ballotCount.setBallot(ballot);
			ballotCount.setUnmodifiedBallots(VALID_VOTES_1);
			ballotCount.setModifiedBallots(0);

			ballotCountRepository.create(rbacTestFixture.getUserData(), ballotCount);

			Assert.assertNotNull(ballotCount);
			Assert.assertTrue(ballotCount.getUnmodifiedBallots() == VALID_VOTES_1);

		} finally {
			// cleanup
			if (ballotCount != null && ballotCount.getPk() != null) {
				ballotCountRepository.delete(rbacTestFixture.getUserData(), ballotCount.getPk());
			}
		}
	}

	@Test
	public void testFindByPk() {
		BallotCount ballotCount = null;
		try {
			ballotCount = new BallotCount();
			ballotCount.setVoteCount(votingCount);
			ballotCount.setBallot(ballot);
			ballotCount.setUnmodifiedBallots(VALID_VOTES_1);
			ballotCount.setModifiedBallots(0);
			ballotCountRepository.create(rbacTestFixture.getUserData(), ballotCount);

			BallotCount ballotCount2 = ballotCountRepository.findByPk(ballotCount.getPk());

			Assert.assertEquals(ballotCount.getPk(), ballotCount2.getPk());
		} finally {
			if (ballotCount != null && ballotCount.getPk() != null) {
				ballotCountRepository.delete(rbacTestFixture.getUserData(), ballotCount.getPk());
			}
		}
	}
}
