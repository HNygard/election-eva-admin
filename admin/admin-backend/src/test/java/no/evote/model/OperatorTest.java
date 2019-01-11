package no.evote.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.validation.OperatorValidationManual;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OperatorTest {
	private static Validator validator;
	public static final String PROPERTY_ELECTION = "election";
	public static final String PROPERTY_ID = "id";

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testIdIsOk() {
		Operator operator = buildOperator();

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testIdIsNaN() {
		Operator operator = buildOperator();
		operator.setId("1231231231xz");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
	}

	@Test
	public void testIdIsNull() {
		Operator operator = buildOperator();
		operator.setId(null);

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), PROPERTY_ID);
	}

	@Test
	public void testFirstNameMissing() {
		Operator operator = buildOperator();
		operator.setFirstName(null);

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testFirstNameIsBogus() {
		Operator operator = buildOperator();
		operator.setFirstName("Name123");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testLastNameMissing() {
		Operator operator = buildOperator();
		operator.setLastName(null);

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testLastNameIsBogus() {
		Operator operator = buildOperator();
		operator.setLastName("Name123");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testPostTownIsBogus() {
		Operator operator = buildOperator();
		operator.setPostTown("Name123");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testFirstNameBogusIsOk() {

		Operator operator = buildOperator();
		operator.setFirstName("Navn231");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "firstName");
	}

	@Test
	public void testFirstNameIsTooLong() {

		Operator operator = buildOperator();
		operator.setFirstName("stringTooLongqqwqwqwqwqwqwqwqwqwqwqwqwqwqwqwqwwwqqwasasasasasasssasaasasasaasas");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "firstName");
	}



	@Test
	public void testPostalCodeTooLong() {

		Operator operator = buildOperator();
		operator.setPostalCode("123123");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "postalCode");
	}

	@Test
	public void testPostalCodeIsBogus() {

		Operator operator = buildOperator();
		operator.setPostalCode("ab12");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "postalCode");
	}

	@Test
	public void testAddressLine1IsBogusIsOk() {

		Operator operator = buildOperator();
		operator.setAddressLine1("  gata");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validateProperty(operator, "addressLine1", OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testAddressLine2IsBogusIsOk() {

		Operator operator = buildOperator();
		operator.setAddressLine2(" --gata");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testAddressLine1TooLong() {

		Operator operator = buildOperator();
		operator.setAddressLine1("ssdsdksodksldksldøsklldskldskdlkdlskldskldsksdsdsdsdsdsdssdsdsdsdsdsdsdsdsdsdsdsdsdsdsdsddlskdlskdlsldklskdslsd");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "addressLine1");
	}

	@Test
	public void testAddressLine2TooLong() {

		Operator operator = buildOperator();
		operator.setAddressLine2("ssdsdksodksldksldøsklldskldskdlkdlskldskldskdlskdlskdlsldklskdslsd");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "addressLine2");
	}

	@Test
	public void testTlfTooLong() {

		Operator operator = buildOperator();
		operator.setTelephoneNumber("1234567890123456789012345678901234567890");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 2);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "telephoneNumber");
	}

	@Test
	public void testTlfWrongFormat() {

		Operator operator = buildOperator();
		operator.setTelephoneNumber("+1234acd");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getPropertyPath().toString(), "telephoneNumber");
	}

	@Test
	public void testTlfOK() {

		Operator operator = buildOperator();
		operator.setTelephoneNumber("+4712345678");

		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		Assert.assertEquals(constraintViolations.size(), 0);
	}

	private Operator buildOperator() {
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		operator.setId("25038014008");
		operator.setFirstName("Firstname");
		operator.setLastName("Lastname");
		return operator;
	}

}
