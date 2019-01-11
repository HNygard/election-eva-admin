package no.valg.eva.admin.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {

    private static final int LAST_DAY_OF_WEEK = 7;
    private static final int LAST_DAY_OF_MONTH = 31;
    private static final int LAST_MONTH_OF_YEAR = 12;
    private static final int FIRST_MONTH_OF_YEAR = 1;
    private static final int FIRST_DAY_OF_MONTH = 1;
    public static final int LAST_HOUR_OF_DAY = 23;

    private static Locale locale = Locale.getDefault();

    private static java.time.format.DateTimeFormatter shortDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static java.time.format.DateTimeFormatter shortTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
    private static java.time.format.DateTimeFormatter dateIdFormatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");

    private DateUtil() {
    }

    private static DateTimeFormatter getDateFormat() {
        return DateTimeFormat.forPattern("dd.MM.yyyy").withLocale(locale);
    }

    private static DateTimeFormatter getDateIdFormat() {
        return DateTimeFormat.forPattern("ddMMyy").withLocale(locale);
    }

    public static String formatToShortDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("Argument localDateTime is null");
        }

        return shortDateFormatter.format(localDateTime);
    }

    public static String formatToShortTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("Argument localDateTime is null");
        }

        return shortTimeFormatter.format(localDateTime);
    }

    public static String getFormattedShortDate(DateTime date) {
        if (date == null) {
            return "";
        }
        return getDateFormat().print(date);
    }

    public static String getFormattedShortDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return shortDateFormatter.format(date);
    }

    public static String getFormattedShortDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return getDateFormat().print(date);
    }

    public static String getFormattedShortIdDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return getDateIdFormat().print(date);
    }

    public static String getFormattedTime(LocalTime localTime, DateTimeFormatter timeFormat) {
        if (localTime == null) {
            return "";
        }
        return timeFormat.print(localTime);
    }

    public static String getFormattedTime(java.time.LocalTime localTime, java.time.format.DateTimeFormatter timeFormat) {
        if (localTime == null) {
            return "";
        }
        return timeFormat.format(localTime);
    }

    public static String getFormattedDate(LocalDate localDate, DateTimeFormatter timeFormat) {
        if (localDate == null) {
            return "";
        }
        return timeFormat.print(localDate);
    }

    public static String getFormattedDate(java.time.LocalDate localDate, java.time.format.DateTimeFormatter timeFormat) {
        if (localDate == null) {
            return "";
        }
        return timeFormat.format(localDate);
    }

    public static LocalDate parseLocalDate(String date) {
        DateTimeFormatter dateFormat = getDateFormat();
        try {
            return dateFormat.parseLocalDate(date);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static DateTime setHourAndMinuteForDate(DateTime date, DateTime hourAndMinute) {
        return date.withHourOfDay(hourAndMinute.getHourOfDay()).withMinuteOfHour(hourAndMinute.getMinuteOfHour());
    }

    public static String getAgeInYears(LocalDate dateOfBirth) {
        if (dateOfBirth != null) {
            LocalDate now = LocalDate.now();
            if (dateOfBirth.isAfter(now)) {
                return "";
            }
            int ageInYears = new Period(dateOfBirth, now).getYears();
            return Integer.toString(ageInYears);
        }
        return "";
    }

    public static LocalDate convertToJodaDate(java.time.LocalDate javaTimeLocalDate) {
        return new LocalDate(javaTimeLocalDate.getYear(), javaTimeLocalDate.getMonthValue(), javaTimeLocalDate.getDayOfMonth());
    }

    public static org.joda.time.LocalDateTime toJodaLocalDateTime(LocalDateTime javaTimeLocalDateTime) {
        return org.joda.time.LocalDateTime.now().withYear(javaTimeLocalDateTime.getYear())
                .withMonthOfYear(javaTimeLocalDateTime.getMonth().getValue())
                .withDayOfMonth(javaTimeLocalDateTime.getDayOfMonth())
                .withHourOfDay(javaTimeLocalDateTime.getHour())
                .withMinuteOfHour(javaTimeLocalDateTime.getMinute())
                .withSecondOfMinute(javaTimeLocalDateTime.getSecond());

    }

    public static LocalDateTime convertToLocalDateTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
    }

    public static java.time.LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date fromLocalDate(java.time.LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static java.time.LocalDate toLocalDate(LocalDate date) {
        if (date == null) return null;
        return java.time.LocalDate.of(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }
    
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // for mandag og returnerte dermed 1 dag høyere. Property filene forventer at dag 1 i uka er søndag for øyeblikket - Ref under:
    public static int dayOfWeek(java.time.LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("LocalDate is null!");
        }
        int dayOfWeek = java.time.LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()).getDayOfWeek().getValue();
        return dayOfWeek < LAST_DAY_OF_WEEK ? dayOfWeek + 1 : 1;
    }

    public static java.time.LocalDate parseShortIdDateString(String dateString) {
        return java.time.LocalDate.from(dateIdFormatter.parse(dateString));
    }

    public static String formatShortIdDate(java.time.LocalDate localDate) {
        return dateIdFormatter.format(localDate);
    }

    public static java.time.LocalDate lastDateOfYear(int year) {
        return java.time.LocalDate.of(year, LAST_MONTH_OF_YEAR, LAST_DAY_OF_MONTH);
    }

    public static java.time.LocalDate firstDayOfYear(int year) {
        return java.time.LocalDate.of(year, FIRST_MONTH_OF_YEAR, FIRST_DAY_OF_MONTH);
    }

    public static LocalDateTime startOfDay(LocalDate localDate) {
        return startOfDay(toLocalDate(localDate));
    }

    public static LocalDateTime startOfDay(java.time.LocalDate localDate) {
        return LocalDateTime.of(localDate, java.time.LocalTime.now()).with(java.time.LocalTime.MIN);
    }

    public static java.time.LocalDateTime endOfDay(LocalDate localDate) {
        return endOfDay(toLocalDate(localDate));
    }

    public static java.time.LocalDateTime endOfDay(java.time.LocalDate localDate) {
        return LocalDateTime.of(localDate, java.time.LocalTime.now()).with(java.time.LocalTime.MAX);
    }
    
    public static LocalDate firstDayOfYearJT(int year) {
        return new LocalDate(year, 1, 1);
    }

    public static org.joda.time.LocalDateTime toLocalDateTime(org.joda.time.LocalDate date, org.joda.time.LocalTime time) {
        return new org.joda.time.LocalDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour());
    }
}
