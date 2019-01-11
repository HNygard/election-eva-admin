package no.evote.presentation.validation;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SocialSecurityNumberValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "validInputs")
	public void validate_withValidInput_verifyNoException(String input) throws Exception {
		SocialSecurityNumberValidator validator = initializeMocks(SocialSecurityNumberValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@Test(dataProvider = "invalidInputs", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.invalid.id")
	public void validate_withInvalidInput_verifyException(String input) throws Exception {
		SocialSecurityNumberValidator validator = initializeMocks(SocialSecurityNumberValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@DataProvider(name = "validInputs")
	public Object[][] validInputs() {
		return new Object[][] {
				{ "10037549911" }
		};
	}

	@DataProvider(name = "invalidInputs")
	public Object[][] invalidInputs() {
		return new Object[][] {
				{ "39485768594" },
				{ "18027533221" },
				{ "aaaaa" },
				{ "180275asdfg" },
				{ "asdfgh49911" }
		};
	}
}
