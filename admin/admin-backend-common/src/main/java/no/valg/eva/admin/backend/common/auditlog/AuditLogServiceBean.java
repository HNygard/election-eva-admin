package no.valg.eva.admin.backend.common.auditlog;

import static java.util.Objects.requireNonNull;

import javax.inject.Singleton;

import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;

import org.apache.log4j.Logger;

@Singleton
public class AuditLogServiceBean {
	private static final String AUDIT_LOGGER_NAME = "AuditLogger";
	private final Logger auditLog;

	public AuditLogServiceBean() {
		auditLog = Logger.getLogger(AUDIT_LOGGER_NAME);
	}

	AuditLogServiceBean(Logger logger) {
		auditLog = logger;
	}

	public void addToAuditTrail(AbstractAuditEvent auditEvent) {
		requireNonNull(auditEvent);

		for (AuditEvent subEvent : auditEvent.getAndClearAllEvents()) {
			String message = new AuditLogMessageFormatter(subEvent).buildMessage();
			auditLog.info(message);
		}
	}
}
