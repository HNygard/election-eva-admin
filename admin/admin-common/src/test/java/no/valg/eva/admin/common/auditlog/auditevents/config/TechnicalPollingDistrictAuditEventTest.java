package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class TechnicalPollingDistrictAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		TechnicalPollingDistrictAuditEvent event = event(AuditEventTypes.Save, new TechnicalPollingDistrict(AreaPath.from("111111.22.33.4444")));

		assertThat(event.objectType()).isSameAs(TechnicalPollingDistrict.class);
	}

	@Test
	public void toJson() throws Exception {
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(AreaPath.from("111111.22.33.4444"));
		district.setId("010100");
		district.setName("My Polling place");
		TechnicalPollingDistrictAuditEvent event = event(AuditEventTypes.Save, district);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", district.getPath().path()))
				.assertThat("$", hasEntry("id", district.getId()))
				.assertThat("$", hasEntry("name", district.getName()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return TechnicalPollingDistrictAuditEvent.class;
	}

	private TechnicalPollingDistrictAuditEvent event(AuditEventTypes eventType, TechnicalPollingDistrict district) {
		return new TechnicalPollingDistrictAuditEvent(createMock(UserData.class), district, eventType, Outcome.Success, "");
	}

}
