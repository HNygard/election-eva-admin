package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class MarkerAvkryssningsmanntallKjortAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		MarkerAvkryssningsmanntallKjortAuditEvent event = event(AuditEventTypes.PartialUpdate, AREA_PATH_MUNICIPALITY, true);

		assertThat(event.objectType()).isSameAs(Municipality.class);
	}

	@Test
	public void toJson() throws Exception {
		MarkerAvkryssningsmanntallKjortAuditEvent event = event(AuditEventTypes.PartialUpdate, AREA_PATH_MUNICIPALITY, true);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", AREA_PATH_MUNICIPALITY.path()))
				.assertThat("$", hasEntry("avkrysningsmanntall_kjort", true));
	}

	protected Class<? extends AuditEvent> getAuditEventClass() {
		return MarkerAvkryssningsmanntallKjortAuditEvent.class;
	}

	private MarkerAvkryssningsmanntallKjortAuditEvent event(AuditEventTypes eventType, AreaPath areaPath, boolean kjort) {
		return new MarkerAvkryssningsmanntallKjortAuditEvent(createMock(UserData.class), areaPath, kjort, eventType, Outcome.Success, "");
	}

}
