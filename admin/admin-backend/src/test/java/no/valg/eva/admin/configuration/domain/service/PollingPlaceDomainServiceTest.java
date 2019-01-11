package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.application.ElectionMapper;
import no.valg.eva.admin.configuration.application.LocalConfigApplicationServiceTest;
import no.valg.eva.admin.configuration.application.OpeningHoursSorter;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.OpeningHoursRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.AreaPath.OSLO_MUNICIPALITY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class PollingPlaceDomainServiceTest extends LocalConfigApplicationServiceTest {

    @DataProvider
    public static Object[][] getOpeningHoursTestData() {
        List<OpeningHours> openingHoursList = completeValidOpeningHourList();
        return new Object[][]{
                {openingHoursList}
        };
    }

    private static List<OpeningHours> completeValidOpeningHourList() {
        List<OpeningHours> completeValidOpeningHourList = new ArrayList<>();

        OpeningHours openingHoursOne = openingHours(1L, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(15).withMinuteOfHour(0), domainElectionDay(1L, new LocalDate()));
        completeValidOpeningHourList.add(openingHoursOne);

        OpeningHours openingHoursTwo = openingHours(2L, new LocalTime().withHourOfDay(16).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(2L, new LocalDate().plusDays(1)));
        completeValidOpeningHourList.add(openingHoursTwo);

        return completeValidOpeningHourList;
    }

    private static no.valg.eva.admin.configuration.domain.model.ElectionDay domainElectionDay(long primaryKey, LocalDate date) {
        no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
        electionDay.setPk(primaryKey);
        electionDay.setDate(date);
        return electionDay;
    }

    private static OpeningHours openingHours(Long primaryKey, LocalTime startTime, LocalTime endTime, no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay) {
        OpeningHours openingHoursOne = new OpeningHours();
        openingHoursOne.setPk(primaryKey);
        openingHoursOne.setStartTime(startTime);
        openingHoursOne.setEndTime(endTime);
        openingHoursOne.setElectionDay(electionDay);
        return openingHoursOne;
    }

    private static OpeningHours openingHours() {
        ElectionDay electionDay = ElectionDay.builder()
                .date(new LocalDate())
                .startTime(new LocalTime())
                .endTime(new LocalTime())
                .build();

        return OpeningHours.builder()
                .startTime(new LocalTime())
                .endTime(new LocalTime())
                .electionDay(electionDay)
                .build();
    }

    @Test
    public void findElectionDayPollingPlacesByArea_withArea_returnsPlaces() throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        UserData userData = userData();
        Municipality municipality = municipality(OSLO_MUNICIPALITY_ID)
                .withOrdinaryPollingDistricts(PollingDistrictType.REGULAR, "1000", "1001")
                .withBoroughs()
                .withOpeningHours()
                .getValue();

        stub_municipalityByElectionEventAndId(municipality);

        when(getInjectMock(MunicipalityRepository.class).municipalityByElectionEventAndId(anyLong(), anyString())).thenReturn(municipality);
        when(getInjectMock(MunicipalityRepository.class).findByPk(anyLong())).thenReturn(municipality);

        List<ElectionDayPollingPlace> result = service.findElectionDayPollingPlacesByArea(userData, MUNICIPALITY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBorough()).isNotNull();
    }

    @Test(enabled = false, dataProvider = "getOpeningHoursTestData")
    public void getOpeningHours_withData_verifyDays(List<OpeningHours> expectedOpeningHours) throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        UserData userData = userData();
        PollingPlace pollingPlace = createMock(PollingPlace.class);
        stub_findElectionDaysByElectionEvent();

        when(getInjectMock(ElectionEventDomainService.class).getDefaultOpeningHoursFromElectionEvent(userData)).thenReturn(expectedOpeningHours);
        when(pollingPlace.getOpeningHours()).thenReturn(Collections.emptySet());

        List<no.valg.eva.admin.common.configuration.model.OpeningHours> result = service.toOpeningHoursDto(pollingPlace);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getElectionDay()).isNotNull();
        assertThat(result.get(0).getStartTime()).isNotNull();
        assertThat(result.get(0).getEndTime()).isNotNull();
        assertThat(result.get(1).getElectionDay()).isNotNull();
        assertThat(result.get(1).getStartTime()).isNotNull();
        assertThat(result.get(1).getEndTime()).isNotNull();
    }

    @Test(enabled = false)
    public void findElectionDayPollingPlaceByAreaAndId_withValidId_returnsPlace() throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        Municipality municipality = municipality(OSLO_MUNICIPALITY_ID)
                .withOrdinaryPollingDistricts(PollingDistrictType.REGULAR, "1000", "1001").getValue();
        stub_municipalityByElectionEventAndId(municipality);
        UserData userData = userData();
        PollingPlace place = createMock(PollingPlace.class);
        when(place.getUsingPollingStations()).thenReturn(true);
        when(place.isElectionDayVoting()).thenReturn(true);
        municipality.regularPollingDistrictById("1000", false, true).getPollingPlaces().add(place);

        assertThat(service.findElectionDayPollingPlaceByAreaAndId(userData, MUNICIPALITY, "1000")).isNotNull();

        verify(getInjectMock(PollingStationRepository.class)).countByPollingPlace(anyLong());
    }

    @Test
    public void saveElectionDayPollingPlace_withNewPlace_verifyCreate() throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        UserData userData = userData();
        ElectionDayPollingPlace place = createMock(ElectionDayPollingPlace.class);
        when(place.getPk()).thenReturn(null);
        stub_pollingDistrictRepository_findByPk(new PollingDistrictsBuilder("0001").withType(PollingDistrictType.REGULAR)
                .getValues().iterator().next());

        when(getInjectMock(PollingPlaceRepository.class)
                .create(eq(userData), any(PollingPlace.class)))
                .thenAnswer(invocation -> invocation.getArguments()[1]);
        when(getInjectMock(PollingPlaceRepository.class).findPollingPlaceByElectionDayVoting(anyLong()))
                .thenReturn(null);
        when(place.getOpeningHours()).thenReturn(new ArrayList<>());

        ElectionDayPollingPlace result = service.saveElectionDayPollingPlace(userData, place);
        assertThat(result).isNotNull();
    }

    @Test(enabled = false)
    public void saveElectionDayPollingPlace_withExistingPlace_verifyUpdate() throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        UserData userData = userData();
        ElectionDayPollingPlace place = createMock(ElectionDayPollingPlace.class);
        when(place.getPk()).thenReturn(1L);
        stub_pollingPlaceServiceBean_findByPk(new PollingPlacesBuilder("0001").withElectionDayVoting(true)
                .withPollingDistrict("0001", PollingDistrictType.REGULAR).getValues().iterator().next());
        when(getInjectMock(PollingPlaceRepository.class).update(eq(userData), any(PollingPlace.class))).thenAnswer(invocation -> invocation.getArguments()[1]);
        stub_municipalityByElectionEventAndId(true);

        ElectionDayPollingPlace result = service.saveElectionDayPollingPlace(userData, place);

        verify(place).setUsePollingStations(false);
        assertThat(result).isNotNull();
    }

    private Municipality stub_municipalityByElectionEventAndId(boolean isElectronicMarkoffs) {
        Municipality municipality = getInjectMock(MunicipalityRepository.class).municipalityByElectionEventAndId(anyLong(), anyString());
        when(municipality.isElectronicMarkoffs()).thenReturn(isElectronicMarkoffs);
        return municipality;
    }

    @Test(dataProvider = "saveOpeningHoursTestData")
    public void saveOpeningHours_withData_verifyOperations(List<OpeningHours> completeValidOpeningHourList) throws Exception {
        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        UserData userData = userData();
        PollingPlace pollingPlace = new PollingPlace();

        when(getInjectMock(PollingPlaceRepository.class).getReference(pollingPlace)).thenReturn(pollingPlace);
        service.saveOpeningHoursForPollingPlace(userData, pollingPlace, completeValidOpeningHourList);

        List<OpeningHours> openingHoursListResult = OpeningHoursSorter.toSortedList(pollingPlace.getOpeningHours());

        assertEquals(openingHoursListResult, completeValidOpeningHourList);

        verify(getInjectMock(PollingPlaceRepository.class), times(1)).deleteOpeningHours(any(UserData.class), any(PollingPlace.class));
        verify(getInjectMock(PollingPlaceRepository.class), times(1)).update(any(UserData.class), any(PollingPlace.class));
    }

    @DataProvider
    public static Object[][] saveOpeningHoursTestData() {
        return new Object[][]{
                {singletonList(openingHours())}
        };
    }

    private void stub_findElectionDaysByElectionEvent() {
        List<no.valg.eva.admin.common.configuration.model.election.ElectionDay> result = new ArrayList<>();
        result.add(electionDay("01012016"));
        result.add(electionDay("02012016"));
        when(getInjectMock(ElectionEventDomainService.class).findElectionDaysByElectionEvent(any())).thenReturn(result);
    }

    private no.valg.eva.admin.common.configuration.model.election.ElectionDay electionDay(String date) {
        stub_findOpeningHoursForPollingPlace(date);
        return new ElectionMapper(null, null).toElectionDay(dbElectionDay(date));
    }

    private void stub_findOpeningHoursForPollingPlace(String date) {
        List<OpeningHours> openingHoursList = new ArrayList<>();
        openingHoursList.add(dbOpeningHours(date));
        openingHoursList.add(dbOpeningHours(date));
        when(getInjectMock(OpeningHoursRepository.class).findOpeningHoursForPollingPlace(anyLong())).thenReturn(openingHoursList);
    }

    private no.valg.eva.admin.configuration.domain.model.ElectionDay dbElectionDay(String date) {
        no.valg.eva.admin.configuration.domain.model.ElectionDay result = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
        result.setDate(DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date));
        result.setStartTime(new LocalTime());
        result.setEndTime(new LocalTime());
        return result;
    }

    private OpeningHours dbOpeningHours(String date) {
        OpeningHours result = new OpeningHours();
        result.setElectionDay(dbElectionDay(date));
        result.setStartTime(new LocalTime());
        result.setEndTime(new LocalTime());
        return result;
    }

    @Test(dataProvider = "filterOpeningHoursThatDifferTestData", dataProviderClass = FilterOpeningHoursThatDifferTestDataProvider.class)
    public void testFilterOpeningHoursThatDiffer_givenOpeningHours_verifiesIfListDiffer(List<MunicipalityOpeningHour> municipalityDefaultOpeningHours,
                                                                                        PollingPlace pollingPlace,
                                                                                        List<OpeningHours> expectedDiffingOpeningHours,
                                                                                        boolean shouldDiffFromDefault)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        PollingPlaceDomainService service = initializeMocks(PollingPlaceDomainService.class);
        Predicate<PollingPlace> predicate = service.filterPollingPlacesWithCustomOpeningHours(municipalityDefaultOpeningHours);

        List<PollingPlace> pollingPlacesWithCustomOpeningHours = new ArrayList<>();
        if (predicate.test(pollingPlace)) {
            pollingPlacesWithCustomOpeningHours.add(pollingPlace);
        }

        if (shouldDiffFromDefault) {
            assertEquals(pollingPlacesWithCustomOpeningHours.get(0), pollingPlace);
        } else {
            assertTrue(pollingPlacesWithCustomOpeningHours.isEmpty(), "Expected empty list of polling places with custom openinghours - got: " + pollingPlacesWithCustomOpeningHours);
        }
    }

}

