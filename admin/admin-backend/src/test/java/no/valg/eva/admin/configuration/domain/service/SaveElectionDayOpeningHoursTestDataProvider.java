package no.valg.eva.admin.configuration.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDomainModelList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class SaveElectionDayOpeningHoursTestDataProvider extends AbstractMunicipalityTestDataProvider {
    private Municipality municipality;
    private ElectionEvent electionEvent;
    private List<ElectionDay> electionDays;
    private List<MunicipalityOpeningHour> expectedOpeningHours;

    @DataProvider
    public static Object[][] getOpeningHoursTestData() {
        Municipality municipalityWithNoOpeningHours = municipality();

        List<ElectionDay> electionDays = electionDays();

        List<OpeningHours> openingHourList = completeValidOpeningHourList();
        
        Municipality municipalityWithElectionDayTwoOpeningHours = municipality(toMunicipalityOpeningHours(openingHourList, municipalityWithNoOpeningHours));

        ElectionEvent electionEvent = new ElectionEvent();
        electionEvent.getElectionDays().addAll(toDomainModelList(electionDays));

        return new Object[][]{
                {noPreviouslySavedOpeningHours(municipalityWithNoOpeningHours, electionDays, electionEvent)},
                {oneOpeningHourForEachElectionDay(electionDays, municipalityWithElectionDayTwoOpeningHours, electionEvent)}

        };
    }

    private static SaveElectionDayOpeningHoursTestDataProvider oneOpeningHourForEachElectionDay(List<ElectionDay> electionDays, 
                                                                                                Municipality municipalityWithElectionDayTwoOpeningHours, 
                                                                                                ElectionEvent electionEvent) {
        return builder()
                .electionDays(electionDays)
                .expectedOpeningHours(toMunicipalityOpeningHours(completeValidOpeningHourList()))
                .electionEvent(electionEvent)
                .municipality(municipalityWithElectionDayTwoOpeningHours)
                .build();
    }

    private static SaveElectionDayOpeningHoursTestDataProvider noPreviouslySavedOpeningHours(Municipality municipalityWithNoOpeningHours, 
                                                                                             List<ElectionDay> electionDays, ElectionEvent electionEvent) {
        return builder()
                .electionDays(electionDays)
                .expectedOpeningHours(openingHourListWithoutPrimaryKeys())
                .electionEvent(electionEvent)
                .municipality(municipalityWithNoOpeningHours)
                .build();
    }

    private static List<MunicipalityOpeningHour> openingHourListWithoutPrimaryKeys() {
        List<OpeningHours> openingHourList = new ArrayList<>();

        OpeningHours openingHoursOne = openingHours(null, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(15).withMinuteOfHour(0), domainElectionDay(1L, new LocalDate()));
        openingHourList.add(openingHoursOne);

        OpeningHours openingHoursTwo = openingHours(null, new LocalTime().withHourOfDay(16).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(2L, new LocalDate().plusDays(1)));
        openingHourList.add(openingHoursTwo);

        return toMunicipalityOpeningHours(openingHourList);
    }

    static no.valg.eva.admin.configuration.domain.model.ElectionDay domainElectionDay(long primaryKey, LocalDate date) {
        no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
        electionDay.setDate(date);
        electionDay.setPk(primaryKey);
        return electionDay;
    }

    private static Municipality municipality() {
        return municipality(emptyList());
    }

    private static Municipality municipality(List<MunicipalityOpeningHour> dbOpeningHours) {
        Municipality municipality = new Municipality();
        municipality.setPk(123L);
        municipality.setOpeningHours(new HashSet<>(dbOpeningHours));
        return municipality;
    }

    private static List<ElectionDay> electionDays() {
        List<ElectionDay> electionDays = new ArrayList<>();
        electionDays.add(electionDay(1L, new LocalTime().withHourOfDay(9).withMinuteOfHour(0), new LocalTime().withHourOfDay(21).withMinuteOfHour(0)));
        electionDays.add(electionDay(2L, new LocalTime().withHourOfDay(12).withMinuteOfHour(0), new LocalTime().withHourOfDay(19).withMinuteOfHour(0)));

        return electionDays;
    }

    private static ElectionDay electionDay(long primaryKey, LocalTime startTime, LocalTime endTime) {
        ElectionDay electionDay = new ElectionDay();
        electionDay.setPk(primaryKey);
        electionDay.setStartTime(startTime);
        electionDay.setEndTime(endTime);

        return electionDay;
    }

    @DataProvider
    public static Object[][] saveOpeningHoursTestData() {
        return new Object[][]{
                completeOpeningHoursListNoPk(),
        };
    }

    private static Object[] completeOpeningHoursListNoPk() {
        Municipality municipality = new Municipality();

        List<MunicipalityOpeningHour> openingHoursList = toMunicipalityOpeningHours(completeValidOpeningHourListNoPk(), municipality);

        return new Object[]{municipality, openingHoursList, true};
    }

    /*private static List<MunicipalityOpeningHour> completeValidOpeningHourListNoPk() {
        List<OpeningHours> openingHourList = new ArrayList<>();

        LocalDate dayOne = new LocalDate();
        LocalDate dayTwo = dayOne.plusDays(1);

        OpeningHours openingHoursOne = openingHours(null, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(15).withMinuteOfHour(0), domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        openingHoursOne = openingHours(null, new LocalTime().withHourOfDay(16).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        OpeningHours openingHoursTwo = openingHours(null, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(16).withMinuteOfHour(0), domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        openingHoursTwo = openingHours(null, new LocalTime().withHourOfDay(17).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        return toMunicipalityOpeningHours(openingHourList);
    }*/
}
