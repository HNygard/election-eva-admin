package no.valg.eva.admin.settlement.domain.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Settlement: Count of affiliation votes
 */
@Entity
@Table(name = "affiliation_vote_count", uniqueConstraints = @UniqueConstraint(columnNames = { "settlement_pk", "affiliation_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "affiliation_vote_count_pk"))
@NamedQueries({ @NamedQuery(name = "AffiliationVoteCount.findBySettlement", query = "SELECT avc FROM AffiliationVoteCount avc WHERE"
		+ " avc.settlement.pk = :settlementPk AND affiliation.ballot.id != 'BLANK' ORDER BY avc.affiliation.displayOrder") })
public class AffiliationVoteCount extends VersionedEntity {

	private Settlement settlement;
	private Affiliation affiliation;
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

	public AffiliationVoteCount() {
	}

	public AffiliationVoteCount(Affiliation affiliation, int ballots, int modifiedBallots, int earlyVotingBallots, int earlyVotingModifiedBallots,
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
		updateVotes();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_pk", nullable = false)
	public Settlement getSettlement() {
		return this.settlement;
	}

	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "affiliation_pk", nullable = false)
	public Affiliation getAffiliation() {
		return this.affiliation;
	}

	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	@Transient
	public Set<Candidate> getAffiliationCandidates() {
		return getAffiliation().getCandidates();
	}

	@Column(name = "ballots", nullable = false)
	public int getBallots() {
		return this.ballots;
	}

	public void setBallots(int ballots) {
		this.ballots = ballots;
	}

	@Transient
	public void incrementBallots(int ballots) {
		setBallots(getBallots() + ballots);
	}

	@Column(name = "modified_ballots", nullable = false)
	public int getModifiedBallots() {
		return this.modifiedBallots;
	}

	public void setModifiedBallots(int modifiedBallots) {
		this.modifiedBallots = modifiedBallots;
	}

	@Transient
	public void incrementModifiedBallots(int modifiedBallots) {
		setModifiedBallots(getModifiedBallots() + modifiedBallots);
	}

	@Column(name = "early_voting_ballots", nullable = false)
	public int getEarlyVotingBallots() {
		return this.earlyVotingBallots;
	}

	public void setEarlyVotingBallots(int earlyVotingBallots) {
		this.earlyVotingBallots = earlyVotingBallots;
	}

	@Transient
	public void incrementEarlyVotingBallots(int earlyVotingBallots) {
		setEarlyVotingBallots(getEarlyVotingBallots() + earlyVotingBallots);
	}

	@Column(name = "early_voting_modified_ballots", nullable = false)
	public int getEarlyVotingModifiedBallots() {
		return this.earlyVotingModifiedBallots;
	}

	public void setEarlyVotingModifiedBallots(int earlyVotingModifiedBallots) {
		this.earlyVotingModifiedBallots = earlyVotingModifiedBallots;
	}

	@Transient
	public void incrementEarlyVotingModifiedBallots(int earlyVotingModifiedBallots) {
		setEarlyVotingModifiedBallots(getEarlyVotingModifiedBallots() + earlyVotingModifiedBallots);
	}

	@Column(name = "election_day_ballots", nullable = false)
	public int getElectionDayBallots() {
		return this.electionDayBallots;
	}

	public void setElectionDayBallots(int electionDayBallots) {
		this.electionDayBallots = electionDayBallots;
	}

	@Transient
	public void incrementElectionDayBallots(int electionDayBallots) {
		setElectionDayBallots(getElectionDayBallots() + electionDayBallots);
	}

	@Column(name = "election_day_modified_ballots", nullable = false)
	public int getElectionDayModifiedBallots() {
		return this.electionDayModifiedBallots;
	}

	public void setElectionDayModifiedBallots(int electionDayModifiedBallots) {
		this.electionDayModifiedBallots = electionDayModifiedBallots;
	}

	@Transient
	public void incrementElectionDayModifiedBallots(int electionDayModifiedBallots) {
		setElectionDayModifiedBallots(getElectionDayModifiedBallots() + electionDayModifiedBallots);
	}

	@Column(name = "baseline_votes", nullable = false)
	public int getBaselineVotes() {
		return this.baselineVotes;
	}

	public void setBaselineVotes(int baselineVotes) {
		this.baselineVotes = baselineVotes;
		updateVotes();
	}

	private void updateBaselineVotes() {
		this.baselineVotes = this.votes - addedVotes + subtractedVotes;
	}

	@Transient
	public void incrementBaselineVotes(int baselineVotes) {
		setBaselineVotes(getBaselineVotes() + baselineVotes);
	}

	@Column(name = "added_votes", nullable = false)
	public int getAddedVotes() {
		return this.addedVotes;
	}

	public void setAddedVotes(int addedVotes) {
		this.addedVotes = addedVotes;
		updateVotes();
	}

	@Transient
	public void incrementAddedVotes(int addedVotes) {
		setAddedVotes(getAddedVotes() + addedVotes);
	}

	@Column(name = "subtracted_votes", nullable = false)
	public int getSubtractedVotes() {
		return this.subtractedVotes;
	}

	public void setSubtractedVotes(int subtractedVotes) {
		this.subtractedVotes = subtractedVotes;
		updateVotes();
	}

	@Transient
	public void incrementSubtractedVotes(int subtractedVotes) {
		setSubtractedVotes(getSubtractedVotes() + subtractedVotes);
	}

	@Column(name = "votes", nullable = false)
	public int getVotes() {
		return this.votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
		updateBaselineVotes();
	}

	private void updateVotes() {
		this.votes = this.baselineVotes + addedVotes - subtractedVotes;
	}

	@Override
	public boolean equals(Object o) {
		if (getPk() != null) {
			return super.equals(o);
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof AffiliationVoteCount)) {
			return false;
		}
		AffiliationVoteCount that = (AffiliationVoteCount) o;
		return new EqualsBuilder()
				.append(settlement, that.settlement)
				.append(affiliation, that.affiliation)
				.append(ballots, that.ballots)
				.append(modifiedBallots, that.modifiedBallots)
				.append(earlyVotingBallots, that.earlyVotingBallots)
				.append(earlyVotingModifiedBallots, that.earlyVotingModifiedBallots)
				.append(electionDayBallots, that.electionDayBallots)
				.append(electionDayModifiedBallots, that.electionDayModifiedBallots)
				.append(baselineVotes, that.baselineVotes)
				.append(addedVotes, that.addedVotes)
				.append(subtractedVotes, that.subtractedVotes)
				.append(votes, that.votes)
				.isEquals();
	}

	@Override
	public int hashCode() {
		if (getPk() != null) {
			return super.hashCode();
		}
		return new HashCodeBuilder(17, 37)
				.append(settlement)
				.append(affiliation)
				.append(ballots)
				.append(modifiedBallots)
				.append(earlyVotingBallots)
				.append(earlyVotingModifiedBallots)
				.append(electionDayBallots)
				.append(electionDayModifiedBallots)
				.append(baselineVotes)
				.append(addedVotes)
				.append(subtractedVotes)
				.append(votes)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("affiliation", affiliation)
				.append("ballots", ballots)
				.append("modifiedBallots", modifiedBallots)
				.append("earlyVotingBallots", earlyVotingBallots)
				.append("earlyVotingModifiedBallots", earlyVotingModifiedBallots)
				.append("electionDayBallots", electionDayBallots)
				.append("electionDayModifiedBallots", electionDayModifiedBallots)
				.append("baselineVotes", baselineVotes)
				.append("addedVotes", addedVotes)
				.append("subtractedVotes", subtractedVotes)
				.append("votes", votes)
				.toString();
	}

	public void accept(SettlementVisitor visitor) {
		visitor.visit(this);
	}

	@Transient
	public Contest getContest() {
		return getSettlement().getContest();
	}

	@Transient
	public Party getParty() {
		return getAffiliation().getParty();
	}
}
