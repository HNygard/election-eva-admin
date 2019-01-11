package no.valg.eva.admin.common.auditlog.auditevents.config;

import com.jayway.jsonassert.JsonAssert;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.Test;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;


public class ElectionDayPollingPlaceAuditEventTest extends AbstractAuditEventTest {

    @Test
    public void objectType_returnsElectionDayPollingPlace() {
        ElectionDayPollingPlaceAuditEvent event = event(AuditEventTypes.Save, new ElectionDayPollingPlace(AreaPath.from("111111.22.33.4444")));
        assertThat(event.objectType()).isSameAs(ElectionDayPollingPlace.class);
    }

    @Test
    public void toJson() {
        ElectionDayPollingPlace place = new ElectionDayPollingPlace(AreaPath.from("111111.22.33.4444"));
        place.setName("My Polling place");
        place.setPostalCode("0101");
        place.getOpeningHours().add(openingHours("01012016"));
        place.getOpeningHours().add(openingHours("02012016"));
        place.getOpeningHours().add(openingHours("03012016"));
        ElectionDayPollingPlaceAuditEvent event = event(AuditEventTypes.Save, place);

        JsonAssert.with(event.toJson())
                .assertThat("$", hasEntry("path", place.getPath().path()))
                .assertThat("$", hasEntry("name", place.getName()))
                .assertThat("$", hasEntry("address", place.getAddress()))
                .assertThat("$", hasEntry("postalCode", place.getPostalCode()))
                .assertThat("$", hasEntry("postTown", place.getPostTown()))
                .assertThat("$", hasEntry("gpsCoordinates", place.getGpsCoordinates()))
                .assertThat("$.openingHours[*]", collectionWithSize(equalTo(3)))
                .assertThat("$.openingHours[*].date", containsInAnyOrder("01012016", "02012016", "03012016"))
                .assertThat("$.openingHours[*].startTime", containsInAnyOrder("09:00", "09:00", "09:00"))
                .assertThat("$.openingHours[*].endTime", containsInAnyOrder("16:00", "16:00", "16:00"));
    }

    @Override
    protected Class<? extends AuditEvent> getAuditEventClass() {
        return AdvancePollingPlaceAuditEvent.class;
    }

    private ElectionDayPollingPlaceAuditEvent event(AuditEventTypes eventType, ElectionDayPollingPlace electionDayPollingPlace) {
        return new ElectionDayPollingPlaceAuditEvent(createMock(UserData.class), electionDayPollingPlace, eventType, Outcome.Success, "");
    }

    private OpeningHours openingHours(String date) {
        ElectionDay electionDay = new ElectionDay(1);
        electionDay.setDate(DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date));
        return OpeningHours.builder()
                .electionDay(electionDay)
                .startTime(DateTimeFormat.forPattern("HHmm").parseLocalTime("0900"))
                .endTime(DateTimeFormat.forPattern("HHmm").parseLocalTime("1600"))
                .build();
    }
}

