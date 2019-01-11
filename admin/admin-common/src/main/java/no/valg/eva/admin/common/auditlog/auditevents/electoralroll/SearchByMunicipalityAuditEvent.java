package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.DateTime;

public class SearchByMunicipalityAuditEvent extends AuditEvent {

	private final String municipalityId;

	public SearchByMunicipalityAuditEvent(UserData userData, String municipalityId, AuditEventTypes auditEventTypes, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), auditEventTypes, Process.ELECTORAL_ROLL, outcome, detail);
		this.municipalityId = municipalityId;
	}

	@Override
	public Class objectType() {
		return Voter.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("municipalityId", municipalityId);
		return builder.toJson();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { String.class };
	}
}
