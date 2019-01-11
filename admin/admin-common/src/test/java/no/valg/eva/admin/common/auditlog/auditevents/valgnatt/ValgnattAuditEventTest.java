package no.valg.eva.admin.common.auditlog.auditevents.valgnatt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

public class ValgnattAuditEventTest {

	@Test
	public void testToJson() throws Exception {
		ValgnattAuditable valgnattAuditable = mock(ValgnattAuditable.class);
		ValgnattAuditEvent valgnattAuditEvent = new ValgnattAuditEvent(mock(UserData.class), DateTime.now(), AuditEventTypes.GenerateReport, Process.COUNTING,
				Outcome.Success, valgnattAuditable);
        valgnattAuditEvent.toJson();
        
        verify(valgnattAuditable, times(1)).toJson();
	}
}
