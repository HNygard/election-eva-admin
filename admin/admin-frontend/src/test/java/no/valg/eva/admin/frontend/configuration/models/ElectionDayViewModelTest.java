package no.valg.eva.admin.frontend.configuration.models;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.configuration.model.ElectionEvent;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ElectionDayViewModelTest {

    private ElectionDayViewModel electionDayViewModel;

    @BeforeMethod
    public void beforeTest() {
        electionDayViewModel = ElectionDayViewModel.builder()
                .activated(true)
                .electionDay(electionDay(hour(8), hour(21)))
                .build();
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.starttime_or_endtime_empty")
    public void validate_missingStartTime_throwsException() {
        given(openingHours(null, hour(21)));
        validate();
    }


    private void given(OpeningHours... openingHours) {
        for (OpeningHours openingHour : openingHours) {
            given(null, openingHour);
        }
    }

    private void given(ElectionDay electionDay, OpeningHours openingHours) {
        if (electionDay != null) {
            electionDayViewModel.setElectionDay(electionDay);
        }
        electionDayViewModel.addOpeningHour(openingHours);
    }

    private OpeningHours openingHours(LocalTime startTime, LocalTime endTime) {
        return OpeningHours.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    private LocalTime hour(int hour) {
        return new LocalTime().withHourOfDay(hour);
    }

    private void validate() {
        electionDayViewModel.validate();
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.starttime_or_endtime_empty")
    public void validate_missingEndTime_throwsException() {
        given(openingHours(hour(8), null));
        validate();
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.start_after_end")
    public void validate_endBeforeStart_throwsException() {
        given(openingHours(hour(9), hour(8)));
        validate();
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.start_time_before_start")
    public void validate_startBeforeElectionDayStart_throwsException() {
        given(
                electionDay(hour(8), hour(21)),
                openingHours(hour(7), hour(21))
        );
        validate();
    }

    private ElectionDay electionDay(LocalTime startTime, LocalTime endTime) {
        return new ElectionDay(1, 1L, new LocalDate(), startTime, endTime, ElectionEvent.builder().pk(1L).build());
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.end_time_after_end")
    public void validate_endAfterElectionDayEnd_throwsException() {
        given(
                electionDay(hour(8), hour(21)),
                openingHours(hour(8), hour(22))
        );
        validate();
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.starttime2_before_endtime1")
    public void validate_firstOverlappingSecond_throwsException() {
        given(
                openingHours(hour(8), hour(11)),
                openingHours(hour(10), hour(12))
        );
        validate();
    }

    @Test
    public void validate_canAddMoreOpeningHours() {
        given(openingHours(hour(8), hour(20)));
        assertTrue(electionDayViewModel.canAddOpeningHours());
    }

    @Test
    public void validate_maxTwoOpeningHours() {
        given(
                openingHours(hour(8), hour(20)),
                openingHours(hour(20), hour(21))
        );
        assertFalse(electionDayViewModel.canAddOpeningHours());
    }
}
