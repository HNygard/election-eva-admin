package no.valg.eva.admin.counting.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;

public class VoteCountBuilderTest {
	private static final int ORDINARY_BALLOT_COUNT = 1;
	private static final int BLANK_BALLOT_COUNT = 2;
	private static final int APPROVED_BALLOTS = ORDINARY_BALLOT_COUNT + BLANK_BALLOT_COUNT;
	private static final int QUESTIONABLE_BALLOT_COUNT = 5;
	private static final int REJECTED_BALLOTS = QUESTIONABLE_BALLOT_COUNT;
	private static final boolean MANUAL_COUNT_TRUE = true;
	private static final String COMMENT = "comment";
	private static final String INFO_TEXT = COMMENT;
	private static final int EXPECTED_BALLOT_COUNT = 7;
	private static final int TECHNICAL_VOTINGS = EXPECTED_BALLOT_COUNT;

	@Test
	public void applyPreliminaryCount_givenPreliminaryCount_updatesVoteCount() throws Exception {
		PreliminaryCount preliminaryCount = mock(PreliminaryCount.class);
		VoteCountBuilder voteCountBuilder = new VoteCountBuilder();
		
		when(preliminaryCount.getOrdinaryBallotCount()).thenReturn(ORDINARY_BALLOT_COUNT);
		when(preliminaryCount.getBlankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(preliminaryCount.getQuestionableBallotCount()).thenReturn(QUESTIONABLE_BALLOT_COUNT);
		when(preliminaryCount.isManualCount()).thenReturn(MANUAL_COUNT_TRUE);
		when(preliminaryCount.getComment()).thenReturn(COMMENT);
		when(preliminaryCount.getExpectedBallotCount()).thenReturn(EXPECTED_BALLOT_COUNT);

		voteCountBuilder.applyPreliminaryCount(preliminaryCount);
		VoteCount voteCount = voteCountBuilder.build();

		assertThat(voteCount.getApprovedBallots()).isEqualTo(APPROVED_BALLOTS);
		assertThat(voteCount.getRejectedBallots()).isEqualTo(REJECTED_BALLOTS);
		assertThat(voteCount.isManualCount()).isEqualTo(MANUAL_COUNT_TRUE);
		assertThat(voteCount.getInfoText()).isEqualTo(INFO_TEXT);
		assertThat(voteCount.getTechnicalVotings()).isEqualTo(TECHNICAL_VOTINGS);
	}
}
