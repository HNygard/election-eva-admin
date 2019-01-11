package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.reportingUnit;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.reportingUnitJsonObject;
import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;

import no.valg.eva.admin.configuration.domain.model.ReportingUnit;

import org.testng.annotations.Test;

public class ReportingUnitAuditDetailsTest {

	@Test
	public void toJsonObject_givenReportingUnit_returnsCorrectJsonObject() throws Exception {
		ReportingUnit reportingUnit = reportingUnit();
		JsonObject jsonObject = new ReportingUnitAuditDetails(reportingUnit).toJsonObject();
		assertThat(jsonObject).isEqualTo(reportingUnitJsonObject());
	}
}
