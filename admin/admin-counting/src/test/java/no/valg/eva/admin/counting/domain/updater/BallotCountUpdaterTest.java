package no.valg.eva.admin.counting.domain.updater;

import static java.util.Collections.addAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;


public class BallotCountUpdaterTest {
	@Test
	public void updateBallotCounts_givenPreliminaryCount_updatesBallotCountsForPreliminaryVoteCount() throws Exception {
		VoteCount voteCount = preliminaryVoteCount();
		BallotCountUpdater.updateBallotCounts(voteCount, stubPreliminaryCount(), ballotMap());
		List<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCounts = voteCount.getBallotCountList();
		assertThat(ballotCounts).hasSize(4);
		assertThat(ballotCounts.get(0).getUnmodifiedBallots()).isEqualTo(7);
		assertThat(ballotCounts.get(3).getBallotId()).isEqualTo("B3");
	}

	private PreliminaryCount stubPreliminaryCount() {
		PreliminaryCount stubPreliminaryCount = stub(PreliminaryCount.class);
		when(stubPreliminaryCount.getBlankBallotCount()).thenReturn(7);
		when(stubPreliminaryCount.getBallotCountMap()).thenReturn(ballotCountMap());
		return stubPreliminaryCount;
	}

	private VoteCount preliminaryVoteCount() {
		VoteCount voteCount = new VoteCount();
		voteCount.setBallotCountSet(ballotCountEntities());
		return voteCount;
	}

	@Test
	public void updateBallotCounts_givenFinalCount_updatesBallotCountsForFinalVoteCount() throws Exception {
		VoteCount voteCount = finalVoteCount();
		BallotCountUpdater.updateBallotCounts(voteCount, stubFinalCount(), ballotMap(), ballotRejectionMap());
		List<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCounts = voteCount.getBallotCountList();
		assertThat(ballotCounts).hasSize(8);
		assertThat(ballotCounts.get(0).getUnmodifiedBallots()).isEqualTo(7);
		assertThat(ballotCounts.get(5).getBallotId()).isEqualTo("B3");
		assertThat(ballotCounts.get(6).getBallotRejectionId()).isEqualTo("FC");
		assertThat(ballotCounts.get(7).getBallotRejectionId()).isEqualTo("FD");
	}

	@Test
	public void updateBlankBallotCount_givenVoteCountWithBlankBallotCount_updatesBlankBallotCount() throws Exception {
		VoteCount voteCount = new VoteCount();
		voteCount.getBallotCountSet().add(ballotCountEntity(EvoteConstants.BALLOT_BLANK, 5, 0));
		BallotCountUpdater.updateBlankBallotCount(voteCount, 7, ballot(EvoteConstants.BALLOT_BLANK));
		assertThat(voteCount.getBlankBallotCount()).isEqualTo(7);
	}

	@Test
	public void updateBlankBallotCount_givenVoteCountWithoutBlankBallotCount_createsNewBlankBallotCount() throws Exception {
		VoteCount voteCount = new VoteCount();
		BallotCountUpdater.updateBlankBallotCount(voteCount, 7, ballot(EvoteConstants.BALLOT_BLANK));
		assertThat(voteCount.getBlankBallotCount()).isEqualTo(7);
	}

	private Map<String, Ballot> ballotMap() {
		Map<String, Ballot> ballotMap = new LinkedHashMap<>();
		ballotMap.put("B1", ballot("B1"));
		ballotMap.put("B2", ballot("B2"));
		ballotMap.put("B3", ballot("B3"));
		return ballotMap;
	}

	private Map<String, BallotRejection> ballotRejectionMap() {
		Map<String, BallotRejection> ballotRejectionMap = new LinkedHashMap<>();
		ballotRejectionMap.put("FA", ballotRejection("FA"));
		ballotRejectionMap.put("FB", ballotRejection("FB"));
		ballotRejectionMap.put("FC", ballotRejection("FC"));
		ballotRejectionMap.put("FD", ballotRejection("FD"));
		return ballotRejectionMap;
	}

	private Set<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountEntitiesWithRejectedBallotCounts() {
		return ballotCountEntities(
				ballotCountEntity(EvoteConstants.BALLOT_BLANK, 5, 0),
				ballotCountEntity("B1", 11, 21),
				ballotCountEntity("B2", 11, 21),
				rejectedBallotCountEntity("FA", 1),
				rejectedBallotCountEntity("FB", 2));
	}

