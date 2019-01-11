package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.Place;

import org.joda.time.DateTime;

public abstract class PlaceAuditEvent<T extends Place> extends AuditEvent {

	private final T place;

	public PlaceAuditEvent(UserData userData, T place, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), (crudType == Save || crudType == Update) && place.getPk() == null ? Create : Update, LOCAL_CONFIGURATION, outcome,
				detail);
		this.place = place;
	}

	@Override
	public Class objectType() {
		return place.getClass();
	}

	@Override
	public String toJson() {
		return placeJsonBuilder().toJson();
	}

	protected JsonBuilder placeJsonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", place.getPath().path());
		objectBuilder.add("id", place.getId());
		objectBuilder.add("pk", place.getPk());
		if (!eventType().equals(Delete)) {
			objectBuilder.add("name", place.getName());

		}
		return objectBuilder;
	}

	public T getPlace() {
		return place;
	}
}
