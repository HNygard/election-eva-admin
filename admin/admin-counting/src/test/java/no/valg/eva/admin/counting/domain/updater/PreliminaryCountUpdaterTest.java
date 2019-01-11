package no.valg.eva.admin.counting.domain.updater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

import org.testng.annotations.Test;

public class PreliminaryCountUpdaterTest {
	private static final int APPROVED_BALLOTS = 11;
	private static final int REJECTED_BALLOTS = 22;
	private static final String INFO_TEXT = "aString";
	private static final int BLANK_BALLOT_COUNT = 5;
	private static final int ORDINARY_BALLOT_COUNT = APPROVED_BALLOTS - BLANK_BALLOT_COUNT;
	private static final int QUESTIONABLE_BALLOT_COUNT = REJECTED_BALLOTS;
	private static final String COMMENT = INFO_TEXT;
	private static final Integer EXPECTED_BALLOTS = 8;

	@Test
	public void testApplyUpdates_givenVoteCountAndCountAndStatus_updatesVoteCount() throws Exception {
		VoteCount voteCount = new VoteCount();
		VoteCountStatus voteCountStatus = new VoteCountStatus();

		new PreliminaryCountUpdater().applyUpdates(voteCount, preliminaryCount(), voteCountStatus);

		assertThat(voteCount.getApprovedBallots()).isEqualTo(APPROVED_BALLOTS);
		assertThat(voteCount.getRejectedBallots()).isEqualTo(REJECTED_BALLOTS);
		assertThat(voteCount.getInfoText()).isEqualTo(INFO_TEXT);
		assertThat(voteCount.getVoteCountStatus()).isSameAs(voteCountStatus);
		assertThat(voteCount.getTechnicalVotings()).isEqualTo(EXPECTED_BALLOTS);
	}

	private PreliminaryCount preliminaryCount() {
		PreliminaryCount preliminaryCount = mock(PreliminaryCount.class);
		when(preliminaryCount.getOrdinaryBallotCount()).thenReturn(ORDINARY_BALLOT_COUNT);
		when(preliminaryCount.getBlankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(preliminaryCount.getQuestionableBallotCount()).thenReturn(QUESTIONABLE_BALLOT_COUNT);
		when(preliminaryCount.getComment()).thenReturn(COMMENT);
		when(preliminaryCount.getExpectedBallotCount()).thenReturn(EXPECTED_BALLOTS);
		return preliminaryCount;
	}
}
