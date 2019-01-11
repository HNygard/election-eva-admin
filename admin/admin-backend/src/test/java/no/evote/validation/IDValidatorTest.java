package no.evote.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.model.ModelTestConstants;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IDValidatorTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testBlankFails() {
		TestType testType = new TestType();
		testType.setId(" ");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testInternationalCharacterFail() {
		TestType testType = new TestType();
		testType.setId("AÅ");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testDashFails() {
		TestType testType = new TestType();
		testType.setId("A-B");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testDotFails() {
		TestType testType = new TestType();
		testType.setId(".");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testLowercaseFails() {
		TestType testType = new TestType();
		testType.setId("ab");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testLettersAndNumbersFails() {
		TestType testType = new TestType();
		testType.setId("ASDF1");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testUnderscoreFails() {
		TestType testType = new TestType();
		testType.setId("_");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testSingleLetterFails() {
		TestType testType = new TestType();
		testType.setId("A");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ID.MESSAGE);
	}

	@Test
	public void testNullFails() {
		TestType testType = new TestType();
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testEmpty() {
		TestType testType = new TestType();
		testType.setId("");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testSingleNumber() {
		TestType testType = new TestType();
		testType.setId("1");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	public static class TestType {
		private String id;

		@ID(size = 3)
		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}
	}
}
