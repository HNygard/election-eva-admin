package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SimpleBallotCount implements BallotCount {
	private CountCategory countCategory;
	private int ballotCount;

	public SimpleBallotCount(CountCategory countCategory, int ballotCount) {
		this.countCategory = countCategory;
		this.ballotCount = ballotCount;
	}

	@Override
	public CountCategory getCountCategory() {
		return countCategory;
	}

	@Override
	public Integer getModifiedBallotCount() {
		return null;
	}

	@Override
	public Integer getUnmodifiedBallotCount() {
		return null;
	}

	@Override
	public int getBallotCount() {
		return ballotCount;
	}

	public void setBallotCount(int ballotCount) {
		this.ballotCount = ballotCount;
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
		SimpleBallotCount rhs = (SimpleBallotCount) obj;
		return new EqualsBuilder()
				.append(this.countCategory, rhs.countCategory)
				.append(this.ballotCount, rhs.ballotCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(countCategory)
				.append(ballotCount)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("countCategory", countCategory)
				.append("ballotCount", ballotCount)
				.toString();
	}
}
