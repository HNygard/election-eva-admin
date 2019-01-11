package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.local.PollingPlace;

public abstract class PollingPlaceAuditEvent<T extends PollingPlace> extends PlaceAuditEvent<T> {

	public PollingPlaceAuditEvent(UserData userData, T place, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, place, crudType == Save && place.getPk() == null ? Create : Update, outcome, detail);
	}

	@Override
	protected JsonBuilder placeJsonBuilder() {
		JsonBuilder objectBuilder = super.placeJsonBuilder();
		if (!eventType().equals(Delete)) {
			objectBuilder.add("address", getPlace().getAddress());
			objectBuilder.add("postalCode", getPlace().getPostalCode());
			objectBuilder.add("postTown", getPlace().getPostTown());
			objectBuilder.add("gpsCoordinates", getPlace().getGpsCoordinates());
		}
		return objectBuilder;
	}
}
