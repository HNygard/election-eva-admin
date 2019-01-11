package no.valg.eva.admin.counting.domain.updater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

import org.testng.annotations.Test;

public class FinalCountUpdaterTest {
	private static final int APPROVED_BALLOTS = 11;
	private static final int REJECTED_BALLOTS = 22;
	private static final String INFO_TEXT = "aString";
	private static final int BLANK_BALLOT_COUNT = 5;
	private static final int ORDINARY_BALLOT_COUNT = APPROVED_BALLOTS - BLANK_BALLOT_COUNT;
	private static final int QUESTIONABLE_BALLOT_COUNT = REJECTED_BALLOTS;
	private static final String COMMENT = INFO_TEXT;

	@Test
	public void testApplyUpdates_givenVoteCountAndCountAndStatus_updatesVoteCount() throws Exception {
		VoteCount voteCount = new VoteCount();
		VoteCountStatus voteCountStatus = new VoteCountStatus();

		new FinalCountUpdater().applyUpdates(voteCount, finalCount(), voteCountStatus);

		assertThat(voteCount.getApprovedBallots()).isEqualTo(APPROVED_BALLOTS);
		assertThat(voteCount.getRejectedBallots()).isEqualTo(REJECTED_BALLOTS);
		assertThat(voteCount.getInfoText()).isEqualTo(INFO_TEXT);
		assertThat(voteCount.getVoteCountStatus()).isSameAs(voteCountStatus);
		assertThat(voteCount.isModifiedBallotsProcessed()).isTrue();
		assertThat(voteCount.isRejectedBallotsProcessed()).isTrue();
	}

	private FinalCount finalCount() {
		FinalCount finalCount = mock(FinalCount.class);
		when(finalCount.getOrdinaryBallotCount()).thenReturn(ORDINARY_BALLOT_COUNT);
		when(finalCount.getBlankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(finalCount.getTotalRejectedBallotCount()).thenReturn(QUESTIONABLE_BALLOT_COUNT);
		when(finalCount.getComment()).thenReturn(COMMENT);
		when(finalCount.isModifiedBallotsProcessed()).thenReturn(true);
		when(finalCount.isRejectedBallotsProcessed()).thenReturn(true);
		return finalCount;
	}
}
