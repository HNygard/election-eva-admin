package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ElectionEventTest {
	private static Validator validator;

	public static final String PROPERTY_LOCALE = "locale";
	public static final String PROPERTY_ELECTION_EVENT_STATUS = "electionEventStatus";
	public static final String PROPERTY_ELECTION_DAY = "electionDay";
	public static final String PROPERTY_ELECTION_DAY_END_TIME = "electionDayEndTime";
	public static final String PROPERTY_ELECTORAL_ROLL_CUT_OFF_DATE = "electoralRollCutOffDate";
	public static final String PROPERTY_VOTING_CARD_DEADLINE = "votingCardDeadline";
	public static final String PROPERTY_VOTING_CARD_ELECTORAL_ROLL_DATE = "votingCardElectoralRollDate";
	public static final String PROPERTY_ELECTORAL_ROLL_LINES_PER_PAGE = "electoralRollLinesPerPage";
	public static final String PROPERTY_ELECTION_DAIES = "electionDaies";
	public static final String PROPERTY_VOTER_IMPORT_DIR_NAME = "voterImportDirName";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testIdIsNull() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setId(null);
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setId("");
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setId("1234567");
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setName(null);
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setName("");
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setName("    ");
		validateProperty(electionEvent, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testElectionEventStatusIsNull() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setElectionEventStatus(null);
		validateProperty(electionEvent, PROPERTY_ELECTION_EVENT_STATUS, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testLocaleIsNull() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setLocale(null);
		validateProperty(electionEvent, PROPERTY_LOCALE, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testElectoralRollCutOffDate() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setElectoralRollCutOffDate(null);
		isNoViolations(electionEvent);
	}

	@Test
	public void testVotingCardDeadline() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setVotingCardDeadline(null);
		isNoViolations(electionEvent);
	}

	@Test
	public void testVotingCardElectoralRollDate() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setVotingCardElectoralRollDate(null);
		isNoViolations(electionEvent);
	}

	@Test
	public void testElectoralRollLinesPerPageIsBelowMin() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setElectoralRollLinesPerPage(-1);
		validateProperty(electionEvent, PROPERTY_ELECTORAL_ROLL_LINES_PER_PAGE, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testVoterImportDirNotValid() {
		ElectionEvent electionEvent = buildElectionEvent();
		electionEvent.setVoterImportDirName("abc");
		validateProperty(electionEvent, PROPERTY_VOTER_IMPORT_DIR_NAME, "{@validation.absolutePath}");
	}

	@Test
	public void testElectoralRollLinesPerPageIsAboveMax() {
		ElectionEvent electionEvent = buildElectionEvent();
		
		electionEvent.setElectoralRollLinesPerPage(10000);
		
		validateProperty(electionEvent, PROPERTY_ELECTORAL_ROLL_LINES_PER_PAGE, ModelTestConstants.MESSAGE_MAX_9999);
	}

	@Test
	public void testElectionEventIsValid() {

		ElectionEvent electionEvent = buildElectionEvent();

		Set<ConstraintViolation<ElectionEvent>> constraintViolations = validator.validate(electionEvent);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	private ElectionEvent buildElectionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId("200801");
		electionEvent.setName("MyElectionEvent");
		electionEvent.setElectionEventStatus(new ElectionEventStatus());
		electionEvent.setLocale(new Locale());
		electionEvent.setElectoralRollCutOffDate(LocalDate.now());
		electionEvent.setVotingCardDeadline(LocalDate.now());
		electionEvent.setVotingCardElectoralRollDate(LocalDate.now());
		return electionEvent;
	}

	private void validateProperty(final ElectionEvent electionEvent, final String property, final String message) {
		Set<ConstraintViolation<ElectionEvent>> constraintViolations = validator.validate(electionEvent);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	private boolean isNoViolations(final ElectionEvent electionEvent) {
		Set<ConstraintViolation<ElectionEvent>> constraintViolations = validator.validate(electionEvent);

		return constraintViolations.isEmpty();
	}

}
