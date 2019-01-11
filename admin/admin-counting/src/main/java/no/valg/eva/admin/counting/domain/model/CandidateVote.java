package no.valg.eva.admin.counting.domain.model;

import static java.lang.String.format;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.baseline;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.personal;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.renumber;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.strikeout;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.writein;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

/**
 * Candidate votes on individual ballot
 */
@Entity
@EntityListeners({ EntityWriteListener.class })
@Table(name = "candidate_vote", uniqueConstraints = @UniqueConstraint(columnNames = { "cast_vote_pk", "candidate_pk", "vote_category_pk" }) )
@AttributeOverride(name = "pk", column = @Column(name = "candidate_vote_pk") )
@NamedQueries({
		@NamedQuery(
				name = "CandidateVote.getByCastBallot",
				query = "SELECT cv FROM CandidateVote cv WHERE cv.castBallot.pk = :cvpk") })
public class CandidateVote extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private Candidate candidate;
	private VoteCategory voteCategory;
	private CastBallot castBallot;
	private Integer renumberPosition;

	public CandidateVote() {
	}

	public CandidateVote(Candidate candidate, VoteCategory voteCategory, CastBallot castBallot, Integer renumberPosition) {
		this.candidate = candidate;
		this.voteCategory = voteCategory;
		this.castBallot = castBallot;
		this.renumberPosition = renumberPosition;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_pk", nullable = false)
	public Candidate getCandidate() {
		return this.candidate;
	}

	public void setCandidate(final Candidate candidate) {
		this.candidate = candidate;
	}

	@Transient
	public Affiliation getCandidateAffiliation() {
		return getCandidate().getAffiliation();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vote_category_pk", nullable = false)
	public VoteCategory getVoteCategory() {
		return this.voteCategory;
	}

	public void setVoteCategory(final VoteCategory voteCategory) {
		this.voteCategory = voteCategory;
	}

	@Transient
	public String getVoteCategoryId() {
		return getVoteCategory().getId();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cast_vote_pk", nullable = false)
	public CastBallot getCastBallot() {
		return this.castBallot;
	}

	public void setCastBallot(final CastBallot castBallot) {
		this.castBallot = castBallot;
	}

	@Transient
	public Affiliation getBallotAffiliation() {
		return getCastBallot().getBallotCount().getBallot().getAffiliation();
	}

	@Transient
	public boolean isEarlyVoting() {
		return getCastBallot().isEarlyVoting();
	}

	@Column(name = "renumber_position")
	public Integer getRenumberPosition() {
		return this.renumberPosition;
	}

	public void setRenumberPosition(final Integer renumberPosition) {
		this.renumberPosition = renumberPosition;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return candidate.getBallot().getContest().getPk();
		}
		return null;
	}

	@Override
	public String toString() {
		return format("<Candidate> vote_category: %s, renumbering: %d", this.getVoteCategory().getId(), this.getRenumberPosition());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CandidateVote)) {
			return false;
		}

		CandidateVote that = (CandidateVote) o;

		if (this.getPk() != null && that.getPk() != null && this.getPk().equals(that.getPk())) {
			return true;
		}

		if (candidate != null ? !candidate.equals(that.candidate) : that.candidate != null) {
			return false;
		}
		if (castBallot != null ? !castBallot.equals(that.castBallot) : that.castBallot != null) {
			return false;
		}
		if (renumberPosition != null ? !renumberPosition.equals(that.renumberPosition) : that.renumberPosition != null) {
			return false;
		}
		return !(voteCategory != null ? !voteCategory.equals(that.voteCategory) : that.voteCategory != null);

	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (candidate != null ? candidate.hashCode() : 0);
		result = 31 * result + (voteCategory != null ? voteCategory.hashCode() : 0);
		result = 31 * result + (castBallot != null ? castBallot.hashCode() : 0);
		result = 31 * result + (renumberPosition != null ? renumberPosition.hashCode() : 0);
		return result;
	}

	@Transient
	public boolean isPersonalVote() {
		return VoteCategoryValues.valueOf(getVoteCategoryId()) == personal;
	}

	@Transient
	public boolean isWriteIn() {
		return VoteCategoryValues.valueOf(getVoteCategoryId()) == writein;
	}

	@Transient
	public boolean isRenumbering() {
		return VoteCategoryValues.valueOf(getVoteCategoryId()) == renumber;
	}

	@Transient
	public boolean isStrikeOut() {
		return VoteCategoryValues.valueOf(getVoteCategoryId()) == strikeout;
	}

	@Transient
	public boolean isBaseline() {
		return VoteCategoryValues.valueOf(getVoteCategoryId()) == baseline;
	}

	public void accept(CountingVisitor countingVisitor) {
		if (countingVisitor.include(this)) {
			countingVisitor.visit(this);
		}
	}
}
