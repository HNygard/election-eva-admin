package no.valg.eva.admin.common.counting.model;

import static java.lang.String.format;
import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

public class Counts implements Serializable {
	public static final String EXPECTED_ONE_FINAL_COUNT_ID_REQUIRED_BUT_GOT_NONE = "expected one final count id required, but got none";
	public static final String EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_NONE = "expected two final count ids required, but got none";
	public static final String EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_ONLY_ONE = "expected two final count ids required, but got only one";
	public static final String COMBINATION_OF_QUALIFIER_S_AND_S_NOT_ALLOWED = "combination of qualifier <%s> and <%s> not allowed";
	public static final String ACTIVE_FINAL_COUNT_INDEX_WAS_NEGATIVE = "finalCountIndex was negative. FinalCountId <%s>";

	private final CountContext context;
    private final String electionName;
    private final String contestName;
    private final boolean penultimateRecount;
    private final String municipalityName;
    private List<ProtocolCount> protocolCounts;
    private PreliminaryCount preliminaryCount;
    private List<FinalCount> finalCounts;
    private List<FinalCount> countyFinalCounts;
    private int finalCountIndex;
    private int countyFinalCountIndex;
    private ProtocolAndPreliminaryCount protocolAndPreliminaryCount;
    private boolean tellekrets;

    public Counts(CountContext context, String electionName, String contestName, boolean penultimateRecount, String municipalityName, boolean tellekrets) {
		this.context = context;
        this.electionName = electionName;
        this.contestName = contestName;
        this.penultimateRecount = penultimateRecount;
        this.municipalityName = municipalityName;
        this.tellekrets = tellekrets;
	}

    public boolean municipalityCountsFinal() {
        return penultimateRecount;
    }
    
