package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.function.BinaryOperator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class RejectedBallotsStatus implements Status {
	private final StatusType statusType;
	private final Boolean approved;
	private final Boolean rejectedBallotsProcessed;
	private final Boolean manualCount;
	private final Integer rejectedBallotCount;

	public RejectedBallotsStatus(boolean county) {
		this.statusType = county ? COUNTY_REJECTED_BALLOTS_STATUS : REJECTED_BALLOTS_STATUS;
		this.approved = null;
		this.rejectedBallotsProcessed = null;
		this.manualCount = null;
		this.rejectedBallotCount = null;
	}

	public RejectedBallotsStatus(boolean county, boolean approved, boolean rejectedBallotsProcessed, boolean manualCount, int rejectedBallotCount) {
		this.statusType = county ? COUNTY_REJECTED_BALLOTS_STATUS : REJECTED_BALLOTS_STATUS;
		this.approved = approved;
		this.rejectedBallotsProcessed = rejectedBallotsProcessed;
		this.manualCount = manualCount;
		this.rejectedBallotCount = rejectedBallotCount;
	}

	private RejectedBallotsStatus(RejectedBallotsStatus oldStatus, RejectedBallotsStatus newStatus) {
		this.statusType = oldStatus.statusType;
		this.approved = merge(oldStatus.approved, newStatus.approved, (b1, b2) -> b1 && b2);
		this.rejectedBallotsProcessed = merge(oldStatus.rejectedBallotsProcessed, newStatus.rejectedBallotsProcessed, (b1, b2) -> b1 && b2);
		this.manualCount = null;
		this.rejectedBallotCount = merge(oldStatus.rejectedBallotCount, newStatus.rejectedBallotCount, (i1, i2) -> i1 + i2);
	}

	private <T> T merge(T value1, T value2, BinaryOperator<T> combiner) {
		if (value1 != null && value2 != null) {
			return combiner.apply(value1, value2);
		}
		return value1 != null ? value1 : value2;
	}

	public boolean isRejectedBallotsPending() {
		return isApproved() && !isRejectedBallotsProcessed() && rejectedBallotCount > 0;
	}

	private boolean isApproved() {
		return approved != null && approved;
	}

	private boolean isRejectedBallotsProcessed() {
		return rejectedBallotsProcessed != null && rejectedBallotsProcessed;
	}

	@Override
	public StatusType getStatusType() {
		return statusType;
	}

	@Override
	public boolean isManualCount() {
		return manualCount != null && manualCount;
	}

	@Override
	public String getPrimaryIconStyle() {
		if (rejectedBallotsProcessed == null || !rejectedBallotsProcessed && rejectedBallotCount == 0) {
			return null;
		}
		if (!isRejectedBallotsProcessed() && rejectedBallotCount > 0) {
			return "eva-icon-warning";
		}
		return "eva-icon-checkmark";
	}

	@Override
	public String getSecondaryIconStyle() {
		if (manualCount == null) {
			return null;
		}
		if (manualCount) {
			return "eva-icon-user";
		}
		return "eva-icon-print";
	}

	@Override
	public Integer getRejectedBallotCount() {
		return rejectedBallotCount;
	}

	@Override
	public String getPanelStyle() {
		if (isRejectedBallotsProcessed()) {
			return format("%s %s", getPrimaryIconStyle(), "completed");
		}
		if (isRejectedBallotsNotProcessed() && rejectedBallotCount > 0) {
			return format("%s %s", getPrimaryIconStyle(), "warning");
		}
		return getPrimaryIconStyle();
	}

	private boolean isRejectedBallotsNotProcessed() {
		return rejectedBallotsProcessed != null && !rejectedBallotsProcessed;
	}

	@Override
	public Status merge(Status status) {
		if (status instanceof RejectedBallotsStatus) {
			return new RejectedBallotsStatus(this, (RejectedBallotsStatus) status);
		}
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RejectedBallotsStatus)) {
			return false;
		}
		RejectedBallotsStatus that = (RejectedBallotsStatus) o;
		return new EqualsBuilder()
				.append(statusType, that.statusType)
				.append(approved, that.approved)
				.append(rejectedBallotsProcessed, that.rejectedBallotsProcessed)
				.append(manualCount, that.manualCount)
				.append(rejectedBallotCount, that.rejectedBallotCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(statusType)
				.append(approved)
				.append(rejectedBallotsProcessed)
				.append(manualCount)
				.append(rejectedBallotCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("statusType", statusType)
				.append("approved", approved)
				.append("rejectedBallotsProcessed", rejectedBallotsProcessed)
				.append("manualCount", manualCount)
				.append("rejectedBallotCount", rejectedBallotCount)
				.toString();
	}
}
