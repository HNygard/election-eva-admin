package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class FilterOpeningHoursThatDifferTestDataProvider extends AbstractMunicipalityTestDataProvider {

    @DataProvider
    public static Object[][] filterOpeningHoursThatDifferTestData() {
        List<OpeningHours> completeValidOpeningHourList = completeValidOpeningHourList();

        List<MunicipalityOpeningHour> municipalityOpeningHourList = toMunicipalityOpeningHours(completeValidOpeningHourList);

        PollingPlace pollingPlaceWithStandardOpeningHours = new PollingPlace();
        pollingPlaceWithStandardOpeningHours.setOpeningHours(new HashSet<>(completeValidOpeningHourList));

        List<OpeningHours> completeValidOpeningHourListDiffingFromDefault = completeValidOpeningHourListDiffingFromDefault();
        PollingPlace pollingPlaceWithCustomOpeningHours = new PollingPlace();
        pollingPlaceWithCustomOpeningHours.setOpeningHours(new HashSet<>(completeValidOpeningHourListDiffingFromDefault));
        
        List<OpeningHours> openingHoursWithFirstDayDiffing = copyListWithoutReferences(completeValidOpeningHourList);
        OpeningHours firstOpeningHour = getCustomOpeningHour(openingHoursWithFirstDayDiffing, 0, -3, 1);
        PollingPlace pollingPlaceWithFirstOpeningHourCustom = new PollingPlace();
        pollingPlaceWithFirstOpeningHourCustom.setOpeningHours(new HashSet<>(openingHoursWithFirstDayDiffing));

        List<OpeningHours> openingHoursWithSecondDayDiffing = copyListWithoutReferences(completeValidOpeningHourList);
        OpeningHours secondOpeningHour = getCustomOpeningHour(openingHoursWithSecondDayDiffing, 1, -3, 1);
        PollingPlace pollingPlaceWithSecondOpeningHourCustom = new PollingPlace();
        pollingPlaceWithSecondOpeningHourCustom.setOpeningHours(new HashSet<>(openingHoursWithSecondDayDiffing));

        List<OpeningHours> openingHoursWithThirdDayDiffing = copyListWithoutReferences(completeValidOpeningHourList);
        OpeningHours thirdOpeningHour = getCustomOpeningHour(openingHoursWithThirdDayDiffing, 2, -3, 1);
        PollingPlace pollingPlaceWithThirdOpeningHourCustom = new PollingPlace();
        pollingPlaceWithThirdOpeningHourCustom.setOpeningHours(new HashSet<>(openingHoursWithThirdDayDiffing));

        return new Object[][]{
                {municipalityOpeningHourList, pollingPlaceWithStandardOpeningHours, Collections.emptyList(), false},
                {municipalityOpeningHourList, pollingPlaceWithCustomOpeningHours, completeValidOpeningHourListDiffingFromDefault, true},
                {municipalityOpeningHourList, pollingPlaceWithFirstOpeningHourCustom, singletonList(firstOpeningHour), true},
                {municipalityOpeningHourList, pollingPlaceWithSecondOpeningHourCustom, singletonList(secondOpeningHour), true},
                {municipalityOpeningHourList, pollingPlaceWithThirdOpeningHourCustom, singletonList(thirdOpeningHour), true}
        };
    }

    private static OpeningHours getCustomOpeningHour(List<OpeningHours> openingHourList, int listIndex, int startTimeDiff, int endTimeDiff) {
        OpeningHours currentOpeningHour = openingHourList.get(listIndex);
        currentOpeningHour.setStartTime(currentOpeningHour.getStartTime().plusHours(startTimeDiff));
        currentOpeningHour.setEndTime(currentOpeningHour.getEndTime().plusHours(endTimeDiff));
        return currentOpeningHour;
    }

    private static List<OpeningHours> copyListWithoutReferences(final List<OpeningHours> openingHoursList) {
        return openingHoursList.stream()
                .map(currentOpeningHour -> {
                    OpeningHours openingHourCopy = OpeningHours.builder()
                            .electionDay(currentOpeningHour.getElectionDay())
                            .startTime(currentOpeningHour.getStartTime())
                            .endTime(currentOpeningHour.getEndTime())
                            .build();
                    openingHourCopy.setPk(currentOpeningHour.getPk());

                    return openingHourCopy;
                }).collect(Collectors.toList());
    }

    private static List<OpeningHours> completeValidOpeningHourListDiffingFromDefault() {
        List<OpeningHours> openingHourList = new ArrayList<>();

        LocalDate dayOne = new LocalDate();
        LocalDate dayTwo = dayOne.plusDays(1);

        OpeningHours openingHoursOne = openingHours(1L, new LocalTime().withHourOfDay(10).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(15).withMinuteOfHour(0),
                domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        openingHoursOne = openingHours(2L, new LocalTime().withHourOfDay(17).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0),
                domainElectionDay(1L, dayOne));
        openingHourList.add(openingHoursOne);

        OpeningHours openingHoursTwo = openingHours(3L, new LocalTime().withHourOfDay(10).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(16).withMinuteOfHour(0),
                domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        openingHoursTwo = openingHours(3L, new LocalTime().withHourOfDay(18).withMinuteOfHour(0),
                new LocalTime().withHourOfDay(21).withMinuteOfHour(0),
                domainElectionDay(2L, dayTwo));
        openingHourList.add(openingHoursTwo);

        return openingHourList;
    }
}
