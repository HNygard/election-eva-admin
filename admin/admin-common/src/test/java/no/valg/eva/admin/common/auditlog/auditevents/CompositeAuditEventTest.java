package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.testng.annotations.Test;


public class CompositeAuditEventTest {
	@Test
	public void testCreateMultipleEvents() throws Exception {
		CompositeAuditEvent.initializeForThread();
		UserData userData = new AuditLogTestsObjectMother().createUserData();
		CompositeAuditEvent.addAuditEvent(SimpleAuditEvent
				.from(userData)
				.ofType(AuditEventTypes.Create)
				.withObjectType(getClass())
				.withDetail("detail 1")
				.withOutcome(Outcome.Success)
				.withProcess(Process.CENTRAL_CONFIGURATION)
				.build());
		CompositeAuditEvent.addAuditEvent(SimpleAuditEvent
				.from(userData)
				.ofType(AuditEventTypes.Create)
				.withObjectType(getClass())
				.withDetail("detail 2")
				.withOutcome(Outcome.Success)
				.withProcess(Process.CENTRAL_CONFIGURATION)
				.build());
		List<AuditEvent> subEvents = new CompositeAuditEvent(userData).getAndClearAllEvents();
		assertThat(subEvents).hasSize(2);
		assertThat(subEvents.get(0).detail().equals("detail 1"));
		assertThat(subEvents.get(0).detail().equals("detail 2"));
	}
}
