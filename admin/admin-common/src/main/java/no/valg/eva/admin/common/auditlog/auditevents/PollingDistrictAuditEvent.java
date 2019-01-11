package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.joda.time.DateTime;

public class PollingDistrictAuditEvent extends AuditEvent {
	protected final PollingDistrict pollingDistrict;

	public PollingDistrictAuditEvent(UserData userData, PollingDistrict pollingDistrict, AuditEventTypes crudType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.pollingDistrict = pollingDistrict;
	}

	public Class objectType() {
		return PollingDistrict.class;
	}

	@Override
	public String toJson() {
		return pollingDistrictJsonBuilder().toJson();
	}

	protected JsonBuilder pollingDistrictJsonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", pollingDistrict.areaPath().path());
		objectBuilder.add("name", pollingDistrict.getName());
		if (!(eventType().equals(Delete) || (eventType().equals(AuditEventTypes.ImportDistrictsChangesDelete)))) {
			objectBuilder.add("parent", pollingDistrict.isParentPollingDistrict());
			objectBuilder.add("child", pollingDistrict.hasParentPollingDistrict());
			objectBuilder.add("technical", pollingDistrict.isTechnicalPollingDistrict());
		}
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { PollingDistrict.class };
	}
}
