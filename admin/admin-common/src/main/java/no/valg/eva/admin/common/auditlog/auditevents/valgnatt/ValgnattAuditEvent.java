package no.valg.eva.admin.common.auditlog.auditevents.valgnatt;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;

import org.joda.time.DateTime;

/**
 * Audit event for valgnatt reports.  AuditLogInterceptor is not supported - valgnatt auditing requires explicit logging.
 */
public class ValgnattAuditEvent extends AuditEvent {

	private final ValgnattAuditable valgnattAuditable;

	public ValgnattAuditEvent(UserData userData, DateTime dateTime, AuditEventType crudType, Process process,
                              Outcome outcome, ValgnattAuditable valgnattAuditable) {
		super(userData, dateTime, crudType, process, outcome, null);
		this.valgnattAuditable = valgnattAuditable;
	}

	@Override
	public Class objectType() {
		return ValgnattAuditable.class;
	}

	@Override
	public String toJson() {
		return valgnattAuditable.toJson();
	}
}
