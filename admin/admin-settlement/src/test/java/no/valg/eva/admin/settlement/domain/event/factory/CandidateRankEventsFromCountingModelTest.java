package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.addAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateRankEventsFromCountingModelTest extends MockUtilsTestCase {
	@Test
	public void include_givenContestReport_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(mock(ContestReport.class))).isTrue();
	}

	@Test
	public void include_givenFinalVoteCountToSettlement_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(voteCount(true, true))).isTrue();
	}

	@Test
	public void include_givenNotFinalVoteCount_returnsFalse() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(voteCount())).isFalse();
	}

	@Test
	public void include_givenFinalVoteCountNotToSettlement_returnsFalse() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(voteCount(true))).isFalse();
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
		assertThat(new CandidateRankEventsFromCountingModel(null).include(ballotCount)).isFalse();
	}

	@Test
	public void include_givenBallotCountWithOrdinaryBallot_returnsTrue() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallotId()).thenReturn("ORDINARY");
		assertThat(new CandidateRankEventsFromCountingModel(null).include(ballotCount)).isTrue();
	}

	@Test
	public void include_givenBallotCountWithBallotRejection_returnsFalse() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallot()).thenReturn(null);
		when(ballotCount.getBallotRejection()).thenReturn(new BallotRejection());
		assertThat(new CandidateRankEventsFromCountingModel(null).include(ballotCount)).isFalse();
	}

	@Test
	public void include_givenCastBallot_returnsTrue() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(mock(CastBallot.class))).isTrue();
	}

	@Test
	public void include_givenCandidateVote_returnsFalse() throws Exception {
		assertThat(new CandidateRankEventsFromCountingModel(null).include(mock(CandidateVote.class))).isFalse();
	}

	@Test
	public void visit_givenBallotCount_ballotAcceptsCandidateRanksVisitor() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getUnmodifiedBallots()).thenReturn(10);
		CandidateRankEventsFromCandidates candidateRankEventsFromCandidates = mock(CandidateRankEventsFromCandidates.class);
		new CandidateRankEventsFromCountingModel(candidateRankEventsFromCandidates).visit(ballotCount);
		verify(candidateRankEventsFromCandidates).setCandidateVotes(TEN);
		verify(ballotCount.getBallot()).accept(candidateRankEventsFromCandidates);
	}

	@Test
	public void visit_givenCastBallot_firesCandidateRanksEvents() throws Exception {
		Affiliation affiliation = affiliation();
		Candidate candidate1 = candidate(1, affiliation);
		Candidate candidate2 = candidate(2, affiliation);
		Candidate candidate3 = candidate(3, affiliation);
		Candidate candidate4 = candidate(4, affiliation);
		Candidate candidate5 = candidate(5, affiliation);
		Set<CandidateVote> candidateVotes = candidateVotes(candidateVote(candidate5, 3), candidateVote(candidate2, null), candidateVote(candidate3, 1),
				candidateVote(candidate4, null));
		CastBallot castBallot = castBallot(candidateVotes, candidate1, candidate2, candidate3, candidate4, candidate5);
		CandidateRankEventListener candidateRankEventListener = mock(CandidateRankEventListener.class);
		CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel = candidateRankEventsFromCountingModel(candidateRankEventListener);

		candidateRankEventsFromCountingModel.visit(castBallot);

		ArgumentCaptor<CandidateRankEvent> argumentCaptor = ArgumentCaptor.forClass(CandidateRankEvent.class);
		verify(candidateRankEventListener, times(12)).candidateRankDelta(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).contains(candidateRankEvents(affiliation, 5, candidate3, candidate1, candidate5));
	}

	private Affiliation affiliation() {
		Affiliation affiliation = new Affiliation();
		Party party = mock(Party.class);
		when(party.getId()).thenReturn("P");
		affiliation.setParty(party);
		return affiliation;
	}

	private Candidate candidate(int displayOrder, Affiliation affiliation) {
		Candidate candidate = new Candidate();
		candidate.setPk((long) displayOrder);
		candidate.setFirstName(String.valueOf(displayOrder));
		candidate.setDisplayOrder(displayOrder);
		candidate.setAffiliation(affiliation);
		return candidate;
	}

	private Set<CandidateVote> candidateVotes(CandidateVote... candidateVotes) {
		Set<CandidateVote> candidateVoteSet = new LinkedHashSet<>();
		addAll(candidateVoteSet, candidateVotes);
		return candidateVoteSet;
	}

	private CandidateVote candidateVote(Candidate candidate, Integer renumberPosition) {
		CandidateVote candidateVote = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		when(candidateVote.getCandidate()).thenReturn(candidate);
		when(candidateVote.getRenumberPosition()).thenReturn(renumberPosition);
		when(candidateVote.isRenumbering()).thenReturn(renumberPosition != null);
		when(candidateVote.isStrikeOut()).thenReturn(renumberPosition == null);
		return candidateVote;
	}

	@SuppressWarnings("unchecked")
	private CastBallot castBallot(Set<CandidateVote> candidateVotes, Candidate... candidates) {
		Set<Candidate> candidateSet = new HashSet<>();
		addAll(candidateSet, candidates);
		CastBallot castBallot = mock(CastBallot.class);
		when(castBallot.getBallotCandidates()).thenReturn(candidateSet);
		when(castBallot.getCandidateVotes()).thenReturn(candidateVotes);
		return castBallot;
	}

	private CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel(CandidateRankEventListener candidateRankEventListener) {
		CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel = new CandidateRankEventsFromCountingModel(null);
		candidateRankEventsFromCountingModel.addEventListener(candidateRankEventListener);
		return candidateRankEventsFromCountingModel;
	}

	private CandidateRankEvent[] candidateRankEvents(Affiliation affiliation, int candidateCount, Candidate... candidates) {
		// when m is the number of candidates on a ballot and n is less than m, a candidate with rank n is ranked from n to m
		// candidate with rank m is ranked only once.
		// Example: when m = 5 and n = 1, candidate has 5 ranks from 1 to 5.
		ArrayList<CandidateRankEvent> candidateRankEvents = new ArrayList<>();
		for (int i = 0; i < candidates.length; i++) {
			for (int rankNumber = i + 1; rankNumber < candidateCount - i; rankNumber++) {
				candidateRankEvents.add(candidateRankEvent(candidates[i], affiliation, rankNumber));
			}
		}
		return candidateRankEvents.toArray(new CandidateRankEvent[candidateRankEvents.size()]);
	}

	private CandidateRankEvent candidateRankEvent(Candidate candidate, Affiliation affiliation, Integer rankNumber) {
		return new CandidateRankEvent(candidate, affiliation, ONE, rankNumber);
	}
}

