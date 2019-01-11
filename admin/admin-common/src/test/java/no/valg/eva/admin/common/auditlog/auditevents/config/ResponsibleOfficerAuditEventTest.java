package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ResponsibleOfficerAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ResponsibleOfficerAuditEvent event = event(AuditEventTypes.Save, new ResponsibleOfficer());

		assertThat(event.objectType()).isSameAs(ResponsibleOfficer.class);
	}

	@Test
	public void toJson_withDelete_verifyJson() throws Exception {
		ResponsibleOfficer officer = officer();
		ResponsibleOfficerAuditEvent event = event(AuditEventTypes.Delete, officer);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("areaPath", "111111"))
				.assertThat("$", hasEntry("firstName", officer.getFirstName()))
				.assertThat("$", hasEntry("middleName", officer.getMiddleName()))
				.assertThat("$", hasEntry("lastName", officer.getLastName()))
				.assertThat("$", hasEntry("responsibility", officer.getResponsibilityId().getId()))
				.assertThat("$", hasEntry("pk", 100));
	}

	@Test
	public void toJson_withUpdate_verifyJson() throws Exception {
		ResponsibleOfficer officer = officer();
		ResponsibleOfficerAuditEvent event = event(AuditEventTypes.Save, officer);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("areaPath", "111111"))
				.assertThat("$", hasEntry("firstName", officer.getFirstName()))
				.assertThat("$", hasEntry("middleName", officer.getMiddleName()))
				.assertThat("$", hasEntry("lastName", officer.getLastName()))
				.assertThat("$", hasEntry("responsibility", officer.getResponsibilityId().getId()))
				.assertThat("$", hasEntry("displayOrder", officer.getDisplayOrder()))
				.assertThat("$", hasEntry("address", officer.getAddress()))
				.assertThat("$", hasEntry("postalCode", officer.getPostalCode()))
				.assertThat("$", hasEntry("postalTown", officer.getPostalTown()))
				.assertThat("$", hasEntry("email", officer.getEmail()))
				.assertThat("$", hasEntry("tlf", officer.getTlf()));
	}

	private ResponsibleOfficer officer() {
		ResponsibleOfficer officer = new ResponsibleOfficer();
		officer.setPk(100L);
		officer.setAreaPath(AreaPath.from("111111"));
		officer.setFirstName("Test");
		officer.setLastName("Testesen");
		officer.setResponsibilityId(ResponsibilityId.LEDER);
		return officer;
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ResponsibleOfficerAuditEvent.class;
	}

	private ResponsibleOfficerAuditEvent event(AuditEventTypes eventType, ResponsibleOfficer responsibleOfficer) {
		return new ResponsibleOfficerAuditEvent(createMock(UserData.class), responsibleOfficer, eventType, Outcome.Success, "");
	}

}

