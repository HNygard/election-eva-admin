package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;

public class AdvancePollingPlaceAuditEvent extends PollingPlaceAuditEvent<AdvancePollingPlace> {

	public AdvancePollingPlaceAuditEvent(UserData userData, AdvancePollingPlace advancePollingPlace, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, advancePollingPlace, crudType, outcome, detail);
	}

	@Override
	protected JsonBuilder placeJsonBuilder() {
		JsonBuilder objectBuilder = super.placeJsonBuilder();
		if (!eventType().equals(Delete)) {
			objectBuilder.add("advanceVoteInBallotBox", getPlace().isAdvanceVoteInBallotBox());
			objectBuilder.add("publicPlace", getPlace().isPublicPlace());
		}
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { AdvancePollingPlace.class };
	}
}
