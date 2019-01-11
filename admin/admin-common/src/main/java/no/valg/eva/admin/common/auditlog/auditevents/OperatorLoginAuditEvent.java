package no.valg.eva.admin.common.auditlog.auditevents;

import static java.util.Objects.requireNonNull;

import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.joda.time.DateTime;

/**
 * This class does not support the {@link no.valg.eva.admin.common.auditlog.AuditLog} annotation.
 */
public class OperatorLoginAuditEvent extends AuditEvent {
	private final SecurityLevel securityLevel;

	public OperatorLoginAuditEvent(UserData userData, Outcome outcome) {
		super(userData, new DateTime(), UserLoggedInAuditEventType.OperatorLoggedIn, Process.AUTHENTICATION, outcome, null);
		this.securityLevel = requireNonNull(userData.getSecurityLevelEnum(), "SecurityLevel is required");
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("securityLevel", securityLevel.getLevel());

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return null;
	}

	public static enum UserLoggedInAuditEventType implements AuditEventType {
		OperatorLoggedIn
	}
}
