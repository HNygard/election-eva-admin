package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateVoteCountEventListener;

public class CandidateVoteCountEventsFromCountingModel extends EventFactory<CandidateVoteCountEventListener>implements CountingVisitor {
	private final CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel;

	public CandidateVoteCountEventsFromCountingModel() {
		this.candidateVoteCountEventsFromConfigurationModel = null;
	}

	public CandidateVoteCountEventsFromCountingModel(CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel) {
		this.candidateVoteCountEventsFromConfigurationModel = candidateVoteCountEventsFromConfigurationModel;
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
		// include only ballot counts with ordinary ballots
		return ballotCount.getBallotId() != null && !ballotCount.isBlank();
	}

	@Override
	public void visit(BallotCount ballotCount) {
		if (candidateVoteCountEventsFromConfigurationModel != null) {
			// visit ballot configuration for this ballot count
			candidateVoteCountEventsFromConfigurationModel.setCurrentBallotCount(ballotCount);
			ballotCount.getBallot().accept(candidateVoteCountEventsFromConfigurationModel);
		}
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
		// include all
		return true;
	}

	@Override
	public void visit(CandidateVote candidateVote) {
		Affiliation ballotAffiliation = candidateVote.getBallotAffiliation();
		Candidate candidate = candidateVote.getCandidate();
		VoteCategory voteCategory = candidateVote.getVoteCategory();
		Integer rankNumber = candidateVote.getRenumberPosition();
		if (candidateVote.isEarlyVoting()) {
			fireEvent(ballotAffiliation, candidate, voteCategory, rankNumber, ONE, ONE, ZERO);
		} else {
			fireEvent(ballotAffiliation, candidate, voteCategory, rankNumber, ONE, ZERO, ONE);
		}
	}

	private void fireEvent(Affiliation affiliation, Candidate candidate, VoteCategory voteCategory, Integer rankNumber, BigDecimal votes,
			BigDecimal earlyVotingVotes, BigDecimal electionDayVotes) {
		CandidateVoteCountEvent candidateVoteCountEvent = new CandidateVoteCountEvent(affiliation, candidate, voteCategory, rankNumber, votes, earlyVotingVotes,
				electionDayVotes);
		for (CandidateVoteCountEventListener eventListener : eventListeners) {
			eventListener.candidateVoteCountDelta(candidateVoteCountEvent);
		}
	}
}
