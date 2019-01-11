package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import no.evote.exception.ValidateException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BallotCount implements Serializable {
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_MODIFIED_BALLOT_COUNT = "@count.error.validation.negative.modified_ballot_count";
	public static final String COUNT_ERROR_VALIDATION_NEGATIVE_UNMODIFIED_BALLOT_COUNT = "@count.error.validation.negative.unmodified_ballot_count";

	protected String id;
	protected String name;
	protected int unmodifiedCount;
	protected int modifiedCount;
	private BallotCountRef ballotCountRef;

	public BallotCount() {
	}

	public BallotCount(String id, String name, int unmodifiedCount, int modifiedCount) {
		this.id = id;
		this.name = name;
		this.unmodifiedCount = unmodifiedCount;
		this.modifiedCount = modifiedCount;
	}

	public BallotCount(String id, String name, int unmodifiedCount, int modifiedCount, BallotCountRef ballotCountRef) {
		this(id, name, unmodifiedCount, modifiedCount);
		this.ballotCountRef = ballotCountRef;
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

	public int getUnmodifiedCount() {
		return unmodifiedCount;
	}

	public void setUnmodifiedCount(int unmodifiedCount) {
		this.unmodifiedCount = unmodifiedCount;
	}

	public int getModifiedCount() {
		return modifiedCount;
	}

	public void setModifiedCount(int modifiedCount) {
		this.modifiedCount = modifiedCount;
	}
	
	public int getCount() {
		return unmodifiedCount + modifiedCount;
	}

	public void validate() {
		if (unmodifiedCount < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_UNMODIFIED_BALLOT_COUNT, id);
		}
		if (modifiedCount < 0) {
			throw new ValidateException(COUNT_ERROR_VALIDATION_NEGATIVE_MODIFIED_BALLOT_COUNT, id);
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
		BallotCount rhs = (BallotCount) obj;
		return new EqualsBuilder()
				.append(this.id, rhs.id)
				.append(this.name, rhs.name)
				.append(this.unmodifiedCount, rhs.unmodifiedCount)
				.append(this.modifiedCount, rhs.modifiedCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.append(name)
				.append(unmodifiedCount)
				.append(modifiedCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.append("unmodifiedCount", unmodifiedCount)
				.append("modifiedCount", modifiedCount)
				.toString();
	}

	public BallotCountRef getBallotCountRef() {
		return ballotCountRef;
	}

	public void setBallotCountRef(BallotCountRef ballotCountRef) {
		this.ballotCountRef = ballotCountRef;
	}
}
