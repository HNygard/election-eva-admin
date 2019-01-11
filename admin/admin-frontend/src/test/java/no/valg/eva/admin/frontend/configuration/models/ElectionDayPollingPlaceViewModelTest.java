package no.valg.eva.admin.frontend.configuration.models;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.ElectionEvent;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.BOROUGH;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.MUNICIPALITY;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.POLLING_PLACE;
import static org.testng.Assert.assertEquals;

public class ElectionDayPollingPlaceViewModelTest {

    @Test
    public void testMunicipalityType() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aMunicipality(), zeroOpeningHours(), electionDays());
        assertEquals(viewModel.getType(), MUNICIPALITY);
    }

    private Municipality aMunicipality() {
        final Municipality municipalityEntity = new Municipality();
        municipalityEntity.setPk(1L);
        municipalityEntity.setId("1");
        municipalityEntity.setName("Krøkerud kommune");
        return municipalityEntity;
    }

    private List<OpeningHours> zeroOpeningHours() {
        return emptyList();
    }

    private List<ElectionDay> electionDays() {
        return asList(
                new ElectionDay(1, 1L, new LocalDate().minusDays(1), new LocalTime().withHourOfDay(10).withMinuteOfHour(0),
                        new LocalTime().withHourOfDay(20).withMinuteOfHour(0), ElectionEvent.builder().pk(1L).build()),
                new ElectionDay(2, 2L, new LocalDate().minusDays(1), new LocalTime().withHourOfDay(8).withMinuteOfHour(0),
                        new LocalTime().withHourOfDay(21).withMinuteOfHour(0), ElectionEvent.builder().pk(1L).build())

        );
    }

    @Test
    public void testBoroughType() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aBorough());
        assertEquals(viewModel.getType(), BOROUGH);
    }

    private Borough aBorough() {
        Borough aBorough = new Borough(new AreaPath("111111.47.01.0101.000001"), 0);
        aBorough.setPk(1L);
        aBorough.setId("1");
        aBorough.setName("Den store bydelen");
        return aBorough;
    }

    @Test
    public void testPollingPlaceType() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aPollingPlace(), electionDays());
        assertEquals(viewModel.getType(), POLLING_PLACE);
    }

    private ElectionDayPollingPlace aPollingPlace() {
        ElectionDayPollingPlace aPollingPlace = new ElectionDayPollingPlace(new AreaPath("111111.47.01.0101.000001.0001.0001"), 0);
        aPollingPlace.setPk(1L);
        aPollingPlace.setId("1");
        aPollingPlace.setName("Det lille Biblotek");
        aPollingPlace.setAddress("Skogsveien 2");
        aPollingPlace.setPostalCode("4321");
        aPollingPlace.setPostTown("Storby");
        aPollingPlace.setGpsCoordinates("63.1234,12.4321");
        return aPollingPlace;
    }

    @Test
    public void validate_municipalityWithZeroOpeningHours_doesNotThrowException() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aMunicipality(), zeroOpeningHours(), electionDays());
        viewModel.validateOpeningHours();
    }

    @Test
    public void validate_municipalityWithOpeningHours_doesNotThrowException() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aMunicipality(), someOpeningHours(), electionDays());
        viewModel.validateOpeningHours();
    }

    private List<OpeningHours> someOpeningHours() {
        return asList(
                OpeningHours.builder()
                        .electionDay(electionDays().get(0))
                        .startTime(new LocalTime().withHourOfDay(10))
                        .endTime(new LocalTime().withHourOfDay(12))
                        .build(),
                OpeningHours.builder()
                        .electionDay(electionDays().get(1))
                        .startTime(new LocalTime().withHourOfDay(10))
                        .endTime(new LocalTime().withHourOfDay(20))
                        .build()
        );
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.missing_max_when_one_selected")
    public void validate_municipalityWithNoOpeningHoursOnLastElectionDay_throwsException() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aMunicipality(), openingHoursOnFirstElectionDay(), electionDays());
        viewModel.validateOpeningHours();
    }

    private List<OpeningHours> openingHoursOnFirstElectionDay() {
        return singletonList(
                OpeningHours.builder()
                        .electionDay(electionDays().get(0))
                        .startTime(new LocalTime().withHourOfDay(10))
                        .endTime(new LocalTime().withHourOfDay(12))
                        .build()
        );
    }

    @Test(expectedExceptions = EvoteException.class,
            expectedExceptionsMessageRegExp = "@config.local.election_day_polling_place.validate.missing_opening_hours")
    public void validate_pollingPlaceWithZeroOpeningHours_throwsException() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aPollingPlaceWithoutOpeningHours(), electionDays());
        viewModel.validateOpeningHours();
    }

    private ElectionDayPollingPlace aPollingPlaceWithoutOpeningHours() {
        return aPollingPlace();
    }

    @Test
    public void validate_pollingPlaceWithOpeningHours_doesNotThrowException() {
        ElectionDayPollingPlaceViewModel viewModel = new ElectionDayPollingPlaceViewModel(aPollingPlaceWithOpeningHours(), electionDays());
        viewModel.validateOpeningHours();
    }

    private ElectionDayPollingPlace aPollingPlaceWithOpeningHours() {
        ElectionDayPollingPlace pollingPlace = aPollingPlace();
        pollingPlace.setOpeningHours(someOpeningHours());
        return pollingPlace;
    }
}
