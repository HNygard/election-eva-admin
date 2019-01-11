package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.LOCALE_ID;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;

import org.testng.annotations.Test;


public class CountyAuditEventTest extends AbstractAuditEventTest {

	private static final String STATUS_NAME = "LOCAL_CONFIGURATION";
	private static final String STATUS_NAME_2 = "CENTRAL_CONFIGURATION";
	private County county = createCounty(1);
	private County countyForDelete = createCountyForDelete();

	private County createCountyForDelete() {
		return createCounty(0);
	}

	@Test
	public void toJson_forSuccessfulCreateCounty_isCorrect() {
		CountyAuditEvent auditEvent = createCountyAuditEvent(county, Create);
		checkEventJson(auditEvent, STATUS_NAME, 1);
	}

	@Test
	public void toJson_forSuccessfulDeleteMunicipality_isCorrect() {
		CountyAuditEvent auditEvent = createDeleteCountyAuditEvent(countyForDelete, Create);
		checkEventJson(auditEvent, STATUS_NAME_2, 0);
	}

	private void checkEventJson(CountyAuditEvent auditEvent, String statusName, int value) {
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("path", "950000.47.01"))
				.assertThat("$", hasEntry("name", "County"))
				.assertThat("$", hasEntry("locale", LOCALE_ID))
				.assertThat("$.countyStatus", hasEntry("id", value))
				.assertThat("$.countyStatus", hasEntry("name", statusName));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return MunicipalityAuditEvent.class;
	}

	protected CountyAuditEvent createCountyAuditEvent(County county, AuditEventTypes crudType) {
		CountyAuditEventForLocalConfiguration detail = new CountyAuditEventForLocalConfiguration(
				new AuditLogTestsObjectMother().createUserData(),
				county, crudType, Success, "detail");
		return detail;
	}

	protected CountyAuditEvent createDeleteCountyAuditEvent(County county, AuditEventTypes crudType) {
		return new CountyAuditEventForLocalConfiguration(new AuditLogTestsObjectMother().createUserData(), county, crudType, Success, "detail");
	}

	protected County createCounty(int id) {
		County county = new AuditLogTestsObjectMother().createCounty();
		CountyStatus countyStatus = new CountyStatus();
		countyStatus.setId(id);
		county.setCountyStatus(countyStatus);
		return county;
	}
}
