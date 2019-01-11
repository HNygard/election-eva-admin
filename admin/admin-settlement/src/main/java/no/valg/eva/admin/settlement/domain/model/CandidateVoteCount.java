package no.valg.eva.admin.settlement.domain.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Settlement: Count of candidate votes
 */
@Entity
@Table(name = "candidate_vote_count", uniqueConstraints = { @UniqueConstraint(columnNames = { "settlement_pk", "candidate_pk", "vote_category_pk",
		"rank_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "candidate_vote_count_pk"))
public class CandidateVoteCount extends VersionedEntity implements java.io.Serializable {

	private Settlement settlement;
	private Candidate candidate;
	private Affiliation affiliation;
	private VoteCategory voteCategory;
	private Integer rankNumber;
	private BigDecimal votes;
	private BigDecimal earlyVotingVotes;
	private BigDecimal electionDayVotes;

	public CandidateVoteCount() {
	}

	public CandidateVoteCount(Candidate candidate, Affiliation affiliation, VoteCategory voteCategory, Integer rankNumber,
			BigDecimal votes, BigDecimal earlyVotingVotes, BigDecimal electionDayVotes) {
		this.candidate = candidate;
		this.affiliation = affiliation;
		this.voteCategory = voteCategory;
		this.rankNumber = rankNumber;
		this.votes = votes;
		this.earlyVotingVotes = earlyVotingVotes;
		this.electionDayVotes = electionDayVotes;
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
	@JoinColumn(name = "candidate_pk", nullable = false)
	public Candidate getCandidate() {
		return this.candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	@Transient
	public Affiliation getCandidateAffiliation() {
		return candidate.getAffiliation();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "affiliation_pk", nullable = false)
	public Affiliation getAffiliation() {
		return this.affiliation;
	}

	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vote_category_pk", nullable = false)
	public VoteCategory getVoteCategory() {
		return this.voteCategory;
	}

	public void setVoteCategory(VoteCategory voteCategory) {
		this.voteCategory = voteCategory;
	}

	@Column(name = "rank_number")
	public Integer getRankNumber() {
		return this.rankNumber;
	}

	public void setRankNumber(Integer rankNumber) {
		this.rankNumber = rankNumber;
	}

	@Column(name = "votes", nullable = false, precision = 10)
	public BigDecimal getVotes() {
		return this.votes;
	}

	public void setVotes(BigDecimal votes) {
		this.votes = votes;
	}

	@Transient
	public void incrementVotes(BigDecimal votes) {
		setVotes(getVotes().add(votes));
	}

	@Column(name = "early_voting_votes", nullable = false, precision = 10)
	public BigDecimal getEarlyVotingVotes() {
		return earlyVotingVotes;
	}

	public void setEarlyVotingVotes(BigDecimal earlyVotingVotes) {
		this.earlyVotingVotes = earlyVotingVotes;
	}

	@Transient
	public void incrementEarlyVotingVotes(BigDecimal earlyVotingVotes) {
		setEarlyVotingVotes(getEarlyVotingVotes().add(earlyVotingVotes));
	}

	@Column(name = "election_day_votes", nullable = false, precision = 10)
	public BigDecimal getElectionDayVotes() {
		return electionDayVotes;
	}

	public void setElectionDayVotes(BigDecimal electionDayVotes) {
		this.electionDayVotes = electionDayVotes;
	}

	public void incrementElectionDayVotes(BigDecimal electionDayVotes) {
		setElectionDayVotes(getElectionDayVotes().add(electionDayVotes));
	}

	@Override
	public boolean equals(Object o) {
		if (getPk() != null) {
			return super.equals(o);
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateVoteCount)) {
			return false;
		}
		CandidateVoteCount that = (CandidateVoteCount) o;
		return new EqualsBuilder()
				.append(settlement, that.settlement)
				.append(affiliation, that.affiliation)
				.append(candidate, that.candidate)
				.append(voteCategory, that.voteCategory)
				.append(rankNumber, that.rankNumber)
				.append(votes, that.votes)
				.append(earlyVotingVotes, that.earlyVotingVotes)
				.append(electionDayVotes, that.electionDayVotes)
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
				.append(candidate)
				.append(voteCategory)
				.append(rankNumber)
				.append(votes)
				.append(earlyVotingVotes)
				.append(electionDayVotes)
				.toHashCode();
	}

	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("candidate", candidate)
				.append("affiliation", affiliation)
				.append("voteCategory", voteCategory);
		if (rankNumber != null) {
			toStringBuilder.append("rankNumber", rankNumber);
		}
		return toStringBuilder
				.append("votes", votes)
				.append("earlyVotingVotes", earlyVotingVotes)
				.append("electionDayVotes", electionDayVotes)
				.toString();
	}
}
