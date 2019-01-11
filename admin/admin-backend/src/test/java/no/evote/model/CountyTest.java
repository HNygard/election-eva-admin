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
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CountyTest {
	private static Validator validator;

	public static final String PROPERTY_COUNTRY = "country";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testCountryIsNull() {
		County county = buildCounty();
		county.setCountry(null);
		validateProperty(county, PROPERTY_COUNTRY, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		County county = buildCounty();
		county.setId(null);
		validateProperty(county, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		County county = buildCounty();
		county.setId("");
		validateProperty(county, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		County county = buildCounty();
		county.setId("123");
		validateProperty(county, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		County county = buildCounty();
		county.setName(null);
		validateProperty(county, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		County county = buildCounty();
		county.setName("");
		validateProperty(county, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		County county = buildCounty();
		county.setName(" ");
		validateProperty(county, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameTooLong() {
		County county = buildCounty();
		county.setName("sdsdsdsdsdsdsdsdfdsdsdsdsdsdsdsdsdsdsdsdsdsdssdsdsdsdsddssdsdsdssfsdfsdfsdfsdfs");
		validateProperty(county, ModelTestConstants.PROPERTY_NAME, ModelTestConstants.MESSAGE_SIZE_0_50);
	}

	@Test
	public void testCountyIsValid() {
		County county = buildCounty();

		Set<ConstraintViolation<County>> constraintViolations = validator.validate(county);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		County county = buildCounty();
		Long pk = 1L;
		county.setPk(pk);

		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.COUNTY), pk);
		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.COUNTRY), null);
		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.MUNICIPALITY), null);
		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.BOROUGH), null);
		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.POLLING_DISTRICT), null);
		Assert.assertEquals(county.getAreaPk(AreaLevelEnum.POLLING_PLACE), null);
	}

	@Test
	public void testGetElectionPk() {
		County county = buildCounty();
		county.setPk(1L);

		Assert.assertEquals(county.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	private County buildCounty() {
		County county = new County();
		county.setCountry(new Country());
		county.setId("57");
		county.setName("MyCounty");
		county.setLocale(new Locale());

		return county;
	}

	private void validateProperty(final County county, final String property, final String message) {
		Set<ConstraintViolation<County>> constraintViolations = validator.validate(county);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

}
