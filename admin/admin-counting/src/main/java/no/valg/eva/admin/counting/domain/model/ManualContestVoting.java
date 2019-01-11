package no.valg.eva.admin.counting.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Entity for the manual_contest_voting table. When polling places use manual markoffs, this entity is used to store the manual counting of markoffs per
 * election day and voting category.
 */
@Entity
@Table(name = "manual_contest_voting", uniqueConstraints = @UniqueConstraint(columnNames = {"contest_pk", "mv_area_pk", "voting_category_pk",
		"election_day_pk"}))
@AttributeOverride(name = "pk", column = @Column(name = "manual_contest_voting_pk"))
@NamedQueries({
		@NamedQuery(
				name = "ManualContestVoting.findByElectionAreaCategoryDay",
				query = "SELECT mcv from ManualContestVoting mcv WHERE mcv.contest.pk = :contestPk "
						+ "AND mcv.mvArea.pk = :mvAreaPk AND mcv.votingCategory.pk = :votingCategoryPk AND mcv.electionDay.pk = :electionDayPk"),
		@NamedQuery(
				name = "ManualContestVoting.findByContestAreaCategory",
				query = "SELECT mcv from ManualContestVoting mcv LEFT JOIN FETCH mcv.electionDay ed WHERE mcv.contest.pk = :contestPk "
						+ "AND mcv.mvArea.pk = :mvAreaPk AND mcv.votingCategory.pk = :votingCategoryPk")
})
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "ManualContestVoting.findVOStemmegivninger",
				query = "SELECT sum(votings) FROM manual_contest_voting vt JOIN mv_area a ON vt.mv_area_pk = a.mv_area_pk "
						+ "WHERE text2ltree(area_path) <@ text2ltree(:areaPath) and contest_pk = :contest_pk")

})

public class ManualContestVoting extends VersionedEntity implements java.io.Serializable {

	private Contest contest;
	private MvArea mvArea;
	private VotingCategory votingCategory;
	private ElectionDay electionDay;
	private int votings;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return contest;
	}

	public void setContest(final Contest contest) {
		this.contest = contest;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mv_area_pk", nullable = false)
	public MvArea getMvArea() {
		return mvArea;
	}

	public void setMvArea(final MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voting_category_pk", nullable = false)
	public VotingCategory getVotingCategory() {
		return votingCategory;
	}

	public void setVotingCategory(final VotingCategory votingCategory) {
		this.votingCategory = votingCategory;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "election_day_pk", nullable = false)
	public ElectionDay getElectionDay() {
		return electionDay;
	}

	public void setElectionDay(final ElectionDay electionDay) {
		this.electionDay = electionDay;
	}

	@NotNull
	@Min(0)
	@Column(name = "votings")
	public int getVotings() {
		return votings;
	}

	public void setVotings(int votings) {
		this.votings = votings;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ManualContestVoting rhs = (ManualContestVoting) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.contest, rhs.contest)
				.append(this.mvArea, rhs.mvArea)
				.append(this.votingCategory, rhs.votingCategory)
				.append(this.electionDay, rhs.electionDay)
				.append(this.votings, rhs.votings)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(contest)
				.append(mvArea)
				.append(votingCategory)
				.append(electionDay)
				.append(votings)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("contest", contest)
				.append("mvArea", mvArea)
				.append("votingCategory", votingCategory)
				.append("electionDay", electionDay)
				.append("votings", votings)
				.toString();
	}
}
