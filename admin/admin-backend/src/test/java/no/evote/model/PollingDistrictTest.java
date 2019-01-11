package no.evote.model;

import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.TECHNICAL;
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
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PollingDistrictTest extends MockUtilsTestCase {
	private static final String PROPERTY_BOROUGH = "borough";
	private static final String POLLING_DISTRICT_ID_1 = "0001";
	private static final String POLLING_DISTRICT_ID_2 = "0002";
	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testBoroughIsNull() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setBorough(null);
		validateProperty(pollingDistrict, PROPERTY_BOROUGH, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testIdIsNull() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setId(null);
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdIsEmpty() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setId("");
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testIdTooLong() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setId("01234");
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_ID, ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testNameIsNull() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setName(null);
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsEmpty() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setName("");
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameIsBlanks() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setName(" ");
		validateProperty(pollingDistrict, ModelTestConstants.PROPERTY_NAME, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testPollingDistrictIsValid() {

		PollingDistrict pollingDistrict = pollingDistrict();

		Set<ConstraintViolation<PollingDistrict>> constraintViolations = validator.validate(pollingDistrict);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testGetAreaPk() {
		PollingDistrict pollingDistrict = pollingDistrict();
		Long pk = 1L;
		pollingDistrict.setPk(pk);

		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.POLLING_DISTRICT), pk);
		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.COUNTRY), null);
		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.COUNTY), null);
		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.MUNICIPALITY), null);
		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.BOROUGH), null);
		Assert.assertEquals(pollingDistrict.getAreaPk(AreaLevelEnum.POLLING_PLACE), null);
	}

	@Test
	public void testGetElectionPk() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setPk(1L);

		Assert.assertEquals(pollingDistrict.getElectionPk(ElectionLevelEnum.ELECTION), null);
	}

	private PollingDistrict pollingDistrict(String id) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setBorough(new Borough());
		pollingDistrict.setId(id);
		pollingDistrict.setName("MyPollingDistrict");
		return pollingDistrict;
	}

	private PollingDistrict pollingDistrict() {
		return pollingDistrict("0123");
	}

	private void validateProperty(final PollingDistrict pollingDistrict, final String property, final String message) {
		Set<ConstraintViolation<PollingDistrict>> constraintViolations = validator.validate(pollingDistrict);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	@Test
	public void before_whenIdOneBeforeIdTwo_returnTrue() throws Exception {
		assertThat(pollingDistrict(POLLING_DISTRICT_ID_1).before(pollingDistrict(POLLING_DISTRICT_ID_2))).isTrue();
	}

	@Test
	public void before_whenIdOneAfterIdTwo_returnFalse() throws Exception {
		assertThat(pollingDistrict(POLLING_DISTRICT_ID_2).before(pollingDistrict(POLLING_DISTRICT_ID_1))).isFalse();
	}

	@Test
	public void before_whenIdOneEqualsIdTwo_returnFalse() throws Exception {
		assertThat(pollingDistrict(POLLING_DISTRICT_ID_1).before(pollingDistrict(POLLING_DISTRICT_ID_1))).isFalse();
	}

	@Test
	public void sortById_returnsComparatorThatSortsCorrectlyById() throws Exception {
		List<PollingDistrict> list = Arrays.asList(
				pollingDistrict("300"),
				pollingDistrict("101"),
				pollingDistrict("100"));

		Collections.sort(list, PollingDistrict.sortById());

		assertThat(list.get(0).getId()).isEqualTo("100");
		assertThat(list.get(1).getId()).isEqualTo("101");
		assertThat(list.get(2).getId()).isEqualTo("300");
	}

	@Test(dataProvider = "isEditableCentrally")
	public void isEditableCentrally_withDataProvider_verifyExpected(PollingDistrictType pollingDistrictType, boolean expected) throws Exception {
		PollingDistrict pollingDistrict = fromType(pollingDistrictType);

		assertThat(pollingDistrict.isEditableCentrally()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] isEditableCentrally() {
		return new Object[][] {
				{ REGULAR, true },
				{ MUNICIPALITY, true },
				{ TECHNICAL, false },
				{ PARENT, false },
				{ CHILD, false }
		};
	}

	private PollingDistrict fromType(PollingDistrictType type) {
		PollingDistrict result = new PollingDistrict();
		if (type == MUNICIPALITY) {
			result.setMunicipality(true);
		} else if (type == PARENT) {
			result.setParentPollingDistrict(true);
		} else if (type == TECHNICAL) {
			result.setTechnicalPollingDistrict(true);
		} else if (type == CHILD) {
			result.setPollingDistrict(createMock(PollingDistrict.class));
		}
		return result;
	}
}
