package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.joda.time.DateTime;

/**
 * User id (uid) differs between the SAML session and the application server session, which indicates a session hijacking attempt. 
 */
public class HttpSessionHijackedAuditEvent extends AuditEvent {

	private String samlUid;

	public HttpSessionHijackedAuditEvent(UserData userData, String samlUid) {
		super(userData, new DateTime(), HttpSessionHijackedAuditEventType.HttpSessionHijacked, Process.AUTHENTICATION, Outcome.GenericError, null);
		this.samlUid = samlUid;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("samlUid", samlUid);

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return null;
	}

	public enum HttpSessionHijackedAuditEventType implements AuditEventType {
		HttpSessionHijacked
	}
}
