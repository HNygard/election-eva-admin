package no.valg.eva.admin.counting.domain.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;

class ReportingUnitAuditDetails {
	private String areaPath;
	private String electionPath;
	private ReportingUnitTypeId reportingUnitTypeId;
	private String name;

	public ReportingUnitAuditDetails(ReportingUnit reportingUnit) {
		areaPath = reportingUnit.getMvArea().getAreaPath();
		electionPath = reportingUnit.getMvElection().getElectionPath();
		reportingUnitTypeId = reportingUnit.reportingUnitTypeId();
		name = reportingUnit.getNameLine();
	}

	public JsonObject toJsonObject() {
		return new JsonBuilder()
				.add("areaPath", areaPath)
				.add("electionPath", electionPath)
				.add("reportingUnitType", reportingUnitTypeId.name())
				.add("name", name)
				.asJsonObject();
	}
}
