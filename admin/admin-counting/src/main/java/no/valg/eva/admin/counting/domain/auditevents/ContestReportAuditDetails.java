package no.valg.eva.admin.counting.domain.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.counting.domain.model.ContestReport;

public class ContestReportAuditDetails {
	private ReportingUnitAuditDetails reportingUnitAuditDetails;
	private ContestAuditDetails contestAuditDetails;

	public ContestReportAuditDetails(ContestReport contestReport) {
		this.reportingUnitAuditDetails = new ReportingUnitAuditDetails(contestReport.getReportingUnit());
		this.contestAuditDetails = new ContestAuditDetails(contestReport.getContest());
	}

	public JsonObject toJsonObject() {
		return new JsonBuilder()
				.add("reportingUnit", reportingUnitAuditDetails.toJsonObject())
				.add("contest", contestAuditDetails.toJsonObject())
				.asJsonObject();
	}
}
