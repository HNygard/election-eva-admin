package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import no.evote.exception.ValidateException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.LocalDate;

public class DailyMarkOffCount implements Serializable {
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_MARK_OFF_COUNT = "@count.error.validation.negative.mark_off_count";

	private final LocalDate date;
	private int markOffCount;

	public DailyMarkOffCount(LocalDate date) {
		this.date = date;
	}

	public DailyMarkOffCount(LocalDate date, int markOffCount) {
		this.date = date;
		this.markOffCount = markOffCount;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getMarkOffCount() {
		return markOffCount;
	}

	public void setMarkOffCount(int markOffCount) {
		this.markOffCount = markOffCount;
	}

	public void incrementMarkOffCount() {
		markOffCount++;
	}

	/**
	 * @throws no.evote.exception.ValidateException
	 *             if markOffCount is negative
	 * @throws java.lang.NullPointerException
	 *             if date is null
	 */
	public void validate() {
		if (date == null) {
			throw new NullPointerException("date is null");
		}

		if (markOffCount < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_MARK_OFF_COUNT);
		}
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
		DailyMarkOffCount rhs = (DailyMarkOffCount) obj;
		return new EqualsBuilder()
				.append(this.date, rhs.date)
				.append(this.markOffCount, rhs.markOffCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(date)
				.append(markOffCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("date", date)
				.append("markOffCount", markOffCount)
				.toString();
	}
}
