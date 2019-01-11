package no.valg.eva.admin.counting.domain.updater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

import org.testng.annotations.Test;

public class ProtocolCountUpdaterTest {
	private static final int APPROVED_BALLOTS = 11;
	private static final int REJECTED_BALLOTS = 22;
	private static final String INFO_TEXT = "aString";
	private static final int BLANK_BALLOT_COUNT = 5;
	private static final int ORDINARY_BALLOT_COUNT = APPROVED_BALLOTS - BLANK_BALLOT_COUNT;
	private static final int QUESTIONABLE_BALLOTS = REJECTED_BALLOTS;
	private static final String COMMENT = INFO_TEXT;
	private static final int FOREIGN_SPECIAL_COVERS = 1;
	private static final int SPECIAL_COVERS = 2;
	private static final int EMERGENCY_SPECIAL_COVERS = 3;
	private static final int BALLOTS_FOR_OTHER_CONTESTS = 4;

	@Test
	public void testApplyUpdates_givenVoteCountAndCountAndStatus_updatesVoteCount() throws Exception {
		VoteCount voteCount = new VoteCount();
		VoteCountStatus voteCountStatus = new VoteCountStatus();

		new ProtocolCountUpdater().applyUpdates(voteCount, protocolCount(), voteCountStatus);

		assertThat(voteCount.getApprovedBallots()).isEqualTo(APPROVED_BALLOTS);
		assertThat(voteCount.getRejectedBallots()).isEqualTo(REJECTED_BALLOTS);
		assertThat(voteCount.getInfoText()).isEqualTo(INFO_TEXT);
		assertThat(voteCount.getVoteCountStatus()).isSameAs(voteCountStatus);
		assertThat(voteCount.getForeignSpecialCovers()).isEqualTo(FOREIGN_SPECIAL_COVERS);
		assertThat(voteCount.getSpecialCovers()).isEqualTo(SPECIAL_COVERS);
		assertThat(voteCount.getEmergencySpecialCovers()).isEqualTo(EMERGENCY_SPECIAL_COVERS);
		assertThat(voteCount.getBallotsForOtherContests()).isEqualTo(BALLOTS_FOR_OTHER_CONTESTS);
	}

	private ProtocolCount protocolCount() {
		ProtocolCount protocolCount = mock(ProtocolCount.class);
		when(protocolCount.getOrdinaryBallotCount()).thenReturn(ORDINARY_BALLOT_COUNT);
		when(protocolCount.getBlankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(protocolCount.getQuestionableBallotCount()).thenReturn(QUESTIONABLE_BALLOTS);
		when(protocolCount.getComment()).thenReturn(COMMENT);
		when(protocolCount.getForeignSpecialCovers()).thenReturn(FOREIGN_SPECIAL_COVERS);
		when(protocolCount.getSpecialCovers()).thenReturn(SPECIAL_COVERS);
		when(protocolCount.getEmergencySpecialCovers()).thenReturn(EMERGENCY_SPECIAL_COVERS);
		when(protocolCount.getBallotCountForOtherContests()).thenReturn(BALLOTS_FOR_OTHER_CONTESTS);
		return protocolCount;
	}
}
