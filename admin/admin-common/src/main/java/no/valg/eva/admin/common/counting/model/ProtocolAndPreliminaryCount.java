package no.valg.eva.admin.common.counting.model;

import static java.util.Collections.emptyList;

import java.io.Serializable;
import java.util.List;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

// can not be final since it is mocked in unit test
public class ProtocolAndPreliminaryCount implements Count, Serializable {
	private final ProtocolCount protocolCount;
	private final PreliminaryCount preliminaryCount;

	private ProtocolAndPreliminaryCount(ProtocolCount protocolCount, PreliminaryCount preliminaryCount) {
		this.protocolCount = protocolCount;
		this.preliminaryCount = preliminaryCount;
	}

	public static ProtocolAndPreliminaryCount from(final ProtocolCount protocolCount, final PreliminaryCount preliminaryCount) {
		return new ProtocolAndPreliminaryCount(protocolCount, preliminaryCount);
	}

	public String getQualifierName() {
		return "@count_qualifier[PF].name";
	}

	public ProtocolCount getProtocolCount() {
		updateProtocolCountBallotCounts();
		return protocolCount;
	}

	private void updateProtocolCountBallotCounts() {
		protocolCount.setOrdinaryBallotCount(preliminaryCount.getOrdinaryBallotCount());
	}

	public PreliminaryCount getPreliminaryCount() {
		return preliminaryCount;
	}

	public AreaPath getAreaPath() {
		return preliminaryCount.getAreaPath();
	}

	public String getAreaName() {
		return preliminaryCount.getAreaName();
	}

	public String getReportingUnitName() {
		return preliminaryCount.getReportingUnitAreaName();
	}

	public CountStatus getStatus() {
		return preliminaryCount.getStatus();
	}

	public void setStatus(CountStatus status) {
		protocolCount.setStatus(status);
		preliminaryCount.setStatus(status);
	}

	public String getStatusName() {
		return preliminaryCount.getStatusName();
	}

	@Override
	public CountCategory getCategory() {
		return CountCategory.VO;
	}

	public boolean isManualCount() {
		return protocolCount.isManualCount();
	}

	public boolean isNew() {
		return protocolCount.isNew() && preliminaryCount.isNew();
	}

	public boolean isSaved() {
		return protocolCount.isSaved() && preliminaryCount.isSaved();
	}

	@Override
	public boolean isRevoked() {
		return protocolCount.isRevoked() && preliminaryCount.isRevoked();
	}

	@Override
	public boolean isEditable() {
		return protocolCount.isEditable() && preliminaryCount.isEditable();
	}

	public boolean isApproved() {
		return protocolCount.isApproved() && preliminaryCount.isApproved();
	}

	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return protocolCount.getDailyMarkOffCounts();
	}

	public void setDailyMarkOffCounts(DailyMarkOffCounts dailyMarkOffCounts) {
		protocolCount.setDailyMarkOffCounts(dailyMarkOffCounts);
	}

	public int getMarkOffCount() {
		return protocolCount.getDailyMarkOffCounts().getMarkOffCount();
	}

	public List<BallotCount> getBallotCounts() {
		return preliminaryCount.getBallotCounts();
	}

	@Override
	public int getOrdinaryBallotCount() {
		return preliminaryCount.getOrdinaryBallotCount();
	}

	@Override
	public int getModifiedBallotCount() {
		return preliminaryCount.getModifiedBallotCount();
	}

	@Override
	public int getUnmodifiedBallotCount() {
		return preliminaryCount.getUnmodifiedBallotCount();
	}

	@Override
	public Integer getQuestionableBallotCount() {
		return preliminaryCount.getQuestionableBallotCount();
	}

	@Override
	public void setQuestionableBallotCount(Integer questionableBallotCount) {
		protocolCount.setQuestionableBallotCount(questionableBallotCount);
		preliminaryCount.setQuestionableBallotCount(questionableBallotCount);
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
		throw new UnsupportedOperationException("protocol and preliminary count does not have rejected ballot counts");
	}

	@Override
	public Integer getLateValidationCovers() {
		return null;
	}

	public Integer getBlankBallotCount() {
		return preliminaryCount.getBlankBallotCount();
	}

	public void setBlankBallotCount(Integer blankBallotCount) {
		protocolCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
	}

	public int getTotalBallotCount() {
		return preliminaryCount.getTotalBallotCount();
	}

	public int getDifferenceBetweenTotalBallotCountsAndMarkOffCount() {
		return getTotalBallotCount() - getMarkOffCount();
	}

	public String getComment() {
		return protocolCount.getComment();
	}

	public void setComment(String comment) {
		protocolCount.setComment(comment);
		preliminaryCount.setComment(comment);
	}

	public boolean isForeignSpecialCoversEnabled() {
		return protocolCount.isForeignSpecialCoversEnabled();
	}

	public Integer getForeignSpecialCovers() {
		return protocolCount.getForeignSpecialCovers();
	}

	public void setForeignSpecialCovers(Integer foreignSpecialCovers) {
		protocolCount.setForeignSpecialCovers(foreignSpecialCovers);
	}

	public int getSpecialCovers() {
		return protocolCount.getSpecialCovers();
	}

	public void setSpecialCovers(int specialCovers) {
		protocolCount.setSpecialCovers(specialCovers);
	}

	public boolean isElectronicMarkOffs() {
		return protocolCount.isElectronicMarkOffs();
	}

	public Integer getEmergencySpecialCovers() {
		return protocolCount.getEmergencySpecialCovers();
	}

	public void setEmergencySpecialCovers(Integer emergencySpecialCovers) {
		protocolCount.setEmergencySpecialCovers(emergencySpecialCovers);
	}

	public void validate() {
		protocolCount.validate();
		preliminaryCount.validate();
	}

	public void validateForApproval() {
		validate();
		if (!hasComment() && isCommentRequired()) {
			throw new ValidateException("@count.error.validation.missing_comment");
		}
	}

	public boolean hasComment() {
		return protocolCount.hasComment();
	}

	public boolean isCommentRequired() {
		return getDifferenceBetweenTotalBallotCountsAndMarkOffCount() != 0;
	}

	@Override
	public boolean isPreliminaryCount() {
		return true;
	}

	@Override
	public boolean isFinalCount() {
		return false;
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
		ProtocolAndPreliminaryCount rhs = (ProtocolAndPreliminaryCount) obj;
		return new EqualsBuilder()
				.append(this.protocolCount, rhs.protocolCount)
				.append(this.preliminaryCount, rhs.preliminaryCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(protocolCount)
				.append(preliminaryCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("areaPath", getAreaPath())
				.append("status", getStatus())
				.append("areaName", getAreaName())
				.append("reportingUnitAreaName", getReportingUnitName())
				.append("comment", getComment())
				.append("manualCount", isManualCount())
				.append("ballotCounts", getBallotCounts())
				.append("questionableBallotCount", getQuestionableBallotCount())
				.append("blankBallotCount", getBlankBallotCount())
				.append("dailyMarkOffCounts", getDailyMarkOffCounts())
				.append("specialCovers", getSpecialCovers())
				.append("foreignSpecialCoversEnabled", isForeignSpecialCoversEnabled())
				.append("foreignSpecialCovers", getForeignSpecialCovers())
				.append("electronicMarkOffs", isElectronicMarkOffs())
				.append("emergencySpecialCovers", getEmergencySpecialCovers())
				.toString();
	}
}
