package no.evote.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LettersOrDigitsValidatorTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testInternationalCharacters() {
		TestType testType = new TestType();
		testType.setText("AÅ");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testDash() {
		TestType testType = new TestType();
		testType.setText("Jan-Arne");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testRegular() {
		TestType testType = new TestType();
		testType.setText("Knut");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testApostrophe() {
		TestType testType = new TestType();
		testType.setText("Valg'09");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testApostropheFailsWithDisallowApostropheParam() {
		TestType testType = new TestType();
		testType.setTextWithoutApostrophe("Valg'09");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testRegularTextWithDisallowApostropheParam() {
		TestType testType = new TestType();
		testType.setTextWithoutApostrophe("Valg09");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testNumbers() {
		TestType testType = new TestType();
		testType.setText("12345");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testDot() {
		TestType testType = new TestType();
		testType.setText("Valg.");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testSpace() {
		TestType testType = new TestType();
		testType.setText("Valg 09");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 0);
	}

	@Test
	public void testBackTickFails() {
		TestType testType = new TestType();
		testType.setText("Valg`09");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	@Test
	public void testQuotationMarkFails() {
		TestType testType = new TestType();
		testType.setText("Valg\"09\"");
		Set<ConstraintViolation<TestType>> constraintViolations = validator.validate(testType);
		Assert.assertEquals(constraintViolations.size(), 1);
	}

	public static class TestType {
		private String text;
		private String textWithoutApostrophe;

		@LettersOrDigits
		public String getText() {
			return text;
		}

		public void setText(final String text) {
			this.text = text;
		}

		@LettersOrDigits(extraChars = " .-")
		public String getTextWithoutApostrophe() {
			return textWithoutApostrophe;
		}

		public void setTextWithoutApostrophe(final String textWithoutApostrophe) {
			this.textWithoutApostrophe = textWithoutApostrophe;
		}
	}
}
