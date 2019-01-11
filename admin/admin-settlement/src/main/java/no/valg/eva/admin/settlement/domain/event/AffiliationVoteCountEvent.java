package no.valg.eva.admin.settlement.domain.event;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

public class AffiliationVoteCountEvent implements Event {
	private final Affiliation affiliation;
	private final int ballots;
	private final int modifiedBallots;
	private final int earlyVotingBallots;
	private final int earlyVotingModifiedBallots;
	private final int electionDayBallots;
	private final int electionDayModifiedBallots;
	private final int baselineVotes;
	private final int addedVotes;
	private final int subtractedVotes;

	public AffiliationVoteCountEvent(Affiliation affiliation, int ballots, int modifiedBallots, int earlyVotingBallots, int earlyVotingModifiedBallots,
			int electionDayBallots, int electionDayModifiedBallots, int baselineVotes, int addedVotes, int subtractedVotes) {
		this.affiliation = affiliation;
		this.ballots = ballots;
		this.modifiedBallots = modifiedBallots;
		this.earlyVotingBallots = earlyVotingBallots;
		this.earlyVotingModifiedBallots = earlyVotingModifiedBallots;
		this.electionDayBallots = electionDayBallots;
		this.electionDayModifiedBallots = electionDayModifiedBallots;
		this.baselineVotes = baselineVotes;
		this.addedVotes = addedVotes;
		this.subtractedVotes = subtractedVotes;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public int getBallots() {
		return ballots;
	}

	public int getModifiedBallots() {
		return modifiedBallots;
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

	public AffiliationVoteCount toAffiliationVoteCount() {
		return new AffiliationVoteCount(affiliation, ballots, modifiedBallots, earlyVotingBallots, earlyVotingModifiedBallots,
				electionDayBallots, electionDayModifiedBallots, baselineVotes, addedVotes, subtractedVotes);
	}
}
