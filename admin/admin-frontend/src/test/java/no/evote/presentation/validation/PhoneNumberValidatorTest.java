package no.evote.presentation.validation;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PhoneNumberValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "validInputs")
	public void validate_withValidInput_verifyNoException(String input) throws Exception {
		PhoneNumberValidator validator = initializeMocks(PhoneNumberValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@Test(dataProvider = "invalidInputs", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.tlf.regex")
	public void validate_withInvalidInput_verifyException(String input) throws Exception {
		PhoneNumberValidator validator = initializeMocks(PhoneNumberValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@DataProvider(name = "validInputs")
	public Object[][] validInputs() {
		return new Object[][] {
				{ "+123" },
				{ "123" },
				{ "+11223344556677" },
				{ "11223344556677" },
		};
	}

	@DataProvider(name = "invalidInputs")
	public Object[][] invalidInputs() {
		return new Object[][] {
				{ "12" },
				{ "asd" },
				{ "111111111111111" },
		};
	}
}
