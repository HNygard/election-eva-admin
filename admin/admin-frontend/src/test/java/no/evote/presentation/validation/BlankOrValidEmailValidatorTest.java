package no.evote.presentation.validation;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BlankOrValidEmailValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "validInputs")
	public void validate_withValidInput_verifyNoException(String input) throws Exception {
		BlankOrValidEmailValidator validator = initializeMocks(BlankOrValidEmailValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@Test(dataProvider = "invalidInputs", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.email")
	public void validate_withInvalidInput_verifyException(String input) throws Exception {
		BlankOrValidEmailValidator validator = initializeMocks(BlankOrValidEmailValidator.class);

		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@DataProvider(name = "validInputs")
	public Object[][] validInputs() {
		return new Object[][] {
				{ "a1@b2.cc" },
				{ "a-._3@test.no" },
				{ "a@aa.aa" }
		};
	}

	@DataProvider(name = "invalidInputs")
	public Object[][] invalidInputs() {
		return new Object[][] {
				{ "aaa" },
				{ "aaa@aaa" },
				{ "a@a.a" },
				{ "aa@a.aa" },
				{ "aa@aa.a" },
				{ "aa@aa.AA" },
				{ "aa@aa.a1" },
				{ "test#@test.no" },
				{ "test@test#.no" }
		};
	}
}
