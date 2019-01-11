package no.valg.eva.admin.counting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class CandidateVoteTest {

	@Test
	public void isPersonalVote_voteCategoryIsPersonal_returnsTrue() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.personal.name());

		Assertions.assertThat(candidateVote(voteCategory).isPersonalVote()).isTrue();
	}

	@Test
	public void isPersonalVote_voteCategoryIsNotPersonal_returnsFalse() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.writein.name());

		assertThat(candidateVote(voteCategory).isPersonalVote()).isFalse();
	}

	@Test
	public void isWriteIn_voteCategoryIsWritein_returnsTrue() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.writein.name());

		assertThat(candidateVote(voteCategory).isWriteIn()).isTrue();
	}

	@Test
	public void isWriteIn_voteCategoryIsNotWritein_returnsFalse() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.personal.name());

		assertThat(candidateVote(voteCategory).isWriteIn()).isFalse();
	}

	@Test
	public void isRenumbering_voteCategoryIsRenumber_returnsTrue() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.renumber.name());

		assertThat(candidateVote(voteCategory).isRenumbering()).isTrue();
	}

	@Test
	public void isRenumbering_voteCategoryIsNotRenumber_returnsFalse() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.writein.name());

		assertThat(candidateVote(voteCategory).isRenumbering()).isFalse();
	}

	@Test
	public void isStrikeOut_voteCategoryIsStrikeout_returnsTrue() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.strikeout.name());

		assertThat(candidateVote(voteCategory).isStrikeOut()).isTrue();
	}

	@Test
	public void isStrikeOut_voteCategoryIsNotStrikeout_returnsFalse() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.writein.name());

		assertThat(candidateVote(voteCategory).isStrikeOut()).isFalse();
	}

	@Test
	public void isBaseline_voteCategoryIsBaseline_returnsTrue() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.baseline.name());

		assertThat(candidateVote(voteCategory).isBaseline()).isTrue();
	}

	@Test
	public void isBaseline_voteCategoryIsNotBaseline_returnsFalse() {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(VoteCategory.VoteCategoryValues.writein.name());

		assertThat(candidateVote(voteCategory).isBaseline()).isFalse();
	}

	private CandidateVote candidateVote(VoteCategory voteCategory) {
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setVoteCategory(voteCategory);
		return candidateVote;
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setPk(new Random().nextLong());
		when(countingVisitor.include(candidateVote)).thenReturn(true);
		candidateVote.accept(countingVisitor);
		verify(countingVisitor).visit(candidateVote);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setPk(new Random().nextLong());
		when(countingVisitor.include(candidateVote)).thenReturn(false);
		candidateVote.accept(countingVisitor);
		verify(countingVisitor, never()).visit(candidateVote);
	}

	@Test
	public void getCandidateAffiliation_givenCandidateVote_returnsCandidateAffiliation() throws Exception {
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		CandidateVote candidateVote = new CandidateVote(candidate, null, null, null);
		assertThat(candidateVote.getCandidateAffiliation()).isSameAs(candidate.getAffiliation());
	}

	@Test
	public void getBallotAffiliation_givenCandidateVote_returnsBallotAffiliation() throws Exception {
		CastBallot castBallot = mock(CastBallot.class, RETURNS_DEEP_STUBS);
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setCastBallot(castBallot);
		assertThat(candidateVote.getBallotAffiliation()).isSameAs(castBallot.getBallotCount().getBallot().getAffiliation());
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnCastBallotIsTrue_returnsTrue() throws Exception {
		CastBallot castBallot = mock(CastBallot.class);
		when(castBallot.isEarlyVoting()).thenReturn(true);
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setCastBallot(castBallot);
		assertThat(candidateVote.isEarlyVoting()).isTrue();
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnCastBallotIsFalse_returnsFalse() throws Exception {
		CastBallot castBallot = mock(CastBallot.class);
		when(castBallot.isEarlyVoting()).thenReturn(false);
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setCastBallot(castBallot);
		assertThat(candidateVote.isEarlyVoting()).isFalse();
	}
}
