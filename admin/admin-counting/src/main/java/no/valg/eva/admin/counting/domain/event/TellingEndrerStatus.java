package no.valg.eva.admin.counting.domain.event;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;

/**
 * Domain event som kastet når en foreløpig eller endelig telling godkjennes.
 */
public class TellingEndrerStatus {
	private final AreaPath areaPath;

	private final CountQualifier countQualifier;
	private final ElectionPath contestPath;
	private final CountCategory countCategory;
	private final ReportingUnitTypeId reportingUnitTypeId;
	
	public TellingEndrerStatus(
			AreaPath areaPath, CountQualifier countQualifier, ElectionPath contestPath, CountCategory category, ReportingUnitTypeId reportingUnitTypeId) {
		if (countQualifier == null || countQualifier == PROTOCOL) {
			throw new IllegalArgumentException("CountQualifier må være enten PRELIMINARY eller FINAL.");
		}
		contestPath.assertContestLevel();
		if (reportingUnitTypeId == null) {
			throw new IllegalArgumentException("ReportingUnitType må være satt.");
		}
		
		this.areaPath = areaPath;
		this.countQualifier = countQualifier;
		this.contestPath = contestPath;
		this.countCategory = category;
		this.reportingUnitTypeId = reportingUnitTypeId;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public CountQualifier getCountQualifier() {
		return countQualifier;
	}

	public ElectionPath getContestPath() {
		return contestPath;
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}

	public ReportingUnitTypeId getReportingUnitTypeId() {
		return reportingUnitTypeId;
	}

	@Override
	public String toString() {
		return "TellingEndrerStatus{areaPath=" + areaPath + ", countQualifier=" + countQualifier
				+ ", contestPath=" + contestPath + ", countCategory=" + countCategory + ", reportingUnitTypeId=" + reportingUnitTypeId + "}";
	}
}
