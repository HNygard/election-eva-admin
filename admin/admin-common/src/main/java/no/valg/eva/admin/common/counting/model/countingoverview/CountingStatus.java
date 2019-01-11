package no.valg.eva.admin.common.counting.model.countingoverview;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNTING;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNT_NOT_REQUIRED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.MUNICIPALITY_REJECTED_BALLOTS;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.NOT_STARTED;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class CountingStatus implements Status {
	private final StatusType statusType;
	private final Value value;
	private final Boolean manualCount;
	private final Integer rejectedBallotCount;

	public CountingStatus() {
		this((StatusType) null, null);
	}

	public CountingStatus(StatusType statusType) {
		this(statusType, null);
	}

	public CountingStatus(StatusType statusType, Value value) {
		this.statusType = statusType;
		this.value = value == null ? NOT_STARTED : value;
		this.manualCount = null;
		this.rejectedBallotCount = null;
	}

    public CountingStatus(StatusType statusType, Value value, Boolean manualCount) {
		this.statusType = statusType;
		this.value = value == null ? NOT_STARTED : value;
		this.manualCount = manualCount;
		this.rejectedBallotCount = null;
	}

    public CountingStatus(StatusType statusType, Value value, Boolean manualCount, Integer rejectedBallotCount) {
		this.statusType = statusType;
		this.value = value == null ? NOT_STARTED : value;
		this.manualCount = manualCount;
		this.rejectedBallotCount = rejectedBallotCount;
	}

	private CountingStatus(CountingStatus oldCountingStatus, CountingStatus newCountingStatus) {
		Integer newRejectedBallotCount = null;

		if (oldCountingStatus.statusType == newCountingStatus.statusType) {
			this.statusType = oldCountingStatus.statusType;
		} else {
			this.statusType = null;
		}
		if (oldCountingStatus.value == MUNICIPALITY_REJECTED_BALLOTS || newCountingStatus.value == MUNICIPALITY_REJECTED_BALLOTS) {
			this.value = MUNICIPALITY_REJECTED_BALLOTS;
			newRejectedBallotCount = 0;
			if (oldCountingStatus.rejectedBallotCount != null) {
				newRejectedBallotCount += oldCountingStatus.rejectedBallotCount;
			}
			if (newCountingStatus.rejectedBallotCount != null) {
				newRejectedBallotCount += newCountingStatus.rejectedBallotCount;
			}
			this.manualCount = null;
		} else if (oldCountingStatus.value == COUNT_NOT_REQUIRED) {
			this.value = newCountingStatus.value;
			newRejectedBallotCount = newCountingStatus.rejectedBallotCount;
			this.manualCount = null;
		} else if (newCountingStatus.value == COUNT_NOT_REQUIRED) {
			this.value = oldCountingStatus.value;
			newRejectedBallotCount = oldCountingStatus.rejectedBallotCount;
			this.manualCount = oldCountingStatus.manualCount;
		} else if (oldCountingStatus.value == NOT_STARTED && newCountingStatus.value == NOT_STARTED) {
			this.value = NOT_STARTED;
			this.manualCount = null;
		} else if (oldCountingStatus.value == APPROVED && newCountingStatus.value == APPROVED) {
			this.value = APPROVED;
			this.manualCount = null;
		} else {
			this.value = COUNTING;
			this.manualCount = null;
		}
		this.rejectedBallotCount = newRejectedBallotCount;
		
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
		switch (value) {
		case COUNT_NOT_REQUIRED:
			return "eva-icon-ellipsis";
		case COUNTING:
			return "eva-icon-file";
		case APPROVED:
			return "eva-icon-checkmark";
		case MUNICIPALITY_REJECTED_BALLOTS:
			return "eva-icon-warning";
		default:
			return null;
		}
	}

	@Override
	public String getSecondaryIconStyle() {
		if (value == NOT_STARTED || value == COUNT_NOT_REQUIRED || manualCount == null) {
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
		switch (value) {
		case APPROVED:
			return format("%s %s", getPrimaryIconStyle(), "completed");
		case COUNTING:
			return getPrimaryIconStyle();
		default:
			return null;
		}
	}

	@Override
	public Status merge(Status status) {
		if (status == null) {
			return this;
		}
		if (status instanceof CountingStatus) {
			return new CountingStatus(this, (CountingStatus) status);
		}
		RejectedBallotsStatus rejectedBallotsStatus = (RejectedBallotsStatus) status;
		if (this.value == COUNT_NOT_REQUIRED || rejectedBallotsStatus.isRejectedBallotsPending()) {
			return status;
		}
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingStatus)) {
			return false;
		}
		CountingStatus that = (CountingStatus) o;
		return new EqualsBuilder()
				.append(statusType, that.statusType)
				.append(manualCount, that.manualCount)
				.append(value, that.value)
				.append(rejectedBallotCount, that.rejectedBallotCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(statusType)
				.append(value)
				.append(manualCount)
				.append(rejectedBallotCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("statusType", statusType)
				.append("value", value)
				.append("manualCount", manualCount)
				.append("rejectedBallotCount", rejectedBallotCount)
				.toString();
	}

	public Value getValue() {
		return value;
	}

	public enum Value {
		COUNT_NOT_REQUIRED, NOT_STARTED, COUNTING, APPROVED, MUNICIPALITY_REJECTED_BALLOTS
	}
}
