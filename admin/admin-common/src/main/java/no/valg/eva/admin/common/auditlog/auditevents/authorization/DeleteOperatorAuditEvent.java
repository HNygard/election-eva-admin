package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rbac.Operator;

import org.joda.time.DateTime;

public class DeleteOperatorAuditEvent extends AuditEvent {

	private final Operator operator;

	public DeleteOperatorAuditEvent(UserData userData, Operator operator, AuditEventTypes auditEventTypes, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventTypes, Process.AUTHORIZATION, outcome, detail);
		this.operator = operator;
	}

	@Override
	public Class objectType() {
		return Operator.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("personId", operator.getPersonId().getId());
		return builder.toJson();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Operator.class };
	}
}
