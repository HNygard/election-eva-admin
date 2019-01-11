package no.valg.eva.admin.settlement.domain.event.factory;

import static java.util.Collections.addAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateSeatEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateSeatEventsFromAffiliationVoteCountsTest {
	@Test
	public void consume_givenAffiliationVoteCount_fireCandidateSeatEventsForAllAffiliationCandidates() throws Exception {
		CandidateSeatEventListener eventListener = mock(CandidateSeatEventListener.class);
		CandidateSeatEventsFromAffiliationVoteCounts candidateSeatEventsFromAffiliationVoteCounts = new CandidateSeatEventsFromAffiliationVoteCounts();
		candidateSeatEventsFromAffiliationVoteCounts.addEventListener(eventListener);
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate1 = candidate(affiliation);
		Candidate candidate2 = candidate(affiliation);
		Candidate candidate3 = candidate(affiliation);
		AffiliationVoteCount affiliationVoteCount = mock(AffiliationVoteCount.class);
		when(affiliationVoteCount.getAffiliationCandidates()).thenReturn(candidates(candidate1, candidate2, candidate3));
		when(affiliationVoteCount.getVotes()).thenReturn(1);

		candidateSeatEventsFromAffiliationVoteCounts.consume(affiliationVoteCount);

		ArgumentCaptor<CandidateSeatEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateSeatEvent.class);
		verify(eventListener, times(3)).candidateSeatDelta(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).contains(
				candidateSeatEvent(affiliation, candidate1, 1), candidateSeatEvent(affiliation, candidate2, 1), candidateSeatEvent(affiliation, candidate3, 1));
	}

	private Set<Candidate> candidates(Candidate... candidates) {
		Set<Candidate> candidateSet = new LinkedHashSet<>();
		addAll(candidateSet, candidates);
		return candidateSet;
	}

	private Candidate candidate(Affiliation affiliation) {
		Candidate candidate = mock(Candidate.class);
		when(candidate.getAffiliation()).thenReturn(affiliation);
		return candidate;
	}

	private CandidateSeatEvent candidateSeatEvent(Affiliation affiliation, Candidate candidate, int dividend) {
		return new CandidateSeatEvent(affiliation, candidate, dividend);
	}
}

