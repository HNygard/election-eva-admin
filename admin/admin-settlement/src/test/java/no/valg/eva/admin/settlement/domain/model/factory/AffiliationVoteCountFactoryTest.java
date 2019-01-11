package no.valg.eva.admin.settlement.domain.model.factory;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.consumer.AffiliationVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.AffiliationVoteCountEvent;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class AffiliationVoteCountFactoryTest {

	@Test
    public void buildAffiliationVoteCounts_givenEvent_sendsAffiliationVoteCountToConsumer() {
		AffiliationVoteCountFactory factory = new AffiliationVoteCountFactory();
		AffiliationVoteCountConsumer consumer = mock(AffiliationVoteCountConsumer.class);
		factory.addConsumer(consumer);
		Affiliation affiliation = mock(Affiliation.class);
		AffiliationVoteCountEvent event = new AffiliationVoteCountEvent(affiliation, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		factory.affiliationVoteCountDelta(event);
		factory.buildAffiliationVoteCounts();

		verify(consumer).consume(any(AffiliationVoteCount.class));
	}

	@Test
    public void buildAffiliationVoteCounts_givenTwoEventDeltasForSameAffiliation_sendsOneCombinedAffiliationVoteCountToConsumer() {
		AffiliationVoteCountFactory factory = new AffiliationVoteCountFactory();
		AffiliationVoteCountConsumer consumer = mock(AffiliationVoteCountConsumer.class);
		factory.addConsumer(consumer);
		Affiliation affiliation = mock(Affiliation.class);
		AffiliationVoteCountEvent event1 = new AffiliationVoteCountEvent(affiliation, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		AffiliationVoteCountEvent event2 = new AffiliationVoteCountEvent(affiliation, 1, 2, 3, 4, 5, 6, 7, 8, 9);

		factory.affiliationVoteCountDelta(event1);
		factory.affiliationVoteCountDelta(event2);
		factory.buildAffiliationVoteCounts();

		ArgumentCaptor<AffiliationVoteCount> argumentCaptor = ArgumentCaptor.forClass(AffiliationVoteCount.class);
		verify(consumer, times(1)).consume(argumentCaptor.capture());
		AffiliationVoteCount affiliationVoteCount = argumentCaptor.getValue();
		assertThat(affiliationVoteCount.getBallots()).isEqualTo(2);
		assertThat(affiliationVoteCount.getModifiedBallots()).isEqualTo(4);
		assertThat(affiliationVoteCount.getEarlyVotingBallots()).isEqualTo(6);
		assertThat(affiliationVoteCount.getEarlyVotingModifiedBallots()).isEqualTo(8);
		assertThat(affiliationVoteCount.getElectionDayBallots()).isEqualTo(10);
		assertThat(affiliationVoteCount.getElectionDayModifiedBallots()).isEqualTo(12);
		assertThat(affiliationVoteCount.getBaselineVotes()).isEqualTo(14);
		assertThat(affiliationVoteCount.getAddedVotes()).isEqualTo(16);
		assertThat(affiliationVoteCount.getSubtractedVotes()).isEqualTo(18);
	}
}

