package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.FetchType.LAZY;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Party;

@Entity
@Table(name = "election_vote_count", uniqueConstraints = { @UniqueConstraint(columnNames = { "leveling_seat_settlement_pk", "party_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "election_vote_count_pk"))
public class ElectionVoteCount extends VersionedEntity {
	private LevelingSeatSettlement levelingSeatSettlement;
	private Party party;
	private int ballots;
	private int modifiedBallots;
	private int earlyVotingBallots;
	private int earlyVotingModifiedBallots;
	private int electionDayBallots;
	private int electionDayModifiedBallots;
	private int baselineVotes;
	private int addedVotes;
	private int subtractedVotes;
	private int votes;
	private int totalVotes;
	private int contestSeats;
	private boolean eligibleForLevelingSeats;

	public ElectionVoteCount() {
	}

	public ElectionVoteCount(Party party, int earlyVotingBallots, int earlyVotingModifiedBallots, int electionDayBallots,
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
		updateBallots();
		updateModifiedBallots();
		updateVotes();
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "leveling_seat_settlement_pk", nullable = false)
	public LevelingSeatSettlement getLevelingSeatSettlement() {
		return levelingSeatSettlement;
	}

	public void setLevelingSeatSettlement(LevelingSeatSettlement levelingSeatSettlement) {
		this.levelingSeatSettlement = levelingSeatSettlement;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	@Column(name = "ballots", nullable = false)
	public int getBallots() {
		return ballots;
	}

	public void setBallots(int ballots) {
		this.ballots = ballots;
	}

	private void updateBallots() {
		this.ballots = this.earlyVotingBallots + this.electionDayBallots;
	}

	@Column(name = "modified_ballots", nullable = false)
	public int getModifiedBallots() {
		return modifiedBallots;
	}

	public void setModifiedBallots(int modifiedBallots) {
		this.modifiedBallots = modifiedBallots;
	}

	private void updateModifiedBallots() {
		this.modifiedBallots = this.earlyVotingModifiedBallots + this.electionDayModifiedBallots;
	}

	@Column(name = "early_voting_ballots", nullable = false)
	public int getEarlyVotingBallots() {
		return earlyVotingBallots;
	}

	public void setEarlyVotingBallots(int earlyVotingBallots) {
		this.earlyVotingBallots = earlyVotingBallots;
		updateBallots();
	}

	public void incrementEarlyVotingBallots(int earlyVotingBallots) {
		setEarlyVotingBallots(getEarlyVotingBallots() + earlyVotingBallots);
	}

	@Column(name = "early_voting_modified_ballots", nullable = false)
	public int getEarlyVotingModifiedBallots() {
		return earlyVotingModifiedBallots;
	}

	public void setEarlyVotingModifiedBallots(int earlyVotingModifiedBallots) {
		this.earlyVotingModifiedBallots = earlyVotingModifiedBallots;
		updateModifiedBallots();
	}

	public void incrementEarlyVotingModifiedBallots(int earlyVotingModifiedBallots) {
		setEarlyVotingModifiedBallots(getEarlyVotingModifiedBallots() + earlyVotingModifiedBallots);
	}

	@Column(name = "election_day_ballots", nullable = false)
	public int getElectionDayBallots() {
		return electionDayBallots;
	}

	public void setElectionDayBallots(int electionDayBallots) {
		this.electionDayBallots = electionDayBallots;
		updateBallots();
	}

	public void incrementElectionDayBallots(int electionDayBallots) {
		setElectionDayBallots(getElectionDayBallots() + electionDayBallots);
	}

	@Column(name = "election_day_modified_ballots", nullable = false)
	public int getElectionDayModifiedBallots() {
		return electionDayModifiedBallots;
	}

	public void setElectionDayModifiedBallots(int electionDayModifiedBallots) {
		this.electionDayModifiedBallots = electionDayModifiedBallots;
		updateModifiedBallots();
	}

	public void incrementElectionDayModifiedBallots(int electionDayModifiedBallots) {
		setElectionDayModifiedBallots(getElectionDayModifiedBallots() + electionDayModifiedBallots);
	}

	@Column(name = "baseline_votes", nullable = false)
	public int getBaselineVotes() {
		return baselineVotes;
	}

	public void setBaselineVotes(int baselineVotes) {
		this.baselineVotes = baselineVotes;
		updateVotes();
	}

	public void incrementBaselineVotes(int baselineVotes) {
		setBaselineVotes(getBaselineVotes() + baselineVotes);
	}

	@Column(name = "added_votes", nullable = false)
	public int getAddedVotes() {
		return addedVotes;
	}

	public void setAddedVotes(int addedVotes) {
		this.addedVotes = addedVotes;
		updateVotes();
	}

	public void incrementAddedVotes(int addedVotes) {
		setAddedVotes(getAddedVotes() + addedVotes);
	}

	@Column(name = "subtracted_votes", nullable = false)
	public int getSubtractedVotes() {
		return subtractedVotes;
	}

	public void setSubtractedVotes(int subtractedVotes) {
		this.subtractedVotes = subtractedVotes;
		updateVotes();
	}

	public void incrementSubtractedVotes(int subtractedVotes) {
		setSubtractedVotes(getSubtractedVotes() + subtractedVotes);
	}

	@Column(name = "votes", nullable = false)
	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	private void updateVotes() {
		this.votes = this.baselineVotes + this.addedVotes - this.subtractedVotes;
	}

	@Column(name = "total_votes", nullable = false)
	public int getTotalVotes() {
		return totalVotes;
	}

	public void setTotalVotes(int totalVotes) {
		this.totalVotes = totalVotes;
	}

	@Column(name = "contest_seats", nullable = false)
	public int getContestSeats() {
		return contestSeats;
	}

	public void setContestSeats(int contestSeats) {
		this.contestSeats = contestSeats;
	}

	public void incrementContestSeats(int contestSeats) {
		setContestSeats(getContestSeats() + contestSeats);
	}

	@Column(name = "eligible_for_leveling_seats", nullable = false)
	public boolean isEligibleForLevelingSeats() {
		return eligibleForLevelingSeats;
	}

	public void setEligibleForLevelingSeats(boolean eligibleForLevelingSeats) {
		this.eligibleForLevelingSeats = eligibleForLevelingSeats;
	}

	public void updateEligibleForLevelingSeats(BigDecimal levelingSeatsVoteShareThreshold) {
		BigDecimal levelingSeatsVotesThreshold = levelingSeatsVoteShareThreshold.multiply(BigDecimal.valueOf(totalVotes));
		boolean newEligibleForLevelingSeats = BigDecimal.valueOf(votes).compareTo(levelingSeatsVotesThreshold) >= 0;
		setEligibleForLevelingSeats(newEligibleForLevelingSeats && !party.isLokaltParti());
	}
}
