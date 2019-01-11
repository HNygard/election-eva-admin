package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import org.joda.time.DateTime;

public class PollingPlaceAuditEvent extends AuditEvent {
	private final PollingPlace pollingPlace;

	public PollingPlaceAuditEvent(UserData userData, PollingPlace pollingPlace, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.pollingPlace = pollingPlace;
	}

	@Override
	public Class objectType() {
		return PollingPlace.class;
	}

	@Override
	public String toJson() {
		return pollingPlaceJonBuilder().toJson();
	}

	protected JsonBuilder pollingPlaceJonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", pollingPlace.areaPath().path());
		objectBuilder.add("name", pollingPlace.getName());
		if (!eventType().equals(Delete)) {
			objectBuilder.add("advanceVoteInBallotBox", pollingPlace.isAdvanceVoteInBallotBox());
			objectBuilder.add("electionDayVoting", pollingPlace.isElectionDayVoting());
			objectBuilder.add("usingPollingStations", pollingPlace.getUsingPollingStations());
			objectBuilder.add("addressLine1", pollingPlace.getAddressLine1());
			objectBuilder.add("addressLine2", pollingPlace.getAddressLine2());
			objectBuilder.add("addressLine3", pollingPlace.getAddressLine3());
			objectBuilder.add("postalCode", pollingPlace.getPostalCode());
			objectBuilder.add("postTown", pollingPlace.getPostTown());
			objectBuilder.add("gpsCoordinates", pollingPlace.getGpsCoordinates());
			objectBuilder.add("infoText", pollingPlace.getInfoText());
		}
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { PollingPlace.class };
	}
}
