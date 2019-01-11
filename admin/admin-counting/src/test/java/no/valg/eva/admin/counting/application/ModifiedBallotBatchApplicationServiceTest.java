package no.valg.eva.admin.counting.application;

import no.evote.exception.ModifiedBallotBatchCreationFailed;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.counting.builder.BallotCountBuilder;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.domain.modifiedballots.ModifiedBallotDomainService;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchTestSupport;
import no.valg.eva.admin.test.TestGroups;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


@Test(groups = TestGroups.REPOSITORY)
public class ModifiedBallotBatchApplicationServiceTest extends ModifiedBallotBatchTestSupport {
	@Mock
	private ModifiedBallotBatchRepository mockModifiedBallotBatchRepository;
	@Mock
	private ModifiedBallotDomainService stubModifiedBallotDomainService;
	private ModifiedBallotBatchApplicationService modifiedBallotBatchApplicationService;
	@Mock
	private ContestReportRepository stubContestReportRepository;
	@Mock
	private CandidateRepository candidateRepository;

	@BeforeSuite(alwaysRun = true)
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		setupTransactionSynchronizationRegistry();
		modifiedBallotBatchApplicationService = new ModifiedBallotBatchApplicationService();
		modifiedBallotBatchApplicationService.modifiedBallotBatchRepository = mockModifiedBallotBatchRepository;
		modifiedBallotBatchApplicationService.modifiedBallotDomainService = stubModifiedBallotDomainService;
		modifiedBallotBatchApplicationService.candidateRepository = candidateRepository;
		setUpRepositories();
		modifiedBallotBatchApplicationService.contestReportRepository = stubContestReportRepository;
		setupTestData();
		setUpMocks();
	}

	private void setUpMocks() {
		final List<ModifiedBallotBatch> batches = newArrayList();
		when(mockModifiedBallotBatchRepository
				.createModifiedBallotBatch(userData, new ModifiedBallotBatch(userData.getOperator(), ballotCount, MODIFIED_BALLOTS_PROCESS)))
				.thenAnswer(new Answer<Object>() {
					@Override
					public Object answer(InvocationOnMock invocation) {
								ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch(null, ballotCount, MODIFIED_BALLOTS_PROCESS);
						batches.add(modifiedBallotBatch);
						return modifiedBallotBatch;
					}
				});
		when(mockModifiedBallotBatchRepository.activeBatchesForOperator(operator)).thenReturn(batches);
		when(
				stubModifiedBallotDomainService.buildModifiedBallotsStatus(any(BallotCount.class),
						any(no.valg.eva.admin.common.counting.model.BallotCount.class), anyString(), any(ModifiedBallotBatchProcess.class)))
				.thenReturn(createModifiedBallotsStatusForSomeInProgress());
		when(
				stubModifiedBallotDomainService.buildModifiedBallotsStatus(any(BallotCount.class),
						any(no.valg.eva.admin.common.counting.model.BallotCount.class), any(ModifiedBallotBatchProcess.class)))
				.thenReturn(createModifiedBallotsStatusForSomeInProgress());
		ContestReport stubContestReport = mock(ContestReport.class, RETURNS_DEEP_STUBS);
		when(stubContestReport.getBallotCount(any(BallotCountRef.class))).thenReturn(ballotCount);
		when(stubContestReportRepository.findByBallotCount(any(BallotCountRef.class))).thenReturn(stubContestReport);
		Candidate stubCandidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(stubCandidate.getNameLine()).thenReturn("Ole Olsen");
		when(stubCandidate.getPk()).thenReturn(1L);
		when(candidateRepository.findByAffiliation(anyLong())).thenReturn(newArrayList(stubCandidate));
	}

	private ModifiedBallotsStatus createModifiedBallotsStatusForSomeInProgress() {
		return new ModifiedBallotsStatus(null, A_LOT, SOME, 0, null);
	}

	private ModifiedBallotsStatus createModifiedBallotsStatusForNearlyAllInProgress() {
		return new ModifiedBallotsStatus(null, A_LOT, NEARLY_ALL, 0, null);
	}

	@Test()
	public void testCreateModifiedBallotBatch() {
		BatchId modifiedBallotBatchId = modifiedBallotBatchApplicationService.createModifiedBallotBatch(userData,
				new BallotCountBuilder().applyEntity(ballotCount).build(), WANTED_MODIFIED_BALLOTS_IN_BATCH, MODIFIED_BALLOTS_PROCESS).getBatchId();

		assertEquals(modifiedBallotBatchId, expectedIdForBatch());

		when(mockModifiedBallotBatchRepository.findHighestBatchMemberSerialNumberForBallotCount(any(BallotCountRef.class)))
				.thenReturn((WANTED_MODIFIED_BALLOTS_IN_BATCH));
		modifiedBallotBatchId = modifiedBallotBatchApplicationService.createModifiedBallotBatch(userData,
				new BallotCountBuilder().applyEntity(ballotCount).build(), WANTED_MODIFIED_BALLOTS_IN_BATCH, MODIFIED_BALLOTS_PROCESS).getBatchId();
		assertEquals(modifiedBallotBatchId, expectedBatchIdSecondBatch());
	}

	@Test()
	public void testRetrieveBatch() {
		genericTestRepository.createEntity(ballotCount);
		BatchId modifiedBallotBatchId = modifiedBallotBatchApplicationService.createModifiedBallotBatch(userData,
				new BallotCountBuilder().applyEntity(ballotCount).build(), WANTED_MODIFIED_BALLOTS_IN_BATCH, MODIFIED_BALLOTS_PROCESS).getBatchId();
		no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch modifiedBallotBatch =
				modifiedBallotBatchApplicationService.findActiveBatchByBatchId(userData, modifiedBallotBatchId);
		assertNotNull(modifiedBallotBatch);
		assertEquals(modifiedBallotBatch.getBatchId(), modifiedBallotBatchId);
	}

	@Test(expectedExceptions = ModifiedBallotBatchCreationFailed.class)
	public void testWhenExpectingTooManyModifiedBallots() {
		when(
				stubModifiedBallotDomainService.buildModifiedBallotsStatus(any(BallotCount.class),
						any(no.valg.eva.admin.common.counting.model.BallotCount.class), any(ModifiedBallotBatchProcess.class)))
				.thenReturn(createModifiedBallotsStatusForNearlyAllInProgress());
		modifiedBallotBatchApplicationService
				.createModifiedBallotBatch(userData, new BallotCountBuilder().applyEntity(ballotCount).build(), 15, MODIFIED_BALLOTS_PROCESS);
	}

	@Test
	public void hasModifiedBallotBatchForBallotCountPks_withInvalidPks_returnsFalse() {
		List<BallotCountRef> pks = new ArrayList<>();
		pks.add(new BallotCountRef(-1L));
		assertEquals(false, modifiedBallotBatchApplicationService.hasModifiedBallotBatchForBallotCountPks(userData, pks));
	}

	@Test
	public void hasModifiedBallotBatchForBallotCountPks_withValidPks_returnsTrue() {
		when(mockModifiedBallotBatchRepository.countModifiedBallotBatchForBallotCountPks(anyList())).thenReturn(2L);
		List<BallotCountRef> pks = new ArrayList<>();
		pks.add(new BallotCountRef(100));
		pks.add(new BallotCountRef(101));
		assertEquals(true, modifiedBallotBatchApplicationService.hasModifiedBallotBatchForBallotCountPks(userData, pks));
	}

	private BatchId expectedIdForBatch() {
		return new BatchId(ballotCount.getPk() + "_1_" + WANTED_MODIFIED_BALLOTS_IN_BATCH);
	}

	private BatchId expectedBatchIdSecondBatch() {
		return new BatchId(ballotCount.getPk() + "_" + (WANTED_MODIFIED_BALLOTS_IN_BATCH + 1) + "_"
				+ (WANTED_MODIFIED_BALLOTS_IN_BATCH + WANTED_MODIFIED_BALLOTS_IN_BATCH));
	}
}

