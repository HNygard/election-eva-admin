package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.Rode;

import org.joda.time.DateTime;

public class PollingStationAuditEvent extends AuditEvent {

	private final AreaPath areaPath;
	private final List<Rode> divisionList;

	public PollingStationAuditEvent(UserData userData, AreaPath areaPath, List<Rode> divisionList, AuditEventTypes crudType,
									Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.areaPath = areaPath;
		this.divisionList = divisionList;
	}

	@Override
	public String toJson() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", areaPath.path());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Rode division : divisionList) {
			JsonObjectBuilder divisionObjectBuilder = Json.createObjectBuilder();
			divisionObjectBuilder.add("id", division.getId());
			divisionObjectBuilder.add("fra", division.getFra());
			divisionObjectBuilder.add("til", division.getTil());
			divisionObjectBuilder.add("antallVelgere", division.getAntallVelgere());
			arrayBuilder.add(divisionObjectBuilder.build());
		}
		objectBuilder.add("pollingStations", arrayBuilder.build());
		return objectBuilder.toJson();
	}

	@Override
	public Class objectType() {
		return Rode.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { AreaPath.class, List.class };
	}
}
