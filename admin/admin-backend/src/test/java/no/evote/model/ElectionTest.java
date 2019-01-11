package no.evote.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ElectionTest {
	public static final String PROPERTY_ELECTION_GROUP = "electionGroup";
	public static final String PROPERTY_LOCALE = "locale";
	public static final String PROPERTY_ELECTION_TYPE = "electionType";
	public static final String PROPERTY_END_DATE_OF_BIRTH = "endDateOfBirth";
	public static final String PROPERTY_AREA_LEVEL = "areaLevel";
	public static final String BASELINE_VOTE_FACTOR = "baselineVoteFactor";
	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testElectionGroupIsNull() {
		Election election = buildElection();
		election.setElectionGroup(null);
		validateProperty(election, PROPERTY_ELECTION_GROUP, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		Election election = buildElection();
		election.setId(null);
		validateProperty(election, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		Election election = buildElection();
		election.setId("");
		validateProperty(election, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		Election election = buildElection();
		election.setId("012");
		validateProperty(election, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		Election election = buildElection();
		election.setName(null);
		validateProperty(election, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		Election election = buildElection();
		election.setName("");
		validateProperty(election, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		Election election = buildElection();
		election.setName(" ");
		validateProperty(election, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testElectionTypeIsNull() {
		Election election = buildElection();
		election.setElectionType(null);
		validateProperty(election, PROPERTY_ELECTION_TYPE, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testEndDateOfBirthIsNull() {
		Election election = buildElection();
		election.setEndDateOfBirth(null);
		validateProperty(election, PROPERTY_END_DATE_OF_BIRTH, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testEndDateOfBirthIsNotPast() {
		Election election = buildElection();
		election.setEndDateOfBirth(LocalDate.now().plusYears(1));
		validateProperty(election, PROPERTY_END_DATE_OF_BIRTH, ModelTestConstants.MESSAGE_MUST_BE_IN_THE_PAST);
	}

	@Test
	public void testAreaLevelIsBelowMin() {
		Election election = buildElection();
		election.setAreaLevel(-1);
		validateProperty(election, PROPERTY_AREA_LEVEL, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testAreaLevelIsAboveMax() {
		Election election = buildElection();
		election.setAreaLevel(10000);
		validateProperty(election, PROPERTY_AREA_LEVEL, ModelTestConstants.MESSAGE_MAX_7);
	}

	@Test
	public void testBaselineVoteFactorIsBelowMin() {
		Election election = buildElection();
		election.setBaselineVoteFactor(new BigDecimal(-1));
		validateProperty(election, BASELINE_VOTE_FACTOR, ModelTestConstants.MESSAGE_MIN_0);
	}

	@Test
	public void testBaselineVoteFactorIsAboveMax() {
		Election election = buildElection();
		election.setBaselineVoteFactor(new BigDecimal("1.1"));
		validateProperty(election, BASELINE_VOTE_FACTOR, ModelTestConstants.MESSAGE_MAX_1);
	}

	@Test
	public void testElectionIsValid() {
		Election election = buildElection();

		Set<ConstraintViolation<Election>> constraintViolations = validator.validate(election);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		Election election = buildElection();
		election.setPk(1L);
		Assert.assertEquals(election.getAreaPk(AreaLevelEnum.COUNTY), null);
	}

	@Test
	public void testGetElectionPk() {
		Election election = buildElection();
		Long pk = 1L;
		election.setPk(pk);

		Assert.assertEquals(election.getElectionPk(ElectionLevelEnum.ELECTION), pk);
		Assert.assertEquals(election.getElectionPk(ElectionLevelEnum.CONTEST), null);
		Assert.assertEquals(election.getElectionPk(ElectionLevelEnum.ELECTION_GROUP), null);
	}

	private Election buildElection() {
		Election election = new Election();
		election.setElectionGroup(new ElectionGroup());
		election.setId("01");
		election.setName("MyElection");
		election.setElectionType(new ElectionType());
		election.setEndDateOfBirth(LocalDate.now().minusYears(18));
		return election;
	}

	private void validateProperty(final Election election, final String property, final String message) {
		Set<ConstraintViolation<Election>> constraintViolations = validator.validate(election);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	@Test
	public void hasLevelingSeats_givenLevelingSeatsZero_returnsFalse() throws Exception {
		Election election = new Election();
		election.setLevelingSeats(0);
		assertThat(election.hasLevelingSeats()).isFalse();
	}

	@Test
	public void hasLevelingSeats_givenLevelingSeatsNonZero_returnsTrue() throws Exception {
		Election election = new Election();
		election.setLevelingSeats(1);
		assertThat(election.hasLevelingSeats()).isTrue();
	}
}

