package no.valg.eva.admin.backend.common.auditlog;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.Outcome.GenericError;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.AccessDeniedInBackend;
import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.OperatorLoggedOut;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

public class AuditLogMessageFormatterTest {

	private final String hostAddress = getLocalHost().getHostAddress();
	
	private final DateTime timestamp = new DateTime(2014, 12, 24, 17, 0, DateTimeZone.forOffsetHours(1));
	

	private final Class objectType = getClass();

	@Test
	public void buildMessage_givenSimpleAuditEvent_formatsCorrectly() {
		AuditLogMessageFormatter formatter = new AuditLogMessageFormatter(createSimpleAuditEventWithAllProperties());
		String message = formatter.buildMessage();

		assertThat(message).isEqualTo("time=\"2014-12-24 17:00:00,000 +0100\", client=" + hostAddress + ", electionEvent=950000, process=AUTHENTICATION, "
				+ "objectType=AuditLogMessageFormatterTest, "
				+ "eventType=OperatorSelectedRole, outcome=Success, uid=01010198765, role=valgansvarlig_kommune, roleAreaPath=950000.47.03.0301, "
				+ "roleElectionPath=950000.01.01.030100");
	}

	@Test
	public void buildMessage_givenSimpleAuditEventWithoutOptionalProperties_formatsCorrectly() {
		AuditLogMessageFormatter formatter = new AuditLogMessageFormatter(createSimpleAuditEventWithoutOptionalProperties());
		String message = formatter.buildMessage();

		assertThat(message).isEqualTo(
				"time=\"2014-12-24 17:00:00,000 +0100\", client=" + hostAddress
						+ ", process=AUTHENTICATION, objectType=AuditLogMessageFormatterTest, eventType=OperatorLoggedOut, "
						+ "outcome=Success, uid=01010198765");
	}

	@Test
	public void buildMessage_givenSimpleAuditEventWithError_formatsCorrectlyWithEscapedQuotes() {
		AuditLogMessageFormatter formatter = new AuditLogMessageFormatter(createSimpleAuditEventWithError());
		String message = formatter.buildMessage();

		assertThat(message).isEqualTo("time=\"2014-12-24 17:00:00,000 +0100\", client=" + hostAddress
				+ ", process=AUTHORIZATION, objectType=AuditLogMessageFormatterTest, eventType=AccessDeniedInBackend, "
				+ "outcome=GenericError, uid=01010198765, detail=\"Something bad\"");
	}

	private AuditEvent createSimpleAuditEventWithAllProperties() {
		return new SimpleAuditEvent("01010198765",
				"950000",
				"valgansvarlig_kommune",
				AreaPath.from("950000.47.03.0301"),
				ElectionPath.from("950000.01.01.030100"),
				getLocalHost(),
				timestamp,
				SimpleAuditEventType.OperatorSelectedRole,
				CENTRAL_CONFIGURATION, Success,
				null, objectType);
	}

	private AuditEvent createSimpleAuditEventWithoutOptionalProperties() {
		return new SimpleAuditEvent(
				"01010198765",
				null,
				null,
				null,
				null,
				getLocalHost(),
				timestamp,
				OperatorLoggedOut,
				CENTRAL_CONFIGURATION,
				Success,
				null, objectType);
	}

	private AuditEvent createSimpleAuditEventWithError() {
		return new SimpleAuditEvent(
				"01010198765",
				null,
				null,
				null,
				null,
				getLocalHost(),
				timestamp,
				AccessDeniedInBackend,
				CENTRAL_CONFIGURATION,
				GenericError,
				"Something bad", objectType);
	}

	private InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

}
