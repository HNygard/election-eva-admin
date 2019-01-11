package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SplitBallotCount implements BallotCount {
	private CountCategory countCategory;
	private int modifiedBallotCount;
	private int unmodifiedBallotCount;

	public SplitBallotCount(CountCategory countCategory, int modifiedBallotCount, int unmodifiedBallotCount) {
		this.countCategory = countCategory;
		this.modifiedBallotCount = modifiedBallotCount;
		this.unmodifiedBallotCount = unmodifiedBallotCount;
	}

	@Override
	public CountCategory getCountCategory() {
		return countCategory;
	}

	@Override
	public Integer getModifiedBallotCount() {
		return modifiedBallotCount;
	}

	public void setModifiedBallotCount(Integer modifiedBallotCount) {
		this.modifiedBallotCount = modifiedBallotCount;
	}

	@Override
	public Integer getUnmodifiedBallotCount() {
		return unmodifiedBallotCount;
	}

	public void setUnmodifiedBallotCount(Integer unmodifiedBallotCount) {
		this.unmodifiedBallotCount = unmodifiedBallotCount;
	}

	@Override
	public int getBallotCount() {
		return modifiedBallotCount + unmodifiedBallotCount;
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
		SplitBallotCount rhs = (SplitBallotCount) obj;
		return new EqualsBuilder()
				.append(this.countCategory, rhs.countCategory)
				.append(this.modifiedBallotCount, rhs.modifiedBallotCount)
				.append(this.unmodifiedBallotCount, rhs.unmodifiedBallotCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(countCategory)
				.append(modifiedBallotCount)
				.append(unmodifiedBallotCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("countCategory", countCategory)
				.append("modifiedBallotCount", modifiedBallotCount)
				.append("unmodifiedBallotCount", unmodifiedBallotCount)
				.toString();
	}
}
