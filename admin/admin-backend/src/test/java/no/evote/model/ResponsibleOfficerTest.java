package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.evote.validation.ValideringVedManuellRegistrering;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Responsibility;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResponsibleOfficerTest {
	private static Validator validator;

	public static final String PROPERTY_REPORTING_UNIT = "reportingUnit";
	public static final String PROPERTY_NAME_LINE = "nameLine";
	public static final String PROPERTY_FIRST_NAME = "firstName";
	public static final String PROPERTY_LAST_NAME = "lastName";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testReportingUnitIsNull() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setReportingUnit(null);
		validateProperty(responsibleOfficer, PROPERTY_REPORTING_UNIT, ModelTestConstants.MESSAGE_NOT_NULL, true);
	}

	@Test
	public void testNameLineIsNull() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setNameLine(null);
		validateProperty(responsibleOfficer, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE, false);
	}

	@Test
	public void testNameLineIsEmpty() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setNameLine("");
		validateProperty(responsibleOfficer, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE, false);
	}

	@Test
	public void testNameLineIsBlanks() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setNameLine(" ");
		validateProperty(responsibleOfficer, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE, false);
	}

	@Test
	public void testNameLineTooLong() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setNameLine("01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
				+ "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
				+ "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		validateProperty(responsibleOfficer, PROPERTY_NAME_LINE, ModelTestConstants.MESSAGE_SIZE_0_152, false);
	}

	@Test
	public void testFirstNameIsNull() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setFirstName(null);
		validateProperty(responsibleOfficer, PROPERTY_FIRST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testFirstNameIsEmpty() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setFirstName("");
		validateProperty(responsibleOfficer, PROPERTY_FIRST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testFirstNameIsBlanks() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setFirstName(" ");
		validateProperty(responsibleOfficer, PROPERTY_FIRST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testFirstNameTooLong() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setFirstName("wewewewewewewewewewewewewewewewewewewewewweeeeeeewewewewewewewewewewewewewewewewewewewewewweeeeeee");
		validateProperty(responsibleOfficer, PROPERTY_FIRST_NAME, ModelTestConstants.MESSAGE_SIZE_0_50, true);
	}

	@Test
	public void testLastNameIsNull() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setLastName(null);
		validateProperty(responsibleOfficer, PROPERTY_LAST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testLastNameIsEmpty() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setLastName("");
		validateProperty(responsibleOfficer, PROPERTY_LAST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testLastNameIsBlanks() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setLastName(" ");
		validateProperty(responsibleOfficer, PROPERTY_LAST_NAME, StringNotNullEmptyOrBlanks.MESSAGE, true);
	}

	@Test
	public void testLastNameTooLong() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();
		responsibleOfficer.setLastName("wewewewewewewewewewewewewewewewewewewewewweeeeeeewewewewewewewewewewewewewewewewewewewewewweeeeeee");
		validateProperty(responsibleOfficer, PROPERTY_LAST_NAME, ModelTestConstants.MESSAGE_SIZE_0_50, true);
	}

	@Test
	public void testResponsibleOfficerIsValid() {
		ResponsibleOfficer responsibleOfficer = buildResponsibleOfficer();

		Set<ConstraintViolation<ResponsibleOfficer>> constraintViolations = validator.validate(responsibleOfficer, ValideringVedManuellRegistrering.class);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	private ResponsibleOfficer buildResponsibleOfficer() {
		ResponsibleOfficer responsibleOfficer = new ResponsibleOfficer();
		responsibleOfficer.setReportingUnit(new ReportingUnit());
		responsibleOfficer.setResponsibility(new Responsibility());
		responsibleOfficer.setDisplayOrder(1);
		responsibleOfficer.setNameLine("Ola Nordmann");
		responsibleOfficer.setFirstName("Ola");
		responsibleOfficer.setLastName("Nordmann");
		return responsibleOfficer;
	}

	private void validateProperty(final ResponsibleOfficer responsibleOfficer, final String property, final String message, boolean manuellValidering) {
		Set<ConstraintViolation<ResponsibleOfficer>> constraintViolations = sjekkValidering(responsibleOfficer, manuellValidering);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

	private Set<ConstraintViolation<ResponsibleOfficer>> sjekkValidering(ResponsibleOfficer responsibleOfficer, boolean manuellValidering) {
		if (manuellValidering) return validator.validate(responsibleOfficer, ValideringVedManuellRegistrering.class);
		else return validator.validate(responsibleOfficer);
	}

}
