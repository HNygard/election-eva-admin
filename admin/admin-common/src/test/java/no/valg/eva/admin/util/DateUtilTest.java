package no.valg.eva.admin.util;

import no.valg.eva.admin.test.BaseTakeTimeTest;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static no.valg.eva.admin.util.DateUtil.formatToShortDate;
import static no.valg.eva.admin.util.DateUtil.getFormattedShortDate;
import static org.testng.Assert.assertEquals;

public class DateUtilTest extends BaseTakeTimeTest {

    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final int LAST_MONTH_OF_YEAR = 12;
    private static final int LAST_DAY_OF_MONTH = 31;
    private static final int FIRST_DAY_OF_MONTH = 1;
    private static final int FIRST_MONTH_OF_YEAR = 1;

    @Test(dataProvider = "testFormatToShortDateTestData")
    public void testGetFormattedShortDate_GivenLocalDateTime_VerifiesDateString(LocalDateTime localDateTime, String expectedDateString) {
        assertEquals(formatToShortDate(localDateTime), expectedDateString);
    }

    @DataProvider
    public Object[][] testFormatToShortDateTestData() {
        return new Object[][]{
                {LocalDateTime.of(2018, 1, 1, 1, 1), "01.01.2018"},
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetFormattedShortDate_GivenNullLocalDateTime_VerifiesException() {
        formatToShortDate(null);
    }

    @Test(dataProvider = "formatToShortTimeTestData")
    public void testFormatToShortTime_givenLocalDateTime_verifiesTimeString(LocalDateTime localDateTime, String expectedTimeString) {
        assertEquals(DateUtil.formatToShortTime(localDateTime), expectedTimeString);
    }

    @DataProvider
    public Object[][] formatToShortTimeTestData() {
        return new Object[][]{
                {LocalDateTime.of(2018, 9, 1, LAST_MONTH_OF_YEAR, 0), "12:00"}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Argument localDateTime is null")
    public void testFormatToShortTime_givenNull_verifiesException() {
        DateUtil.formatToShortTime(null);
    }

    @Test(dataProvider = "getDateTimeFormattedShortDateTestData")
    public void testGetFormattedShortDate_givenDateTime_verifiesDateString(DateTime dateTime, String expectedDateString) {
        assertEquals(getFormattedShortDate(dateTime), expectedDateString);
    }

    @DataProvider
    public Object[][] getDateTimeFormattedShortDateTestData() {
        return new Object[][]{
                {org.joda.time.DateTime.now().withYear(2018)
                        .withMonthOfYear(9)
                        .withDayOfMonth(1)
                        .withHourOfDay(0)
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0), "01.09.2018"},
                {null, ""}
        };
    }

    @Test(dataProvider = "formatToShortDateTestData")
    public void testGetFormattedShortDate_givenLocalDateTime_verifiesDateString(LocalDate localDateTime, String expectedDateString) {
        assertEquals(getFormattedShortDate(localDateTime), expectedDateString);
    }

    @DataProvider
    public Object[][] formatToShortDateTestData() {
        return new Object[][]{
                {org.joda.time.LocalDate.now().withYear(2018).withMonthOfYear(9).withDayOfMonth(1), "01.09.2018"},
                {null, ""}
        };
    }

    @Test(dataProvider = "formatLocalDateTimeToShortDateTestData")
    public void testGetFormattedShortDate_givenLocalDateTime_verifiesDateString(LocalDateTime localDateTime, String expectedDateString) {
        assertEquals(getFormattedShortDate(localDateTime), expectedDateString);
    }

    @DataProvider
    public Object[][] formatLocalDateTimeToShortDateTestData() {
        return new Object[][]{
                {LocalDateTime.of(2018, 9, 1, 0, 0), "01.09.2018"},
                {null, ""}
        };
    }

    @Test(dataProvider = "getFormattedShortIdDateTestData")
    public void testGetFormattedShortIdDate_givenLocalDate_verifiesDateId(LocalDate localDate, String expectedId) {
        assertEquals(DateUtil.getFormattedShortIdDate(localDate), expectedId);
    }

    @DataProvider
    public Object[][] getFormattedShortIdDateTestData() {
        return new Object[][]{
                {LocalDate.now().withYear(2018).withMonthOfYear(9).withDayOfMonth(1), "010918"},
                {null, ""}
        };
    }

    @Test(dataProvider = "getFormattedTimeTestData")
    public void testGetFormattedTime_givenLocalTimeAndFormatter_verifiesTimeString(LocalTime localTime, DateTimeFormatter dateFormatter, String expectedTimeString) {
        assertEquals(DateUtil.getFormattedTime(localTime, dateFormatter), expectedTimeString);
    }

    @DataProvider
    public Object[][] getFormattedTimeTestData() {
        return new Object[][]{
                {LocalTime.now().withHour(1).withMinute(15), timeFormatter(), "01:15"},
                {null, timeFormatter(), ""}
        };
    }

    @Test(dataProvider = "getFormattedJodaTimeTestData")
    public void testGetFormattedJodaTime_givenLocalTimeAndFormatter_verifiesTimeString(org.joda.time.LocalTime localTime,
                                                                                       org.joda.time.format.DateTimeFormatter dateFormatter, String expectedTimeString) {
        assertEquals(DateUtil.getFormattedTime(localTime, dateFormatter), expectedTimeString);
    }

    @DataProvider
    public Object[][] getFormattedJodaTimeTestData() {
        return new Object[][]{
                {org.joda.time.LocalTime.now().withHourOfDay(1).withMinuteOfHour(15), timeFormatterJoda(), "01:15"},
                {null, timeFormatterJoda(), ""}
        };
    }

    @Test(dataProvider = "testGetFormattedJodaDateTestData")
    public void testGetFormattedDate_givenLocalDateAndFormatter_verifiesDateString(LocalDate localDate, org.joda.time.format.DateTimeFormatter dateTimeFormatter,
                                                                                   String expectedDateString) {
        assertEquals(DateUtil.getFormattedDate(localDate, dateTimeFormatter), expectedDateString);
    }

    @DataProvider
    public Object[][] testGetFormattedJodaDateTestData() {
        return new Object[][]{
                {org.joda.time.LocalDate.now().withYear(2018).withMonthOfYear(9).withDayOfMonth(1), dateFormatterJoda(), "01.09.2018"},
                {null, dateFormatterJoda(), ""}
        };
    }

    @Test(dataProvider = "testGetFormattedDateTestData")
    public void testGetFormattedDate_givenLocalDateAndFormatter_verifiesDateString(java.time.LocalDate localDate, DateTimeFormatter dateTimeFormatter,
                                                                                   String expectedDateString) {
        assertEquals(DateUtil.getFormattedDate(localDate, dateTimeFormatter), expectedDateString);
    }

    @DataProvider
    public Object[][] testGetFormattedDateTestData() {
        return new Object[][]{
                {java.time.LocalDate.now().withYear(2018).withMonth(9).withDayOfMonth(1), dateFormatter(), "01.09.2018"},
                {null, dateFormatter(), ""}
        };
    }

    private DateTimeFormatter timeFormatter() {
        return DateTimeFormatter.ofPattern(TIME_FORMAT);
    }

    private org.joda.time.format.DateTimeFormatter timeFormatterJoda() {
        return org.joda.time.format.DateTimeFormat.forPattern(TIME_FORMAT);
    }

    private org.joda.time.format.DateTimeFormatter dateFormatterJoda() {
        return org.joda.time.format.DateTimeFormat.forPattern(DATE_FORMAT);
    }

    private DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Test(dataProvider = "getAgeInYearsTestData")
    public void testGetAgeInYears(LocalDate localDate, String expectedAge) {
        Assert.assertEquals(DateUtil.getAgeInYears(localDate), expectedAge);
    }

    @DataProvider
    public Object[][] getAgeInYearsTestData() {
        return new Object[][]{
                {LocalDate.now(), "0"},
                {LocalDate.now().plusDays(1), ""},
                {LocalDate.now().withYear(2081), ""},
                {LocalDate.now().minusYears(1), "1"},
                {LocalDate.now().withYear(1981).withMonthOfYear(1).withDayOfMonth(1), String.valueOf(LocalDate.now().minusYears(1981).getYear())},
                {null, ""},
        };
    }

    @Test
    public void testSetHourAndMinuteForDate() {
        DateTime date = DateUtil.setHourAndMinuteForDate(DateTime.now(), DateTime.now().withHourOfDay(10).withMinuteOfHour(15));
        assertEquals(10, date.getHourOfDay());
        assertEquals(15, date.getMinuteOfHour());
    }

    @Test(dataProvider = "parseLocalDateTestData")
    public void testConvertToJodaDate_givenDateString_verifiesCorrectParsing(String dateString, LocalDate expectedLocalDate) {
        LocalDate actualLocalDate = DateUtil.parseLocalDate(dateString);
        assertEquals(actualLocalDate, expectedLocalDate);

    }

    @DataProvider
    public Object[][] parseLocalDateTestData() {
        return new Object[][]{
                {"01.01.2018", DateUtil.convertToJodaDate(java.time.LocalDate.of(2018, 1, 1))},
                {"774774774", null}
        };
    }

    @Test(dataProvider = "convertToLocalDateTimeTestData")
    public void testConvertToLocalDateTime(DateTime dateTime, LocalDateTime expectedLocalDateTime) {
        assertEquals(DateUtil.convertToLocalDateTime(dateTime), expectedLocalDateTime);
    }

    @DataProvider
    public Object[][] convertToLocalDateTimeTestData() {
        DateTime dateTime = DateTime.now()
                .withYear(2018)
                .withMonthOfYear(9)
                .withDayOfMonth(30)
                .withHourOfDay(1)
                .withMinuteOfHour(20)
                .withSecondOfMinute(0)
                .withMillis(0);

        return new Object[][]{
                {dateTime,
                        LocalDateTime.of(dateTime.getYear(),
                                dateTime.getMonthOfYear(),
                                dateTime.getDayOfMonth(),
                                dateTime.getHourOfDay(),
                                dateTime.getMinuteOfHour(),
                                dateTime.getSecondOfMinute())
                },
                {null, null}
        };
    }

    @Test(dataProvider = "toLocalDateTestData")
    public void testToLocalDate(Date date, java.time.LocalDate expectedLocalDate) {
        assertEquals(DateUtil.toLocalDate(date), expectedLocalDate);
    }

    @DataProvider
    public Object[][] toLocalDateTestData() {
        DateTime dateTime = DateTime.now()
                .withYear(2018)
                .withMonthOfYear(9)
                .withDayOfMonth(30)
                .withHourOfDay(1)
                .withMinuteOfHour(20);

        return new Object[][]{
                {dateTime.toDate(),
                        java.time.LocalDate.of(dateTime.getYear(),
                                dateTime.getMonthOfYear(),
                                dateTime.getDayOfMonth())
                },
                {null, null}
        };
    }

    @Test(dataProvider = "fromLocalDateTestData")
    public void testFromLocalDate(java.time.LocalDate localDate, Date expectedDate) {
        assertEquals(DateUtil.fromLocalDate(localDate), expectedDate);
    }

    @DataProvider
    public Object[][] fromLocalDateTestData() {
        int dayOfMonth = 30;
        java.time.LocalDate localDate = localDate(dayOfMonth);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, Calendar.SEPTEMBER, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return new Object[][]{
                {localDate, new Date(calendar.getTimeInMillis())},
                {null, null}
        };
    }

    @Test(dataProvider = "dayOfWeekTestData")
    public void testDayOfWeek_givenValidLocalDate_verifiesDayOfWeek(java.time.LocalDate localDate, int expectedDayOfWeek) {
        assertEquals(DateUtil.dayOfWeek(localDate), expectedDayOfWeek);
    }

    @DataProvider
    public Object[][] dayOfWeekTestData() {
        java.time.LocalDate localDate = java.time.LocalDate.now()
                .withYear(2018)
                .withMonth(10);

        return new Object[][]{
                {localDate.withDayOfMonth(1), 2},
                {localDate.withDayOfMonth(2), 3},
                {localDate.withDayOfMonth(3), 4},
                {localDate.withDayOfMonth(4), 5},
                {localDate.withDayOfMonth(5), 6},
                {localDate.withDayOfMonth(6), 7},
                {localDate.withDayOfMonth(7), 1},
                {localDate.withDayOfMonth(8), 2},
                {localDate.withDayOfMonth(9), 3},
        };
    }

    private java.time.LocalDate localDate(int dayOfMonth) {
        return java.time.LocalDate.now()
                .withYear(2018)
                .withMonth(9)
                .withDayOfMonth(dayOfMonth);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDayOfWeek_givenNull_verifiesIllegalArgumentException() {
        DateUtil.dayOfWeek(null);
    }

    @Test
    public void testGetFormattedShortIdDate() {
        java.time.LocalDate localDate = java.time.LocalDate.of(1976, 9, 30);
        assertEquals(DateUtil.parseShortIdDateString("30091976"), localDate);
    }

    @Test
    public void testLastDateOfYear() {
        assertEquals(DateUtil.lastDateOfYear(2001), java.time.LocalDate.now().withYear(2001).withMonth(LAST_MONTH_OF_YEAR).withDayOfMonth(LAST_DAY_OF_MONTH));
    }

    @Test
    public void testFirstDayOfYear() {
        assertEquals(DateUtil.firstDayOfYear(2002), java.time.LocalDate.now().withYear(2002).withMonth(FIRST_MONTH_OF_YEAR).withDayOfMonth(FIRST_DAY_OF_MONTH));
    }

    @Test
    public void testFirstDayOfAYear() {
        LocalDate expectedDate = LocalDate.parse("2018-01-01");
        assertEquals(DateUtil.firstDayOfYearJT(2018), expectedDate);
    }
}

