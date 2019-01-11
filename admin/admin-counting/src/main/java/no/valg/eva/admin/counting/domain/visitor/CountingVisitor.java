package no.valg.eva.admin.counting.domain.visitor;

import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public interface CountingVisitor {
	boolean include(ContestReport contestReport);

	void visit(ContestReport contestReport);

	boolean include(VoteCount voteCount);

	void visit(VoteCount voteCount);

	boolean include(BallotCount ballotCount);

	void visit(BallotCount ballotCount);

	boolean include(CastBallot castBallot);

	void visit(CastBallot castBallot);

	boolean include(CandidateVote candidateVote);

	void visit(CandidateVote candidateVote);
}
