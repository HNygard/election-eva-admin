package no.evote.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BoroughTest {
	public static final String PROPERTY_MUNICIPALITY = "municipality";
	private static Validator validator;
	private long pollingDistrictPk = 1;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testMunicipalityIsNull() {
		Borough borough = borough();
		borough.setMunicipality(null);
		validateProperty(borough, PROPERTY_MUNICIPALITY, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		Borough borough = borough();
		borough.setId(null);
		validateProperty(borough, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		Borough borough = borough();
		borough.setId("");
		validateProperty(borough, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		Borough borough = borough();
		borough.setId("1234567");
		validateProperty(borough, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		Borough borough = borough();
		borough.setName(null);
		validateProperty(borough, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		Borough borough = borough();
		borough.setName("");
		validateProperty(borough, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		Borough borough = borough();
		borough.setName(" ");
		validateProperty(borough, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testBoroughIsValid() {
		Borough borough = borough();

		Set<ConstraintViolation<Borough>> constraintViolations = validator.validate(borough);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		long municipalityPk = 1;
		long boroughPk = 2;
		Borough borough = borough();
		borough.getMunicipality().setPk(municipalityPk);
		borough.setPk(boroughPk);

		assertThat(borough.getAreaPk(AreaLevelEnum.COUNTRY)).isNull();
		assertThat(borough.getAreaPk(AreaLevelEnum.COUNTY)).isNull();
		assertThat(borough.getAreaPk(AreaLevelEnum.MUNICIPALITY)).isEqualTo(municipalityPk);
		assertThat(borough.getAreaPk(AreaLevelEnum.BOROUGH)).isEqualTo(boroughPk);
		assertThat(borough.getAreaPk(AreaLevelEnum.POLLING_DISTRICT)).isNull();
		assertThat(borough.getAreaPk(AreaLevelEnum.POLLING_PLACE)).isNull();
	}

	@Test
	public void testGetElectionPk() {
		Borough borough = borough();
		borough.setPk(1L);

		Assert.assertEquals(borough.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	private Borough borough(PollingDistrict... pollingDistricts) {
		Borough borough = new Borough();
		borough.setMunicipality(new Municipality());
		borough.setId("010101");
		borough.setName("MyBorough");
		borough.setMunicipality1(false);
		if (pollingDistricts.length > 0) {
			Set<PollingDistrict> pollingDistrictSet = new LinkedHashSet<>();
			Collections.addAll(pollingDistrictSet, pollingDistricts);
			borough.setPollingDistricts(pollingDistrictSet);
		}
		return borough;
	}

	private void validateProperty(final Borough borough, final String property, final String message) {
		Set<ConstraintViolation<Borough>> constraintViolations = validator.validate(borough);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	@Test
	public void getMunicipalityPollingDistrict_givenBoroughWithMunicipalityPollingDistrict_returnsMunicipalityPollingDistrict() throws Exception {
		Borough borough = new Borough();
		borough.setMunicipality1(true);
		PollingDistrict municipalityPollingDistrict = new PollingDistrict();
		municipalityPollingDistrict.setPk(1L);
		municipalityPollingDistrict.setMunicipality(true);
		borough.getPollingDistricts().add(municipalityPollingDistrict);
		PollingDistrict pollingDistrict1 = new PollingDistrict();
		pollingDistrict1.setPk(2L);
		borough.getPollingDistricts().add(pollingDistrict1);
		PollingDistrict pollingDistrict2 = new PollingDistrict();
		
		pollingDistrict2.setPk(3L);
		
		borough.getPollingDistricts().add(pollingDistrict2);

		PollingDistrict result = borough.getMunicipalityPollingDistrict();

		assertThat(result).isSameAs(municipalityPollingDistrict);
	}

	@Test
	public void getMunicipalityPollingDistrict_givenBoroughWithoutMunicipalityPollingDistrict_returnsNull() throws Exception {
		Borough borough = new Borough();
		PollingDistrict pollingDistrict1 = new PollingDistrict();
		pollingDistrict1.setPk(1L);
		borough.getPollingDistricts().add(pollingDistrict1);
		PollingDistrict pollingDistrict2 = new PollingDistrict();
		pollingDistrict2.setPk(2L);
		borough.getPollingDistricts().add(pollingDistrict2);

		PollingDistrict result = borough.getMunicipalityPollingDistrict();

		assertThat(result).isNull();
	}

	@Test
	public void hasRegularPollingDistricts_givenBoroughWithRegularPollingDistricts_returnsTrue() throws Exception {
		Borough borough = new Borough();
		PollingDistrict pollingDistrict1 = new PollingDistrict();
		pollingDistrict1.setPk(1L);
		borough.getPollingDistricts().add(pollingDistrict1);
		PollingDistrict pollingDistrict2 = new PollingDistrict();
		pollingDistrict2.setPk(2L);
		borough.getPollingDistricts().add(pollingDistrict2);

		boolean result = borough.hasRegularPollingDistricts();

		assertThat(result).isTrue();
	}

	@Test
	public void hasRegularPollingDistricts_givenBoroughWithoutRegularPollingDistricts_returnsFalse() throws Exception {
		Borough borough = new Borough();
		borough.setMunicipality1(true);
		PollingDistrict municipalityPollingDistrict = new PollingDistrict();
		municipalityPollingDistrict.setPk(1L);
		municipalityPollingDistrict.setMunicipality(true);
		borough.getPollingDistricts().add(municipalityPollingDistrict);
		PollingDistrict pollingDistrict1 = new PollingDistrict();
		pollingDistrict1.setPk(2L);
		pollingDistrict1.setTechnicalPollingDistrict(true);
		borough.getPollingDistricts().add(pollingDistrict1);
		PollingDistrict pollingDistrict2 = new PollingDistrict();
		
		pollingDistrict2.setPk(3L);
		
		pollingDistrict2.setTechnicalPollingDistrict(true);
		borough.getPollingDistricts().add(pollingDistrict2);

		boolean result = borough.hasRegularPollingDistricts();

		assertThat(result).isFalse();
	}

	@Test
	public void findFirstTechnicalPollingDistrict_whenNoTechnicalPollingDistricts_returnNull() throws Exception {
		Borough borough = borough(
				ordinaryPollingDistrict("0101"),
				ordinaryPollingDistrict("0102"),
				ordinaryPollingDistrict("0103")
				);

		PollingDistrict firstTechnicalPollingDistrict = borough.findFirstTechnicalPollingDistrict();

		assertThat(firstTechnicalPollingDistrict).isNull();
	}

	private PollingDistrict municipalityPollingDistrict(String id) {
		return pollingDistrict(id, true, false);
	}

	private PollingDistrict ordinaryPollingDistrict(String id) {
		return pollingDistrict(id, false, false);
	}

	private PollingDistrict technicalPollingDistrict(String id) {
		return pollingDistrict(id, false, true);
	}

	private PollingDistrict pollingDistrict(String id, boolean municipalityPollingDistrict, boolean technicalPollingDistrict) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setPk(pollingDistrictPk++);
		pollingDistrict.setId(id);
		pollingDistrict.setMunicipality(municipalityPollingDistrict);
		pollingDistrict.setTechnicalPollingDistrict(technicalPollingDistrict);
		return pollingDistrict;
	}

	@Test
	public void findFirstTechnicalPollingDistrict_whenTechnicalPollingDistricts_returnFirstTechnicalPollingDistrict() throws Exception {
		Borough borough = borough(
				municipalityPollingDistrict("0000"),
				technicalPollingDistrict("0001"),
				technicalPollingDistrict("0002")
				);

		PollingDistrict firstTechnicalPollingDistrict = borough.findFirstTechnicalPollingDistrict();

		assertThat(firstTechnicalPollingDistrict).isNotNull();
		assertThat(firstTechnicalPollingDistrict.getId()).isEqualTo("0001");
	}
}
