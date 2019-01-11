package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MunicipalityTest extends MockUtilsTestCase {
	private static Validator validator;

	private static final String PROPERTY_COUNTY = "county";
	private static final String PROPERTY_LOCALE = "locale";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testCountyIsNull() {
		Municipality municipality = buildMunicipality();
		municipality.setCounty(null);
		validateProperty(municipality, PROPERTY_COUNTY, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		Municipality municipality = buildMunicipality();
		municipality.setId(null);
		validateProperty(municipality, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		Municipality municipality = buildMunicipality();
		municipality.setId("");
		validateProperty(municipality, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		Municipality municipality = buildMunicipality();
		municipality.setId("01234");
		validateProperty(municipality, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		Municipality municipality = buildMunicipality();
		municipality.setName(null);
		validateProperty(municipality, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		Municipality municipality = buildMunicipality();
		municipality.setName("");
		validateProperty(municipality, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		Municipality municipality = buildMunicipality();
		municipality.setName(" ");
		validateProperty(municipality, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameTooLong() {
		Municipality municipality = buildMunicipality();
		municipality.setName("01234567sdsdsdsdsdsdsdsdsdsdrererererererererreererererer89012345678901234567890");
		validateProperty(municipality, ModelTestConstants.PROPERTY_NAME, ModelTestConstants.MESSAGE_SIZE_0_50);
	}

	@Test
	public void testLocaleIsNull() {
		Municipality municipality = buildMunicipality();
		municipality.setLocale(null);
		validateProperty(municipality, PROPERTY_LOCALE, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testMunicipalityIsValid() {

		Municipality municipality = buildMunicipality();

		Set<ConstraintViolation<Municipality>> constraintViolations = validator.validate(municipality);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		Municipality municipality = buildMunicipality();
		Long pk = 1L;
		municipality.setPk(pk);

		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.MUNICIPALITY), pk);
		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.COUNTRY), null);
		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.COUNTY), null);
		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.BOROUGH), null);
		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.POLLING_DISTRICT), null);
		Assert.assertEquals(municipality.getAreaPk(AreaLevelEnum.POLLING_PLACE), null);
	}

	@Test
	public void testGetElectionPk() {
		Municipality municipality = buildMunicipality();
		municipality.setPk(1L);

		Assert.assertEquals(municipality.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	@Test
	public void hasBoroughs_withMoreThanOneBorough_returnsTrue() {
		Municipality municipality = buildMunicipality();
		municipality.add(createMock(Borough.class));
		municipality.add(createMock(Borough.class));

		Assertions.assertThat(municipality.hasBoroughs()).isTrue();
	}

	private Municipality buildMunicipality() {
		Municipality municipality = new Municipality();
		municipality.setCounty(new County());
		municipality.setId("0123");
		municipality.setName("MyMunicipality");
		municipality.setLocale(new Locale());
		municipality.setElectronicMarkoffs(true);

		return municipality;
	}

	private void validateProperty(final Municipality municipality, final String property, final String message) {
		Set<ConstraintViolation<Municipality>> constraintViolations = validator.validate(municipality);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}
}
