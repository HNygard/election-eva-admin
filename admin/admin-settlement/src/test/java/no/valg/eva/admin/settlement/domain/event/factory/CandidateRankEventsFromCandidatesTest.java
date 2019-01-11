package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateRankEventsFromCandidatesTest {
	@Test
	public void include_givenContest_returnsFalse() throws Exception {
		assertThat(new CandidateRankEventsFromCandidates().include(mock(Contest.class))).isFalse();
	}

	@Test
	public void include_givenBallot_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromCandidates().include(mock(Ballot.class))).isTrue();
	}

	@Test
	public void include_givenApprovedAffiliation_returnsTrue() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(true);
		assertThat(new CandidateRankEventsFromCandidates().include(affiliation)).isTrue();
	}

	@Test
	public void include_givenNotApprovedAffiliation_returnsFalse() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(false);
		assertThat(new CandidateRankEventsFromCandidates().include(affiliation)).isFalse();
	}

	@Test
	public void include_givenCandidate_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromCandidates().include(mock(Candidate.class))).isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void visit_givenCandidate_fireCandidateRankEvents() throws Exception {
		Set<Candidate> candidates = mock(Set.class);
		when(candidates.size()).thenReturn(5);
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidate.getAffiliation()).thenReturn(affiliation);
		when(candidate.getDisplayOrder()).thenReturn(3);
		when(candidate.getAffiliation().getCandidates()).thenReturn(candidates);
		CandidateRankEventListener eventListener = mock(CandidateRankEventListener.class);
		CandidateRankEventsFromCandidates candidateRankEventsFromCandidates = new CandidateRankEventsFromCandidates();
		candidateRankEventsFromCandidates.setCandidateVotes(TEN);
		candidateRankEventsFromCandidates.addEventListener(eventListener);

		candidateRankEventsFromCandidates.visit(candidate);

		ArgumentCaptor<CandidateRankEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateRankEvent.class);
		verify(eventListener, times(3)).candidateRankDelta(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).contains(
				candidateRankEvent(candidate, affiliation, TEN, 3),
				candidateRankEvent(candidate, affiliation, TEN, 4),
				candidateRankEvent(candidate, affiliation, TEN, 5));
	}

	private CandidateRankEvent candidateRankEvent(Candidate candidate, Affiliation affiliation, BigDecimal votes, int rankNumber) {
		return new CandidateRankEvent(candidate, affiliation, votes, rankNumber);
	}
}

