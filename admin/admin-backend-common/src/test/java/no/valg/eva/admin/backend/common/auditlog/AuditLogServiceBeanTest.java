package no.valg.eva.admin.backend.common.auditlog;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuditLogServiceBeanTest {

	@Test
	public void addToAuditTrail_whenInvokedWithAnAuditEvent_logsToLogger() {
		Logger loggerMock = mock(Logger.class);
		AuditLogServiceBean auditLogService = new AuditLogServiceBean(loggerMock);
		FakeAuditEvent auditEvent = new FakeAuditEvent();
		auditLogService.addToAuditTrail(auditEvent);

		verify(loggerMock).info(isA(String.class));
	}

	@Test(expectedExceptions = NullPointerException.class)
    public void addToAuditTrail_whenInvokedWithNullAsAuditEvent_shallThrowNullPointerException() {
		AuditLogServiceBean auditLogService = new AuditLogServiceBean();
		auditLogService.addToAuditTrail(null);
	}

	private class FakeAuditEvent extends AuditEvent {
		FakeAuditEvent() {
			super("123451234", "950000", "valgansvarlig_kommune", AreaPath.from("950000.47.03.0301"), ElectionPath.from("950000.01"), getLocalHost(),
					DateTime.now(),
					SimpleAuditEventType.OperatorLoggedOut, Process.AUTHENTICATION, Outcome.Success, null);
		}

		@Override
		public Class objectType() {
			return null;
		}

		@Override
		public String toJson() {
			return null;
		}
	}

	private static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
