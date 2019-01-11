package no.valg.eva.admin.common.counting.model;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PreliminaryCount extends AbstractCount {
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_QUESTIONABLE_BALLOT_COUNT = "@count.error.validation.negative.questionable_ballot_count";
	public static final String COUNT_ERROR_VALIDATION_MISSING_COMMENT = "@count.error.validation.missing_comment.preliminary_count";
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_BALLOT_COUNT = "@count.error.validation.negative.ballot_count";

	protected Integer markOffCount;
	protected Integer expectedBallotCount;
	protected Integer totalBallotCountForOtherPollingDistricts;
	protected List<BallotCount> ballotCounts;
	protected int questionableBallotCount;
	protected Integer lateValidationCovers;
	protected DailyMarkOffCounts dailyMarkOffCounts;
	protected boolean electronicMarkOffs;
	protected boolean requiredProtocolCount;
	protected boolean isAntallStemmesedlerLagtTilSideLagret;

	public PreliminaryCount(String id, AreaPath areaPath, CountCategory category, String areaName, String reportingUnitAreaName, boolean manualCount) {
		super(id, areaPath, CountQualifier.PRELIMINARY, category, areaName, reportingUnitAreaName, manualCount, null);
	}

	public PreliminaryCount(
			String id,
			AreaPath areaPath,
			CountCategory category,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			Integer blankBallotCount) {
		super(id, areaPath, CountQualifier.PRELIMINARY, category, areaName, reportingUnitAreaName, manualCount, blankBallotCount);
	}

	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return dailyMarkOffCounts;
	}

	public void setDailyMarkOffCounts(DailyMarkOffCounts dailyMarkOffCounts) {
		this.dailyMarkOffCounts = dailyMarkOffCounts;
	}

	public Integer getMarkOffCount() {
		return markOffCount;
	}

	public void setMarkOffCount(Integer markOffCount) {
		this.markOffCount = markOffCount;
	}

	/**
	 * Used for FO counts for technical polling districts describing expected number og ballots
	 * 
	 * @return expected ballot count or null if not set
	 */
	public Integer getExpectedBallotCount() {
		return expectedBallotCount;
	}

	public void setExpectedBallotCount(Integer expectedBallotCount) {
		if (this.expectedBallotCount == null && expectedBallotCount == null) {
			return;
		}
		if (expectedBallotCount == null) {
			this.expectedBallotCount = 0;
		} else {
			this.expectedBallotCount = expectedBallotCount;
		}
	}

	/**
	 * Used for FO counts for technical polling districts describing total ballot count for all polling districts
	 *
	 * @return expected ballot count or null if not set
	 */
	public Integer getTotalBallotCountForOtherPollingDistricts() {
		return totalBallotCountForOtherPollingDistricts;
	}

	public void setTotalBallotCountForOtherPollingDistricts(Integer totalBallotCountForOtherPollingDistricts) {
		this.totalBallotCountForOtherPollingDistricts = totalBallotCountForOtherPollingDistricts;
	}

	public Integer getTotalBallotCountForAllPollingDistricts() {
		if (totalBallotCountForOtherPollingDistricts != null) {
			return getTotalBallotCount() + totalBallotCountForOtherPollingDistricts;
		}
		return null;
	}

	public List<BallotCount> getBallotCounts() {
		return ballotCounts;
	}

	public void setBallotCounts(List<BallotCount> ballotCounts) {
		this.ballotCounts = ballotCounts;
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
		return emptyList();
	}

	@Override
	public int getTotalRejectedBallotCount() {
		return 0;
	}

	@Override
	public Integer getLateValidationCovers() {
		return lateValidationCovers;
	}

	public void setLateValidationCovers(Integer lateValidationCovers) {
		this.lateValidationCovers = lateValidationCovers;
	}

	public boolean isAntallStemmesedlerLagtTilSideLagret() {
		return isAntallStemmesedlerLagtTilSideLagret;
	}

	public void setAntallStemmesedlerLagtTilSideLagret(boolean antallStemmesedlerLagtTilSideLagret) {
		isAntallStemmesedlerLagtTilSideLagret = antallStemmesedlerLagtTilSideLagret;
	}

	public boolean isElectronicMarkOffs() {
		return electronicMarkOffs;
	}

	public void setElectronicMarkOffs(boolean electronicMarkOffs) {
		this.electronicMarkOffs = electronicMarkOffs;
	}

	public boolean isRequiredProtocolCount() {
		return requiredProtocolCount;
	}

	public void setRequiredProtocolCount(boolean requiredProtocolCount) {
		this.requiredProtocolCount = requiredProtocolCount;
	}

	@Override
	public void validate() {
		super.validate();

		if (questionableBallotCount < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_QUESTIONABLE_BALLOT_COUNT);
		}
		if (blankBallotCount == null) {
			throw new IllegalArgumentException("ILLEGAL_ARGUMENT_BLANK_IS_NULL");
		}
		if (ballotCounts == null) {
			throw new IllegalArgumentException("ILLEGAL_ARGUMENT_BALLOT_COUNTS_IS_NULL");
		}
		if (ballotCounts.isEmpty()) {
			throw new IllegalArgumentException("ILLEGAL_ARGUMENT_BALLOT_COUNTS_IS_EMPTY");
		}
		
		try {
			ballotCounts.forEach(BallotCount::validate);
		} catch (ValidateException e) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_BALLOT_COUNT, e.getParams());
		}
	}

	@Override
	public void validateForApproval() {
		validate();
		if (!hasComment() && isCommentRequired()) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_MISSING_COMMENT);
		}
	}

	public boolean isCommentRequired() {
		return (expectedBallotCount == null || markOffCount != null) && getMarkOffCountDifferenceWithTotalBallotCount() != 0;
	}

	@Override
	public int getTotalBallotCount() {
		return getOrdinaryBallotCount() + blankBallotCount + questionableBallotCount;
	}

	/**
	 * @throws java.lang.NullPointerException
	 *             when mark off count is missing
	 */
	public int getMarkOffCountDifferenceWithTotalBallotCount() {
		Integer calculatedMarkOffCount;
		switch (category) {
		case FO:
			calculatedMarkOffCount = this.markOffCount - lateValidationCovers;
			break;
		case FS:
			calculatedMarkOffCount = this.markOffCount + lateValidationCovers;
			break;
		default:
			calculatedMarkOffCount = this.markOffCount != null ? this.markOffCount : 0;
		}
		if (totalBallotCountForOtherPollingDistricts != null) {
			return getTotalBallotCount() + totalBallotCountForOtherPollingDistricts - calculatedMarkOffCount;
		}
		return getTotalBallotCount() - calculatedMarkOffCount;
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
		PreliminaryCount rhs = (PreliminaryCount) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.markOffCount, rhs.markOffCount)
				.append(this.expectedBallotCount, rhs.expectedBallotCount)
				.append(this.totalBallotCountForOtherPollingDistricts, rhs.totalBallotCountForOtherPollingDistricts)
				.append(this.ballotCounts, rhs.ballotCounts)
				.append(this.questionableBallotCount, rhs.questionableBallotCount)
				.append(this.lateValidationCovers, rhs.lateValidationCovers)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(markOffCount)
				.append(expectedBallotCount)
				.append(totalBallotCountForOtherPollingDistricts)
				.append(ballotCounts)
				.append(questionableBallotCount)
				.append(lateValidationCovers)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("markOffCount", markOffCount)
				.append("expectedBallotCount", expectedBallotCount)
				.append("totalBallotCountForOtherPollingDistricts", totalBallotCountForOtherPollingDistricts)
				.append("ballotCounts", ballotCounts)
				.append("questionableBallotCount", questionableBallotCount)
				.append("lateValidationCovers", lateValidationCovers)
				.toString();
	}

	public Map<String, BallotCount> getBallotCountMap() {
		Map<String, BallotCount> ballotCountMap = new HashMap<>();
		for (final BallotCount ballotCount : ballotCounts) {
			ballotCountMap.put(ballotCount.getId(), ballotCount);
		}
		return ballotCountMap;
	}

	public boolean useDailyMarkOffCounts() {
		return getCategory() == CountCategory.VO && !isRequiredProtocolCount() && !isElectronicMarkOffs();
	}
}
