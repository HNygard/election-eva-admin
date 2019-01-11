package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ONE;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;

public class CandidateRankEventsFromCountingModel extends EventFactory<CandidateRankEventListener>implements CountingVisitor {
	private final CandidateRankEventsFromCandidates candidateRankEventsFromCandidates;

	public CandidateRankEventsFromCountingModel(CandidateRankEventsFromCandidates candidateRankEventsFromCandidates) {
		this.candidateRankEventsFromCandidates = candidateRankEventsFromCandidates;
	}

	@Override
	public boolean include(ContestReport contestReport) {
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
		candidateRankEventsFromCandidates.setCandidateVotes(BigDecimal.valueOf(ballotCount.getUnmodifiedBallots()));
		ballotCount.getBallot().accept(candidateRankEventsFromCandidates);
	}

	@Override
	public boolean include(CastBallot castBallot) {
		return true;
	}

	@Override
	public void visit(CastBallot castBallot) {
		List<Candidate> candidates = sortedCandidates(castBallot);
		removeStrickenAndRenumberedCandidates(candidates, castBallot);
		addRenumberedCandidates(candidates, castBallot);
		fireCandidateRankEvents(candidates, castBallot.getBallotCandidates().size());
	}

	private List<Candidate> sortedCandidates(CastBallot castBallot) {
		return castBallot.getBallotCandidates()
				.stream()
				.sorted(this::orderByDisplayOrder)
				.collect(toList());
	}


	private int orderByDisplayOrder(Candidate candidate1, Candidate candidate2) {
		return candidate1.getDisplayOrder() - candidate2.getDisplayOrder();
	}

	private void removeStrickenAndRenumberedCandidates(List<Candidate> candidates, CastBallot castBallot) {
		castBallot.getCandidateVotes()
				.stream()
				.filter(this::isStrikeOutOrRenumbering)
				.map(CandidateVote::getCandidate)
				.forEach(candidates::remove);
	}

	private boolean isStrikeOutOrRenumbering(CandidateVote candidateVote) {
		return candidateVote.isStrikeOut() || candidateVote.isRenumbering();
	}

	private void addRenumberedCandidates(List<Candidate> candidates, CastBallot castBallot) {
		List<CandidateVote> sortedRenumberingCandidateVotes = sortedRenumberingCandidateVotes(castBallot);
		for (CandidateVote candidateVote : sortedRenumberingCandidateVotes) {
			int candidateIndex = candidateVote.getRenumberPosition() - 1;
			if (candidateIndex < candidates.size()) {
				candidates.add(candidateIndex, candidateVote.getCandidate());
			} else {
				candidates.add(candidateVote.getCandidate());
			}
		}
	}

	private List<CandidateVote> sortedRenumberingCandidateVotes(CastBallot castBallot) {
		return castBallot.getCandidateVotes()
				.stream()
				.filter(CandidateVote::isRenumbering)
				.sorted(this::orderByRenumberPosition)
				.collect(toList());
	}

	private int orderByRenumberPosition(CandidateVote candidateVote1, CandidateVote candidateVote2) {
		return candidateVote1.getRenumberPosition() - candidateVote2.getRenumberPosition();
	}

	private void fireCandidateRankEvents(List<Candidate> candidates, int numberOfCandidates) {
		for (int i = 0; i < candidates.size(); i++) {
			Candidate candidate = candidates.get(i);
			Affiliation affiliation = candidate.getAffiliation();
			int candidateRank = i + 1;
			fireCandidateRankEvents(numberOfCandidates, candidateRank, candidate, affiliation);
		}
	}

	private void fireCandidateRankEvents(int numberOfCandidates, int candidateRank, Candidate candidate, Affiliation affiliation) {
		// each candidate with a rank candidateRank also has candidate ranks from candidateRank + 1 to numberOfCandidates to ensure that
		// the candidate may win the next rank if the current rank at candidateRank is lost
		for (int rankNumber = candidateRank; rankNumber < numberOfCandidates + 1; rankNumber++) {
			fireEvent(candidate, affiliation, ONE, rankNumber);
		}
	}

	@Override
	public boolean include(CandidateVote candidateVote) {
		return false;
	}

	@Override
	public void visit(CandidateVote candidateVote) {
		// do nothing
	}

	private void fireEvent(Candidate candidate, Affiliation affiliation, BigDecimal votes, int rankNumber) {
		for (CandidateRankEventListener eventListener : eventListeners) {
			eventListener.candidateRankDelta(new CandidateRankEvent(candidate, affiliation, votes, rankNumber));
		}
	}

}
