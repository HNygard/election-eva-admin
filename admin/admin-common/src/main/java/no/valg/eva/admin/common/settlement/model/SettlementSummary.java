package no.valg.eva.admin.common.settlement.model;

import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SettlementSummary implements Serializable {
	private final List<CountCategory> countCategories;
	private final List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries;
	private final BallotCountSummary<SimpleBallotCount> blankBallotCountSummary;
	private final List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries;

	public SettlementSummary(
			List<CountCategory> countCategories,
			List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries,
			BallotCountSummary<SimpleBallotCount> blankBallotCountSummary,
			List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries) {
		this.countCategories = countCategories.stream()
				.sorted((o1, o2) -> o1.compareTo(o2))
				.collect(Collectors.toList());
		this.ordinaryBallotCountSummaries = ordinaryBallotCountSummaries;
		this.blankBallotCountSummary = blankBallotCountSummary;
		this.rejectedBallotCountSummaries = rejectedBallotCountSummaries;
	}

	public List<CountCategory> getCountCategories() {
		return countCategories;
	}

	public List<BallotCountSummary> getBallotCountSummaries() {
		return new AbstractList<BallotCountSummary>() {
			@Override
			public BallotCountSummary get(int index) {
				if (index < 0) {
					throw new IllegalArgumentException(format("Negative index: <%d>", index));
				}
				if (index < ordinaryBallotCountSummaries.size()) {
					return ordinaryBallotCountSummaries.get(index);
				}
				int offset = ordinaryBallotCountSummaries.size();
				if (index == offset) {
					return getSumOfOrdinaryBallotCountSummaries();
				}
				offset++;
				if (index == offset) {
					return blankBallotCountSummary;
				}
				offset++;
				if (index < rejectedBallotCountSummaries.size() + offset) {
					return rejectedBallotCountSummaries.get(index - offset);
				}
				offset += rejectedBallotCountSummaries.size();
				if (index == offset) {
					return getSumOfRejectedBallotCountSummaries();
				}
				offset++;
				if (index == offset) {
					return getTotalBallotCountSummary();
				}
				throw new IndexOutOfBoundsException(format("Index: <%d>; size: <%d>", index, size()));
			}

			@Override
			public int size() {
				return ordinaryBallotCountSummaries.size() + 2 + rejectedBallotCountSummaries.size() + 2;
			}
		};
	}

	public BallotCountSummary<SplitBallotCount> getSumOfOrdinaryBallotCountSummaries() {
		LinkedHashMap<CountCategory, SplitBallotCount> sumOfOrdinaryBallotCountsMap = new LinkedHashMap<>();
		for (BallotCountSummary<SplitBallotCount> ordinaryBallotCountSummary : ordinaryBallotCountSummaries) {
			List<SplitBallotCount> ballotCounts = ordinaryBallotCountSummary.getBallotCounts();
			for (SplitBallotCount ballotCount : ballotCounts) {
				CountCategory countCategory = ballotCount.getCountCategory();
				if (!sumOfOrdinaryBallotCountsMap.containsKey(countCategory)) {
					SplitBallotCount ordinaryBallotCount = new SplitBallotCount(countCategory, ballotCount.getModifiedBallotCount(),
							ballotCount.getUnmodifiedBallotCount());
					sumOfOrdinaryBallotCountsMap.put(countCategory, ordinaryBallotCount);
				} else {
					SplitBallotCount ordinaryBallotCount = sumOfOrdinaryBallotCountsMap.get(countCategory);
					ordinaryBallotCount.setModifiedBallotCount(ordinaryBallotCount.getModifiedBallotCount() + ballotCount.getModifiedBallotCount());
					ordinaryBallotCount.setUnmodifiedBallotCount(ordinaryBallotCount.getUnmodifiedBallotCount() + ballotCount.getUnmodifiedBallotCount());
				}
			}
		}
		BallotInfo ballotInfo = new BallotInfo(null, "@count.ballot.totalOrdinary");
		return new BallotCountSummary<>(ballotInfo, buildArrayListFromMapValues(sumOfOrdinaryBallotCountsMap));
	}

	public BallotCountSummary<SimpleBallotCount> getBlankBallotCountSummary() {
		return blankBallotCountSummary;
	}

	public BallotCountSummary<SimpleBallotCount> getSumOfRejectedBallotCountSummaries() {
		LinkedHashMap<CountCategory, SimpleBallotCount> sumOfRejectedBallotCountsMap = new LinkedHashMap<>();
		for (BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary : rejectedBallotCountSummaries) {
			List<SimpleBallotCount> ballotCounts = rejectedBallotCountSummary.getBallotCounts();
			for (SimpleBallotCount ballotCount : ballotCounts) {
				CountCategory countCategory = ballotCount.getCountCategory();
				if (!sumOfRejectedBallotCountsMap.containsKey(countCategory)) {
					SimpleBallotCount rejectedBallotCount = new SimpleBallotCount(countCategory, ballotCount.getBallotCount());
					sumOfRejectedBallotCountsMap.put(countCategory, rejectedBallotCount);
				} else {
					SimpleBallotCount rejectedBallotCount = sumOfRejectedBallotCountsMap.get(countCategory);
					rejectedBallotCount.setBallotCount(rejectedBallotCount.getBallotCount() + ballotCount.getBallotCount());
				}
			}
		}
		BallotInfo ballotInfo = new BallotInfo(null, "@count.ballot.totalRejected");
		return new BallotCountSummary<>(ballotInfo, buildArrayListFromMapValues(sumOfRejectedBallotCountsMap));
	}

	public BallotCountSummary<SimpleBallotCount> getTotalBallotCountSummary() {
		LinkedHashMap<CountCategory, SimpleBallotCount> totalBallotCountsMap = new LinkedHashMap<>();
		BallotCountSummary<SplitBallotCount> sumOfOrdinaryBallotCountSummaries = getSumOfOrdinaryBallotCountSummaries();
		for (SplitBallotCount ballotCount : sumOfOrdinaryBallotCountSummaries.getBallotCounts()) {
			CountCategory countCategory = ballotCount.getCountCategory();
			SimpleBallotCount totalBallotCount = new SimpleBallotCount(countCategory, ballotCount.getBallotCount());
			totalBallotCountsMap.put(countCategory, totalBallotCount);
		}
		for (SimpleBallotCount ballotCount : blankBallotCountSummary.getBallotCounts()) {
			CountCategory countCategory = ballotCount.getCountCategory();
			SimpleBallotCount totalBallotCount = totalBallotCountsMap.get(countCategory);
			totalBallotCount.setBallotCount(totalBallotCount.getBallotCount() + ballotCount.getBallotCount());
		}
		BallotCountSummary<SimpleBallotCount> sumOfRejectedBallotCountSummaries = getSumOfRejectedBallotCountSummaries();
		for (SimpleBallotCount ballotCount : sumOfRejectedBallotCountSummaries.getBallotCounts()) {
			CountCategory countCategory = ballotCount.getCountCategory();
			SimpleBallotCount totalBallotCount = totalBallotCountsMap.get(countCategory);
			totalBallotCount.setBallotCount(totalBallotCount.getBallotCount() + ballotCount.getBallotCount());
		}
		BallotInfo ballotInfo = new BallotInfo(null, "@count.ballot.total");
		return new BallotCountSummary<>(ballotInfo, buildArrayListFromMapValues(totalBallotCountsMap));
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
		SettlementSummary rhs = (SettlementSummary) obj;
		return new EqualsBuilder()
				.append(this.countCategories, rhs.countCategories)
				.append(this.ordinaryBallotCountSummaries, rhs.ordinaryBallotCountSummaries)
				.append(this.blankBallotCountSummary, rhs.blankBallotCountSummary)
				.append(this.rejectedBallotCountSummaries, rhs.rejectedBallotCountSummaries)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(countCategories)
				.append(ordinaryBallotCountSummaries)
				.append(blankBallotCountSummary)
				.append(rejectedBallotCountSummaries)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("countCategories", countCategories)
				.append("ordinaryBallotCountSummaries", ordinaryBallotCountSummaries)
				.append("blankBallotCountSummary", blankBallotCountSummary)
				.append("rejectedBallotCountSummaries", rejectedBallotCountSummaries)
				.toString();
	}

	private <T> List<T> buildArrayListFromMapValues(Map<?, T> map) {
		return new ArrayList<>(map.values());
	}
}
