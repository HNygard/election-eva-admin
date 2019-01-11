package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rbac.service.ContactInfo;

import org.joda.time.DateTime;

public class ContactInfoChangedAuditEvent extends AuditEvent {

	private final ContactInfo contactInfo;

	public ContactInfoChangedAuditEvent(UserData userData, ContactInfo contactInfo, AuditEventTypes auditEventTypes, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventTypes, Process.AUTHORIZATION, outcome, detail);
		this.contactInfo = contactInfo;
	}

	@Override
	public Class objectType() {
		return ContactInfo.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("phone", contactInfo.getPhone());
		builder.add("email", contactInfo.getEmail());
		return builder.toJson();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ContactInfo.class };
	}
}
