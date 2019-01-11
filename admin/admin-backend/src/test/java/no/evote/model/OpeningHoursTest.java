package no.evote.model;

import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class OpeningHoursTest {
    private static final String PROPERTY_ELECTION_DAY = "electionDay";
    private static final String PROPERTY_START_TIMESTAMP = "startTime";
    private static final String PROPERTY_END_TIMESTAMP = "endTime";
    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void isSameDay_withSameDay_returnsTrue() {
        LocalDate date = new LocalDate(2016, 1, 1);
        ElectionDay electionDay = new ElectionDay();
        electionDay.setDate(date);
        OpeningHours oh = new OpeningHours();
        oh.setElectionDay(electionDay);

        assertThat(oh.isSameDay(date)).isTrue();
    }

    @DataProvider
    public static Object[][] equalsTestData() {

        OpeningHours baseOpeningHour = OpeningHours.builder()
                .startTime(new LocalTime().withHourOfDay(8).withMinuteOfHour(0))
                .endTime(new LocalTime().withHourOfDay(16).withMinuteOfHour(0))
                .electionDay(electionDay())
                .build();

        baseOpeningHour.setPk(1L);

        OpeningHours openingHourWithDifferentPK = OpeningHours.builder()
                .startTime(baseOpeningHour.getStartTime())
                .endTime(baseOpeningHour.getEndTime())
                .electionDay(baseOpeningHour.getElectionDay())
                .build();

        openingHourWithDifferentPK.setPk(2L);

        OpeningHours openingHourWithSamePKAndDifferentStartTime = OpeningHours.builder()
                .startTime(baseOpeningHour.getStartTime().minusHours(2))
                .endTime(baseOpeningHour.getEndTime())
                .electionDay(baseOpeningHour.getElectionDay())
                .build();

        openingHourWithSamePKAndDifferentStartTime.setPk(baseOpeningHour.getPk());

        OpeningHours openingHourWithSamePKAndDifferentEndTime = OpeningHours.builder()
                .startTime(baseOpeningHour.getStartTime())
                .endTime(baseOpeningHour.getEndTime().plusHours(2))
                .electionDay(baseOpeningHour.getElectionDay())
                .build();

        openingHourWithSamePKAndDifferentEndTime.setPk(baseOpeningHour.getPk());

        OpeningHours openingHoursNoPk = OpeningHours.builder()
                .startTime(baseOpeningHour.getStartTime())
                .endTime(baseOpeningHour.getEndTime())
                .electionDay(baseOpeningHour.getElectionDay())
                .build();

        return new Object[][]{
                {baseOpeningHour, baseOpeningHour, true},
                {baseOpeningHour, openingHourWithDifferentPK, false},
                {baseOpeningHour, openingHourWithSamePKAndDifferentStartTime, false},
                {baseOpeningHour, openingHourWithSamePKAndDifferentEndTime, false},
                {openingHoursNoPk, openingHoursNoPk, true},
        };
    }

    private static OpeningHours buildOpeningHours() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setPollingPlace(new PollingPlace());
        openingHours.setStartTime(new LocalTime(9, 30));
        openingHours.setEndTime(new LocalTime(12, 0));
        openingHours.setElectionDay(ElectionDay.builder()
                .startTime(openingHours.getStartTime())
                .endTime(openingHours.getEndTime())
                .date(new LocalDate())
                .build());
        return openingHours;
    }

    @DataProvider
    public static Object[][] samePKWithChangedStartOrEndTimeTestData() {

        OpeningHours openingHours = new OpeningHours();
        openingHours.setPk(1L);
        openingHours.setStartTime(new LocalTime());
        openingHours.setEndTime(new LocalTime());

        OpeningHours changedOpeningHours = new OpeningHours();
        changedOpeningHours.setPk(openingHours.getPk());
        changedOpeningHours.setStartTime(openingHours.getStartTime().plusHours(1));
        changedOpeningHours.setEndTime(openingHours.getEndTime().plusHours(2));

        OpeningHours otherOpeningHours = new OpeningHours();
        otherOpeningHours.setPk(2L);
        otherOpeningHours.setStartTime(openingHours.getStartTime());
        otherOpeningHours.setEndTime(openingHours.getEndTime());

        return new Object[][]{
                {openingHours, openingHours, false},
                {openingHours, changedOpeningHours, true},
                {openingHours, otherOpeningHours, false}
        };
    }

    private static ElectionDay electionDay() {
        return ElectionDay.builder()
                .date(new LocalDate())
                .startTime(new LocalTime().withHourOfDay(8).withMinuteOfHour(0))
                .endTime(new LocalTime().withMinuteOfHour(0).withHourOfDay(15))
                .build();
    }

    @Test
    public void testElectionDayIsNull() {
        OpeningHours openingHours = buildOpeningHours();
        openingHours.setElectionDay(null);
        Set<ConstraintViolation<OpeningHours>> constraintViolations = validator.validateProperty(openingHours, PROPERTY_ELECTION_DAY, Default.class);
        assertEquals(constraintViolations.size(), 1);
        assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ELECTION_DAY);
        assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_NOT_NULL);
    }

    @Test
    public void testOpeningHoursIsValid() {
        OpeningHours openingHours = buildOpeningHours();
        openingHours.setElectionDay(new ElectionDay());
        openingHours.setPollingPlace(new PollingPlace());

        Set<ConstraintViolation<OpeningHours>> constraintViolations = validator.validate(openingHours);
        Assert.assertTrue(constraintViolations.isEmpty());
    }

    @Test(dataProvider = "samePKWithChangedStartOrEndTimeTestData")
    public void testSamePKWithChangedStartAndEndTime_GivenTwoOpeningHours_VerifiesIfSame(OpeningHours openingHours, OpeningHours otherOpeningHour, boolean expectingToBeEqual) {
        assertThat(openingHours.samePKWithChangedStartAndEndTime(otherOpeningHour)).isEqualTo(expectingToBeEqual);
    }

    @Test
    public void testStartTimestampIsNull() {
        OpeningHours openingHours = buildOpeningHours();
        openingHours.setStartTime(null);
        Set<ConstraintViolation<OpeningHours>> constraintViolations = validator.validateProperty(openingHours, PROPERTY_START_TIMESTAMP, Default.class);
        assertEquals(constraintViolations.size(), 1);
        assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_START_TIMESTAMP);
        assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_NOT_NULL);
    }

    @Test
    public void testEndTimestampIsNull() {
        OpeningHours openingHours = buildOpeningHours();
        openingHours.setEndTime(null);
        Set<ConstraintViolation<OpeningHours>> constraintViolations = validator.validateProperty(openingHours, PROPERTY_END_TIMESTAMP, Default.class);
        assertEquals(constraintViolations.size(), 1);
        assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_END_TIMESTAMP);
        assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_NOT_NULL);
    }

    @Test(dataProvider = "equalsTestData")
    public void testEquals(OpeningHours openingHours, OpeningHours expectedOpeningHours, boolean expectedToBeEqual) {
        if (expectedToBeEqual) {
            assertEquals(openingHours, expectedOpeningHours);
        } else {
            assertNotEquals(openingHours, expectedOpeningHours);
        }
    }
}

