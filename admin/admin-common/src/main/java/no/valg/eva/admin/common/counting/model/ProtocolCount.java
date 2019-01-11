package no.valg.eva.admin.common.counting.model;

import static no.valg.eva.admin.util.LangUtil.zeroIfNull;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import java.util.List;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * View model class for protocol count data. Intended to facilitate viewing of data and to back up view components such as protocolBallotCounts.xhtml and
 * dailyMarkOffs.xhtml
 */
public class ProtocolCount extends AbstractCount {

	protected DailyMarkOffCounts dailyMarkOffCounts = new DailyMarkOffCounts();
	protected DailyMarkOffCounts dailyMarkOffCountsForOtherContests = null;
	protected int ordinaryBallotCount;
	protected Integer questionableBallotCount;
	protected int specialCovers;
	protected Integer ballotCountForOtherContests;
	protected Integer foreignSpecialCovers;
	protected boolean electronicMarkOffs;
	protected Integer emergencySpecialCovers;

	public ProtocolCount(String id, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
		super(id, areaPath, PROTOCOL, VO, areaName, reportingUnitAreaName, manualCount, null);
	}

	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return dailyMarkOffCounts;
	}

	public void setDailyMarkOffCounts(DailyMarkOffCounts dailyMarkOffCounts) {
		this.dailyMarkOffCounts = dailyMarkOffCounts;
	}

	public DailyMarkOffCounts getDailyMarkOffCountsForOtherContests() {
		return dailyMarkOffCountsForOtherContests;
	}

	public void setDailyMarkOffCountsForOtherContests(DailyMarkOffCounts dailyMarkOffCountsForOtherContests) {
		this.dailyMarkOffCountsForOtherContests = dailyMarkOffCountsForOtherContests;
	}

	@Override
	public List<BallotCount> getBallotCounts() {
		throw new UnsupportedOperationException("protocol count does not have ballot counts");
	}

	public Integer getQuestionableBallotCount() {
		return questionableBallotCount;
	}

	public void setQuestionableBallotCount(Integer questionableBallotCount) {
		this.questionableBallotCount = questionableBallotCount;
	}

	@Override
	public boolean hasRejectedBallotCounts() {
		return false;
	}

	@Override
	public List<RejectedBallotCount> getRejectedBallotCounts() {
		throw new UnsupportedOperationException("protocol count does not have rejected ballot counts");
	}

	@Override
	public int getTotalRejectedBallotCount() {
		throw new UnsupportedOperationException("protocol count does not have rejected ballot counts");
	}

	public int getSpecialCovers() {
		return specialCovers;
	}

	public void setSpecialCovers(int specialCovers) {
		this.specialCovers = specialCovers;
	}

	public Integer getBallotCountForOtherContests() {
		return ballotCountForOtherContests;
	}

	public void setBallotCountForOtherContests(Integer ballotCountForOtherContests) {
		this.ballotCountForOtherContests = ballotCountForOtherContests;
	}

	public boolean isForeignSpecialCoversEnabled() {
		return !electronicMarkOffs;
	}

	public Integer getForeignSpecialCovers() {
		if (electronicMarkOffs) {
			return null;
		}
		return foreignSpecialCovers == null ? 0 : foreignSpecialCovers;
	}

	public void setForeignSpecialCovers(Integer foreignSpecialCovers) {
		this.foreignSpecialCovers = foreignSpecialCovers;
	}

	public boolean isElectronicMarkOffs() {
		return electronicMarkOffs;
	}

	public void setElectronicMarkOffs(boolean electronicMarkOffs) {
		this.electronicMarkOffs = electronicMarkOffs;
	}

	public Integer getEmergencySpecialCovers() {
		if (!electronicMarkOffs) {
			return null;
		}
		return emergencySpecialCovers == null ? 0 : emergencySpecialCovers;
	}

	public void setEmergencySpecialCovers(Integer emergencySpecialCovers) {
		this.emergencySpecialCovers = emergencySpecialCovers;
	}

	@Override
	public Integer getLateValidationCovers() {
		return null;
	}

	@Override
	public void validate() {
		super.validate();

		if (dailyMarkOffCounts == null) {
			throw new IllegalArgumentException("daily mark off counts is null");
		}
		if (dailyMarkOffCounts.isEmpty()) {
			throw new IllegalArgumentException("daily mark off count is empty");
		}
		for (final DailyMarkOffCount dailyMarkOffCount : dailyMarkOffCounts) {
			dailyMarkOffCount.validate();
		}
		if (ordinaryBallotCount < 0) {
			throw new ValidateException("@count.error.validation.negative.ordinary_ballot_count");
		}
		if (questionableBallotCount != null && questionableBallotCount < 0) {
			throw new ValidateException("@count.error.validation.negative.questionable_ballot_count");
		}
		if (foreignSpecialCovers != null && foreignSpecialCovers < 0) {
			throw new ValidateException("@count.error.validation.negative.foreign_special_cover_count");
		}
		if (specialCovers < 0) {
			throw new ValidateException("@count.error.validation.negative.special_cover_count");
		}
		if (emergencySpecialCovers != null && emergencySpecialCovers < 0) {
			throw new ValidateException("@count.error.validation.negative.emergency_special_cover_count");
		}
	}

	@Override
	public void validateForApproval() {
		validate();

		if (!hasComment() && isCommentRequired()) {
			throw new ValidateException("@count.error.validation.missing_comment");
		}
	}

	public boolean isCommentRequired() {
		return (getDifferenceBetweenTotalBallotCountsAndMarkOffCount() != 0
		|| getDifferenceBetweenTotalBallotCountsForOtherContestsAndMarkOffCountForOtherContests() != 0);
	}

	@Override
	public int getOrdinaryBallotCount() {
		return ordinaryBallotCount;
	}

	@Override
	public int getModifiedBallotCount() {
		return 0;
	}

	@Override
	public int getUnmodifiedBallotCount() {
		return 0;
	}

	public void setOrdinaryBallotCount(int ordinaryBallotCount) {
		this.ordinaryBallotCount = ordinaryBallotCount;
	}

	/**
	 * @return The total number of ballots. Note that "total" only includes ballots from own contest, i.e. the ballots from other contests are not counted
	 */
	@Override
	public int getTotalBallotCount() {
		return ordinaryBallotCount
				+ zeroIfNull(questionableBallotCount);
	}

	public int getDifferenceBetweenTotalBallotCountsAndMarkOffCount() {
		return getTotalBallotCount() - getDailyMarkOffCounts().getMarkOffCount();
	}

	public int getDifferenceBetweenTotalBallotCountsForOtherContestsAndMarkOffCountForOtherContests() {
		return zeroIfNull(ballotCountForOtherContests)
				- (getDailyMarkOffCountsForOtherContests() == null ? 0 : getDailyMarkOffCountsForOtherContests().getMarkOffCount());
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
		ProtocolCount rhs = (ProtocolCount) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.dailyMarkOffCounts, rhs.dailyMarkOffCounts)
				.append(this.dailyMarkOffCountsForOtherContests, rhs.dailyMarkOffCountsForOtherContests)
				.append(this.ordinaryBallotCount, rhs.ordinaryBallotCount)
				.append(this.questionableBallotCount, rhs.questionableBallotCount)
				.append(this.specialCovers, rhs.specialCovers)
				.append(this.ballotCountForOtherContests, rhs.ballotCountForOtherContests)
				.append(this.foreignSpecialCovers, rhs.foreignSpecialCovers)
				.append(this.electronicMarkOffs, rhs.electronicMarkOffs)
				.append(this.emergencySpecialCovers, rhs.emergencySpecialCovers)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(dailyMarkOffCounts)
				.append(dailyMarkOffCountsForOtherContests)
				.append(ordinaryBallotCount)
				.append(questionableBallotCount)
				.append(ballotCountForOtherContests)
				.append(specialCovers)
				.append(foreignSpecialCovers)
				.append(electronicMarkOffs)
				.append(emergencySpecialCovers)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("dailyMarkOffCounts", dailyMarkOffCounts)
				.append("dailyMarkOffCountsForOtherContests", dailyMarkOffCountsForOtherContests)
				.append("ordinaryBallotCount", ordinaryBallotCount)
				.append("questionableBallotCount", questionableBallotCount)
				.append("ballotCountForOtherContests", ballotCountForOtherContests)
				.append("specialCovers", specialCovers)
				.append("foreignSpecialCovers", foreignSpecialCovers)
				.append("electronicMarkOffs", electronicMarkOffs)
				.append("emergencySpecialCovers", emergencySpecialCovers)
				.toString();
	}

	public boolean isIncludeMarkOffsFromOtherContests() {
		return getDailyMarkOffCountsForOtherContests() != null;
	}

	public boolean isIncludeBallotCountFromOtherContests() {
		return getBallotCountForOtherContests() != null;
	}
}
