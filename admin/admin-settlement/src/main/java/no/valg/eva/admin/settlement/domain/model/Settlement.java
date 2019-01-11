package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.CascadeType.ALL;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Settlement for contest
 */
@Entity
@Table(name = "settlement", uniqueConstraints = { @UniqueConstraint(columnNames = { "contest_pk", "final_settlement" }),
		@UniqueConstraint(columnNames = { "contest_pk", "settlement_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "settlement_pk"))
@NamedQueries({ @NamedQuery(name = "Settlement.findByContest", query = "SELECT s FROM Settlement s WHERE s.contest.pk = :contestPk") })

public class Settlement extends VersionedEntity implements java.io.Serializable {

	private Contest contest;
	private int number;
	private boolean finalSettlement;
	private Set<AffiliationVoteCount> affiliationVoteCounts = new LinkedHashSet<>();
	private Set<CandidateVoteCount> candidateVoteCounts = new LinkedHashSet<>();
	private Set<CandidateRank> candidateRanks = new LinkedHashSet<>();
	private Set<CandidateSeat> candidateSeats = new LinkedHashSet<>();

	public Settlement() {
	}

	public Settlement(Contest contest) {
		this.contest = contest;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return this.contest;
	}

	public void setContest(final Contest contest) {
		this.contest = contest;
	}

	@Column(name = "settlement_number", nullable = false)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}

	@Column(name = "final_settlement", nullable = false)
	public boolean isFinalSettlement() {
		return this.finalSettlement;
	}

	public void setFinalSettlement(final boolean finalSettlement) {
		this.finalSettlement = finalSettlement;
	}

	@OneToMany(mappedBy = "settlement", cascade = ALL)
	public Set<AffiliationVoteCount> getAffiliationVoteCounts() {
		return affiliationVoteCounts;
	}

	public void setAffiliationVoteCounts(Set<AffiliationVoteCount> affiliationVoteCounts) {
		this.affiliationVoteCounts = affiliationVoteCounts;
	}

	public void addAffiliationVoteCount(AffiliationVoteCount affiliationVoteCount) {
		affiliationVoteCount.setSettlement(this);
		affiliationVoteCounts.add(affiliationVoteCount);
	}

	@OneToMany(mappedBy = "settlement", cascade = ALL)
	public Set<CandidateVoteCount> getCandidateVoteCounts() {
		return candidateVoteCounts;
	}

	public void setCandidateVoteCounts(Set<CandidateVoteCount> candidateVoteCounts) {
		this.candidateVoteCounts = candidateVoteCounts;
	}

	public void addCandidateVoteCount(CandidateVoteCount candidateVoteCount) {
		candidateVoteCount.setSettlement(this);
		candidateVoteCounts.add(candidateVoteCount);
	}

	@OneToMany(mappedBy = "settlement", cascade = ALL)
	public Set<CandidateRank> getCandidateRanks() {
		return candidateRanks;
	}

	public void setCandidateRanks(Set<CandidateRank> candidateRanks) {
		this.candidateRanks = candidateRanks;
	}

	public void addCandidateRank(CandidateRank candidateRank) {
		candidateRank.setSettlement(this);
		candidateRanks.add(candidateRank);
	}

	@OneToMany(mappedBy = "settlement", cascade = ALL)
	public Set<CandidateSeat> getCandidateSeats() {
		return candidateSeats;
	}

	public void setCandidateSeats(Set<CandidateSeat> candidateSeats) {
		this.candidateSeats = candidateSeats;
	}

	public void addCandidateSeat(CandidateSeat candidateSeat) {
		candidateSeat.setSettlement(this);
		candidateSeats.add(candidateSeat);
	}

	@Override
	public boolean equals(Object o) {
		if (getPk() != null) {
			return super.equals(o);
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof Settlement)) {
			return false;
		}
		Settlement that = (Settlement) o;
		return new EqualsBuilder()
				.append(number, that.number)
				.append(contest, that.contest)
				.isEquals();
	}

	@Override
	public int hashCode() {
		if (getPk() != null) {
			return super.hashCode();
		}
		return new HashCodeBuilder(17, 37)
				.append(contest)
				.append(number)
				.toHashCode();
	}

	public void accept(SettlementVisitor settlementVisitor) {
		settlementVisitor.visit(this);
		for (AffiliationVoteCount affiliationVoteCount : affiliationVoteCounts) {
			affiliationVoteCount.accept(settlementVisitor);
		}
		for (CandidateSeat candidateSeat : candidateSeats) {
			candidateSeat.accept(settlementVisitor);
		}
	}
}
