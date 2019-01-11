package no.valg.eva.admin.counting.domain.model;

import static javax.persistence.CascadeType.ALL;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Count of ballots
 */
@Entity
@EntityListeners({EntityWriteListener.class})
@Table(name = "ballot_count", uniqueConstraints = { @UniqueConstraint(columnNames = { "vote_count_pk", "ballot_pk", "ballot_rejection_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "ballot_count_pk"))
@NamedQueries({
		@NamedQuery(
				name = "BallotCount.findModifiedBallotCount",
				query = "SELECT COUNT(cv) FROM CastBallot cv WHERE cv.ballotCount.pk = :bcPk"),
		@NamedQuery(
				name = "BallotCount.getByVoteCount",
				query = "SELECT bc FROM BallotCount bc WHERE bc.voteCount.pk = :vcpk") })
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "BallotCount.findBallotCountByVCBallot",
				query = "select bc.* from admin.ballot as b "
						+ "left outer join admin.ballot_count as bc on bc.ballot_pk = b.ballot_pk and bc.vote_count_pk = ?1 "
						+ "where b.contest_pk = ?2 AND b.approved = ?3 order by b.display_order ASC", resultClass = BallotCount.class),
		@NamedNativeQuery(
				name = "BallotCount.findBallotCountByVCBallotRejection",
				query = "select bc.* from admin.ballot_rejection br "
						+ "left outer join admin.ballot_count as bc on bc.ballot_rejection_pk = br.ballot_rejection_pk and bc.vote_count_pk = ?1 "
						+ "where br.early_voting = ?2 order by br.ballot_rejection_id ASC;", resultClass = BallotCount.class) })
public class BallotCount extends VersionedEntity implements java.io.Serializable, ContextSecurable, Comparable<BallotCount> {
	private VoteCount voteCount;
	private Ballot ballot;
	private BallotRejection ballotRejection;
	private int unmodifiedBallots;
	private int modifiedBallots;
	private Set<CastBallot> castBallots = new LinkedHashSet<>();
	private Set<ModifiedBallotBatch> modifiedBallotBatches = new LinkedHashSet<>();

	public BallotCount() {
	}

	public BallotCount(VoteCount voteCount, Ballot ballot, BallotRejection ballotRejection, int unmodifiedCount, int modifiedCount) {
		this.voteCount = voteCount;
		this.ballot = ballot;
		this.ballotRejection = ballotRejection;
		this.unmodifiedBallots = unmodifiedCount;
		this.modifiedBallots = modifiedCount;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vote_count_pk", nullable = false)
	public VoteCount getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(VoteCount voteCount) {
		this.voteCount = voteCount;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ballot_pk", nullable = true)
	public Ballot getBallot() {
		return ballot;
	}

	public void setBallot(Ballot ballot) {
		this.ballot = ballot;
	}

	@Transient
	public String getBallotId() {
		return ballot != null ? ballot.getId() : null;
	}

	@Transient
	public Affiliation getBallotAffiliation() {
		Ballot theBallot = getBallot();
		if (theBallot != null) {
			return theBallot.getAffiliation();
		}
		return null;
	}

	@Transient
	public Set<Candidate> getBallotCandidates() {
		Ballot theBallot = getBallot();
		if (theBallot != null) {
			return theBallot.getAffiliationCandidates();
		}
		return null;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ballot_rejection_pk")
	public BallotRejection getBallotRejection() {
		return ballotRejection;
	}

	public void setBallotRejection(BallotRejection ballotRejection) {
		this.ballotRejection = ballotRejection;
	}

	@Transient
	public String getBallotRejectionId() {
		return ballotRejection != null ? ballotRejection.getId() : null;
	}

	@NotNull
	@Min(0)
	@Column(name = "unmodified_ballots", nullable = false)
	public int getUnmodifiedBallots() {
		return unmodifiedBallots;
	}

	public void setUnmodifiedBallots(int unmodifiedBallots) {
		this.unmodifiedBallots = unmodifiedBallots;
	}

	@NotNull
	@Min(0)
	@Column(name = "modified_ballots", nullable = false)
	public int getModifiedBallots() {
		return modifiedBallots;
	}

	public void setModifiedBallots(int modifiedBallots) {
		this.modifiedBallots = modifiedBallots;
	}

	@Transient
	public int getBallots() {
		return unmodifiedBallots + modifiedBallots;
	}

	@Override
	public Long getAreaPk(AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.POLLING_DISTRICT)) {
			return voteCount.getPollingDistrict().getPk();
		} else {
			return null;
		}
	}

	@Override
	public Long getElectionPk(ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return voteCount.getContestReport().getContest().getPk();
		} else {
			return null;
		}
	}

	@OneToMany(mappedBy = "ballotCount", cascade = ALL)
	public Set<CastBallot> getCastBallots() {
		return castBallots;
	}

	public void setCastBallots(Set<CastBallot> castBallotSet) {
		this.castBallots = castBallotSet;
	}

	@OneToMany(mappedBy = "ballotCount", cascade = ALL)
	public Set<ModifiedBallotBatch> getModifiedBallotBatches() {
		return modifiedBallotBatches;
	}

	public void setModifiedBallotBatches(Set<ModifiedBallotBatch> modifiedBallotBatches) {
		this.modifiedBallotBatches = modifiedBallotBatches;
	}

	@Transient
	public CastBallot getCastBallot(CastBallotId castBallotId) {
		for (CastBallot castBallot : getCastBallots()) {
			if (castBallot.getId().equals(castBallotId.id())) {
				return castBallot;
			}
		}
		return null;
	}

	public void addCastBallot(CastBallot castBallot) {
		castBallot.setBallotCount(this);
		getCastBallots().add(castBallot);
	}

	public void addCastBallotAndSetCastBallotId(CastBallot castBallot) {
		addCastBallot(castBallot);
		castBallot.setId(String.format("%010d", getCastBallots().size()));
	}

	@Transient
	public Set<CastBallot> getModifiedCastBallots() {
		Set<CastBallot> modifiedCastBallots = new LinkedHashSet<>();
		for (CastBallot castBallot : castBallots) {
			if (castBallot.getType() == MODIFIED) {
				modifiedCastBallots.add(castBallot);
			}
		}
		return modifiedCastBallots;
	}

	@Override
	public int compareTo(BallotCount other) {
		if (this.getBallot() != null) {
			return getBallot().compareTo(other.getBallot());
		}
		return getBallotRejection().compareTo(other.getBallotRejection());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BallotCount rhs = (BallotCount) obj;
		return new EqualsBuilder()
				.append(this.voteCount, rhs.voteCount)
				.append(this.ballot, rhs.ballot)
				.append(this.ballotRejection, rhs.ballotRejection)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(voteCount)
				.append(ballot)
				.append(ballotRejection)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("voteCount", voteCount)
				.append("ballot", ballot)
				.append("ballotRejection", ballotRejection)
				.append("unmodifiedBallots", unmodifiedBallots)
				.append("modifiedBallots", modifiedBallots)
				.toString();
	}

	public String partyName() {
		Ballot theBallot = getBallot();
		return theBallot != null ? theBallot.partyName() : null;
	}

	public void accept(CountingVisitor countingVisitor) {
		if (countingVisitor.include(this)) {
			countingVisitor.visit(this);
			for (CastBallot castBallot : getCastBallots()) {
				castBallot.accept(countingVisitor);
			}
		}
	}

	public boolean hasBallot() {
		return getBallot() != null;
	}

	@Transient
	public boolean isEarlyVoting() {
		return voteCount.isEarlyVoting();
	}

	@Transient
	public boolean isForhånd() {
		return voteCount.isEarlyVoting();
	}

	@Transient
	public boolean isValgting() {
		return !voteCount.isEarlyVoting();
	}

	@Transient
	public boolean isForeløpigForhånd() {
		return isForeløpig() && isForhånd();
	}

	@Transient
	public boolean isForeløpigValgting() {
		return isForeløpig() && isValgting();
	}

	@Transient
	public boolean isEndeligForhånd() {
		return isEndelig() && isForhånd();
	}

	@Transient
	public boolean isEndeligValgting() {
		return isEndelig() && isValgting();
	}

	@Transient
	public boolean isForeløpig() {
		return voteCount.isPreliminaryCount();
	}

	@Transient
	public boolean isEndelig() {
		return voteCount.isFinalCount();
	}
	
	@Transient
	public boolean isBlank() {
		return EvoteConstants.BALLOT_BLANK.equals(getBallotId());
	}
	
	public boolean harKategori(no.valg.eva.admin.common.counting.model.CountCategory countCategory) {
		return countCategory.getId().equals(getVoteCount().getVoteCountCategoryId());
	}
}
