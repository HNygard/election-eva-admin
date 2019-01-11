package no.valg.eva.admin.settlement.domain.event;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;

public class ElectionVoteCountEvent implements Event {
	private final Party party;
	private final int earlyVotingBallots;
	private final int earlyVotingModifiedBallots;
	private final int electionDayBallots;
	private final int electionDayModifiedBallots;
	private final int baselineVotes;
	private final int addedVotes;
	private final int subtractedVotes;
	private final int contestSeats;

	public ElectionVoteCountEvent(Party party, int earlyVotingBallots, int earlyVotingModifiedBallots, int electionDayBallots,
			int electionDayModifiedBallots, int baselineVotes, int addedVotes, int subtractedVotes, int contestSeats) {
		this.party = party;
		this.earlyVotingBallots = earlyVotingBallots;
		this.earlyVotingModifiedBallots = earlyVotingModifiedBallots;
		this.electionDayBallots = electionDayBallots;
		this.electionDayModifiedBallots = electionDayModifiedBallots;
		this.baselineVotes = baselineVotes;
		this.addedVotes = addedVotes;
		this.subtractedVotes = subtractedVotes;
		this.contestSeats = contestSeats;
	}

	public Party getParty() {
		return party;
	}

	public int getEarlyVotingBallots() {
		return earlyVotingBallots;
	}

	public int getEarlyVotingModifiedBallots() {
		return earlyVotingModifiedBallots;
	}

	public int getElectionDayBallots() {
		return electionDayBallots;
	}

	public int getElectionDayModifiedBallots() {
		return electionDayModifiedBallots;
	}

	public int getBaselineVotes() {
		return baselineVotes;
	}

	public int getAddedVotes() {
		return addedVotes;
	}

	public int getSubtractedVotes() {
		return subtractedVotes;
	}

	public int getContestSeats() {
		return contestSeats;
	}

	public ElectionVoteCount toElectionVoteCount() {
		return new ElectionVoteCount(party, earlyVotingBallots, earlyVotingModifiedBallots, electionDayBallots, electionDayModifiedBallots,
				baselineVotes, addedVotes, subtractedVotes, contestSeats);
	}
}
