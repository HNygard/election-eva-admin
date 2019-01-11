package no.valg.eva.admin.counting.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;

public class VoteCountDigest {
	private final CountCategory countCategory;
	private final AreaPath areaPath;
	private final CountQualifier countQualifier;
	private final AreaLevelEnum reportingUnitAreaLevel;
	private final String id;
	private final boolean approved;
	private final boolean toSettlement;
	private final boolean manualCount;
	private final boolean rejectedBallotsProcessed;
	private final int rejectedBallots;

	public VoteCountDigest(String countCategoryId, String areaPathString, String countQualifierId, int reportingUnitAreaLevelValue, String id,
						   boolean approved, boolean toSettlement, boolean manualCount, boolean rejectedBallotsProcessed, int rejectedBallots) {
		this.countCategory = CountCategory.fromId(countCategoryId);
		this.areaPath = AreaPath.from(areaPathString);
		this.countQualifier = CountQualifier.fromId(countQualifierId);
		this.reportingUnitAreaLevel = AreaLevelEnum.getLevel(reportingUnitAreaLevelValue);
		this.id = id;
		this.approved = approved;
		this.toSettlement = toSettlement;
		this.manualCount = manualCount;
		this.rejectedBallotsProcessed = rejectedBallotsProcessed;
		this.rejectedBallots = rejectedBallots;
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public CountQualifier getCountQualifier() {
		return countQualifier;
	}

	public AreaLevelEnum getReportingUnitAreaLevel() {
		return reportingUnitAreaLevel;
	}

	public String getId() {
		return id;
	}

	public boolean isApproved() {
		return approved;
	}

	public boolean isToSettlement() {
		return toSettlement;
	}

	public boolean isManualCount() {
		return manualCount;
	}

	public boolean isRejectedBallotsProcessed() {
		return rejectedBallotsProcessed;
	}

	public Integer getRejectedBallots() {
		return rejectedBallots;
	}
}
