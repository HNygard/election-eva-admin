package no.valg.eva.admin.settlement.domain.event.factory;

import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class CandidateRankEventsFromCandidateVoteCountsTest {
	@Test
	public void consume_givenCandidateVoteCount_firesCandidateRankEvent() throws Exception {
		CandidateRankEventListener eventListener = mock(CandidateRankEventListener.class);
		CandidateRankEventsFromCandidateVoteCounts candidateRankEventsFromCandidateVoteCounts = new CandidateRankEventsFromCandidateVoteCounts();
		candidateRankEventsFromCandidateVoteCounts.addEventListener(eventListener);
		CandidateVoteCount candidateVoteCount = mock(CandidateVoteCount.class, RETURNS_DEEP_STUBS);
		candidateRankEventsFromCandidateVoteCounts.consume(candidateVoteCount);
		ArgumentCaptor<CandidateRankEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateRankEvent.class);
		verify(eventListener).candidateRankDelta(argumentCaptor.capture());
		CandidateRankEvent event = argumentCaptor.getValue();
		assertThat(event.getCandidate()).isSameAs(candidateVoteCount.getCandidate());
		assertThat(event.getAffiliation()).isSameAs(candidateVoteCount.getCandidateAffiliation());
		assertThat(event.getVotes()).isSameAs(candidateVoteCount.getVotes());
	}
}
