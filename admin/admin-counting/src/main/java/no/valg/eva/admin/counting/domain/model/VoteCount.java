package no.valg.eva.admin.counting.domain.model;

import static javax.persistence.FetchType.LAZY;
import static no.evote.constants.VoteCountStatusEnum.APPROVED;
import static no.evote.constants.VoteCountStatusEnum.COUNTING;
import static no.evote.constants.VoteCountStatusEnum.REJECTED;
import static no.evote.constants.VoteCountStatusEnum.TO_APPROVAL;
import static no.evote.constants.VoteCountStatusEnum.TO_SETTLEMENT;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.exception.ValidateException;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.security.ContextSecurableDynamicArea;
import no.valg.eva.admin.common.counting.model.BallotId;
import no.valg.eva.admin.common.counting.model.BallotRejectionId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Count of votes per polling district per vote count category, with optional underlying count of ballots. The polling district is within the authority of the
 * reporting unit
 */
@Entity
@EntityListeners({EntityWriteListener.class})
@Table(name = "vote_count", uniqueConstraints = @UniqueConstraint(columnNames = { "contest_report_pk", "polling_district_pk", "vote_count_category_pk",
		"count_qualifier_pk", "vote_count_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "vote_count_pk"))
@NamedQueries({
		@NamedQuery(name = "VoteCount.findByCoReMvArCoQuVoCoCa", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.pk = :crpk AND "
				+ "vc.pollingDistrict.pk = :pdpk AND vc.countQualifier.pk = :cqpk AND vc.voteCountCategory.pk = :vccpk ORDER BY vc.pk"),
		@NamedQuery(name = "VoteCount.findByCoPdCqVcc", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.contest.pk = :cpk "
				+ "AND vc.pollingDistrict.pk = :pdpk AND vc.countQualifier.pk = :cqpk AND vc.voteCountCategory.pk = :vccpk ORDER BY vc.pk"),
		@NamedQuery(name = "VoteCount.findByContestPdCqVcc", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.contest.pk = :cpk AND "
				+ "vc.pollingDistrict.pk = :pdpk AND vc.countQualifier.pk = :cqpk AND vc.voteCountCategory.pk = :vccpk ORDER BY vc.pk"),
		@NamedQuery(
				name = "VoteCount.findByContestCountQualifierAreaLevel",
				query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.contest.pk = :cpk AND "
						+ "vc.countQualifier.pk = :cqpk AND vc.contestReport.reportingUnit.mvArea.areaLevel = :areaLevel ORDER BY vc.pk"),
		@NamedQuery(name = "VoteCount.findByContestCountQualifierMvArea", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.contest.pk = :cpk AND "
				+ "vc.countQualifier.pk = :cqpk AND vc.contestReport.reportingUnit.mvArea.pk = :mvAreaPk ORDER BY vc.pk"),
		@NamedQuery(name = "VoteCount.findByCoRePdVoCoQuCoCaApproved", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.pk = :crpk AND "
				+ "vc.pollingDistrict.pk = :pdpk AND vc.countQualifier.pk = :cqpk AND vc.voteCountCategory.pk = :vccpk AND " + "vc.voteCountStatus.id = 2"),
		@NamedQuery(name = "VoteCount.findUnique", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.pk = :crpk AND "
				+ "vc.pollingDistrict.pk = :pdpk AND vc.countQualifier.pk = :cqpk AND vc.voteCountCategory.pk = :vccpk AND vc.id = :id"),
		@NamedQuery(name = "VoteCount.findbyContestReport", query = "SELECT vc FROM VoteCount vc WHERE vc.contestReport.pk = :crpk")} )
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "VoteCount.findDigestsByContestAndMunicipalityReportingArea",
				query = "SELECT vcc.vote_count_category_id, mva_vc.area_path, cq.count_qualifier_id, mva_ru.area_level, vc.vote_count_id, " +
						"	vcs.vote_count_status_id = 2 as approved, vcs.vote_count_status_id = 3 as to_settlement, vc.manual_count, " +
						"	vc.rejected_ballots_processed, vc.rejected_ballots " +
						"FROM vote_count vc " +
						"JOIN vote_count_category vcc USING (vote_count_category_pk) " +
						"JOIN mv_area mva_vc ON mva_vc.mv_area_pk = vc.mv_area_pk " +
						"JOIN count_qualifier cq USING (count_qualifier_pk) " +
						"JOIN contest_report cr USING (contest_report_pk) " +
						"JOIN reporting_unit ru USING (reporting_unit_pk) " +
						"JOIN mv_area mva_ru ON mva_ru.mv_area_pk = ru.mv_area_pk " +
						"JOIN vote_count_status vcs USING (vote_count_status_pk) " +
						"WHERE cr.contest_pk = ?0 AND mva_ru.municipality_pk = ?1",
				resultSetMapping = "VoteCountDigest" ),
		@NamedNativeQuery(
				name = "VoteCount.findDigestsByContestAndCountyReportingAreaAndMunicipalityCountingArea",
				query = "SELECT vcc.vote_count_category_id, mva_vc.area_path, cq.count_qualifier_id, mva_ru.area_level, vc.vote_count_id, " +
						"	vcs.vote_count_status_id = 2 as approved, vcs.vote_count_status_id = 3 as to_settlement, vc.manual_count, " +
						"	vc.rejected_ballots_processed, vc.rejected_ballots " +
						"FROM vote_count vc " +
						"JOIN vote_count_category vcc USING (vote_count_category_pk) " +
						"JOIN mv_area mva_vc ON mva_vc.mv_area_pk = vc.mv_area_pk " +
						"JOIN count_qualifier cq USING (count_qualifier_pk) " +
						"JOIN contest_report cr USING (contest_report_pk) " +
						"JOIN reporting_unit ru USING (reporting_unit_pk) " +
						"JOIN mv_area mva_ru ON mva_ru.mv_area_pk = ru.mv_area_pk " +
						"JOIN vote_count_status vcs USING (vote_count_status_pk) " +
						"WHERE cr.contest_pk = ?0 AND mva_ru.county_pk = ?1 and mva_vc.municipality_pk = ?2",
				resultSetMapping = "VoteCountDigest" ),
		@NamedNativeQuery(
				name = "VoteCount.findDigestsByContestAndMunicipalityCountingArea",
				query = "SELECT vcc.vote_count_category_id, mva_vc.area_path, cq.count_qualifier_id, mva_ru.area_level, vc.vote_count_id, " +
						"	vcs.vote_count_status_id = 2 as approved, vcs.vote_count_status_id = 3 as to_settlement, vc.manual_count, " +
						"	vc.rejected_ballots_processed, vc.rejected_ballots " +
						"FROM vote_count vc " +
						"JOIN vote_count_category vcc USING (vote_count_category_pk) " +
						"JOIN mv_area mva_vc ON mva_vc.mv_area_pk = vc.mv_area_pk " +
						"JOIN count_qualifier cq USING (count_qualifier_pk) " +
						"JOIN contest_report cr USING (contest_report_pk) " +
						"JOIN reporting_unit ru USING (reporting_unit_pk) " +
						"JOIN mv_area mva_ru ON mva_ru.mv_area_pk = ru.mv_area_pk " +
						"JOIN vote_count_status vcs USING (vote_count_status_pk) " +
						"WHERE cr.contest_pk = ?0 AND mva_vc.municipality_pk = ?1",
				resultSetMapping = "VoteCountDigest" )} )
