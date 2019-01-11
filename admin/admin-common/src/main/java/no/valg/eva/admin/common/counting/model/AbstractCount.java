package no.valg.eva.admin.common.counting.model;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

import java.io.Serializable;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class AbstractCount implements Count, Serializable {
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_BLANK_BALLOT_COUNT = "@count.error.validation.negative.blank_ballot_count";
	public static final String COUNT_ERROR_VALIDATION_COMMENT_TOO_LONG = "@count.error.validation.comment.too_long";
	public static final int COMMENT_MAX_LENGTH = 150;
	protected final AreaPath areaPath;
	protected final CountQualifier qualifier;
	protected final CountCategory category;
	protected final String areaName;
	protected final String reportingUnitAreaName;
	protected final boolean manualCount;
	protected String id;
	protected int version;
	protected CountStatus status = CountStatus.NEW;
	protected String comment;
	protected Integer blankBallotCount;

	protected AbstractCount(
			String id,
			AreaPath areaPath,
			CountQualifier qualifier,
			CountCategory category,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			Integer blankBallotCount) {
		this.id = id;
		this.areaPath = areaPath;
		this.qualifier = qualifier;
		this.category = category;
		this.areaName = areaName;
		this.reportingUnitAreaName = reportingUnitAreaName;
		this.manualCount = manualCount;
		this.blankBallotCount = blankBallotCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public CountQualifier getQualifier() {
		return qualifier;
	}

	@Override
	public CountCategory getCategory() {
		return category;
	}

	public String getQualifierName() {
		return qualifier.getName();
	}

	@Override
	public CountStatus getStatus() {
		return status;
	}

	public void setStatus(CountStatus status) {
		this.status = status;
	}

	/**
	 * @return string representation of count status
	 */
	public String getStatusName() {
		return status.getName();
	}

	public boolean isNew() {
		return status == CountStatus.NEW;
	}

	public boolean isSaved() {
		return status == CountStatus.SAVED;
	}

	@Override
	public boolean isRevoked() {
		return status == CountStatus.REVOKED;
	}

	@Override
	public boolean isEditable() {
		return isNew() || isSaved() || isRevoked();
	}

	/**
	 * @return true if count status is APPROVED or TO_SETTLEMENT, false otherwise
	 */
	public boolean isApproved() {
		return status == CountStatus.APPROVED || status == CountStatus.TO_SETTLEMENT;
	}

	public boolean isReadyForSettlement() {
		return status == CountStatus.TO_SETTLEMENT;
	}

	public String getAreaName() {
		return areaName;
	}

	public String getReportingUnitAreaName() {
		return reportingUnitAreaName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean hasComment() {
		return comment != null && comment.trim().length() > 0;
	}

	public boolean isManualCount() {
		return manualCount;
	}

	public Integer getBlankBallotCount() {
		return blankBallotCount;
	}

	public void setBlankBallotCount(Integer blankBallotCount) {
		this.blankBallotCount = blankBallotCount;
	}

	/**
	 * @throws no.evote.exception.ValidateException
	 *             when validation fails
	 */
	public void validate() {
		if (blankBallotCount != null && blankBallotCount < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_BLANK_BALLOT_COUNT);
		}
		if (comment != null && comment.length() > COMMENT_MAX_LENGTH) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_COMMENT_TOO_LONG);
		}
	}

	/**
	 * @throws no.evote.exception.ValidateException
	 *             when validation fails
	 */
	public abstract void validateForApproval();

	/**
	 * @return count of ordinary, blank and questionable ballots
	 */
	public abstract int getTotalBallotCount();

	@Override
	public boolean isFinalCount() {
		return qualifier == FINAL;
	}

	@Override
	public boolean isPreliminaryCount() {
		return qualifier == PRELIMINARY;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		AbstractCount rhs = (AbstractCount) obj;
		return new EqualsBuilder()
				.append(this.id, rhs.id)
				.append(this.areaPath, rhs.areaPath)
				.append(this.qualifier, rhs.qualifier)
				.append(this.status, rhs.status)
				.append(this.areaName, rhs.areaName)
				.append(this.reportingUnitAreaName, rhs.reportingUnitAreaName)
				.append(this.comment, rhs.comment)
				.append(this.manualCount, rhs.manualCount)
				.append(this.blankBallotCount, rhs.blankBallotCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.append(areaPath)
				.append(qualifier)
				.append(status)
				.append(areaName)
				.append(reportingUnitAreaName)
				.append(comment)
				.append(manualCount)
				.append(blankBallotCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("areaPath", areaPath)
				.append("status", status)
				.append("areaName", areaName)
				.append("reportingUnitAreaName", reportingUnitAreaName)
				.append("comment", comment)
				.append("manualCount", manualCount)
				.append("blankBallotCount", blankBallotCount)
				.toString();
	}

	@Override
	public int getOrdinaryBallotCount() {
		int ordinaryBallotCount = 0;
		for (BallotCount ballotCount : getBallotCounts()) {
			ordinaryBallotCount += ballotCount.getCount();
		}
		return ordinaryBallotCount;
	}

	@Override
	public int getModifiedBallotCount() {
		int modifiedBallotCount = 0;
		for (BallotCount ballotCount : getBallotCounts()) {
			modifiedBallotCount += ballotCount.getModifiedCount();
		}
		return modifiedBallotCount;
	}

	@Override
	public int getUnmodifiedBallotCount() {
		int unmodifedBallotCount = 0;
		for (BallotCount ballotCount : getBallotCounts()) {
			unmodifedBallotCount += ballotCount.getUnmodifiedCount();
		}
		return unmodifedBallotCount;
	}
}
