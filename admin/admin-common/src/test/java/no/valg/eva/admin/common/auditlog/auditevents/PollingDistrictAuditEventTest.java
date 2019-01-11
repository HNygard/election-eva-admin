package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAsserter;


public class PollingDistrictAuditEventTest {

	private final PollingDistrict pollingDistrict = createPollingDistrict();

	@Test
	public void toJson_forSuccessfulCreatePollingDistrict_isCorrect() {
		PollingDistrictAuditEvent auditEvent = createPollingDistrictAuditEvent(pollingDistrict, Create);
		assertPollingDistrictWithoutDetails(auditEvent)
				.assertThat("$", hasEntry("technical", true))
				.assertThat("$", hasEntry("parent", true))
				.assertThat("$", hasEntry("child", true));
	}

	@Test
	public void toJson_forSuccessfulDeletePollingDistrict_isCorrect() {
		PollingDistrictAuditEvent auditEvent = createPollingDistrictAuditEvent(pollingDistrict, Delete);
		assertPollingDistrictWithoutDetails(auditEvent);
	}

	public JsonAsserter assertPollingDistrictWithoutDetails(PollingDistrictAuditEvent auditEvent) {
		return with(auditEvent.toJson())
				.assertThat("$", hasEntry("path", pollingDistrict.areaPath().path()))
				.assertThat("$", hasEntry("name", pollingDistrict.getName()));
	}

	protected PollingDistrictAuditEvent createPollingDistrictAuditEvent(PollingDistrict pollingDistrict, AuditEventTypes crudType) {
		return new PollingDistrictAuditEvent(
				new AuditLogTestsObjectMother().createUserData(),
				pollingDistrict, crudType, Success, "detail");
	}

	protected PollingDistrict createPollingDistrict() {
		PollingDistrict pollingDistrict = new AuditLogTestsObjectMother().createPollingDistrict();
		pollingDistrict.setParentPollingDistrict(true);
		pollingDistrict.setTechnicalPollingDistrict(true);
		pollingDistrict.setPollingDistrict(new PollingDistrict());
		return pollingDistrict;
	}
}
