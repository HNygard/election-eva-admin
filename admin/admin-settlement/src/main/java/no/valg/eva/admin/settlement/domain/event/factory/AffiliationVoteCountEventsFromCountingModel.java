package no.valg.eva.admin.settlement.domain.event.factory;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;
import no.valg.eva.admin.settlement.domain.event.AffiliationVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.AffiliationVoteCountEventListener;

public class AffiliationVoteCountEventsFromCountingModel extends EventFactory<AffiliationVoteCountEventListener>implements CountingVisitor {
	private final int baselineVotesFactor;

	public AffiliationVoteCountEventsFromCountingModel(int baselineVotesFactor) {
		this.baselineVotesFactor = baselineVotesFactor;
	}

	@Override
	public boolean include(ContestReport contestReport) {
		// include all
		return true;
	}

	@Override
	public void visit(ContestReport contestReport) {
		// do nothing
	}

	@Override
	public boolean include(VoteCount voteCount) {
		// include only final vote counts to settlement
		return voteCount.isFinalCount() && voteCount.isToSettlement();
	}

	@Override
	public void visit(VoteCount voteCount) {
		// do nothing
	}

	@Override
	public boolean include(BallotCount ballotCount) {
		// include only ballot counts with ballots
		return ballotCount.getBallotId() != null;
	}

	@Override
	public void visit(BallotCount ballotCount) {
		Affiliation affiliation = ballotCount.getBallotAffiliation();
		int ballots = ballotCount.getBallots();
		int modifiedBallots = ballotCount.getModifiedBallots();
		int baselineVotes = ballots * baselineVotesFactor;
		if (ballotCount.isEarlyVoting()) {
			fireEarlyVotingEvent(affiliation, ballots, modifiedBallots, ballots, modifiedBallots, baselineVotes);
		} else {
			fireElectionDayEvent(affiliation, ballots, modifiedBallots, ballots, modifiedBallots, baselineVotes);
		}
	}

	private void fireEarlyVotingEvent(
			Affiliation affiliation, int ballots, int modifiedBallots, int earlyVotingBallots, int earlyVotingModifiedBallots, int baselineVotes) {
		fireEvent(new AffiliationVoteCountEvent(
				affiliation, ballots, modifiedBallots, earlyVotingBallots, earlyVotingModifiedBallots, 0, 0, baselineVotes, 0, 0));
	}

	private void fireElectionDayEvent(
			Affiliation affiliation, int ballots, int modifiedBallots, int electionDayBallots, int electionDayModifiedBallots, int baselineVotes) {
		fireEvent(new AffiliationVoteCountEvent(
				affiliation, ballots, modifiedBallots, 0, 0, electionDayBallots, electionDayModifiedBallots, baselineVotes, 0, 0));
	}

	@Override
	public boolean include(CastBallot castBallot) {
		// include all
		return true;
	}

	@Override
	public void visit(CastBallot castBallot) {
		// do nothing
	}

	@Override
	public boolean include(CandidateVote candidateVote) {
		// include only write in candidate votes
		return candidateVote.isWriteIn();
	}

	@Override
	public void visit(CandidateVote candidateVote) {
		fireEvent(candidateVote.getCandidateAffiliation(), 1, 0);
		fireEvent(candidateVote.getBallotAffiliation(), 0, 1);
	}

	private void fireEvent(Affiliation affiliation, int addedVotes, int subtractedVotes) {
		fireEvent(new AffiliationVoteCountEvent(affiliation, 0, 0, 0, 0, 0, 0, 0, addedVotes, subtractedVotes));
	}

	private void fireEvent(AffiliationVoteCountEvent affiliationVoteCountEvent) {
		for (AffiliationVoteCountEventListener eventListener : eventListeners) {
			eventListener.affiliationVoteCountDelta(affiliationVoteCountEvent);
		}
	}
}
