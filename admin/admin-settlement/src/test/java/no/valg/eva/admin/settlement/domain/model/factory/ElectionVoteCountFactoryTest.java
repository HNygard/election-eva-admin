package no.valg.eva.admin.settlement.domain.model.factory;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.ElectionVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.ElectionVoteCountEvent;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ElectionVoteCountFactoryTest {
	@Test
    public void buildElectionVoteCounts_givenEvent_sendsElectionVoteCountToConsumer() {
		ElectionVoteCountConsumer consumer = mock(ElectionVoteCountConsumer.class);
        ElectionVoteCountFactory factory = new ElectionVoteCountFactory(ONE);
		factory.addConsumer(consumer);
		Party party = mock(Party.class);
		ElectionVoteCountEvent event = new ElectionVoteCountEvent(party, 1, 1, 1, 1, 1, 1, 1, 1);
		factory.electionVoteCountDelta(event);
		factory.buildElectionVoteCounts();
		verify(consumer).consume(any(ElectionVoteCount.class));
	}

	@Test
    public void buildElectionVoteCounts_givenTwoEventDeltasForSameParty_sendsOneCombinedElectionVoteCountToConsumer() {
        ElectionVoteCountFactory factory = new ElectionVoteCountFactory(ONE);
		ElectionVoteCountConsumer consumer = mock(ElectionVoteCountConsumer.class);
		factory.addConsumer(consumer);
		Party party = mock(Party.class);
		ElectionVoteCountEvent event1 = new ElectionVoteCountEvent(party, 1, 1, 1, 1, 1, 1, 1, 1);
		ElectionVoteCountEvent event2 = new ElectionVoteCountEvent(party, 1, 1, 1, 1, 1, 1, 1, 1);

		factory.electionVoteCountDelta(event1);
		factory.electionVoteCountDelta(event2);
		factory.buildElectionVoteCounts();

		ArgumentCaptor<ElectionVoteCount> argumentCaptor = ArgumentCaptor.forClass(ElectionVoteCount.class);
		verify(consumer, times(1)).consume(argumentCaptor.capture());
		ElectionVoteCount electionVoteCount = argumentCaptor.getValue();
		assertElectionVoteCount(electionVoteCount, 2, 2, 2, 2, 2, 2, 2, 2, 2, true);
	}

	@Test
    public void buildElectionVoteCounts_givenTwoEventDeltasForDifferentParties_sendsTwoElectionVoteCountsToConsumer() {
		BigDecimal levelingSeatsVoteShareThreshold = new BigDecimal("0.5");
		ElectionVoteCountFactory factory = new ElectionVoteCountFactory(levelingSeatsVoteShareThreshold);
		ElectionVoteCountConsumer consumer = mock(ElectionVoteCountConsumer.class);
		factory.addConsumer(consumer);
		Party party1 = mock(Party.class);
		Party party2 = mock(Party.class);
		ElectionVoteCountEvent event1 = new ElectionVoteCountEvent(party1, 1, 1, 1, 1, 1, 1, 1, 1);
		ElectionVoteCountEvent event2 = new ElectionVoteCountEvent(party2, 1, 1, 1, 1, 1, 1, 1, 1);

		factory.electionVoteCountDelta(event1);
		factory.electionVoteCountDelta(event2);
		factory.buildElectionVoteCounts();

		ArgumentCaptor<ElectionVoteCount> argumentCaptor = ArgumentCaptor.forClass(ElectionVoteCount.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<ElectionVoteCount> electionVoteCounts = argumentCaptor.getAllValues();
		assertElectionVoteCount(electionVoteCounts.get(0), 1, 1, 1, 1, 1, 1, 1, 2, 1, true);
		assertElectionVoteCount(electionVoteCounts.get(1), 1, 1, 1, 1, 1, 1, 1, 2, 1, true);
	}

	private void assertElectionVoteCount(ElectionVoteCount electionVoteCount, int earlyVotingBallots,
			int earlyVotingModifiedBallots, int electionDayBallots, int electionDayModifiedBallots, int baselineVotes,
			int addedVotes, int subtractedVotes, int totalVotes, int contestSeats,
			boolean eligibleForLevelingSeats) {
		assertThat(electionVoteCount.getBallots()).isEqualTo(earlyVotingBallots + electionDayBallots);
		assertThat(electionVoteCount.getModifiedBallots()).isEqualTo(earlyVotingModifiedBallots + earlyVotingModifiedBallots);
		assertThat(electionVoteCount.getEarlyVotingBallots()).isEqualTo(earlyVotingBallots);
		assertThat(electionVoteCount.getEarlyVotingModifiedBallots()).isEqualTo(earlyVotingModifiedBallots);
		assertThat(electionVoteCount.getElectionDayBallots()).isEqualTo(electionDayBallots);
		assertThat(electionVoteCount.getElectionDayModifiedBallots()).isEqualTo(electionDayModifiedBallots);
		assertThat(electionVoteCount.getBaselineVotes()).isEqualTo(baselineVotes);
		assertThat(electionVoteCount.getAddedVotes()).isEqualTo(addedVotes);
		assertThat(electionVoteCount.getSubtractedVotes()).isEqualTo(subtractedVotes);
		assertThat(electionVoteCount.getVotes()).isEqualTo(baselineVotes + addedVotes - subtractedVotes);
		assertThat(electionVoteCount.getTotalVotes()).isEqualTo(totalVotes);
		assertThat(electionVoteCount.getContestSeats()).isEqualTo(contestSeats);
		assertThat(electionVoteCount.isEligibleForLevelingSeats()).isEqualTo(eligibleForLevelingSeats);
	}
}
