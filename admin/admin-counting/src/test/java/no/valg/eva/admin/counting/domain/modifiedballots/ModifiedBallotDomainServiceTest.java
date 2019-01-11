package no.valg.eva.admin.counting.domain.modifiedballots;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.counting.builder.FinalCountMockups;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.BallotCountRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.REJECTED_BALLOTS_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModifiedBallotDomainServiceTest extends AbstractJpaTestBase {

    private static final int UNMODIFIED = 100;
	private static final AreaPath AREA_PATH = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
	private static final int TOTAL_IN_MODIFIED_BALLOT_PROCESS = 31;
	private static final int TOTAL_IN_REJECTED_BALLOT_PROCESS = 19;
	private static final int TOTAL = 50;
	private static final int NOT_IN_MODIFIED_BALLOT_PROCESS = TOTAL_IN_REJECTED_BALLOT_PROCESS;
	private static final int NOT_IN_REJECTED_BALLOT_PROCESS = TOTAL_IN_MODIFIED_BALLOT_PROCESS;
	private static final int IN_PROGRESS = 10;
	private static final int COMPLETED = 8;
	private static final int REMAINING_IN_MODIFIED_BALLOT_PROCESS = TOTAL_IN_MODIFIED_BALLOT_PROCESS - IN_PROGRESS - COMPLETED;
	private static final int REMAINING_IN_REJECTED_BALLOT_PROCESS = TOTAL_IN_REJECTED_BALLOT_PROCESS - IN_PROGRESS - COMPLETED;

	@Test
	public void buildModifiedBallotStatuses_returnsAListOfModifiedBallotStatuses() {
		ModifiedBallotDomainService modifiedBallotDomainService = buildModifiedBallotDomainService();
		FinalCount finalCount = buildFinalCount();

		ContestReport stubContestReport = mock(ContestReport.class, RETURNS_DEEP_STUBS);
		VoteCount stubVoteCount = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		List<BallotCount> ballotCounts = setUpBallotCountsDatabaseEntities();
		when(stubVoteCount.getBallotCountList()).thenReturn(ballotCounts);
		when(stubContestReport.getVoteCount(anyLong())).thenReturn(stubVoteCount);
		when(modifiedBallotDomainService.contestReportRepository.findByFinalCount(anyLong())).thenReturn(stubContestReport);
		when(modifiedBallotDomainService.ballotCountRepository.findByPk(anyLong())).thenReturn(ballotCounts.get(0));
		UserData userData = mock(UserData.class);
		Operator operator = mock(Operator.class);
		when(operator.getPk()).thenReturn(1L);
		when(userData.getOperator()).thenReturn(operator);
		List<ModifiedBallotsStatus> modifiedBallotsStatuses = modifiedBallotDomainService.buildModifiedBallotsStatuses(finalCount, userData.getOperator(),
				MODIFIED_BALLOTS_PROCESS);

		assertThat(modifiedBallotsStatuses.size()).isEqualTo(finalCount.getBallotCounts().size());
	}

	private ModifiedBallotDomainService buildModifiedBallotDomainService() {
		ModifiedBallotDomainService modifiedBallotDomainService = new ModifiedBallotDomainService();
		modifiedBallotDomainService.modifiedBallotBatchRepository = mock(ModifiedBallotBatchRepository.class);
		modifiedBallotDomainService.contestReportRepository = mock(ContestReportRepository.class);
		modifiedBallotDomainService.ballotCountRepository = mock(BallotCountRepository.class);
		return modifiedBallotDomainService;
	}

	private FinalCount buildFinalCount() {
		FinalCount finalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", AREA_PATH, "", "", "FVO1", ReportingUnitTypeId.FYLKESVALGSTYRET);
		finalCount.setVoteCountPk(1L);
		return finalCount;
	}

	private List<BallotCount> setUpBallotCountsDatabaseEntities() {
		List<BallotCount> ballotCountDatabaseEntities = new ArrayList<>();
		BallotCount ballotCount = getBallotCountEntity();
		ballotCountDatabaseEntities.add(ballotCount);
		return ballotCountDatabaseEntities;
	}

	private BallotCount getBallotCountEntity() {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(1L);
		ballotCount.setModifiedBallots(TOTAL);
		Ballot ballot = new Ballot();
		Contest contest = new Contest();
		ElectionPath electionPath = new ElectionPath("950002.47.01");
		Election election = mock(Election.class);
		when(election.electionPath()).thenReturn(electionPath);
		contest.setElection(election);
		contest.setId("000001");
		ballot.setContest(contest);
		ballot.setId("Ap");
		ballotCount.setBallot(ballot);
		ballotCount.setCastBallots(buildCastBallotSet());
		return ballotCount;
	}

	private Set<CastBallot> buildCastBallotSet() {
		HashSet<CastBallot> castBallotSet = new HashSet<>();
		for (int i = 0; i < IN_PROGRESS + COMPLETED; i++) {
			CastBallot placeholderInOrderToIndicateCompletedOrInProgressCastBallot = new CastBallot();
			placeholderInOrderToIndicateCompletedOrInProgressCastBallot.setType(MODIFIED);
			placeholderInOrderToIndicateCompletedOrInProgressCastBallot.setId(i + "");
			castBallotSet.add(placeholderInOrderToIndicateCompletedOrInProgressCastBallot);
		}
		return castBallotSet;
	}

	@Test
	public void buildModifiedBallotsStatus_whenInModifiedBallotsProcess_returnCorrectStatus() {

		ModifiedBallotDomainService modifiedBallotDomainService = buildModifiedBallotDomainService();
		when(modifiedBallotDomainService.modifiedBallotBatchRepository.countModifiedBallotsNotInProcess(any(BallotCountRef.class),
				eq(MODIFIED_BALLOTS_PROCESS)))
						.thenReturn(NOT_IN_MODIFIED_BALLOT_PROCESS);
		BallotCount ballotCountEntity = getBallotCountEntity();
		ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatch(MODIFIED_BALLOTS_PROCESS, IN_PROGRESS, COMPLETED);
		ballotCountEntity.getModifiedBallotBatches().add(modifiedBallotBatch);
		no.valg.eva.admin.common.counting.model.BallotCount ballotCountForView = buildBallotCountForView();

		ModifiedBallotsStatus modifiedBallotsStatus = modifiedBallotDomainService.buildModifiedBallotsStatus(ballotCountEntity, ballotCountForView, null,
				MODIFIED_BALLOTS_PROCESS);

		assertThat(modifiedBallotsStatus.getTotal()).isEqualTo(TOTAL_IN_MODIFIED_BALLOT_PROCESS);
		assertThat(modifiedBallotsStatus.getInProgress()).isEqualTo(IN_PROGRESS);
		assertThat(modifiedBallotsStatus.getCompleted()).isEqualTo(COMPLETED);
		assertThat(modifiedBallotsStatus.getRemaining()).isEqualTo(REMAINING_IN_MODIFIED_BALLOT_PROCESS);
	}

	@Test
	public void buildModifiedBallotsStatus_whenInRejectedBallotsProcess_returnCorrectStatus() {

		ModifiedBallotDomainService modifiedBallotDomainService = buildModifiedBallotDomainService();
		when(modifiedBallotDomainService.modifiedBallotBatchRepository.countModifiedBallotsNotInProcess(any(BallotCountRef.class),
				eq(REJECTED_BALLOTS_PROCESS)))
						.thenReturn(NOT_IN_REJECTED_BALLOT_PROCESS);
		BallotCount ballotCountEntity = getBallotCountEntity();
		ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatch(REJECTED_BALLOTS_PROCESS, IN_PROGRESS, COMPLETED);
		ballotCountEntity.getModifiedBallotBatches().add(modifiedBallotBatch);
		no.valg.eva.admin.common.counting.model.BallotCount ballotCountForView = buildBallotCountForView();

		ModifiedBallotsStatus modifiedBallotsStatus = modifiedBallotDomainService.buildModifiedBallotsStatus(ballotCountEntity, ballotCountForView, null,
				REJECTED_BALLOTS_PROCESS);

		assertThat(modifiedBallotsStatus.getTotal()).isEqualTo(TOTAL_IN_REJECTED_BALLOT_PROCESS);
		assertThat(modifiedBallotsStatus.getInProgress()).isEqualTo(IN_PROGRESS);
		assertThat(modifiedBallotsStatus.getCompleted()).isEqualTo(COMPLETED);
		assertThat(modifiedBallotsStatus.getRemaining()).isEqualTo(REMAINING_IN_REJECTED_BALLOT_PROCESS);
	}

	private ModifiedBallotBatch modifiedBallotBatch(ModifiedBallotBatchProcess process, int inProgressCount, int completedCount) {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		modifiedBallotBatch.setProcess(process);
		for (int i = 0; i < inProgressCount; i++) {
			ModifiedBallotBatchMember modifiedBallotBatchMember = new ModifiedBallotBatchMember();
			modifiedBallotBatchMember.setPk(new Random().nextLong());
			modifiedBallotBatch.getBatchMembers().add(modifiedBallotBatchMember);
		}
		for (int i = 0; i < completedCount; i++) {
			ModifiedBallotBatchMember modifiedBallotBatchMember = new ModifiedBallotBatchMember();
			modifiedBallotBatchMember.setPk(new Random().nextLong());
			modifiedBallotBatchMember.setDone(true);
			modifiedBallotBatch.getBatchMembers().add(modifiedBallotBatchMember);
		}
		return modifiedBallotBatch;
	}

	private no.valg.eva.admin.common.counting.model.BallotCount buildBallotCountForView() {
		return new no.valg.eva.admin.common.counting.model.BallotCount("SomeId", "SomeName", UNMODIFIED, TOTAL, new BallotCountRef(1));
	}
}
