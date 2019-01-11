package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;

public class ParentPollingDistrictAuditEvent extends PollingDistrictAuditEvent<ParentPollingDistrict> {

	public ParentPollingDistrictAuditEvent(UserData userData, ParentPollingDistrict district, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, district, crudType, outcome, detail);
	}

	@Override
	protected JsonBuilder placeJsonBuilder() {
		JsonBuilder objectBuilder = super.placeJsonBuilder();
		if (!eventType().equals(Delete)) {
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (RegularPollingDistrict child : getPlace().getChildren()) {
				JsonBuilder ohBuilder = new JsonBuilder();
				ohBuilder.add("pk", child.getPk());
				ohBuilder.add("id", child.getId());
				ohBuilder.add("name", child.getName());
				ohBuilder.add("path", child.getPath().path());
				arrayBuilder.add(ohBuilder.asJsonObject());
			}
			objectBuilder.add("children", arrayBuilder.build());
		}
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ParentPollingDistrict.class };
	}
}
