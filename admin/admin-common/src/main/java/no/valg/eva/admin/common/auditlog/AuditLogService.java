package no.valg.eva.admin.common.auditlog;

import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;

/**
 * The audit log service allows audit events to be added to the audit trail.
 */
public interface AuditLogService {

	/**
	 * Adds an audit event to the audit trail (i.e. logs to the audit log).
	 */
	void addToAuditTrail(AbstractAuditEvent auditEvent);
}