	private Set<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountEntities() {
		return ballotCountEntities(
				ballotCountEntity(EvoteConstants.BALLOT_BLANK, 5, 0),
				ballotCountEntity("B1", 11, 21),
				ballotCountEntity("B2", 11, 21));
	}

	private Set<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountEntities(no.valg.eva.admin.counting.domain.model.BallotCount... ballotCounts) {
		Set<no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap = new LinkedHashSet<>();
		addAll(ballotCountMap, ballotCounts);
		return ballotCountMap;
	}

	private no.valg.eva.admin.counting.domain.model.BallotCount rejectedBallotCountEntity(String ballotRejectionId, int ballots) {
		no.valg.eva.admin.counting.domain.model.BallotCount ballotCount = new no.valg.eva.admin.counting.domain.model.BallotCount();
		ballotCount.setPk(new Random().nextLong());
		ballotCount.setBallotRejection(ballotRejection(ballotRejectionId));
		ballotCount.setUnmodifiedBallots(ballots);
		return ballotCount;
	}

	private BallotRejection ballotRejection(String ballotRejectionId) {
		BallotRejection ballotRejection = new BallotRejection();
		ballotRejection.setPk(new Random().nextLong());
		ballotRejection.setId(ballotRejectionId);
		return ballotRejection;
	}

	private no.valg.eva.admin.counting.domain.model.BallotCount ballotCountEntity(String ballotId, int unmodifiedBallots, int modifiedBallots) {
		no.valg.eva.admin.counting.domain.model.BallotCount ballotCount = new no.valg.eva.admin.counting.domain.model.BallotCount();
		ballotCount.setPk(new Random().nextLong());
		ballotCount.setBallot(ballot(ballotId));
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		ballotCount.setModifiedBallots(modifiedBallots);
		return ballotCount;
	}

	private Ballot ballot(String ballotId) {
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		ballot.setId(ballotId);
		return ballot;
	}

	private VoteCount finalVoteCount() {
		VoteCount voteCount = new VoteCount();
		voteCount.setBallotCountSet(ballotCountEntitiesWithRejectedBallotCounts());
		return voteCount;
	}

	private FinalCount stubFinalCount() {
		FinalCount stubFinalCount = stub(FinalCount.class);
		when(stubFinalCount.getBlankBallotCount()).thenReturn(7);
		when(stubFinalCount.getBallotCountMap()).thenReturn(ballotCountMap());
		when(stubFinalCount.getRejectedBallotCountMap()).thenReturn(rejectedBallotCountMap());
		return stubFinalCount;
	}

	private Map<String, RejectedBallotCount> rejectedBallotCountMap() {
		HashMap<String, RejectedBallotCount> rejectedBallotCountMap = new LinkedHashMap<>();
		rejectedBallotCountMap.put("FA", rejectedBallotCount("FA", 1));
		rejectedBallotCountMap.put("FB", rejectedBallotCount("FB", 2));
		rejectedBallotCountMap.put("FC", rejectedBallotCount("FC", 3));
		rejectedBallotCountMap.put("FD", rejectedBallotCount("FD", 4));
		return rejectedBallotCountMap;
	}

	private RejectedBallotCount rejectedBallotCount(String ballotRejectionId, int count) {
		RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();
		rejectedBallotCount.setId(ballotRejectionId);
		rejectedBallotCount.setCount(count);
		return rejectedBallotCount;
	}

	private Map<String, BallotCount> ballotCountMap() {
		Map<String, BallotCount> ballotCountMap = new LinkedHashMap<>();
		ballotCountMap.put("B1", ballotCount("B1", 11, 21));
		ballotCountMap.put("B2", ballotCount("B2", 12, 22));
		ballotCountMap.put("B3", ballotCount("B3", 13, 23));
		return ballotCountMap;
	}

	private BallotCount ballotCount(String ballotId, int unmodifiedCount, int modifiedCount) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setId(ballotId);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		return ballotCount;
	}

	private <T> T stub(Class<T> type) {
		return mock(type, RETURNS_DEEP_STUBS);
	}
}

