package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

class AbstractMunicipalityTestDataProvider {

    static List<OpeningHours> completeValidOpeningHourList() {
        List<OpeningHours> openingHourList = new ArrayList<>();

        LocalDate dayOne = new LocalDate();
        LocalDate dayTwo = dayOne.plusDays(1);

        OpeningHours openingHoursOne = openingHours(1L, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(15).withMinuteOfHour(0), domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        openingHoursOne = openingHours(2L, new LocalTime().withHourOfDay(16).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        OpeningHours openingHoursTwo = openingHours(3L, new LocalTime().withHourOfDay(9).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(16).withMinuteOfHour(0), domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        openingHoursTwo = openingHours(3L, new LocalTime().withHourOfDay(17).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0), domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        return openingHourList;
    }

    static List<OpeningHours> completeValidOpeningHourListNoPk() {
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

        return openingHourList;
    }

    static OpeningHours openingHours(Long primaryKey, LocalTime startTime, LocalTime endTime, no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay) {
        OpeningHours openingHoursOne = new OpeningHours();
        openingHoursOne.setPk(primaryKey);
        openingHoursOne.setStartTime(startTime);
        openingHoursOne.setEndTime(endTime);
        openingHoursOne.setElectionDay(electionDay);

        return openingHoursOne;
    }

    static no.valg.eva.admin.configuration.domain.model.ElectionDay domainElectionDay(long primaryKey, LocalDate date) {
        no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
        electionDay.setDate(date);
        electionDay.setPk(primaryKey);
        return electionDay;
    }

    static List<MunicipalityOpeningHour> toMunicipalityOpeningHours(List<no.valg.eva.admin.configuration.domain.model.OpeningHours> openingHourList) {
        return toMunicipalityOpeningHours(openingHourList, null);
    }
    
    static List<MunicipalityOpeningHour> toMunicipalityOpeningHours(List<no.valg.eva.admin.configuration.domain.model.OpeningHours> openingHourList, Municipality municipality) {
        return openingHourList
                .stream()
                .map(currentOpeningHour -> toDomainModel(currentOpeningHour, municipality))
                .collect(toList());
    }

    static MunicipalityOpeningHour toDomainModel(no.valg.eva.admin.configuration.domain.model.OpeningHours openingHours, Municipality municipality) {
        return MunicipalityOpeningHour.builder()
                .endTime(openingHours.getEndTime())
                .startTime(openingHours.getStartTime())
                .electionDay(openingHours.getElectionDay())
                .municipality(municipality)
                .build();
    }
}
