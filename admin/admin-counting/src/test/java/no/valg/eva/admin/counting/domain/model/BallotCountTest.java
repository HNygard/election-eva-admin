package no.valg.eva.admin.counting.domain.model;


import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.testng.annotations.Test;

public class BallotCountTest {

	@Test
	public void accept_withVisitorIncludeTrue_callsVisitOnVisitor() {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		when(countingVisitor.include(ballotCount)).thenReturn(true);
		ballotCount.accept(countingVisitor);
		verify(countingVisitor).visit(ballotCount);
	}

	@Test
	public void accept_withVisitorIncludeFalse_doesNotCallVisitOnVisitor() {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		when(countingVisitor.include(ballotCount)).thenReturn(false);
		ballotCount.accept(countingVisitor);
		verify(countingVisitor, never()).visit(ballotCount);
	}

	@Test
	public void accept_withVisitorIncludeTrue_callsAcceptOnCastBallots() {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		CastBallot castBallot = mock(CastBallot.class);
		ballotCount.getCastBallots().add(castBallot);
		when(countingVisitor.include(ballotCount)).thenReturn(true);
		ballotCount.accept(countingVisitor);
		verify(castBallot).accept(countingVisitor);
	}

	@Test
	public void accept_withVisitorIncludeFalse_doesNotCallAcceptOnCastBallots() {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		CastBallot castBallot = mock(CastBallot.class);
		ballotCount.getCastBallots().add(castBallot);
		when(countingVisitor.include(ballotCount)).thenReturn(false);
		ballotCount.accept(countingVisitor);
		verify(castBallot, never()).accept(countingVisitor);
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnVoteCountTrue_returnsTrue() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.isEarlyVoting()).thenReturn(true);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setVoteCount(voteCount);
		assertThat(ballotCount.isEarlyVoting()).isTrue();
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnVoteCountFalse_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.isEarlyVoting()).thenReturn(false);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setVoteCount(voteCount);
		assertThat(ballotCount.isEarlyVoting()).isFalse();
	}

	@Test
	public void getBallotCandidates_givenAffiliationCandidates_returnsAffiliationCandidates() throws Exception {
		Set<Candidate> candidates = new HashSet<>();
		Ballot ballot = mock(Ballot.class);
		when(ballot.getAffiliationCandidates()).thenReturn(candidates);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallot(ballot);
		assertThat(ballotCount.getBallotCandidates()).isSameAs(candidates);
	}

	@Test
	public void getBallotCandidates_givenBallotCountWithBallotRejection_returnsNull() throws Exception {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallotRejection(mock(BallotRejection.class));
		assertThat(ballotCount.getBallotCandidates()).isNull();
	}
	
	@Test
	public void harKategori_voteCountHarSammeKategori_true() {
		BallotCount ballotCount = new BallotCount();
		VoteCount fakeVoteCount = mock(VoteCount.class);
		when(fakeVoteCount.getVoteCountCategoryId()).thenReturn(FO.getId());
		ballotCount.setVoteCount(fakeVoteCount);

		assertThat(ballotCount.harKategori(FO)).isTrue();
	}

	@Test
	public void harKategori_voteCountHarIkkeSammeKategori_false() {
		BallotCount ballotCount = new BallotCount();
		VoteCount fakeVoteCount = mock(VoteCount.class);
		when(fakeVoteCount.getVoteCountCategoryId()).thenReturn(FO.getId());
		ballotCount.setVoteCount(fakeVoteCount);

		assertThat(ballotCount.harKategori(VO)).isFalse();
	}
}
