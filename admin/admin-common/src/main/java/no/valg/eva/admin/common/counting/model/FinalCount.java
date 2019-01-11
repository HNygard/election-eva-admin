package no.valg.eva.admin.common.counting.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

public class FinalCount extends AbstractCount {
	protected List<BallotCount> ballotCounts = new ArrayList<>();
	protected List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
	protected boolean modifiedBallotsProcessed;
	protected boolean rejectedBallotsProcessed;
	protected DateTime modifiedDate;
	private ReportingUnitTypeId reportingUnitTypeId;

	private Long voteCountPk;

	public FinalCount(
			String id,
			AreaPath areaPath,
			CountCategory category,
			String areaName,
			ReportingUnitTypeId reportingUnitTypeId,
			String reportingUnitAreaName,
			boolean manualCount) {
		super(id, areaPath, CountQualifier.FINAL, category, areaName, reportingUnitAreaName, manualCount, 0);
		this.reportingUnitTypeId = reportingUnitTypeId;
	}

	public FinalCount(
			String id,
			AreaPath areaPath,
			CountCategory category,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			Integer blankBallotCount) {
		super(id, areaPath, CountQualifier.FINAL, category, areaName, reportingUnitAreaName, manualCount, blankBallotCount);
	}

	public DateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(DateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getIndex() {
		if (id == null) {
			return 1;
		}
		
		return Integer.valueOf(id.substring(3));
		
	}

	public List<BallotCount> getBallotCounts() {
		return ballotCounts;
	}

	public void setBallotCounts(List<BallotCount> ballotCounts) {
		this.ballotCounts = ballotCounts;
	}

	public Map<String, BallotCount> getBallotCountMap() {
		Map<String, BallotCount> ballotCountMap = new HashMap<>();
		for (BallotCount ballotCount : ballotCounts) {
			ballotCountMap.put(ballotCount.getId(), ballotCount);
		}
		return ballotCountMap;
	}

	@Override
	public Integer getQuestionableBallotCount() {
		throw new UnsupportedOperationException("final count does not have questionable ballot count");
	}

	@Override
	public void setQuestionableBallotCount(Integer questionableBallotCount) {
		throw new UnsupportedOperationException("final count does not have questionable ballot count");
	}

	@Override
	public boolean hasRejectedBallotCounts() {
		return true;
	}

	public List<RejectedBallotCount> getRejectedBallotCounts() {
		return rejectedBallotCounts;
	}

	public void setRejectedBallotCounts(List<RejectedBallotCount> rejectedBallotCounts) {
		this.rejectedBallotCounts = rejectedBallotCounts;
	}

	public Map<String, RejectedBallotCount> getRejectedBallotCountMap() {
		Map<String, RejectedBallotCount> rejectedBallotCountMap = new HashMap<>();
		for (RejectedBallotCount rejectedBallotCount : rejectedBallotCounts) {
			rejectedBallotCountMap.put(rejectedBallotCount.getId(), rejectedBallotCount);
		}
		return rejectedBallotCountMap;
	}

	public boolean isModifiedBallotsProcessed() {
		return modifiedBallotsProcessed;
	}

	public void setModifiedBallotsProcessed(boolean modifiedBallotsProcessed) {
		this.modifiedBallotsProcessed = modifiedBallotsProcessed;
	}

	public boolean isRejectedBallotsProcessed() {
		return rejectedBallotsProcessed;
	}

	public void setRejectedBallotsProcessed(boolean rejectedBallotsProcessed) {
		this.rejectedBallotsProcessed = rejectedBallotsProcessed;
	}

	public Long getVoteCountPk() {
		return voteCountPk;
	}

	public void setVoteCountPk(final Long voteCountPk) {
		this.voteCountPk = voteCountPk;
	}

	@Override
	public Integer getLateValidationCovers() {
		return null;
	}

	@Override
	public void validate() {
		super.validate();

		if (blankBallotCount == null) {
			throw new IllegalStateException("blank ballot counts cannot be null");
		} 
		if (ballotCounts == null) {
			throw new IllegalStateException("ballot counts cannot be null");
		}
		if (ballotCounts.isEmpty()) {
			throw new IllegalStateException("ballot counts cannot be empty");
		}
		if (rejectedBallotCounts == null) {
			throw new IllegalStateException("rejected ballot counts cannot be null");
		}
		if (rejectedBallotCounts.isEmpty()) {
			throw new IllegalStateException("rejected ballot counts cannot be empty");
		}
		
		ballotCounts.forEach(BallotCount::validate);
	}

	@Override
	public void validateForApproval() {
		validate();
	}

	public int getTotalRejectedBallotCount() {
		int totalRejectedBallotCount = 0;
		for (RejectedBallotCount rejectedBallotCount : rejectedBallotCounts) {
			totalRejectedBallotCount += rejectedBallotCount.getCount();
		}
		return totalRejectedBallotCount;
	}

	@Override
	public int getTotalBallotCount() {
		return getOrdinaryBallotCount() + blankBallotCount + getTotalRejectedBallotCount();
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
		FinalCount rhs = (FinalCount) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.ballotCounts, rhs.ballotCounts)
				.append(this.rejectedBallotCounts, rhs.rejectedBallotCounts)
				.append(this.modifiedBallotsProcessed, rhs.modifiedBallotsProcessed)
				.append(this.rejectedBallotsProcessed, rhs.rejectedBallotsProcessed)
				.append(this.modifiedDate, rhs.modifiedDate)
				.append(this.voteCountPk, rhs.voteCountPk)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(ballotCounts)
				.append(rejectedBallotCounts)
				.append(modifiedBallotsProcessed)
				.append(rejectedBallotsProcessed)
				.append(modifiedDate)
				.append(voteCountPk)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("ballotCounts", ballotCounts)
				.append("rejectedBallotCounts", rejectedBallotCounts)
				.append("modifiedBallotsProcessed", modifiedBallotsProcessed)
				.append("rejectedBallotsProcessed", rejectedBallotsProcessed)
				.append("modifiedDate", modifiedDate)
				.append("voteCountPk", voteCountPk)
				.toString();
	}

	public ReportingUnitTypeId getReportingUnitTypeId() {
		return reportingUnitTypeId;
	}
}