@SqlResultSetMappings({
		@SqlResultSetMapping(
				name = "VoteCountDigest",
				classes = {
						@ConstructorResult(
								targetClass = VoteCountDigest.class,
								columns = {
										@ColumnResult(name = "vote_count_category_id"),
										@ColumnResult(name = "area_path"),
										@ColumnResult(name = "count_qualifier_id"),
										@ColumnResult(name = "area_level"),
										@ColumnResult(name = "vote_count_id"),
										@ColumnResult(name = "approved"),
										@ColumnResult(name = "to_settlement"),
										@ColumnResult(name = "manual_count"),
										@ColumnResult(name = "rejected_ballots_processed"),
										@ColumnResult(name = "rejected_ballots")
								}
						)
				}
		)})
public class VoteCount extends VersionedEntity implements java.io.Serializable, ContextSecurable, ContextSecurableDynamicArea {
	
	private VoteCountStatus voteCountStatus;
	private VoteCountCategory voteCountCategory;
	@Deprecated
	private PollingDistrict pollingDistrict;
	private MvArea mvArea;
	private ContestReport contestReport;
	private CountQualifier countQualifier;
	private String id;
	private Integer approvedBallots;
	private Integer rejectedBallots;
	private Integer technicalVotings;
	private boolean manualCount;
	private boolean modifiedBallotsProcessed;
	private boolean rejectedBallotsProcessed;
	private String infoText;
	private Integer foreignSpecialCovers;
	private Integer specialCovers;
	private Integer emergencySpecialCovers;
	private Set<BallotCount> ballotCountSet = new LinkedHashSet<>();
	private Integer ballotsForOtherContests;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "vote_count_status_pk", nullable = false)
	public VoteCountStatus getVoteCountStatus() {
		return voteCountStatus;
	}

	public void setVoteCountStatus(final VoteCountStatus voteCountStatus) {
		this.voteCountStatus = voteCountStatus;
	}

	@Transient
	public int getVoteCountStatusId() {
		return voteCountStatus.getId();
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "vote_count_category_pk", nullable = false)
	public VoteCountCategory getVoteCountCategory() {
		return voteCountCategory;
	}

	public void setVoteCountCategory(final VoteCountCategory voteCountCategory) {
		this.voteCountCategory = voteCountCategory;
	}

	@Transient
	public String getVoteCountCategoryId() {
		return voteCountCategory.getId();
	}

	@Transient
	public boolean isEarlyVoting() {
		return getVoteCountCategory().isEarlyVoting();
	}

	@Deprecated
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "polling_district_pk", nullable = true)
	public PollingDistrict getPollingDistrict() {
		return pollingDistrict;
	}

	@Deprecated
	public void setPollingDistrict(final PollingDistrict pollingDistrict) {
		this.pollingDistrict = pollingDistrict;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "mv_area_pk", nullable = false)
	public MvArea getMvArea() {
		return mvArea;
	}

	public void setMvArea(final MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "contest_report_pk", nullable = false)
	public ContestReport getContestReport() {
		return contestReport;
	}

	public void setContestReport(final ContestReport contestReport) {
		this.contestReport = contestReport;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "count_qualifier_pk", nullable = false)
	public CountQualifier getCountQualifier() {
		return countQualifier;
	}

	public void setCountQualifier(final CountQualifier countQualifier) {
		this.countQualifier = countQualifier;
	}

	@Transient
	public String getCountQualifierId() {
		return getCountQualifier().getId();
	}

	@Column(name = "vote_count_id", nullable = false, length = 10)
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@NotNull
	@Min(0)
	@Column(name = "approved_ballots", nullable = false)
	public Integer getApprovedBallots() {
		return approvedBallots;
	}

	public void setApprovedBallots(final Integer approvedBallots) {
		this.approvedBallots = approvedBallots;
	}

	@Min(0)
	@Column(name = "rejected_ballots")
	public Integer getRejectedBallots() {
		return rejectedBallots;
	}

	public void setRejectedBallots(Integer rejectedBallots) {
		this.rejectedBallots = rejectedBallots;
	}

	@Min(0)
	@Column(name = "technical_votings", nullable = true)
	public Integer getTechnicalVotings() {
		return technicalVotings;
	}

	public void setTechnicalVotings(final Integer technicalVotings) {
		this.technicalVotings = technicalVotings;
	}

	@Column(name = "manual_count", nullable = false)
	public boolean isManualCount() {
		return manualCount;
	}

	public void setManualCount(final boolean manualCount) {
		this.manualCount = manualCount;
	}

	@Column(name = "modified_ballots_processed", nullable = false)
	public boolean isModifiedBallotsProcessed() {
		return modifiedBallotsProcessed;
	}

	public void setModifiedBallotsProcessed(final boolean modifiedBallotsProcessed) {
		this.modifiedBallotsProcessed = modifiedBallotsProcessed;
	}

	@Column(name = "rejected_ballots_processed", nullable = false)
	public boolean isRejectedBallotsProcessed() {
		return rejectedBallotsProcessed;
	}

	public void setRejectedBallotsProcessed(final boolean rejectedBallotsProcessed) {
		this.rejectedBallotsProcessed = rejectedBallotsProcessed;
	}

	@Column(name = "info_text", length = 150)
	@Size(max = 150)
	public String getInfoText() {
		return infoText;
	}

	public void setInfoText(final String infoText) {
		this.infoText = infoText;
	}

	@Min(0)
	@Column(name = "foreign_special_covers")
	public Integer getForeignSpecialCovers() {
		return foreignSpecialCovers;
	}

	public void setForeignSpecialCovers(Integer foreignSpecialCovers) {
		this.foreignSpecialCovers = foreignSpecialCovers;
	}

	@Min(0)
	@Column(name = "special_covers")
	public Integer getSpecialCovers() {
		return specialCovers;
	}

	public void setSpecialCovers(Integer specialCovers) {
		this.specialCovers = specialCovers;
	}

	@Min(0)
	@Column(name = "emergency_special_covers")
	public Integer getEmergencySpecialCovers() {
		return emergencySpecialCovers;
	}

	public void setEmergencySpecialCovers(Integer emergencySpecialCovers) {
		this.emergencySpecialCovers = emergencySpecialCovers;
	}

	@OneToMany(mappedBy = "voteCount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY)
	public Set<BallotCount> getBallotCountSet() {
		return ballotCountSet;
	}

	public void setBallotCountSet(final Set<BallotCount> ballotCountSet) {
		this.ballotCountSet = ballotCountSet;
	}

	public boolean hasBlankBallotCount() {
		return getBallotCountMap().containsKey(EvoteConstants.BALLOT_BLANK);
	}

	@Transient
	public int getBlankBallotCount() {
		Map<String, BallotCount> ballotCountMap = getBallotCountMap();
		return ballotCountMap.containsKey(EvoteConstants.BALLOT_BLANK) ? ballotCountMap.get(EvoteConstants.BALLOT_BLANK).getUnmodifiedBallots() : 0;
	}

	@Min(0)
	@Column(name = "ballots_for_other_contests", nullable = true)
	public Integer getBallotsForOtherContests() {
		return ballotsForOtherContests;
	}

	public void setBallotsForOtherContests(Integer ballotsForOtherContests) {
		this.ballotsForOtherContests = ballotsForOtherContests;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.POLLING_DISTRICT)) {
			return pollingDistrict.getPk();
		} else {
			return null;
		}
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return contestReport.getContest().getPk();
		} else {
			return null;
		}
	}

	@Override
	@Transient
	public AreaLevelEnum getActualAreaLevel() {
		return AreaLevelEnum.getLevel(mvArea.getAreaLevel());
	}

	@Transient
	public boolean isCounting() {
		return voteCountStatus != null && voteCountStatus.getId() == COUNTING.getStatus();
	}

	@Transient
	public boolean isToApproval() {
		return voteCountStatus != null && voteCountStatus.getId() == TO_APPROVAL.getStatus();
	}

	@Transient
	public boolean isApproved() {
		return voteCountStatus != null && (voteCountStatus.getId() == APPROVED.getStatus());
	}

	@Transient
	public boolean isToSettlement() {
		return voteCountStatus != null && (voteCountStatus.getId() == TO_SETTLEMENT.getStatus());
	}

	@Transient
	public boolean isRejected() {
		return voteCountStatus != null && (voteCountStatus.getId() == REJECTED.getStatus());
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
		VoteCount rhs = (VoteCount) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.voteCountStatus, rhs.voteCountStatus)
				.append(this.voteCountCategory, rhs.voteCountCategory)
				.append(this.pollingDistrict, rhs.pollingDistrict)
				.append(this.mvArea, rhs.mvArea)
				.append(this.contestReport, rhs.contestReport)
				.append(this.countQualifier, rhs.countQualifier)
				.append(this.id, rhs.id)
				.append(this.approvedBallots, rhs.approvedBallots)
				.append(this.rejectedBallots, rhs.rejectedBallots)
				.append(this.technicalVotings, rhs.technicalVotings)
				.append(this.manualCount, rhs.manualCount)
				.append(this.modifiedBallotsProcessed, rhs.modifiedBallotsProcessed)
				.append(this.rejectedBallotsProcessed, rhs.rejectedBallotsProcessed)
				.append(this.infoText, rhs.infoText)
				.append(this.foreignSpecialCovers, rhs.foreignSpecialCovers)
				.append(this.specialCovers, rhs.specialCovers)
				.append(this.emergencySpecialCovers, rhs.emergencySpecialCovers)
				.append(this.ballotsForOtherContests, rhs.ballotsForOtherContests)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(voteCountStatus)
				.append(voteCountCategory)
				.append(pollingDistrict)
				.append(mvArea)
				.append(contestReport)
				.append(countQualifier)
				.append(id)
				.append(approvedBallots)
				.append(rejectedBallots)
				.append(technicalVotings)
				.append(manualCount)
				.append(modifiedBallotsProcessed)
				.append(rejectedBallotsProcessed)
				.append(infoText)
				.append(foreignSpecialCovers)
				.append(specialCovers)
				.append(emergencySpecialCovers)
				.append(ballotsForOtherContests)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("voteCountStatus", voteCountStatus)
				.append("voteCountCategory", voteCountCategory)
				.append("pollingDistrict", pollingDistrict)
				.append("mvArea", mvArea)
				.append("contestReport", contestReport)
				.append("countQualifier", countQualifier)
				.append("id", id)
				.append("approvedBallots", approvedBallots)
				.append("rejectedBallots", rejectedBallots)
				.append("technicalVotings", technicalVotings)
				.append("manualCount", manualCount)
				.append("modifiedBallotsProcessed", modifiedBallotsProcessed)
				.append("rejectedBallotsProcessed", rejectedBallotsProcessed)
				.append("infoText", infoText)
				.append("foreignSpecialCovers", foreignSpecialCovers)
				.append("specialCovers", specialCovers)
				.append("emergencySpecialCovers", emergencySpecialCovers)
				.append("emergencySpecialCovers", ballotsForOtherContests)
				.toString();
	}

	/**
	 * VoteCount is protocol count if category is VO and qualifier is PROTOCOL.
	 * @return true if protocol count, else false
	 */
	@Transient
	public boolean isProtocolCount() {
		if (getCountQualifier().getId().equals(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL.getId())) {
			if (getVoteCountCategory().getId().equals(CountCategory.VO.getId())) {
				return true;
			} else {
				throw new IllegalStateException("VoteCount has illegal state, qualifier PROTOCOL is only allowed when category is VO.");
			}
		}
		return false;
	}

	/**
	 * VoteCount is preliminary count if qualifier is PRELIMINARY.
	 * @return true if preliminary count, else false
	 */
	@Transient
	public boolean isPreliminaryCount() {
		return getCountQualifier().getId().equals(PRELIMINARY.getId());
	}

	@Transient
	public boolean isFinalCount() {
		return getCountQualifier().getId().equals(FINAL.getId());
	}

	@Transient
	public List<BallotCount> getBallotCountList() {
		return new ArrayList<>(getBallotCountSet());
	}

	/**
	 * @return map of ballot counts with ballot.getId() as key
	 */
	@Transient
	public Map<String, BallotCount> getBallotCountMap() {
		Map<String, BallotCount> ballotCountMap = new TreeMap<>();
		for (BallotCount ballotCount : getBallotCountSet()) {
			Ballot ballot = ballotCount.getBallot();
			if (ballot != null) {
				ballotCountMap.put(ballot.getId(), ballotCount);
			}
		}
		return ballotCountMap;
	}

	@Transient
	public BallotCount getBallotCount(BallotId ballotId) {
		return getBallotCountMap().get(ballotId.id());
	}

	/**
	 * @return map of rejected ballot counts with ballotRejection.getId() as key
	 */
	@Transient
	public Map<String, BallotCount> getRejectedBallotCountMap() {
		Map<String, BallotCount> ballotCountMap = new TreeMap<>();
		for (BallotCount ballotCount : getBallotCountSet()) {
			BallotRejection ballotRejection = ballotCount.getBallotRejection();
			if (ballotRejection != null) {
				ballotCountMap.put(ballotRejection.getId(), ballotCount);
			}
		}
		return ballotCountMap;
	}

	@Transient
	public BallotCount getRejectedBallotCount(BallotRejectionId ballotRejectionId) {
		return getRejectedBallotCountMap().get(ballotRejectionId.id());
	}

	/**
	 * @return true if count belongs to pollingdistrict (or area), else false
	 */
	@Transient
	public boolean belongsTo(final MvArea pollingDistrict) {
		return getMvArea().equals(pollingDistrict);
	}

	/**
	 * Adds a new ballot count to this
	 */
	public BallotCount addNewBallotCount(Ballot ballot, int unmodifiedCount, int modifiedCount) {
		BallotCount ballotCount = new BallotCount(this, ballot, null, unmodifiedCount, modifiedCount);
		ballotCountSet.add(ballotCount);
		return ballotCount;
	}

	/**
	 * Adds a new ballot count to this for ballot_rejection
	 */
	public BallotCount addNewRejectedBallotCount(BallotRejection ballotRejection, int count) {
		BallotCount ballotCount = new BallotCount(this, null, ballotRejection, count, 0);
		ballotCountSet.add(ballotCount);
		return ballotCount;
	}

	/**
	 * Validates vote count's qualifier, status and area.
	 */
	public void validate(
			final no.valg.eva.admin.common.counting.model.CountQualifier requiredQualifier,
			final Long requiredMvAreaPk,
			final CountCategory expectedCountCategory,
			final CountStatus expectedStatus,
			final CountStatus... otherExpectedStatuses) {

		EnumSet<CountStatus> expectedStatuses = EnumSet.of(expectedStatus, otherExpectedStatuses);
		if (!expectedStatuses.contains(CountStatus.fromId(getVoteCountStatus().getId()))) {
			throw new ValidateException("vote count status is not correct");
		}

		VoteCountCategory theVoteCountCategory = getVoteCountCategory();
		CountCategory countCategory = CountCategory
				.valueOf(theVoteCountCategory.getId());
		if (countCategory != expectedCountCategory) {
			throw new ValidateException("vote count category is not VO");
		}

		CountQualifier theCountQualifier = getCountQualifier();
		if (requiredQualifier != no.valg.eva.admin.common.counting.model.CountQualifier.fromId(theCountQualifier.getId())) {
			throw new ValidateException("required count qualifier is " + requiredQualifier + ", qualifier was " + theCountQualifier);
		}

		if (!(getMvArea().getPk().equals(requiredMvAreaPk))) {
			throw new ValidateException("required area pk is " + requiredMvAreaPk + ", area pk was " + getMvArea().getPk());
		}
	}

	@Transient
	public CountStatus getCountStatus() {
		return getVoteCountStatus().getCountStatus();
	}

	@Transient
	public int getTotalBallotCount() {
		int totalBallotCount = 0;
		for (BallotCount ballotCount : getBallotCountSet()) {
			totalBallotCount += ballotCount.getModifiedBallots() + ballotCount.getUnmodifiedBallots();
		}
		return totalBallotCount;
	}

	public void clearBallotCountSet() {
		Set<BallotCount> theBallotCountSet = getBallotCountSet();
		for (BallotCount ballotCount : theBallotCountSet) {
			ballotCount.setVoteCount(null);
		}
		theBallotCountSet.clear();
	}

	@Transient
	public CountContext getCountContext() {
		return new CountContext(getContestReport().getContest().electionPath(), getCountCategory());
	}

	@Transient
	public CountCategory getCountCategory() {
		return getVoteCountCategory().getCountCategory();
	}

	public void accept(CountingVisitor countingVisitor) {
		if (countingVisitor.include(this)) {
			countingVisitor.visit(this);
			for (BallotCount ballotCount : getBallotCountSet()) {
				ballotCount.accept(countingVisitor);
			}
		}
	}
}
