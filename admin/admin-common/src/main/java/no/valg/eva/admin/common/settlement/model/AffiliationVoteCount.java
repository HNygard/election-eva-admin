package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import no.valg.eva.admin.configuration.domain.model.Affiliation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AffiliationVoteCount implements Serializable {
	private final Affiliation affiliation;
	private final int earlyVotingBallots;
	private final int earlyVotingModifiedBallots;
	private final int electionDayBallots;
	private final int electionDayModifiedBallots;
	private final int baselineVotes;
	private final int addedVotes;
	private final int subtractedVotes;

	public AffiliationVoteCount(Affiliation affiliation, int earlyVotingBallots, int earlyVotingModifiedBallots, int electionDayBallots,
			int electionDayModifiedBallots, int baselineVotes, int addedVotes, int subtractedVotes) {
		this.affiliation = affiliation;
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
		return this.earlyVotingBallots + this.electionDayBallots;
	}

	public int getModifiedBallots() {
		return this.earlyVotingModifiedBallots + electionDayModifiedBallots;
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

	public int getVotes() {
		return this.baselineVotes + this.addedVotes - this.subtractedVotes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AffiliationVoteCount)) {
			return false;
		}
		AffiliationVoteCount that = (AffiliationVoteCount) o;
		return new EqualsBuilder()
				.append(earlyVotingBallots, that.earlyVotingBallots)
				.append(earlyVotingModifiedBallots, that.earlyVotingModifiedBallots)
				.append(electionDayBallots, that.electionDayBallots)
				.append(electionDayModifiedBallots, that.electionDayModifiedBallots)
				.append(baselineVotes, that.baselineVotes)
				.append(addedVotes, that.addedVotes)
				.append(subtractedVotes, that.subtractedVotes)
				.append(affiliation, that.affiliation)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(affiliation)
				.append(earlyVotingBallots)
				.append(earlyVotingModifiedBallots)
				.append(electionDayBallots)
				.append(electionDayModifiedBallots)
				.append(baselineVotes)
				.append(addedVotes)
				.append(subtractedVotes)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("affiliation", affiliation)
				.append("earlyVotingBallots", earlyVotingBallots)
				.append("earlyVotingModifiedBallots", earlyVotingModifiedBallots)
				.append("electionDayBallots", electionDayBallots)
				.append("electionDayModifiedBallots", electionDayModifiedBallots)
				.append("baselineVotes", baselineVotes)
				.append("addedVotes", addedVotes)
				.append("subtractedVotes", subtractedVotes)
				.toString();
	}
}
