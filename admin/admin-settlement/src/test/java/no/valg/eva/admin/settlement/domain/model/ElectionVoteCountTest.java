package no.valg.eva.admin.settlement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Party;

import org.testng.annotations.Test;


public class ElectionVoteCountTest {
	@Test
	public void constructor_givenEarlyVotingBallotsAndElectionDayBallots_setsBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 1, 0, 2, 0, 0, 0, 0, 0);
		assertThat(electionVoteCount.getBallots()).isEqualTo(3);
	}

	@Test
	public void constructor_givenEarlyVotingModifiedBallotsAndElectionDayModifiedBallots_setsModifiedBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 1, 0, 2, 0, 0, 0, 0);
		assertThat(electionVoteCount.getModifiedBallots()).isEqualTo(3);
	}

	@Test
	public void constructor_givenBaselineVotesAndAddedVotesAndSubtractedVotes_setsVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 1, 2, 1, 0);
		assertThat(electionVoteCount.getVotes()).isEqualTo(2);
	}

	@Test
	public void setEarlyVotingBallots_givenEarlyVotingBallots_updatesBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setEarlyVotingBallots(1);
		assertThat(electionVoteCount.getBallots()).isEqualTo(1);
	}

	@Test
	public void setElectionDayBallots_givenElectionDayBallots_updatesBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setElectionDayBallots(1);
		assertThat(electionVoteCount.getBallots()).isEqualTo(1);
	}

	@Test
	public void setEarlyVotingModifiedBallots_givenEarlyVotingModifiedBallots_updatesModifiedBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setEarlyVotingModifiedBallots(1);
		assertThat(electionVoteCount.getModifiedBallots()).isEqualTo(1);
	}

	@Test
	public void setBaselineVotes_givenBaselineVotes_updatesVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setBaselineVotes(1);
		assertThat(electionVoteCount.getVotes()).isEqualTo(1);
	}

	@Test
	public void setAddedVotes_givenAddedVotes_updatesVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setAddedVotes(1);
		assertThat(electionVoteCount.getVotes()).isEqualTo(1);
	}

	@Test
	public void setSubtractedVotes_givenSubtractedVotes_updatesVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setSubtractedVotes(1);
		assertThat(electionVoteCount.getVotes()).isEqualTo(-1);
	}

	@Test
	public void setElectionDayModifiedBallots_givenElectionDayModifiedBallots_updatesModifiedBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.setElectionDayModifiedBallots(1);
		assertThat(electionVoteCount.getModifiedBallots()).isEqualTo(1);
	}

	@Test
	public void incrementEarlyVotingBallots_givenEarlyVotingBallots_incrementsEarlyVotingBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 1, 0, 0, 0, 0, 0, 0, 0);
		electionVoteCount.incrementEarlyVotingBallots(1);
		assertThat(electionVoteCount.getEarlyVotingBallots()).isEqualTo(2);
	}

	@Test
	public void incrementEarlyVotingModifiedBallots_givenEarlyVotingBallots_incrementsEarlyVotingModifiedBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 1, 0, 0, 0, 0, 0, 0);
		electionVoteCount.incrementEarlyVotingModifiedBallots(1);
		assertThat(electionVoteCount.getEarlyVotingModifiedBallots()).isEqualTo(2);
	}

	@Test
	public void incrementElectionDayBallots_givenEarlyVotingBallots_incrementsElectionDayBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 1, 0, 0, 0, 0, 0);
		electionVoteCount.incrementElectionDayBallots(1);
		assertThat(electionVoteCount.getElectionDayBallots()).isEqualTo(2);
	}

	@Test
	public void incrementElectionDayModifiedBallots_givenEarlyVotingBallots_incrementsElectionDayModifiedBallots() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 1, 0, 0, 0, 0);
		electionVoteCount.incrementElectionDayModifiedBallots(1);
		assertThat(electionVoteCount.getElectionDayModifiedBallots()).isEqualTo(2);
	}

	@Test
	public void incrementBaselineVotes_givenEarlyVotingBallots_incrementsBaselineVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 1, 0, 0, 0);
		electionVoteCount.incrementBaselineVotes(1);
		assertThat(electionVoteCount.getBaselineVotes()).isEqualTo(2);
	}

	@Test
	public void incrementAddedVotes_givenEarlyVotingBallots_incrementsAddedVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 1, 0, 0);
		electionVoteCount.incrementAddedVotes(1);
		assertThat(electionVoteCount.getAddedVotes()).isEqualTo(2);
	}

	@Test
	public void incrementSubtractedVotes_givenEarlyVotingBallots_incrementsSubtractedVotes() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 1, 0);
		electionVoteCount.incrementSubtractedVotes(1);
		assertThat(electionVoteCount.getSubtractedVotes()).isEqualTo(2);
	}

	@Test
	public void incrementContestSeats_givenEarlyVotingBallots_incrementsContestSeats() throws Exception {
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, 0, 0, 0, 1);
		electionVoteCount.incrementContestSeats(1);
		assertThat(electionVoteCount.getContestSeats()).isEqualTo(2);
	}

	@Test
	public void updateEligibleForLevelingSeats_givenLevelingSeatsVoteShareThresholdAndVotesBelowThreshold_setsEligibleForLevelingSeatsToFalse()
			throws Exception {
		int baselineVotes = 3;
		int totalVotes = 100;
		BigDecimal levelingSeatsVoteShareThreshold = new BigDecimal("0.04");
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(null, 0, 0, 0, 0, baselineVotes, 0, 0, 0);
		electionVoteCount.setTotalVotes(totalVotes);
		electionVoteCount.updateEligibleForLevelingSeats(levelingSeatsVoteShareThreshold);
		assertThat(electionVoteCount.isEligibleForLevelingSeats()).isFalse();
	}

	@Test
	public void updateEligibleForLevelingSeats_givenLevelingSeatsVoteShareThresholdAndVotesEqualToThreshold_setsEligibleForLevelingSeatsToTrue()
			throws Exception {
		int baselineVotes = 4;
		int totalVotes = 100;
		BigDecimal levelingSeatsVoteShareThreshold = new BigDecimal("0.04");
		Party party = mock(Party.class, RETURNS_DEEP_STUBS);
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(party, 0, 0, 0, 0, baselineVotes, 0, 0, 0);
		electionVoteCount.setTotalVotes(totalVotes);
		electionVoteCount.updateEligibleForLevelingSeats(levelingSeatsVoteShareThreshold);
		assertThat(electionVoteCount.isEligibleForLevelingSeats()).isTrue();
	}

	@Test
	public void updateEligibleForLevelingSeats_givenLevelingSeatsVoteShareThresholdAndVotesGreaterThanThreshold_setsEligibleForLevelingSeatsToTrue()
			throws Exception {
		int baselineVotes = 5;
		int totalVotes = 100;
		BigDecimal levelingSeatsVoteShareThreshold = new BigDecimal("0.04");
		Party party = mock(Party.class, RETURNS_DEEP_STUBS);
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(party, 0, 0, 0, 0, baselineVotes, 0, 0, 0);
		electionVoteCount.setTotalVotes(totalVotes);
		electionVoteCount.updateEligibleForLevelingSeats(levelingSeatsVoteShareThreshold);
		assertThat(electionVoteCount.isEligibleForLevelingSeats()).isTrue();
	}

	@Test
	public void updateEligibleForLevelingSeats_isAboveThresholdButLocalParty_setsEligibleForLevelingSeatsToFalse()
			throws Exception {
		int baselineVotes = 5;
		int totalVotes = 100;
		BigDecimal levelingSeatsVoteShareThreshold = new BigDecimal("0.04");
		Party party = mock(Party.class, RETURNS_DEEP_STUBS);
		when(party.isLokaltParti()).thenReturn(true);
		ElectionVoteCount electionVoteCount = new ElectionVoteCount(party, 0, 0, 0, 0, baselineVotes, 0, 0, 0);
		electionVoteCount.setTotalVotes(totalVotes);
		electionVoteCount.updateEligibleForLevelingSeats(levelingSeatsVoteShareThreshold);
		assertThat(electionVoteCount.isEligibleForLevelingSeats()).isFalse();
	}
}

