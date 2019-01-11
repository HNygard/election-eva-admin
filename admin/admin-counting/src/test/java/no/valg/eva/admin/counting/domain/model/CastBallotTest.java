package no.valg.eva.admin.counting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.testng.annotations.Test;

public class CastBallotTest {

	public static final int ONE_PERSONAL_ONE_WRITEIN_AND_ONE_BASELINE = 3;
	private static final Long CANDIDATE_PK_1 = 1L;
	private static final Long CANDIDATE_PK_2 = 2L;
	private static final Long CANDIDATE_PK_3 = 3L;
	private static final Long CANDIDATE_PK_4 = 4L;
	private static final Long CANDIDATE_PK_5 = 5L;

	@Test
	public void updateCandidateVotes_oneExistingCandidateVoteOneNew_existingIsReplacedNewIsAdded() {
		CastBallot castBallot = createModifiedBallot();
		Set<CandidateVote> candidateVotes = new HashSet<>();
		CandidateVote candidateVote1 = createCandidateVote(CANDIDATE_PK_2, castBallot, VoteCategory.VoteCategoryValues.personal);
		candidateVotes.add(candidateVote1);
		CandidateVote candidateVote2 = createCandidateVote(CANDIDATE_PK_4, castBallot, VoteCategory.VoteCategoryValues.writein);
		candidateVotes.add(candidateVote2);

		castBallot.updateCandidateVotes(candidateVotes);

		assertThat(castBallot.getCandidateVotes()).hasSize(ONE_PERSONAL_ONE_WRITEIN_AND_ONE_BASELINE);
		assertThat(castBallot.getCandidateVotes()).contains(candidateVote1);
		assertThat(castBallot.getCandidateVotes()).contains(candidateVote2);
		castBallot.getCandidateVotes().remove(candidateVote1);
		castBallot.getCandidateVotes().remove(candidateVote2);
		assertThat(castBallot.getCandidateVotes().iterator().next().getVoteCategory().getId()).isEqualTo(VoteCategory.VoteCategoryValues.baseline.name());
	}

	private CastBallot createModifiedBallot() {
		CastBallot castBallot = new CastBallot();
		Set<CandidateVote> candidateVotes = new HashSet<>();
		candidateVotes.add(createCandidateVote(CANDIDATE_PK_1, castBallot, VoteCategory.VoteCategoryValues.personal)); // skal tas bort
		candidateVotes.add(createCandidateVote(CANDIDATE_PK_2, castBallot, VoteCategory.VoteCategoryValues.personal));
		candidateVotes.add(createCandidateVote(CANDIDATE_PK_3, castBallot, VoteCategory.VoteCategoryValues.renumber)); // skal tas bort
		candidateVotes.add(createCandidateVote(CANDIDATE_PK_5, castBallot, VoteCategory.VoteCategoryValues.baseline)); // skal eksistere
		castBallot.setCandidateVotes(candidateVotes);
		return castBallot;
	}

	private CandidateVote createCandidateVote(Long candidatePk, CastBallot castBallot, VoteCategory.VoteCategoryValues voteCategoryId) {
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setCandidate(createCandidate(candidatePk));
		candidateVote.setCastBallot(castBallot);
		candidateVote.setVoteCategory(createVoteCategory(voteCategoryId));
		return candidateVote;
	}

	private VoteCategory createVoteCategory(VoteCategory.VoteCategoryValues voteCategoryId) {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(voteCategoryId.name());
		return voteCategory;
	}

	private Candidate createCandidate(Long candidatePk) {
		Candidate candidate = new Candidate();
		candidate.setPk(candidatePk);
		return candidate;
	}

	@Test
	public void accept_withVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		when(countingVisitor.include(castBallot)).thenReturn(true);
		castBallot.accept(countingVisitor);
		verify(countingVisitor).visit(castBallot);
	}

	@Test
	public void accept_withVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		when(countingVisitor.include(castBallot)).thenReturn(false);
		castBallot.accept(countingVisitor);
		verify(countingVisitor, never()).visit(castBallot);
	}

	@Test
	public void accept_withVisitorWithIncludeTrue_callsAcceptOnCandidateVotes() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		CandidateVote candidateVote = mock(CandidateVote.class);
		castBallot.getCandidateVotes().add(candidateVote);
		when(countingVisitor.include(castBallot)).thenReturn(true);
		castBallot.accept(countingVisitor);
		verify(candidateVote).accept(countingVisitor);
	}

	@Test
	public void accept_withVisitorWithIncludeFalse_doesNotCallAcceptOnCandidateVotes() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		CandidateVote candidateVote = mock(CandidateVote.class);
		castBallot.getCandidateVotes().add(candidateVote);
		when(countingVisitor.include(castBallot)).thenReturn(false);
		castBallot.accept(countingVisitor);
		verify(candidateVote, never()).accept(countingVisitor);
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnBallotCountIsTrue_returnsTrue() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class);
		when(ballotCount.isEarlyVoting()).thenReturn(true);
		CastBallot castBallot = new CastBallot();
		castBallot.setBallotCount(ballotCount);
		assertThat(castBallot.isEarlyVoting()).isTrue();
	}

	@Test
	public void isEarlyVoting_givenEarlyVotingOnBallotCountIsFalse_returnsFalse() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class);
		when(ballotCount.isEarlyVoting()).thenReturn(false);
		CastBallot castBallot = new CastBallot();
		castBallot.setBallotCount(ballotCount);
		assertThat(castBallot.isEarlyVoting()).isFalse();
	}

	@Test
	public void getBallotCandidates_givenBallotCandidatesOnBallotCount_returnsBallotCandidates() throws Exception {
		Set<Candidate> candidates = new HashSet<>();
		BallotCount ballotCount = mock(BallotCount.class);
		when(ballotCount.getBallotCandidates()).thenReturn(candidates);
		CastBallot castBallot = new CastBallot();
		castBallot.setBallotCount(ballotCount);
		assertThat(castBallot.getBallotCandidates()).isSameAs(candidates);
	}
}
