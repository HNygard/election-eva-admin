package no.valg.eva.admin.counting.domain.model;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VoteCountTest {
	private static final int EMERGENCY_SPECIAL_COVERS = 1;
	private static final int MODIFIED_BALLOTS_1 = 31;
	private static final int MODIFIED_BALLOTS_2 = 23;
	private static final int MODIFIED_BALLOTS_3 = 7;
	private static final int UNMODIFIED_BALLOTS_1 = 41;
	private static final int UNMODIFIED_BALLOTS_2 = 54;
	private static final int UNMODIFIED_BALLOTS_3 = 9;
	private static final int TOTAL_BALLOT_COUNT =
			MODIFIED_BALLOTS_1 + UNMODIFIED_BALLOTS_1
					+ MODIFIED_BALLOTS_2 + UNMODIFIED_BALLOTS_2
					+ MODIFIED_BALLOTS_3 + UNMODIFIED_BALLOTS_3;
	private static final int BLANK_BALLOT_COUNT = 2;
	private static final String BALLOT_ID_1 = "B1";
	private static final String BALLOT_ID_2 = "B2";
	private static final String BALLOT_ID_3 = "B3";

	private VoteCount voteCount;

	@BeforeMethod
	public void setUp() {
		voteCount = new VoteCount();
		voteCount.setEmergencySpecialCovers(EMERGENCY_SPECIAL_COVERS);
		voteCount.setApprovedBallots(0);
	}

	@Test
	public void whenVoteCountIsVoAndHasQualifierProtocolThenIsProtocolCountReturnsTrue() {
		final VoteCountCategory voteCountCategory = new VoteCountCategory();
		final String id = CountCategory.VO.getId();
		voteCountCategory.setId(id);
		voteCount.setVoteCountCategory(voteCountCategory);
		final CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(PROTOCOL.getId());
		voteCount.setCountQualifier(countQualifier);
		assertTrue(voteCount.isProtocolCount());
	}

	@Test
	public void whenVoteCountIsVoAndHasNotQualifierProtocolThenIsProtocolCountReturnsFalse() {
		final VoteCountCategory voteCountCategory = new VoteCountCategory();
		final String id = CountCategory.VO.getId();
		voteCountCategory.setId(id);
		voteCount.setVoteCountCategory(voteCountCategory);
		final CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(PRELIMINARY.getId());
		voteCount.setCountQualifier(countQualifier);
		assertFalse(voteCount.isProtocolCount());
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void whenVoteCountIsNotVoButHasQualifierProtocolThenIsProtocolCountThrowsIllegalStateException() {
		final VoteCountCategory voteCountCategory = new VoteCountCategory();
		final String id = CountCategory.FO.getId();
		voteCountCategory.setId(id);
		voteCount.setVoteCountCategory(voteCountCategory);
		final CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(PROTOCOL.getId());
		voteCount.setCountQualifier(countQualifier);
		voteCount.isProtocolCount();
	}

	@Test
	public void countIsPreliminaryCountWhenQualifierIsPreliminary() {
		final CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(PRELIMINARY.getId());
		voteCount.setCountQualifier(countQualifier);
		assertTrue(voteCount.isPreliminaryCount());
	}

	@Test
	public void testAddBallotCount() {
		VoteCount voteCount1 = new VoteCount();
		Ballot ballot = new Ballot();
		ballot.setId(EvoteConstants.BALLOT_BLANK);
		
		ballot.setDisplayOrder(12);
		
		BallotCount ballotCount = voteCount1.addNewBallotCount(ballot, 1, 2);
		
		assertThat(ballotCount).isNotNull();
		assertThat(voteCount1.getBallotCountList().get(0)).isSameAs(ballotCount);
		assertEquals(1, voteCount1.getBallotCountList().get(0).getUnmodifiedBallots());
	}

	@Test
	public void getTotalBallotCount_whenBallotCounts_returnSumOfBallotCounts() throws Exception {
		voteCount = voteCount();

		int totalBallotCount = voteCount.getTotalBallotCount();

		assertThat(totalBallotCount).isEqualTo(TOTAL_BALLOT_COUNT);
	}

	@Test
	public void getBlankBallotCount_withBallotCountsWithBlankBallotCount_returnBlankBallotCount() throws Exception {
		VoteCount voteCount = voteCount(blankBallotCount());
		int blankBallotCount = voteCount.getBlankBallotCount();
		assertThat(blankBallotCount).isEqualTo(BLANK_BALLOT_COUNT);
	}

	@Test
	public void getBlankBallotCount_withBallotCountsWithoutBlankBallotCount_returnZero() throws Exception {
		VoteCount voteCount = voteCount();
		int blankBallotCount = voteCount.getBlankBallotCount();
		assertThat(blankBallotCount).isZero();
	}

	@Test
	public void hasBlankBallotCount_withBallotCountsWithBlankBallotCount_returnTrue() throws Exception {
		VoteCount voteCount = voteCount(blankBallotCount());
		assertThat(voteCount.hasBlankBallotCount()).isTrue();
	}

	@Test
	public void hasBlankBallotCount_withBallotCountsWithoutBlankBallotCount_returnFalse() throws Exception {
		VoteCount voteCount = voteCount();
		assertThat(voteCount.hasBlankBallotCount()).isFalse();
	}

	private BallotCount blankBallotCount() {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallot(ballot(EvoteConstants.BALLOT_BLANK));
		ballotCount.setUnmodifiedBallots(BLANK_BALLOT_COUNT);
		return ballotCount;
	}

	private Ballot ballot(String id) {
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		ballot.setId(id);
		return ballot;
	}

	private VoteCount voteCount() {
		return voteCount(
				ballotCount(BALLOT_ID_1, MODIFIED_BALLOTS_1, UNMODIFIED_BALLOTS_1),
				ballotCount(BALLOT_ID_2, MODIFIED_BALLOTS_2, UNMODIFIED_BALLOTS_2),
				ballotCount(BALLOT_ID_3, MODIFIED_BALLOTS_3, UNMODIFIED_BALLOTS_3));
	}

	private VoteCount voteCount(BallotCount... ballotCounts) {
		VoteCount voteCount = new VoteCount();
		Set<BallotCount> ballotCountSet = new LinkedHashSet<>();
		Collections.addAll(ballotCountSet, ballotCounts);
		voteCount.setBallotCountSet(ballotCountSet);
		return voteCount;
	}

	private BallotCount ballotCount(String id, int modifiedBallots, int unmodifiedBallots) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallot(ballot(id));
		ballotCount.setModifiedBallots(modifiedBallots);
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		return ballotCount;
	}

	@Test
	public void accept_withVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(new Random().nextLong());
		when(countingVisitor.include(voteCount)).thenReturn(true);
		voteCount.accept(countingVisitor);
		verify(countingVisitor).visit(voteCount);
	}

	@Test
	public void accept_withVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(new Random().nextLong());
		when(countingVisitor.include(voteCount)).thenReturn(false);
		voteCount.accept(countingVisitor);
		verify(countingVisitor, never()).visit(voteCount);
	}

	@Test
	public void accept_withVisitorWithIncludeTrue_callsAcceptOnBallotCounts() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(new Random().nextLong());
		BallotCount ballotCount = mock(BallotCount.class);
		voteCount.getBallotCountSet().add(ballotCount);
		when(countingVisitor.include(voteCount)).thenReturn(true);
		voteCount.accept(countingVisitor);
		verify(ballotCount).accept(countingVisitor);
	}

	@Test
	public void accept_withVisitorWithIncludeFalse_doesNotCallAcceptOnBallotCounts() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(new Random().nextLong());
		BallotCount ballotCount = mock(BallotCount.class);
		voteCount.getBallotCountSet().add(ballotCount);
		when(countingVisitor.include(voteCount)).thenReturn(false);
		voteCount.accept(countingVisitor);
		verify(ballotCount, never()).accept(countingVisitor);
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnCategoryTrue_returnsTrue() throws Exception {
		VoteCountCategory voteCountCategory = mock(VoteCountCategory.class);
		when(voteCountCategory.isEarlyVoting()).thenReturn(true);
		VoteCount voteCount = new VoteCount();
		voteCount.setVoteCountCategory(voteCountCategory);
		assertThat(voteCount.isEarlyVoting()).isTrue();
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnCategoryFalse_returnsFalse() throws Exception {
		VoteCountCategory voteCountCategory = mock(VoteCountCategory.class);
		when(voteCountCategory.isEarlyVoting()).thenReturn(false);
		VoteCount voteCount = new VoteCount();
		voteCount.setVoteCountCategory(voteCountCategory);
		assertThat(voteCount.isEarlyVoting()).isFalse();
	}
}
