package no.evote.model;

import static no.valg.eva.admin.common.AreaPath.OSLO_COUNTY_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PollingPlaceTest {
	private static Validator validator;

	public static final String PROPERTY_POLLING_DISTRICT = "pollingDistrict";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testYetOtherInvalidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("34.12 N, 4.5 Ø");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testValidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("37.771008, -122.41175");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testOtherValidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("37.771008, 22.41175");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testYetAnotherPairOfValidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("37.1, 22");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testYetOtherValidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates(null);
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testInvalidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("59854542, 19876540");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testOtherInvalidGpsCoordinates() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setGpsCoordinates("59, 1987");
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testPollingDistrictIsNull() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setPollingDistrict(null);
		validateProperty(pollingPlace, PROPERTY_POLLING_DISTRICT, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setId(null);
		validateProperty(pollingPlace, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setId("");
		validateProperty(pollingPlace, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setId("1234567");
		validateProperty(pollingPlace, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setName(null);
		validateProperty(pollingPlace, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setName("");
		validateProperty(pollingPlace, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testPollingPlaceIsValid() {
		PollingPlace pollingPlace = buildPollingPlace();

		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		PollingPlace pollingPlace = buildPollingPlace();
		Long pk = 1L;
		pollingPlace.setPk(pk);

		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.POLLING_PLACE), pk);
		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.COUNTRY), null);
		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.COUNTY), null);
		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.MUNICIPALITY), null);
		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.BOROUGH), null);
		Assert.assertEquals(pollingPlace.getAreaPk(AreaLevelEnum.POLLING_DISTRICT), null);
	}

	@Test
	public void testGetElectionPk() {
		PollingPlace pollingPlace = buildPollingPlace();
		pollingPlace.setPk(1L);

		Assert.assertEquals(pollingPlace.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	@Test
	public void sortById_returnsComparatorThatSortsCorrectlyById() throws Exception {
		List<PollingPlace> list = Arrays.asList(
				buildPollingPlace("300"),
				buildPollingPlace("101"),
				buildPollingPlace("100")
				);

		Collections.sort(list, PollingPlace.sortById());

		assertThat(list.get(0).getId()).isEqualTo("100");
		assertThat(list.get(1).getId()).isEqualTo("101");
		assertThat(list.get(2).getId()).isEqualTo("300");
	}

	private void validateProperty(final PollingPlace pollingPlace, final String property, final String message) {
		Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	private PollingPlace buildPollingPlace() {
		return buildPollingPlace("0123");
	}

	private PollingPlace buildPollingPlace(String id) {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setPollingDistrict(new PollingDistrict());
		pollingPlace.setId(id);
		pollingPlace.setName("MyPollingPlace");
		pollingPlace.setPostalCode("0000");
		return pollingPlace;
	}

	@Test
	public void areaPathIsDerivedFromAreaHierarchy() {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setId("0101");
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setId("0101");
		Borough borough = new Borough();
		borough.setId("030101");
		Municipality municipality = new Municipality();
		municipality.setId(AreaPath.OSLO_MUNICIPALITY_ID);
		County county = new County();
		county.setId(OSLO_COUNTY_ID);
		Country country = new Country();
		country.setId("47");
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId("201301");
		country.setElectionEvent(electionEvent);
		county.setCountry(country);
		municipality.setCounty(county);
		borough.setMunicipality(municipality);
		pollingDistrict.setBorough(borough);
		pollingPlace.setPollingDistrict(pollingDistrict);

		assertThat(pollingPlace.areaPath()).isEqualTo(AreaPath.from("201301.47.03.0301.030101.0101.0101"));
	}

}
