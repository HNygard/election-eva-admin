package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;

import org.joda.time.DateTime;

public class ElectionCardConfigAuditEvent extends AuditEvent {

	private final ElectionCardConfig card;

	public ElectionCardConfigAuditEvent(UserData userData, ElectionCardConfig card, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.card = card;
	}

	@Override
	public Class objectType() {
		return ElectionCardConfig.class;
	}

	@Override
	public String toJson() {
		JsonBuilder json = new JsonBuilder();
		json.add("reportingUnit", getReportingUnit().asJsonObject());
		json.add("infoText", card.getInfoText());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (ElectionDayPollingPlace place : card.getPlaces()) {
			JsonBuilder placeBuilder = new JsonBuilder();
			placeBuilder.add("pk", place.getPk());
			placeBuilder.add("id", place.getId());
			placeBuilder.add("infoText", place.getInfoText());
			arrayBuilder.add(placeBuilder.asJsonObject());
		}
		json.add("places", arrayBuilder.build());
		return json.toJson();
	}

	private JsonBuilder getReportingUnit() {
		JsonBuilder unitJson = new JsonBuilder();
		unitJson.add("pk", card.getReportingUnit().getPk());
		unitJson.add("areaPath", card.getReportingUnit().getAreaPath().path());
		unitJson.add("type", card.getReportingUnit().getType().name());
		unitJson.add("address", card.getReportingUnit().getAddress());
		unitJson.add("postalCode", card.getReportingUnit().getPostalCode());
		unitJson.add("postTown", card.getReportingUnit().getPostTown());
		return unitJson;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ElectionCardConfig.class };
	}
}
