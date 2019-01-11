package no.valg.eva.admin.common.auditlog.auditevents;

import static java.util.Collections.emptyList;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

public class PollingDistrictParentAuditEvent extends PollingDistrictAuditEvent {
	private List<PollingDistrict> childPollingDistricts = emptyList();
	private List<String> childPollingDistrictsIds = emptyList();

	public PollingDistrictParentAuditEvent(UserData userData, PollingDistrict pollingDistrict, List<PollingDistrict> childPollingDistricts,
			AuditEventTypes crudType,
			Outcome outcome, String detail) {
		super(userData, pollingDistrict, crudType, outcome, detail);
		this.childPollingDistricts = childPollingDistricts;
	}

	public PollingDistrictParentAuditEvent(UserData userData, PollingDistrict pollingDistrict,
			AuditEventTypes crudType,
			Outcome outcome, String detail) {
		super(userData, pollingDistrict, crudType, outcome, detail);
	}

	public PollingDistrictParentAuditEvent(UserData userData, List<String> childPollingDistrictIds, PollingDistrict pollingDistrict,
			AuditEventTypes crudType,
			Outcome outcome, String detail) {
		super(userData, pollingDistrict, crudType, outcome, detail);
		this.childPollingDistrictsIds = childPollingDistrictIds;
	}

	@Override
	public String toJson() {
		JsonBuilder objectBuilder = pollingDistrictJsonBuilder();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (PollingDistrict childPollingDistrict : childPollingDistricts) {
			JsonObjectBuilder childPollingDistrictBuilder = Json.createObjectBuilder();
			childPollingDistrictBuilder.add("id", childPollingDistrict.getId());
			childPollingDistrictBuilder.add("name", childPollingDistrict.getName());
			arrayBuilder.add(childPollingDistrictBuilder.build());
		}

		for (String childPollingDistrictId : childPollingDistrictsIds) {
			JsonObjectBuilder childPollingDistrictBuilder = Json.createObjectBuilder();
			childPollingDistrictBuilder.add("id", childPollingDistrictId);
			arrayBuilder.add(childPollingDistrictBuilder.build());
		}

		objectBuilder.add("childPollingDistricts", arrayBuilder.build());
		return objectBuilder.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		switch ((AuditEventTypes) auditEventType) {
		case AddChildren:
		case RemoveChildren:
			return new Class[] { List.class, PollingDistrict.class };
		case Create:
		case Update:
		case Delete:
			return new Class[] { PollingDistrict.class };
		case CreateParent:
			return new Class[] { PollingDistrict.class, List.class };
		default:
			throw new UnsupportedOperationException();
		}
	}
}
