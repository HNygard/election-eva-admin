package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ElectionGroupTest {
	private static Validator validator;

	public static final String PROPERTY_ELECTION_EVENT = "electionEvent";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testElectionEventIsNull() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setElectionEvent(null);
		validateProperty(electionGroup, PROPERTY_ELECTION_EVENT, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setId(null);
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setId("");
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setId("123456789");
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setName(null);
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setName("");
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setName(" ");
		validateProperty(electionGroup, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testElectionGroupIsValid() {

		ElectionGroup electionGroup = buildElectionGroup();

		Set<ConstraintViolation<ElectionGroup>> constraintViolations = validator.validate(electionGroup);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		ElectionGroup electionGroup = buildElectionGroup();
		electionGroup.setPk(1L);
		Assert.assertEquals(electionGroup.getAreaPk(AreaLevelEnum.COUNTY), null);
	}

	@Test
	public void testGetElectionPk() {
		ElectionGroup electionGroup = buildElectionGroup();
		Long pk = 1L;
		electionGroup.setPk(pk);

		Assert.assertEquals(electionGroup.getElectionPk(ElectionLevelEnum.ELECTION_GROUP), pk);
		Assert.assertEquals(electionGroup.getElectionPk(ElectionLevelEnum.ELECTION), null);
		Assert.assertEquals(electionGroup.getElectionPk(ElectionLevelEnum.CONTEST), null);
	}

	private ElectionGroup buildElectionGroup() {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setElectionEvent(new ElectionEvent());
		electionGroup.setId("12");
		electionGroup.setName("MyElectionGroup");
		return electionGroup;
	}

	private void validateProperty(final ElectionGroup electionGroup, final String property, final String message) {
		Set<ConstraintViolation<ElectionGroup>> constraintViolations = validator.validate(electionGroup);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}
}