	/**
	 * @return true when all protocol counts has status APPROVED, false otherwise
	 */
	public boolean isProtocolCountsApproved() {
		for (ProtocolCount protocolCount : protocolCounts) {
			if (!protocolCount.isApproved()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true when all protocol counts has status NEW, false otherwise
	 */
	public boolean isProtocolCountsNew() {
		if (protocolCounts == null || protocolCounts.isEmpty()) {
			return false;
		}
		for (ProtocolCount protocolCount : protocolCounts) {
			if (!protocolCount.isNew()) {
				return false;
			}
		}
		return true;
	}

	public ProtocolCount getFirstProtocolCount() {
		if (protocolCounts == null || protocolCounts.isEmpty()) {
			return null;
		}
		return protocolCounts.get(0);
	}

	public void setFirstProtocolCount(ProtocolCount protocolCount) {
		if (protocolCounts == null) {
			protocolCounts = new ArrayList<>();
		}
		if (protocolCounts.isEmpty()) {
			protocolCounts.add(protocolCount);
		} else {
			protocolCounts.set(0, protocolCount);
		}
	}

	public PreliminaryCount getPreliminaryCount() {
		return preliminaryCount;
	}

	public void setPreliminaryCount(PreliminaryCount preliminaryCount) {
		this.preliminaryCount = preliminaryCount;
	}

	public ProtocolAndPreliminaryCount getProtocolAndPreliminaryCount() {
		return protocolAndPreliminaryCount;
	}

	public void setProtocolAndPreliminaryCount(ProtocolAndPreliminaryCount protocolAndPreliminaryCount) {
		this.protocolAndPreliminaryCount = protocolAndPreliminaryCount;
	}

	public void updateProtocolAndPreliminaryCount() {
		this.protocolAndPreliminaryCount = ProtocolAndPreliminaryCount.from(getFirstProtocolCount(), getPreliminaryCount());
	}

	public List<FinalCount> getFinalCounts() {
		return finalCounts;
	}

	public void setFinalCountsAndUpdateActiveCount(List<FinalCount> finalCounts) {
		this.finalCounts = finalCounts;
		setFinalCountIndex(getActiveIndex(finalCounts));
	}

	public List<FinalCount> getCountyFinalCounts() {
		return countyFinalCounts;
	}

	public void setCountyFinalCountsAndUpdateActiveCount(List<FinalCount> finalCounts) {
		this.countyFinalCounts = finalCounts;
		setCountyFinalCountIndex(getActiveIndex(countyFinalCounts));
	}

	private int getActiveIndex(List<FinalCount> counts) {
		DateTime lastModifiedDate = null;
		int result = -1;
		boolean approved = false;
		for (FinalCount count : counts) {
			if (count.isApproved()) {
				result = count.getIndex() - 1;
				approved = true;
			} else if (!approved && (lastModifiedDate == null || lastModifiedDate.isBefore(count.getModifiedDate()))) {
				result = count.getIndex() - 1;
				lastModifiedDate = count.getModifiedDate();
			}
		}
		return result;
	}

	public Integer getMarkOffCount() {
		if (hasProtocolCounts()) {
			int markOffCount = 0;
			for (ProtocolCount protocolCount : protocolCounts) {
				markOffCount += protocolCount.getDailyMarkOffCounts().getMarkOffCount();
			}
			return markOffCount;
		} else if (preliminaryCount.getDailyMarkOffCounts() != null) {
			return preliminaryCount.getDailyMarkOffCounts().getMarkOffCount();
		}
		return preliminaryCount.getMarkOffCount();
	}

	public boolean hasProtocolCounts() {
		return protocolCounts != null && !protocolCounts.isEmpty();
	}

	public boolean hasApprovedProtocolCounts() {
		if (!hasProtocolCounts()) {
			return false;
		}
		for (ProtocolCount protocolCount : protocolCounts) {
			if (protocolCount.isApproved()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPreliminaryCount() {
		return preliminaryCount != null;
	}

	public boolean hasApprovedPreliminaryCount() {
		return hasPreliminaryCount() && preliminaryCount.isApproved();
	}

	public boolean hasProtocolAndPreliminaryCount() {
		return protocolAndPreliminaryCount != null;
	}

	public boolean hasApprovedProtocolAndPreliminaryCount() {
		return hasProtocolAndPreliminaryCount() && protocolAndPreliminaryCount.isApproved();
	}

	public boolean hasFinalCounts() {
		return finalCounts != null && !finalCounts.isEmpty();
	}

	public boolean hasApprovedFinalCount() {
		if (!hasFinalCounts()) {
			return false;
		}
		for (FinalCount finalCount : finalCounts) {
			if (finalCount.isApproved()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasCountyFinalCounts() {
		return countyFinalCounts != null && !countyFinalCounts.isEmpty();
	}

	public boolean hasApprovedCountyFinalCount() {
		if (!hasCountyFinalCounts()) {
			return false;
		}
		for (FinalCount countyFinalCount : countyFinalCounts) {
			if (countyFinalCount.isApproved()) {
				return true;
			}
		}
		return false;
	}

	public int getOrdinaryBallotCountForProtocolCounts() {
		int ordinaryBallotCountForProtocolCounts = 0;
		for (ProtocolCount protocolCount : protocolCounts) {
			ordinaryBallotCountForProtocolCounts += protocolCount.getOrdinaryBallotCount();
		}
		return ordinaryBallotCountForProtocolCounts;
	}

	public int getOrdinaryBallotCountDifferenceBetween(CountQualifier qualifier1, final CountQualifier qualifier2, String... finalCountIds) {
		if (qualifier1 == CountQualifier.PROTOCOL && qualifier2 == CountQualifier.PRELIMINARY) {
			int ordinaryBallotCountForPreliminaryCount = preliminaryCount.getOrdinaryBallotCount();
			int ordinaryBallotCountForProtocolCounts = getOrdinaryBallotCountForProtocolCounts();
			return ordinaryBallotCountForPreliminaryCount - ordinaryBallotCountForProtocolCounts;
		}
		if (qualifier1 == CountQualifier.PRELIMINARY && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_ONE_FINAL_COUNT_ID_REQUIRED_BUT_GOT_NONE);
			}
			FinalCount selectedFinalCount = getFinalCountForMunicipalityOrCounty(finalCountIds[0]);
			int ordinaryBallotCountForFinalCount = selectedFinalCount.getOrdinaryBallotCount();
			int ordinaryBallotCountForPreliminaryCount = preliminaryCount.getOrdinaryBallotCount();
			return ordinaryBallotCountForFinalCount - ordinaryBallotCountForPreliminaryCount;
		}
		if (qualifier1 == CountQualifier.FINAL && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_NONE);
			}
			if (finalCountIds.length == 1) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_ONLY_ONE);
			}
			FinalCount selectedFinalCount1 = getFinalCount(finalCountIds[0]);
			FinalCount selectedFinalCount2 = getFinalCount(finalCountIds[1]);
			return selectedFinalCount2.getOrdinaryBallotCount() - selectedFinalCount1.getOrdinaryBallotCount();
		}
		throw new IllegalArgumentException(format(COMBINATION_OF_QUALIFIER_S_AND_S_NOT_ALLOWED, qualifier1, qualifier2));
	}

	public int getCountyOrdinaryBallotCountDifference(String finalCountId) {
		if (finalCountIndex < 0) {
			throw new IllegalArgumentException(format(ACTIVE_FINAL_COUNT_INDEX_WAS_NEGATIVE, finalCountId));
		}
		FinalCount selectedFinalCount1 = finalCounts.get(finalCountIndex);
		FinalCount selectedFinalCount2 = getCountyFinalCount(finalCountId);
		return selectedFinalCount2.getOrdinaryBallotCount() - selectedFinalCount1.getOrdinaryBallotCount();
	}

	private FinalCount getFinalCountForMunicipalityOrCounty(String finalCountId) {
		return municipalityCountsFinal() ? getFinalCount(finalCountId) : getCountyFinalCount(finalCountId);
	}

	private FinalCount getFinalCount(String finalCountId) {
		return getCount(finalCounts, finalCountId);
	}

	private FinalCount getCountyFinalCount(String finalCountId) {
		return getCount(countyFinalCounts, finalCountId);
	}

	private FinalCount getCount(List<FinalCount> counts, String id) {
		FinalCount selectedFinalCount = null;
		for (FinalCount finalCount : counts) {
			if (finalCount.getId().equals(id)) {
				selectedFinalCount = finalCount;
				break;
			}
		}
		if (selectedFinalCount == null) {
			throw new IllegalArgumentException(format("unknown final count id: <%s>", id));
		}
		return selectedFinalCount;
	}

	public int getBlankBallotCountForProtocolCounts() {
		int blankBallotCountForProtocolCounts = 0;
		for (ProtocolCount protocolCount : protocolCounts) {
			blankBallotCountForProtocolCounts += zeroIfNull(protocolCount.getBlankBallotCount());
		}
		return blankBallotCountForProtocolCounts;
	}

	public int getBlankBallotCountDifferenceBetween(CountQualifier qualifier1, final CountQualifier qualifier2, String... finalCountIds) {
		if (qualifier1 == CountQualifier.PROTOCOL && qualifier2 == CountQualifier.PRELIMINARY) {
			int blankBallotCountForPreliminaryCount = zeroIfNull(preliminaryCount.getBlankBallotCount());
			return blankBallotCountForPreliminaryCount - getBlankBallotCountForProtocolCounts();
		}
		if (qualifier1 == CountQualifier.PRELIMINARY && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_ONE_FINAL_COUNT_ID_REQUIRED_BUT_GOT_NONE);
			}
			FinalCount selectedFinalCount = getFinalCountForMunicipalityOrCounty(finalCountIds[0]);
			int blankBallotCountForFinalCount = zeroIfNull(selectedFinalCount.getBlankBallotCount());
			int blankBallotCountForPreliminaryCount = zeroIfNull(preliminaryCount.getBlankBallotCount());
			return blankBallotCountForFinalCount - blankBallotCountForPreliminaryCount;
		}
		if (qualifier1 == CountQualifier.FINAL && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_NONE);
			}
			if (finalCountIds.length == 1) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_ONLY_ONE);
			}
			FinalCount selectedFinalCount1 = getFinalCount(finalCountIds[0]);
			FinalCount selectedFinalCount2 = getFinalCount(finalCountIds[1]);
			return zeroIfNull(selectedFinalCount2.getBlankBallotCount()) - zeroIfNull(selectedFinalCount1.getBlankBallotCount());
		}
		throw new IllegalArgumentException(format(COMBINATION_OF_QUALIFIER_S_AND_S_NOT_ALLOWED, qualifier1, qualifier2));
	}

	public int getCountyBlankBallotCountDifference(String finalCountId) {
		if (finalCountIndex < 0) {
			throw new IllegalArgumentException(format(ACTIVE_FINAL_COUNT_INDEX_WAS_NEGATIVE, finalCountId));
		}
		FinalCount selectedFinalCount1 = finalCounts.get(finalCountIndex);
		FinalCount selectedFinalCount2 = getCountyFinalCount(finalCountId);
		return zeroIfNull(selectedFinalCount2.getBlankBallotCount()) - zeroIfNull(selectedFinalCount1.getBlankBallotCount());
	}

	public int getQuestionableBallotCountForProtocolCounts() {
		int questionableBallotCountForProtocolCounts = 0;
		for (ProtocolCount protocolCount : protocolCounts) {
			questionableBallotCountForProtocolCounts += zeroIfNull(protocolCount.getQuestionableBallotCount());
		}
		return questionableBallotCountForProtocolCounts;
	}

	public int getQuestionableBallotCountDifferenceBetween(CountQualifier qualifier1, final CountQualifier qualifier2, String... finalCountIds) {
		if (qualifier1 == CountQualifier.PROTOCOL && qualifier2 == CountQualifier.PRELIMINARY) {
			int questionableBallotCountForPreliminaryCount = zeroIfNull(preliminaryCount.getQuestionableBallotCount());
			return questionableBallotCountForPreliminaryCount - getQuestionableBallotCountForProtocolCounts();
		}
		if (qualifier1 == CountQualifier.PRELIMINARY && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_ONE_FINAL_COUNT_ID_REQUIRED_BUT_GOT_NONE);
			}
			FinalCount selectedFinalCount = getFinalCount(finalCountIds[0]);
			int rejectedBallotCountForFinalCount = selectedFinalCount.getTotalRejectedBallotCount();
			int questionableBallotCountForPreliminaryCount = zeroIfNull(preliminaryCount.getQuestionableBallotCount());
			return rejectedBallotCountForFinalCount - questionableBallotCountForPreliminaryCount;
		}
		if (qualifier1 == CountQualifier.FINAL && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_NONE);
			}
			if (finalCountIds.length == 1) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_ONLY_ONE);
			}
			FinalCount selectedFinalCount1 = getFinalCount(finalCountIds[0]);
			FinalCount selectedFinalCount2 = getFinalCount(finalCountIds[1]);
			return selectedFinalCount2.getTotalRejectedBallotCount() - selectedFinalCount1.getTotalRejectedBallotCount();
		}
		throw new IllegalArgumentException(format(COMBINATION_OF_QUALIFIER_S_AND_S_NOT_ALLOWED, qualifier1, qualifier2));
	}

	public int getCountyQuestionableBallotCountDifference(String finalCountId) {
		if (finalCountIndex < 0) {
			throw new IllegalArgumentException(format(ACTIVE_FINAL_COUNT_INDEX_WAS_NEGATIVE, finalCountId));
		}
		FinalCount selectedFinalCount1 = finalCounts.get(finalCountIndex);
		FinalCount selectedFinalCount2 = getCountyFinalCount(finalCountId);
		return selectedFinalCount2.getTotalRejectedBallotCount() - selectedFinalCount1.getTotalRejectedBallotCount();
	}

	public int getTotalBallotCountForProtocolCounts() {
		int totalBallotCountForProtocolCounts = 0;
		if (protocolCounts != null) {
			for (ProtocolCount protocolCount : protocolCounts) {
				totalBallotCountForProtocolCounts += protocolCount.getTotalBallotCount();
			}
		}
		return totalBallotCountForProtocolCounts;
	}

	public int getTotalBallotCountDifferenceBetween(CountQualifier qualifier1, final CountQualifier qualifier2, String... finalCountIds) {
		if (qualifier1 == CountQualifier.PROTOCOL && qualifier2 == CountQualifier.PRELIMINARY) {
			int totalBallotCountForPreliminaryCount = preliminaryCount.getTotalBallotCount();
			return totalBallotCountForPreliminaryCount - getTotalBallotCountForProtocolCounts();
		}
		if (qualifier1 == CountQualifier.PRELIMINARY && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_ONE_FINAL_COUNT_ID_REQUIRED_BUT_GOT_NONE);
			}
			FinalCount selectedFinalCount = getFinalCountForMunicipalityOrCounty(finalCountIds[0]);
			int totalBallotCountForFinalCount = selectedFinalCount.getTotalBallotCount();
			int totalBallotCountForPreliminaryCount = preliminaryCount.getTotalBallotCount();
			return totalBallotCountForFinalCount - totalBallotCountForPreliminaryCount;
		}
		if (qualifier1 == CountQualifier.FINAL && qualifier2 == CountQualifier.FINAL) {
			if (finalCountIds == null || finalCountIds.length == 0) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_NONE);
			}
			if (finalCountIds.length == 1) {
				throw new IllegalArgumentException(EXPECTED_TWO_FINAL_COUNT_IDS_REQUIRED_BUT_GOT_ONLY_ONE);
			}
			FinalCount selectedFinalCount1 = getFinalCount(finalCountIds[0]);
			FinalCount selectedFinalCount2 = getFinalCount(finalCountIds[1]);
			return selectedFinalCount2.getTotalBallotCount() - selectedFinalCount1.getTotalBallotCount();
		}
		throw new IllegalArgumentException(format(COMBINATION_OF_QUALIFIER_S_AND_S_NOT_ALLOWED, qualifier1, qualifier2));
	}

	public int getCountyTotalBallotCountDifference(String finalCountId) {
		if (finalCountIndex < 0) {
			throw new IllegalArgumentException(format(ACTIVE_FINAL_COUNT_INDEX_WAS_NEGATIVE, finalCountId));
		}
		FinalCount selectedFinalCount1 = finalCounts.get(finalCountIndex);
		FinalCount selectedFinalCount2 = getCountyFinalCount(finalCountId);
		return selectedFinalCount2.getTotalBallotCount() - selectedFinalCount1.getTotalBallotCount();
	}

	public boolean isPreliminaryCountApproved() {
		return getPreliminaryCount() != null && getPreliminaryCount().isApproved();
	}

	public boolean isPreliminaryCountNew() {
		return getPreliminaryCount() != null && getPreliminaryCount().isNew();
	}

	public String getElectionName() {
		return electionName;
	}

	public String getContestName() {
		return contestName;
	}

	public String getMunicipalityName() {
		return municipalityName;
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
		Counts rhs = (Counts) obj;
		return new EqualsBuilder()
				.append(this.context, rhs.context)
				.append(this.protocolCounts, rhs.protocolCounts)
				.append(this.preliminaryCount, rhs.preliminaryCount)
				.append(this.finalCounts, rhs.finalCounts)
				.append(this.countyFinalCounts, rhs.countyFinalCounts)
				.append(this.finalCountIndex, rhs.finalCountIndex)
				.append(this.countyFinalCountIndex, rhs.countyFinalCountIndex)
				.append(this.electionName, rhs.electionName)
				.append(this.contestName, rhs.contestName)
				.append(this.municipalityName, rhs.municipalityName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(context)
				.append(protocolCounts)
				.append(preliminaryCount)
				.append(finalCounts)
				.append(countyFinalCounts)
				.append(finalCountIndex)
				.append(countyFinalCountIndex)
				.append(electionName)
				.append(contestName)
				.append(municipalityName)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("context", context)
				.append("protocolCounts", protocolCounts)
				.append("preliminaryCount", preliminaryCount)
				.append("finalCounts", finalCounts)
				.append("countyFinalCounts", countyFinalCounts)
				.append("finalCountIndex", finalCountIndex)
				.append("countyFinalCountIndex", countyFinalCountIndex)
				.append("electionName", electionName)
				.append("contestName", contestName)
				.append("penulitimateRecount", penultimateRecount)
				.append("municipalityName", municipalityName)
				.toString();
	}

    public CountContext getContext() {
        return context;
    }

    public List<ProtocolCount> getProtocolCounts() {
        return protocolCounts;
    }

    public void setProtocolCounts(List<ProtocolCount> protocolCounts) {
        this.protocolCounts = protocolCounts;
    }

    public int getFinalCountIndex() {
        return finalCountIndex;
    }

    public void setFinalCountIndex(int finalCountIndex) {
        this.finalCountIndex = finalCountIndex;
    }

    public int getCountyFinalCountIndex() {
        return countyFinalCountIndex;
    }

    public void setCountyFinalCountIndex(int countyFinalCountIndex) {
        this.countyFinalCountIndex = countyFinalCountIndex;
    }

	public boolean isTellekrets() {
		return tellekrets;
	}
}
