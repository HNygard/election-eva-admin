package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateVoteCountEventListener;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateVoteCountEventsFromConfigurationModelTest {

	@Test
	public void include_givenContest_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(new Contest())).isTrue();
	}

	@Test
	public void include_givenBallot_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(new Ballot())).isTrue();
	}

	@Test
	public void include_givenApprovedAffiliation_returnsTrue() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(true);
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(affiliation)).isTrue();
	}

	@Test
	public void include_givenNotApprovedAffiliation_returnsFalse() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.isApproved()).thenReturn(false);
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(affiliation)).isFalse();
	}

	@Test
	public void include_givenBaselineVotesCandidate_returnsTrue() throws Exception {
		Candidate candidate = mock(Candidate.class);
		when(candidate.isBaselineVotes()).thenReturn(true);
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(candidate)).isTrue();
	}

	@Test
	public void include_givenNotBaselineVotesCandidate_returnsFalse() throws Exception {
		Candidate candidate = mock(Candidate.class);
		when(candidate.isBaselineVotes()).thenReturn(false);
		assertThat(new CandidateVoteCountEventsFromConfigurationModel(null, null).include(new Candidate())).isFalse();
	}

	@Test
	public void visit_givenCandidateAndEarlyVotingBallotCount_firesCandidateVoteCountEvent() throws Exception {
		VoteCategory baselineVoteCategory = mock(VoteCategory.class);
		CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel = new CandidateVoteCountEventsFromConfigurationModel(
				baselineVoteCategory, BigDecimal.valueOf(2));
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallots()).thenReturn(100);
		when(ballotCount.isEarlyVoting()).thenReturn(true);
		candidateVoteCountEventsFromConfigurationModel.setCurrentBallotCount(ballotCount);
		CandidateVoteCountEventListener candidateVoteCountEventListener = mock(CandidateVoteCountEventListener.class);
		candidateVoteCountEventsFromConfigurationModel.addEventListener(candidateVoteCountEventListener);
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidate.isBaselineVotes()).thenReturn(true);
		candidateVoteCountEventsFromConfigurationModel.visit(candidate);
		ArgumentCaptor<CandidateVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCountEvent.class);
		verify(candidateVoteCountEventListener).candidateVoteCountDelta(argumentCaptor.capture());
		CandidateVoteCountEvent event = argumentCaptor.getValue();
		assertThat(event.getAffiliation()).isSameAs(candidate.getAffiliation());
		assertThat(event.getCandidate()).isSameAs(candidate);
		assertThat(event.getVoteCategory()).isSameAs(baselineVoteCategory);
		assertThat(event.getVotes()).isEqualTo(BigDecimal.valueOf(200));
		assertThat(event.getEarlyVotingVotes()).isEqualTo(BigDecimal.valueOf(200));
		assertThat(event.getElectionDayVotes()).isEqualTo(ZERO);
	}

	@Test
	public void visit_givenCandidateAndElectionDayBallotCount_firesCandidateVoteCountEvent() throws Exception {
		VoteCategory baselineVoteCategory = mock(VoteCategory.class);
		CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel = new CandidateVoteCountEventsFromConfigurationModel(
				baselineVoteCategory, BigDecimal.valueOf(2));
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallots()).thenReturn(100);
		when(ballotCount.isEarlyVoting()).thenReturn(false);
		candidateVoteCountEventsFromConfigurationModel.setCurrentBallotCount(ballotCount);
		CandidateVoteCountEventListener candidateVoteCountEventListener = mock(CandidateVoteCountEventListener.class);
		candidateVoteCountEventsFromConfigurationModel.addEventListener(candidateVoteCountEventListener);
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidate.isBaselineVotes()).thenReturn(true);
		candidateVoteCountEventsFromConfigurationModel.visit(candidate);
		ArgumentCaptor<CandidateVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCountEvent.class);
		verify(candidateVoteCountEventListener).candidateVoteCountDelta(argumentCaptor.capture());
		CandidateVoteCountEvent event = argumentCaptor.getValue();
		assertThat(event.getAffiliation()).isSameAs(candidate.getAffiliation());
		assertThat(event.getCandidate()).isSameAs(candidate);
		assertThat(event.getVoteCategory()).isSameAs(baselineVoteCategory);
		assertThat(event.getVotes()).isEqualTo(BigDecimal.valueOf(200));
		assertThat(event.getEarlyVotingVotes()).isEqualTo(ZERO);
		assertThat(event.getElectionDayVotes()).isEqualTo(BigDecimal.valueOf(200));
	}
}

