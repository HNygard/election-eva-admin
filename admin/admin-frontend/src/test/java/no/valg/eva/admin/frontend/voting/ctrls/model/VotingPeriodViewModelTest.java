package no.valg.eva.admin.frontend.voting.ctrls.model;

import no.valg.eva.admin.util.DateUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static org.testng.Assert.assertEquals;

public class VotingPeriodViewModelTest {

    @Test(dataProvider = "setDateTestData")
    public void testGetFromDateAsLegacyDate_givenModel_verifiesCorrectLegacyFromDate(LocalDateTime localDateTime) {
        VotingPeriodViewModel votingPeriodViewModel = new VotingPeriodViewModel();
        votingPeriodViewModel.setFromDate(localDateTime);
        assertEquals(votingPeriodViewModel.getFromDate(), localDateTime);
    }

    @Test(dataProvider = "setDateTestData")
    public void testSetToDateAsLegacyDate_givenModel_verifiesCorrectLegacyFromDate(LocalDateTime localDateTime) {
        VotingPeriodViewModel votingPeriodViewModel = new VotingPeriodViewModel();
        votingPeriodViewModel.setToDateIncluding(localDateTime);
        assertEquals(votingPeriodViewModel.getToDateIncluding(), localDateTime);
    }

    @DataProvider
    public Object[][] setDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();

        return new Object[][]{
                {toLocalDateTime(calendar)}
        };
    }

    @Test(dataProvider = "getFromDateAsLegacyDateTestData")
    public void testGetFromDateAsLegacyDate_givenModel_verifiesCorrectLegacyFromDate(VotingPeriodViewModel votingPeriodViewModel, Date expectedLegacyFromDate) {
        assertEquals(votingPeriodViewModel.getFromDateAsLegacyDate(), expectedLegacyFromDate);
    }

    @DataProvider
    public Object[][] getFromDateAsLegacyDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();
        Date date = calendar.getTime();

        return new Object[][]{
                {votingPeriodViewModel(calendar, calendar), date}
        };
    }

    @Test
    public void testSetFromDateAsLegacyDate_setsDateWithTime_toStartOfThatDay() {

        Calendar calendar = calendarInstanceOneInTheMorning();
        Date date = calendar.getTime();
        
        VotingPeriodViewModel votingPeriodViewModel = VotingPeriodViewModel.builder().build();
        votingPeriodViewModel.setFromDateAsLegacyDate(date);
        
        assertEquals(votingPeriodViewModel.getFromDate(), DateUtil.startOfDay(votingPeriodViewModel.getFromDate().toLocalDate()));
    }

    @Test(dataProvider = "getFromDateAsShortDateTestData")
    public void testGetFromDateAsShortDate_givenModel_verifiesCorrectShortDate(VotingPeriodViewModel votingPeriodViewModel, String expectedShortDate) {
        assertEquals(votingPeriodViewModel.getFromDateAsShortDate(), expectedShortDate);
    }

    @DataProvider
    public Object[][] getFromDateAsShortDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();

        return new Object[][]{
                {votingPeriodViewModel(calendar, calendar), "30. oktober"}
        };
    }

    @Test(dataProvider = "getFromDateAsFullDateTestData")
    public void testGetFromDateAsFullDate_givenModel_verifiesCorrectShortDate(VotingPeriodViewModel votingPeriodViewModel, String expectedDate) {
        assertEquals(votingPeriodViewModel.getFromDateAsFullDate(), expectedDate);
    }

    @DataProvider
    public Object[][] getFromDateAsFullDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();

        return new Object[][]{
                {VotingPeriodViewModel.builder()
                        .fromDate(toLocalDateTime(calendar))
                        .build(), "30/10/2018"}
        };
    }

    @Test(dataProvider = "getToDateAsLegacyDateTestData")
    public void testGetToDateAsLegacyDate_givenModel_verifiesCorrectLegacyToDate(VotingPeriodViewModel votingPeriodViewModel, Date expectedDate) {
        assertEquals(votingPeriodViewModel.getToDateAsLegacyDate(), expectedDate);
    }

    @DataProvider
    public Object[][] getToDateAsLegacyDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();
        Date date = calendar.getTime();

        return new Object[][]{
                {VotingPeriodViewModel.builder()
                        .toDateIncluding(toLocalDateTime(calendar))
                        .build(), date}
        };
    }

    @Test
    public void testSetToDateAsLegacyDate_setsDateWithTime_toEndOfThatDay() {

        Calendar calendar = calendarInstanceOneInTheMorning();
        Date date = calendar.getTime();
        
        VotingPeriodViewModel votingPeriodViewModel = VotingPeriodViewModel.builder().build();
        votingPeriodViewModel.setToDateAsLegacyDate(date);
        
        assertEquals(votingPeriodViewModel.getToDateIncluding(), DateUtil.endOfDay(votingPeriodViewModel.getToDateIncluding().toLocalDate()));
    }

    @Test(dataProvider = "getToDateAsShortDateTestData")
    public void testToFromDateAsShortDate_givenModel_verifiesCorrectShortDate(VotingPeriodViewModel votingPeriodViewModel, String expectedShortDate) {
        assertEquals(votingPeriodViewModel.getToDateAsShortDate(), expectedShortDate);
    }

    @DataProvider
    public Object[][] getToDateAsShortDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();

        return new Object[][]{
                {VotingPeriodViewModel.builder()
                        .toDateIncluding(toLocalDateTime(calendar))
                        .build(), "30. oktober"}
        };
    }

    @Test(dataProvider = "getToDateAsFullDateTestData")
    public void testGetToDateAsFullDate_givenModel_verifiesCorrectDate(VotingPeriodViewModel votingPeriodViewModel, String expectedDate) {
        assertEquals(votingPeriodViewModel.getToDateAsFullDate(), expectedDate);
    }

    @DataProvider
    public Object[][] getToDateAsFullDateTestData() {
        Calendar calendar = calendarInstanceOneInTheMorning();

        return new Object[][]{
                {VotingPeriodViewModel.builder()
                        .toDateIncluding(toLocalDateTime(calendar))
                        .build(), "30/10/2018"}
        };
    }

    private Calendar calendarInstanceOneInTheMorning() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 9, 30, 1, 10, 0);
        calendar.set(MILLISECOND, 0);
        return calendar;
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        return LocalDateTime.of(calendar.get(YEAR),
                calendar.get(MONTH) + 1,
                calendar.get(DAY_OF_MONTH),
                calendar.get(HOUR),
                calendar.get(MINUTE),
                calendar.get(SECOND),
                calendar.get(MILLISECOND));
    }

    private VotingPeriodViewModel votingPeriodViewModel(Calendar fromDate, Calendar toDate) {
        return new VotingPeriodViewModel(toLocalDateTime(fromDate), toLocalDateTime(toDate));
    }
}