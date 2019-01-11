package no.valg.eva.admin.settlement.domain.event.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class CandidateRankEventsFromConfigurationModelTest {
	@Test
	public void include_givenContest_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromConfigurationModel().include(new Contest())).isTrue();
	}

	@Test
	public void include_givenBallot_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromConfigurationModel().include(new Ballot())).isTrue();
	}

	@Test
	public void include_givenApprovedAffiliation_returnsTrue() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(true);
		assertThat(new CandidateRankEventsFromConfigurationModel().include(affiliation)).isTrue();
	}

	@Test
	public void include_givenNotApprovedAffiliation_returnsFalse() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(false);
		assertThat(new CandidateRankEventsFromConfigurationModel().include(affiliation)).isFalse();
	}

	@Test
	public void include_givenCandidate_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromConfigurationModel().include(new Candidate())).isTrue();
	}

	@Test
	public void visit_givenCandidate_firesCandidateRankEvent() throws Exception {
		CandidateRankEventListener eventListener = mock(CandidateRankEventListener.class);
		CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel = new CandidateRankEventsFromConfigurationModel();
		candidateRankEventsFromConfigurationModel.addEventListener(eventListener);
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		candidateRankEventsFromConfigurationModel.visit(candidate);
		ArgumentCaptor<CandidateRankEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateRankEvent.class);
		verify(eventListener).candidateRankDelta(argumentCaptor.capture());
		CandidateRankEvent event = argumentCaptor.getValue();
		assertThat(event.getCandidate()).isSameAs(candidate);
		assertThat(event.getAffiliation()).isSameAs(candidate.getAffiliation());
		assertThat(event.getVotes()).isZero();
	}
}
