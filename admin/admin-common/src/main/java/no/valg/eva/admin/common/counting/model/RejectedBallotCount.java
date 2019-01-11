package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import no.evote.exception.ValidateException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RejectedBallotCount implements Serializable {
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_REJECTED_BALLOT_COUNT = "@count.error.validation.negative.rejected_ballot_count";

	protected String id;
	protected String name;
	protected int count;

	public RejectedBallotCount() {
	}

	public RejectedBallotCount(String id, String name, int count) {
		this.id = id;
		this.name = name;
		this.count = count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void validate() {
		if (count < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_REJECTED_BALLOT_COUNT, id);
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
		RejectedBallotCount rhs = (RejectedBallotCount) obj;
		return new EqualsBuilder()
				.append(this.id, rhs.id)
				.append(this.name, rhs.name)
				.append(this.count, rhs.count)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.append(name)
				.append(count)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.append("count", count)
				.toString();
	}
}
