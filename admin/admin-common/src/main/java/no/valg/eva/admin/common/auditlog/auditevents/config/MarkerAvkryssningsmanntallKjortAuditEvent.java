package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.VOTING;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import org.joda.time.DateTime;

public class MarkerAvkryssningsmanntallKjortAuditEvent extends AuditEvent {

	private final AreaPath areaPath;
	private final boolean kjort;

	public MarkerAvkryssningsmanntallKjortAuditEvent(UserData userData, AreaPath areaPath, boolean kjort, AuditEventTypes crudType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), crudType, VOTING, outcome, detail);
		this.areaPath = areaPath;
		this.kjort = kjort;
	}

	@Override
	public Class objectType() {
		return Municipality.class;
	}

	@Override
	public String toJson() {
		return jonBuilder().toJson();
	}

	protected JsonBuilder jonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", areaPath.path());
		objectBuilder.add("avkrysningsmanntall_kjort", kjort);
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { AreaPath.class, boolean.class };
	}

}
