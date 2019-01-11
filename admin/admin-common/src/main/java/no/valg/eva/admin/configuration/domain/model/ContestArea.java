package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurableDynamicArea;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

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

/**
 * One or more areas contained in the contest (Norwegian "valgkrets")
 */
@Entity
@Table(name = "contest_area", uniqueConstraints = @UniqueConstraint(columnNames = { "contest_pk", "mv_area_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "contest_area_pk"))
@NamedQueries({
		@NamedQuery(name = "ContestArea.findContestAreasForContest", query = "SELECT ca FROM ContestArea ca LEFT JOIN ca.mvArea AS ma "
				+ "WHERE ca.contest.pk = :contestPk ORDER BY ma.areaPath"),
		@NamedQuery(
				name = "ContestArea.findAreaPathsForContests",
				query = "SELECT ca.contest.pk, ca.mvArea.areaPath FROM ContestArea ca WHERE ca.contest.pk IN (:contestPks)"),
		@NamedQuery(name = "ContestArea.findByElection", query = "SELECT ca FROM ContestArea ca WHERE ca.contest.election.pk = :electionPk"),
		@NamedQuery(
				name = "ContestArea.finnForValghendelseMedValgdistrikt",
				query = "SELECT ca FROM ContestArea ca"
					 + " LEFT JOIN FETCH ca.contest"
					 + " WHERE ca.mvArea.electionEvent.pk = :electionEventPk")})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestArea extends VersionedEntity implements java.io.Serializable, ContextSecurableDynamicArea {

	@Setter private MvArea mvArea;
	@Setter private Contest contest;
	@Setter private boolean parentArea;
	@Setter private boolean childArea;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mv_area_pk", nullable = false)
	public MvArea getMvArea() {
		return this.mvArea;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return this.contest;
	}

	@Column(name = "parent_area", nullable = false)
	public boolean isParentArea() {
		return this.parentArea;
	}

	@Column(name = "child_area", nullable = false)
	public boolean isChildArea() {
		return this.childArea;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return mvArea.getPkForLevel(getActualAreaLevel().getLevel());
	}

	@Override
	@Transient
	public AreaLevelEnum getActualAreaLevel() {
		return AreaLevelEnum.getLevel(mvArea.getAreaLevel());
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return contest.getPk();
		}
		return null;
	}

	@Transient
	public ElectionPath getContestPath() {
		return getContest().electionPath();
	}

	@Transient
	public AreaPath getAreaPath() {
		return AreaPath.from(getMvArea().getAreaPath());
	}

	@Transient
	public String getElectionName() {
		return contest.getElectionName();
	}

	@Transient
	public String getContestName() {
		return contest.getName();
	}

	/*
	 * MunicipalityForSamiDistrict is a parent area.  It could also be possible to check if countyId of mvArea is "00"
	 */
	@Transient
	public boolean isMunicipalityForSamiDistrict() {
		return isParentArea();
	}
}
