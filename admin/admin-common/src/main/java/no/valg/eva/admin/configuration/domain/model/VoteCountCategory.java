package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Categories for separation of voting counts, e.g. ordinary early votes and ordinary election day votes
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.configuration.domain.model.VoteCountCategory")
@Table(name = "vote_count_category", uniqueConstraints = @UniqueConstraint(columnNames = "vote_count_category_id"))
@AttributeOverride(name = "pk", column = @Column(name = "vote_count_category_pk"))
@NamedQueries({
		@NamedQuery(name = "VoteCountCategory.findByMunicipality", query = "SELECT rcc.voteCountCategory FROM ReportCountCategory rcc "
				+ "WHERE rcc.municipality.pk = :municipalityPk AND rcc.electionGroup.pk = :electionGroupPk ORDER BY rcc.voteCountCategory.id")})
@NamedNativeQueries({
		@NamedNativeQuery(name = "VoteCountCategory.findByContest", query = "SELECT DISTINCT vote_count_category.* FROM admin.contest "
				+ "JOIN admin.contest_area ON contest.contest_pk = contest_area.contest_pk "
				+ "JOIN admin.mv_area ON contest_area.mv_area_pk = mv_area.mv_area_pk "
				+ "JOIN admin.mv_area ac ON text2ltree(ac.area_path) <@ text2ltree(mv_area.area_path) AND ac.area_level = 5 "
				+ "JOIN admin.election ON contest.election_pk = election.election_pk "
				+ "JOIN admin.election_group ON election.election_group_pk = election_group.election_group_pk "
				+ "JOIN admin.report_count_category ON report_count_category.election_group_pk = election_group.election_group_pk "
				+ "AND report_count_category.municipality_pk = ac.municipality_pk "
				+ "JOIN admin.vote_count_category ON report_count_category.vote_count_category_pk = vote_count_category.vote_count_category_pk "
				+ "WHERE contest.contest_pk = ?1 ORDER BY vote_count_category_id ASC", resultClass = VoteCountCategory.class),
		@NamedNativeQuery(name = "VoteCountCategory.findByElectionAndAreaPath", query = "SELECT DISTINCT vote_count_category.* FROM admin.contest "
				+ "JOIN admin.contest_area ON contest.contest_pk = contest_area.contest_pk "
				+ "JOIN admin.mv_area ON contest_area.mv_area_pk = mv_area.mv_area_pk "
				+ "JOIN admin.mv_area ac ON text2ltree(ac.area_path) <@ text2ltree(mv_area.area_path) AND ac.area_level = 5 "
				+ "JOIN admin.election ON contest.election_pk = election.election_pk "
				+ "JOIN admin.election_group ON election.election_group_pk = election_group.election_group_pk "
				+ "JOIN admin.report_count_category ON report_count_category.election_group_pk = election_group.election_group_pk "
				+ "AND report_count_category.municipality_pk = ac.municipality_pk "
				+ "JOIN admin.vote_count_category ON report_count_category.vote_count_category_pk = vote_count_category.vote_count_category_pk "
				+ "WHERE election.election_pk = ?1 AND text2ltree(ac.area_path) <@ text2ltree(?2) "
				+ "ORDER BY vote_count_category_id ASC", resultClass = VoteCountCategory.class),
		@NamedNativeQuery(name = "VoteCountCategory.findByElectionEventAndAreaPath", query = "SELECT DISTINCT vote_count_category.* FROM admin.contest "
				+ "JOIN admin.contest_area ON contest.contest_pk = contest_area.contest_pk "
				+ "JOIN admin.mv_area ON contest_area.mv_area_pk = mv_area.mv_area_pk "
				+ "JOIN admin.mv_area ac ON text2ltree(ac.area_path) <@ text2ltree(mv_area.area_path) AND ac.area_level = 5 "
				+ "JOIN admin.election ON contest.election_pk = election.election_pk "
				+ "JOIN admin.election_group ON election.election_group_pk = election_group.election_group_pk "
				+ "JOIN admin.report_count_category ON report_count_category.election_group_pk = election_group.election_group_pk "
				+ "AND report_count_category.municipality_pk = ac.municipality_pk "
				+ "JOIN admin.vote_count_category ON report_count_category.vote_count_category_pk = vote_count_category.vote_count_category_pk "
				+ "WHERE election_group.election_event_pk = ?1 AND text2ltree(ac.area_path) <@ text2ltree(?2) "
				+ "ORDER BY vote_count_category_id ASC", resultClass = VoteCountCategory.class) })
public class VoteCountCategory extends VersionedEntity implements java.io.Serializable {

	private String id;
	private boolean earlyVoting;
	private boolean mandatoryCount;
	private boolean mandatoryCentralCount;
	private boolean mandatoryTotalCount;
	private String name;

	@Column(name = "vote_count_category_id", nullable = false, length = 4)
	@StringNotNullEmptyOrBlanks
	@Size(max = 4)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "early_voting", nullable = false)
	public boolean isEarlyVoting() {
		return this.earlyVoting;
	}

	public void setEarlyVoting(final boolean earlyVoting) {
		this.earlyVoting = earlyVoting;
	}

	@Column(name = "mandatory_count", nullable = false)
	public boolean isMandatoryCount() {
		return this.mandatoryCount;
	}

	public void setMandatoryCount(final boolean mandatoryCount) {
		this.mandatoryCount = mandatoryCount;
	}

	@Column(name = "mandatory_central_count", nullable = false)
	public boolean isMandatoryCentralCount() {
		return this.mandatoryCentralCount;
	}

	public void setMandatoryCentralCount(final boolean mandatoryCentralCount) {
		this.mandatoryCentralCount = mandatoryCentralCount;
	}

	@Column(name = "mandatory_total_count", nullable = false)
	public boolean isMandatoryTotalCount() {
		return this.mandatoryTotalCount;
	}

	public void setMandatoryTotalCount(final boolean mandatoryTotalCount) {
		this.mandatoryTotalCount = mandatoryTotalCount;
	}

	@Column(name = "vote_count_category_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return true if "Forhåndsstemme ordinær", else false
	 */
	@Transient
	public boolean isCategoryForOrdinaryAdvanceVotes() {
		return CountCategory.FO.getId().equals(getId());
	}

	@Transient
	public CountCategory getCountCategory() {
		return CountCategory.fromId(id);
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
		VoteCountCategory rhs = (VoteCountCategory) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.id, rhs.id)
				.append(this.earlyVoting, rhs.earlyVoting)
				.append(this.mandatoryCount, rhs.mandatoryCount)
				.append(this.mandatoryCentralCount, rhs.mandatoryCentralCount)
				.append(this.mandatoryTotalCount, rhs.mandatoryTotalCount)
				.append(this.name, rhs.name)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(id)
				.append(earlyVoting)
				.append(mandatoryCount)
				.append(mandatoryCentralCount)
				.append(mandatoryTotalCount)
				.append(name)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("earlyVoting", earlyVoting)
				.append("mandatoryCount", mandatoryCount)
				.append("mandatoryCentralCount", mandatoryCentralCount)
				.append("mandatoryTotalCount", mandatoryTotalCount)
				.append("name", name)
				.appendSuper(super.toString())
				.toString();
	}
}
