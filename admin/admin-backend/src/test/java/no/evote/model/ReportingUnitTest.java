package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReportingUnitTest {
	private static Validator validator;

	public static final String PROPERTY_MV_AREA = "mvArea";
	public static final String PROPERTY_MV_ELECTION = "mvElection";
	public static final String PROPERTY_NAME_LINE = "nameLine";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testMvAreaIsNull() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setMvArea(null);
		validateProperty(reportingUnit, PROPERTY_MV_AREA, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testMvElectionIsNull() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setMvElection(null);
		validateProperty(reportingUnit, PROPERTY_MV_ELECTION, ModelTestConstants.MESSAGE_NOT_NULL);
	}

	@Test
	public void testNameLineIsNull() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setNameLine(null);
		validateProperty(reportingUnit, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameLineIsEmpty() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setNameLine("");
		validateProperty(reportingUnit, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameLineIsBlanks() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setNameLine(" ");
		validateProperty(reportingUnit, PROPERTY_NAME_LINE, StringNotNullEmptyOrBlanks.MESSAGE);
	}

	@Test
	public void testNameLineTooLong() {
		ReportingUnit reportingUnit = buildReportingUnit();
		reportingUnit.setNameLine("012345678901234sdsdsdsdsdsds56789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		validateProperty(reportingUnit, PROPERTY_NAME_LINE, ModelTestConstants.MESSAGE_SIZE_0_50);
	}

	@Test
	public void testReportingUnitIsValid() {
		ReportingUnit reportingUnit = buildReportingUnit();

		Set<ConstraintViolation<ReportingUnit>> constraintViolations = validator.validate(reportingUnit);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	private ReportingUnit buildReportingUnit() {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setMvArea(new MvArea());
		reportingUnit.setMvElection(new MvElection());
		reportingUnit.setNameLine("MyReportingUnit");
		return reportingUnit;
	}

	private void validateProperty(final ReportingUnit reportingUnit, final String property, final String message) {
		Set<ConstraintViolation<ReportingUnit>> constraintViolations = validator.validate(reportingUnit);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), property);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), message);
	}

}
