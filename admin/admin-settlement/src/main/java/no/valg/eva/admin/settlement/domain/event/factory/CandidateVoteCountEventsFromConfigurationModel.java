package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateVoteCountEventListener;

public class CandidateVoteCountEventsFromConfigurationModel extends EventFactory<CandidateVoteCountEventListener>implements ConfigurationVisitor {
	private final VoteCategory baselineVoteCategory;
	private final BigDecimal baselineVoteFactor;
	private BallotCount currentBallotCount;

	public CandidateVoteCountEventsFromConfigurationModel(VoteCategory baselineVoteCategory, BigDecimal baselineVoteFactor) {
		this.baselineVoteCategory = baselineVoteCategory;
		this.baselineVoteFactor = baselineVoteFactor;
	}

	public void setCurrentBallotCount(BallotCount currentBallotCount) {
		this.currentBallotCount = currentBallotCount;
	}

	@Override
	public boolean include(Contest contest) {
		// include all
		return true;
	}

	@Override
	public void visit(Contest contest) {
		// do nothing
	}

	@Override
	public boolean include(Ballot ballot) {
		// include all
		return true;
	}

	@Override
	public void visit(Ballot ballot) {
		// do nothing
	}

	@Override
	public boolean include(Affiliation affiliation) {
		return affiliation.isApproved();
	}

	@Override
	public void visit(Affiliation affiliation) {
		// do nothing
	}

	@Override
	public boolean include(Candidate candidate) {
		return candidate.isBaselineVotes();
	}

	@Override
	public void visit(Candidate candidate) {
		Affiliation candidateAffiliation = candidate.getAffiliation();
		BigDecimal votes = baselineVoteFactor.multiply(BigDecimal.valueOf(currentBallotCount.getBallots()));
		if (currentBallotCount.isEarlyVoting()) {
			fireEvent(candidateAffiliation, candidate, baselineVoteCategory, votes, votes, ZERO);
		} else {
			fireEvent(candidateAffiliation, candidate, baselineVoteCategory, votes, ZERO, votes);
		}
	}

	private void fireEvent(Affiliation affiliation, Candidate candidate, VoteCategory voteCategory, BigDecimal votes,
			BigDecimal earlyVotingVotes, BigDecimal electionDayVotes) {
		CandidateVoteCountEvent event = new CandidateVoteCountEvent(affiliation, candidate, voteCategory, null, votes, earlyVotingVotes, electionDayVotes);
		for (CandidateVoteCountEventListener eventListener : eventListeners) {
			eventListener.candidateVoteCountDelta(event);
		}
	}
}
