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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Settlement: Candidate rank
 */
@Entity
@Table(name = "candidate_rank", uniqueConstraints = { @UniqueConstraint(columnNames = { "settlement_pk", "candidate_pk" }),
		@UniqueConstraint(columnNames = { "settlement_pk", "affiliation_pk", "rank_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "candidate_rank_pk"))
public class CandidateRank extends VersionedEntity implements java.io.Serializable {

	private Settlement settlement;
	private Candidate candidate;
	private Affiliation affiliation;
	private BigDecimal votes;
	private Integer rankNumber;

	public CandidateRank() {
	}

	public CandidateRank(Candidate candidate, Affiliation affiliation, BigDecimal votes, Integer rankNumber) {
		this.candidate = candidate;
		this.affiliation = affiliation;
		this.votes = votes;
		this.rankNumber = rankNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_pk", nullable = false)
	public Settlement getSettlement() {
		return this.settlement;
	}

	public void setSettlement(final Settlement settlement) {
		this.settlement = settlement;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_pk", nullable = false)
	public Candidate getCandidate() {
		return this.candidate;
	}

	public void setCandidate(final Candidate candidate) {
		this.candidate = candidate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "affiliation_pk", nullable = false)
	public Affiliation getAffiliation() {
		return this.affiliation;
	}

	public void setAffiliation(final Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	@Column(name = "votes", nullable = false, precision = 10)
	public BigDecimal getVotes() {
		return this.votes;
	}

	public void setVotes(final BigDecimal votes) {
		this.votes = votes;
	}

	@Transient
	public void incrementVotes(BigDecimal votes) {
		setVotes(getVotes().add(votes));
	}

	@Column(name = "rank_number", nullable = false)
	public Integer getRankNumber() {
		return this.rankNumber;
	}

	public void setRankNumber(final Integer rankNumber) {
		this.rankNumber = rankNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (getPk() != null) {
			return super.equals(o);
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateRank)) {
			return false;
		}
		CandidateRank that = (CandidateRank) o;
		return new EqualsBuilder()
				.append(settlement, that.settlement)
				.append(candidate, that.candidate)
				.append(affiliation, that.affiliation)
				.append(votes, that.votes)
				.append(rankNumber, that.rankNumber)
				.isEquals();
	}

	@Override
	public int hashCode() {
		if (getPk() != null) {
			return super.hashCode();
		}
		return new HashCodeBuilder(17, 37)
				.append(settlement)
				.append(candidate)
				.append(affiliation)
				.append(votes)
				.append(rankNumber)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("candidate", candidate)
				.append("affiliation", affiliation)
				.append("votes", votes)
				.append("rankNumber", rankNumber)
				.toString();
	}
}
