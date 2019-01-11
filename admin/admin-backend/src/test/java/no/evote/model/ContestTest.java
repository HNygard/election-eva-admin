package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ContestTest {
	private static Validator validator;

	public static final String PROPERTY_ELECTION = "election";
	public static final String PROPERTY_LOCALE = "locale";
	public static final String PROPERTY_END_DATE_OF_BIRTH = "endDateOfBirth";
	public static final String PROPERTY_MAX_VOTES = "maxVotes";
	public static final String PROPERTY_MIN_VOTES = "minVotes";
	public static final String PROPERTY_MAX_WRITE_IN = "maxWriteIn";
	public static final String PROPERTY_NUMBER_OF_POSITIONS = "numberOfPositions";
	public static final String PROPERTY_MIN_CANDIDATES = "minCandidates";
	public static final String PROPERTY_MAX_CANDIDATES = "maxCandidates";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testElectionIsNull() {
		Contest contest = buildContest();
		contest.setElection(null);
		validateProperty(contest, PROPERTY_ELECTION, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		Contest contest = buildContest();
		contest.setId(null);
		validateProperty(contest, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		Contest contest = buildContest();
		contest.setId("");
		validateProperty(contest, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		Contest contest = buildContest();
		contest.setId("123456789");
		validateProperty(contest, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooShort() {
		Contest contest = buildContest();
		contest.setId("03");
		validateProperty(contest, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		Contest contest = buildContest();
		contest.setName(null);
		validateProperty(contest, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		Contest contest = buildContest();
		contest.setName("");
		validateProperty(contest, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		Contest contest = buildContest();
		contest.setName(" ");
		validateProperty(contest, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testEndDateOfBirthIsNotPast() {
		Contest contest = buildContest();
		contest.setEndDateOfBirth(LocalDate.now().plusYears(1));
		validateProperty(contest, PROPERTY_END_DATE_OF_BIRTH, ModelTestConstants.MESSAGE_MUST_BE_IN_THE_PAST);
	}

	@Test
	public void testMaxWriteInIsBelowMin() {
		Contest contest = buildContest();
		contest.setMaxWriteIn(-1);
		validateProperty(contest, PROPERTY_MAX_WRITE_IN, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testMaxWriteInIsAboveMax() {
		Contest contest = buildContest();
		contest.setMaxWriteIn(10000);
		validateProperty(contest, PROPERTY_MAX_WRITE_IN, ModelTestConstants.MESSAGE_MAX_9999);
	}

	@Test
	public void testNumberOfPositionsIsBelowMin() {
		Contest contest = buildContest();
		contest.setNumberOfPositions(-1);
		validateProperty(contest, PROPERTY_NUMBER_OF_POSITIONS, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testNumberOfPositionsIsAboveMax() {
		Contest contest = buildContest();
		contest.setNumberOfPositions(10000);
		validateProperty(contest, PROPERTY_NUMBER_OF_POSITIONS, ModelTestConstants.MESSAGE_MAX_9999);
	}

	@Test
	public void testMinCandidatesIsBelowMin() {
		Contest contest = buildContest();
		contest.setMinCandidates(-1);
		validateProperty(contest, PROPERTY_MIN_CANDIDATES, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testMinCandidatesIsAboveMax() {
		Contest contest = buildContest();
		contest.setMinCandidates(10000);
		validateProperty(contest, PROPERTY_MIN_CANDIDATES, ModelTestConstants.MESSAGE_MAX_9999);
	}

	@Test
	public void testMaxCandidatesIsBelowMin() {
		Contest contest = buildContest();
		contest.setMaxCandidates(-1);
		validateProperty(contest, PROPERTY_MAX_CANDIDATES, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testMaxCandidatesIsAboveMax() {
		Contest contest = buildContest();
		contest.setMaxCandidates(10000);
		validateProperty(contest, PROPERTY_MAX_CANDIDATES, ModelTestConstants.MESSAGE_MAX_9999);
	}

	@Test
	public void testContestIsValid() {
		Contest contest = buildContest();

		Set<ConstraintViolation<Contest>> constraintViolations = validator.validate(contest);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPkReturnsNull() {
		Contest contest = buildContest();
		Assert.assertEquals(contest.getAreaPk(AreaLevelEnum.COUNTY), null);
	}

	@Test
	public void testGetElectionPk() {
		Contest contest = buildContest();
		Long pk = 1L;
		contest.setPk(pk);

		Assert.assertEquals(contest.getElectionPk(ElectionLevelEnum.CONTEST), pk);
		Assert.assertEquals(contest.getElectionPk(ElectionLevelEnum.ELECTION), null);
		Assert.assertEquals(contest.getElectionPk(ElectionLevelEnum.ELECTION_GROUP), null);
	}

	@Test
	public void testNameIsOk() {
		Contest contest = buildContest();
		contest.setName("Sokkelen nord for 62° N");
		Set<ConstraintViolation<Contest>> constraintViolations = validator.validate(contest);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	private Contest buildContest() {
		Contest contest = new Contest();
		contest.setElection(new Election());
		contest.setId("010101");
		contest.setName("MyContest");

		return contest;
	}

	private void validateProperty(final Contest contest, final String property, final String message) {
		Set<ConstraintViolation<Contest>> constraintViolations = validator.validate(contest);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

}

