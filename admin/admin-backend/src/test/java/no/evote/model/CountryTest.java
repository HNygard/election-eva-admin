package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CountryTest {
	private static Validator validator;

	public static final String PROPERTY_ELECTION_EVENT = "electionEvent";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testElectionEventIsNull() {
		Country country = buildCountry();
		country.setElectionEvent(null);
		validateProperty(country, PROPERTY_ELECTION_EVENT, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		Country country = buildCountry();
		country.setId(null);
		validateProperty(country, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		Country country = buildCountry();
		country.setId("");
		validateProperty(country, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		Country country = buildCountry();
		country.setId("123");
		validateProperty(country, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		Country country = buildCountry();
		country.setName(null);
		validateProperty(country, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		Country country = buildCountry();
		country.setName("");
		validateProperty(country, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		Country country = buildCountry();
		country.setName("    ");
		validateProperty(country, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameTooLong() {
		Country country = buildCountry();
		country.setName("lfkjsldkfjsdlkfjsdlkfjsdlkfjslkfjslkjflskjflksdjflksdjflksjflksdjflkjdj");
		validateProperty(country, ModelTestConstants.PROPERTY_NAME, ModelTestConstants.MESSAGE_SIZE_0_50);
	}

	@Test
	public void testCountryIsValid() {
		Country country = buildCountry();

		Set<ConstraintViolation<Country>> constraintViolations = validator.validate(country);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		Country country = buildCountry();
		Long pk = 1L;
		country.setPk(pk);

		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.COUNTRY), pk);
		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.COUNTY), null);
		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.MUNICIPALITY), null);
		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.BOROUGH), null);
		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.POLLING_DISTRICT), null);
		Assert.assertEquals(country.getAreaPk(AreaLevelEnum.POLLING_PLACE), null);
	}

	@Test
	public void testGetElectionPk() {
		Country country = buildCountry();
		country.setPk(1L);

		Assert.assertEquals(country.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	private Country buildCountry() {
		Country country = new Country();
		country.setElectionEvent(new ElectionEvent());
		country.setId("58");
		country.setName("MyCountry");

		return country;
	}

	private void validateProperty(final Country country, final String property, final String message) {
		Set<ConstraintViolation<Country>> constraintViolations = validator.validate(country);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

}
