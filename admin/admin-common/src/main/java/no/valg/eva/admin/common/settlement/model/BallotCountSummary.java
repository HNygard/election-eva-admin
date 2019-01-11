package no.valg.eva.admin.common.settlement.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BallotCountSummary<T extends BallotCount> implements Serializable {
	private BallotInfo ballotInfo;
	private List<T> ballotCounts;

	public BallotCountSummary(BallotInfo ballotInfo) {
		this.ballotInfo = ballotInfo;
		this.ballotCounts = new ArrayList<>();
	}

	public BallotCountSummary(BallotInfo ballotInfo, List<T> ballotCounts) {
		this.ballotInfo = ballotInfo;
		this.ballotCounts = ballotCounts;
	}

	public BallotInfo getBallotInfo() {
		return ballotInfo;
	}

	public List<T> getBallotCounts() {
		return ballotCounts;
	}

	public T getBallotCount(CountCategory countCategory) {
		for (T ballotCount : ballotCounts) {
			if (ballotCount.getCountCategory() == countCategory) {
				return ballotCount;
			}
		}
		return null;
	}

	public void addBallotCount(T ballotCount) {
		ballotCounts.add(ballotCount);
	}

	public BallotCount getTotalBallotCount() {
		if (ballotCounts.isEmpty()) {
			return null;
		}
		if (ballotCounts.get(0) instanceof SimpleBallotCount) {
			int totalBallotCount = 0;
			for (BallotCount ballotCount : ballotCounts) {
				totalBallotCount += ballotCount.getBallotCount();
			}
			return new SimpleBallotCount(null, totalBallotCount);
		}
		int totalModifiedBallotCount = 0;
		int totalUnmodifiedBallotCount = 0;
		for (BallotCount ballotCount : ballotCounts) {
			totalModifiedBallotCount += ballotCount.getModifiedBallotCount();
			totalUnmodifiedBallotCount += ballotCount.getUnmodifiedBallotCount();
		}
		return new SplitBallotCount(null, totalModifiedBallotCount, totalUnmodifiedBallotCount);
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
		BallotCountSummary rhs = (BallotCountSummary) obj;
		return new EqualsBuilder()
				.append(this.ballotInfo, rhs.ballotInfo)
				.append(this.ballotCounts, rhs.ballotCounts)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(ballotInfo)
				.append(ballotCounts)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("ballotInfo", ballotInfo)
				.append("ballotCounts", ballotCounts)
				.toString();
	}
}
