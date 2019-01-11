package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Borough;

import org.joda.time.DateTime;

public class BoroughAuditEvent extends AuditEvent {
	private final Borough borough;

	public BoroughAuditEvent(UserData userData, Borough borough, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, CENTRAL_CONFIGURATION, outcome, detail);
		this.borough = borough;
	}

	@Override
	public Class objectType() {
		return Borough.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("path", borough.areaPath().path());
		builder.add("name", borough.getName());
		builder.add("representsWholeMunicipality", borough.isMunicipality1());
		return builder.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Borough.class };
	}

}
