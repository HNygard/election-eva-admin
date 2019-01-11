package no.valg.eva.admin.common.auditlog.auditevents.config;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import org.joda.time.LocalTime;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;

public class ElectionDayPollingPlaceAuditEvent extends PollingPlaceAuditEvent<ElectionDayPollingPlace> {

    public ElectionDayPollingPlaceAuditEvent(UserData userData, ElectionDayPollingPlace electionDayPollingPlace, AuditEventTypes crudType, Outcome outcome,
                                      String detail) {
        super(userData, electionDayPollingPlace, crudType, outcome, detail);
    }

    @SuppressWarnings("unused")
    public static Class[] objectClasses(AuditEventType auditEventType) {
        return new Class[]{ElectionDayPollingPlace.class};
    }
    
    @Override
    protected JsonBuilder placeJsonBuilder() {
        JsonBuilder objectBuilder = super.placeJsonBuilder();
        if (!eventType().equals(Delete)) {
            objectBuilder.add("usePollingStations", getPlace().isUsePollingStations());
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (OpeningHours openingHours : getPlace().getOpeningHours()) {
                JsonBuilder ohBuilder = new JsonBuilder();
                ohBuilder.add("date", openingHours.getElectionDay().getDate().toString("ddMMyyyy"));
                addTime(ohBuilder, "startTime", openingHours.getStartTime());
                addTime(ohBuilder, "endTime", openingHours.getEndTime());
                arrayBuilder.add(ohBuilder.asJsonObject());
            }
            objectBuilder.add("openingHours", arrayBuilder.build());
        }
        return objectBuilder;
    }

    private void addTime(JsonBuilder ohBuilder, String key, LocalTime time) {
        if (time == null) {
            ohBuilder.add(key, "null");
        } else {
            ohBuilder.add(key, time.toString("HH:mm"));
        }
    }
}
