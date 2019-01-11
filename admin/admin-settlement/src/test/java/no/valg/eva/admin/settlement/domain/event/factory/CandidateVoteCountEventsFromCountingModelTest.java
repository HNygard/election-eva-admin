package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateVoteCountEventListener;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateVoteCountEventsFromCountingModelTest extends MockUtilsTestCase {

	@Test
	public void include_givenContestReport_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(new ContestReport())).isTrue();
	}

	@Test
	public void include_givenFinalVoteCountToSettlement_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(voteCount(true, true))).isTrue();
	}

	@Test
	public void include_givenNotFinalVoteCount_returnsFalse() throws Exception {
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(voteCount())).isFalse();
	}

	@Test
	public void include_givenFinalVoteCountNotToSettlement_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(FINAL.getId());
		when(voteCount.getCountStatus()).thenReturn(APPROVED);
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(voteCount(true))).isFalse();
	}

	private VoteCount voteCount() {
		return voteCount(false, false);
	}

	private VoteCount voteCount(boolean finalVoteCount) {
		return voteCount(finalVoteCount, false);
	}

	private VoteCount voteCount(boolean finalVoteCount, boolean toSettlement) {
		VoteCount voteCount = createMock(VoteCount.class);
		when(voteCount.isFinalCount()).thenReturn(finalVoteCount);
		when(voteCount.isToSettlement()).thenReturn(toSettlement);
		return voteCount;
	}

	@Test
	public void include_givenBallotCountWithBlankBallot_returnsFalse() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.isBlank()).thenReturn(true);
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(ballotCount)).isFalse();
	}

	@Test
	public void include_givenBallotCountWithOrdinaryBallot_returnsTrue() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallotId()).thenReturn("ORDINARY");
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(ballotCount)).isTrue();
	}

	@Test
	public void include_givenBallotCountWithBallotRejection_returnsFalse() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallot()).thenReturn(null);
		when(ballotCount.getBallotRejection()).thenReturn(new BallotRejection());
		assertThat(new CandidateVoteCountEventsFromCountingModel(null).include(ballotCount)).isFalse();
	}

	@Test
	public void include_givenCastBallot_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromCountingModel().include(new CastBallot())).isTrue();
	}

	@Test
	public void include_givenCandidateVote_returnsTrue() throws Exception {
		assertThat(new CandidateVoteCountEventsFromCountingModel().include(new CandidateVote())).isTrue();
	}

	@Test
	public void visit_givenBallotCount_ballotAcceptsConfigurationVisitor() throws Exception {
		CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel = mock(
				CandidateVoteCountEventsFromConfigurationModel.class);
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		new CandidateVoteCountEventsFromCountingModel(candidateVoteCountEventsFromConfigurationModel).visit(ballotCount);
		verify(ballotCount.getBallot()).accept(candidateVoteCountEventsFromConfigurationModel);
	}

	@Test
	public void visit_givenEarlyVotingCandidateVote_firesCandidateVoteCountEvent() throws Exception {
		CandidateVoteCountEventListener eventListener = mock(CandidateVoteCountEventListener.class);
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = new CandidateVoteCountEventsFromCountingModel();
		candidateVoteCountEventsFromCountingModel.addEventListener(eventListener);
		CandidateVote candidateVote = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		when(candidateVote.isEarlyVoting()).thenReturn(true);
		when(candidateVote.getRenumberPosition()).thenReturn(1);
		candidateVoteCountEventsFromCountingModel.visit(candidateVote);
		ArgumentCaptor<CandidateVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCountEvent.class);
		verify(eventListener).candidateVoteCountDelta(argumentCaptor.capture());
		CandidateVoteCountEvent event = argumentCaptor.getValue();
		assertCandidateVoteCountEvent(event, candidateVote, ONE, ONE, ZERO);
	}

	@Test
	public void visit_givenElectionDayCandidateVote_firesCandidateVoteCountEvent() throws Exception {
		CandidateVoteCountEventListener eventListener = mock(CandidateVoteCountEventListener.class);
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = new CandidateVoteCountEventsFromCountingModel();
		candidateVoteCountEventsFromCountingModel.addEventListener(eventListener);
		CandidateVote candidateVote = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		when(candidateVote.isEarlyVoting()).thenReturn(false);
		when(candidateVote.getRenumberPosition()).thenReturn(1);
		candidateVoteCountEventsFromCountingModel.visit(candidateVote);
		ArgumentCaptor<CandidateVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCountEvent.class);
		verify(eventListener).candidateVoteCountDelta(argumentCaptor.capture());
		CandidateVoteCountEvent event = argumentCaptor.getValue();
		assertCandidateVoteCountEvent(event, candidateVote, ONE, ZERO, ONE);
	}

	@Test
	public void visit_givenThreeCandidateVotesForDifferentAffiliationsAndSameCandidate_firesOneEventForEachAffiliation() throws Exception {
		CandidateVoteCountEventListener eventListener = mock(CandidateVoteCountEventListener.class);
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = new CandidateVoteCountEventsFromCountingModel();
		candidateVoteCountEventsFromCountingModel.addEventListener(eventListener);
		CandidateVote candidateVote1 = candidateVote();
		CandidateVote candidateVote2 = candidateVote();
		CandidateVote candidateVote3 = candidateVote();

		candidateVoteCountEventsFromCountingModel.visit(candidateVote1);
		candidateVoteCountEventsFromCountingModel.visit(candidateVote2);
		candidateVoteCountEventsFromCountingModel.visit(candidateVote3);

		ArgumentCaptor<CandidateVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCountEvent.class);
		verify(eventListener, times(3)).candidateVoteCountDelta(argumentCaptor.capture());
		List<CandidateVoteCountEvent> allValues = argumentCaptor.getAllValues();
		assertCandidateVoteCountEvent(allValues.get(0), candidateVote1, ONE, ZERO, ONE);
		assertCandidateVoteCountEvent(allValues.get(1), candidateVote2, ONE, ZERO, ONE);
		assertCandidateVoteCountEvent(allValues.get(2), candidateVote3, ONE, ZERO, ONE);
	}

	private CandidateVote candidateVote() {
		CandidateVote candidateVote1 = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		when(candidateVote1.isEarlyVoting()).thenReturn(false);
		when(candidateVote1.getRenumberPosition()).thenReturn(null);
		return candidateVote1;
	}

	private void assertCandidateVoteCountEvent(CandidateVoteCountEvent event, CandidateVote candidateVote,
			BigDecimal votes, BigDecimal earlyVotingVotes, BigDecimal electionDayVotes) {
		assertThat(event.getAffiliation()).isSameAs(candidateVote.getBallotAffiliation());
		assertThat(event.getCandidate()).isSameAs(candidateVote.getCandidate());
		assertThat(event.getVoteCategory()).isSameAs(candidateVote.getVoteCategory());
		assertThat(event.getRankNumber()).isEqualTo(candidateVote.getRenumberPosition());
		assertThat(event.getVotes()).isEqualTo(votes);
		assertThat(event.getEarlyVotingVotes()).isEqualTo(earlyVotingVotes);
		assertThat(event.getElectionDayVotes()).isEqualTo(electionDayVotes);
	}
}

