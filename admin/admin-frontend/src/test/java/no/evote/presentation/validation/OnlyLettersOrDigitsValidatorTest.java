package no.evote.presentation.validation;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OnlyLettersOrDigitsValidatorTest extends BaseFrontendTest {

    @Test(dataProvider = "validInputs")
    public void validate_withValidInput_verifyNoException(String input) throws Exception {
        OnlyLettersOrDigitsValidator validator = initializeMocks(OnlyLettersOrDigitsValidator.class);

        validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
    }

    @Test(dataProvider = "invalidInputs", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.lettersOrDigits.*")
    public void validate_withInvalidInput_verifyException(String input) throws Exception {
        OnlyLettersOrDigitsValidator validator = initializeMocks(OnlyLettersOrDigitsValidator.class);

        validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
    }

    @DataProvider(name = "validInputs")
    public Object[][] validInputs() {
        return new Object[][]{
                {"abcdef"},
                {"ABCDEF"},
                {"ÆØÅáÁàÀéÉèÈ"},
                {"A B C D"},
                {"A.B.C"},
                {"A-B"},
                {"A/B"},
                {"A'en"},
                {"ÖöËëÜüÄä ÔôÊêÛûÂâ"}
        };
    }

    @DataProvider(name = "invalidInputs")
    public Object[][] invalidInputs() {
        return new Object[][]{
                {"%"},
                {"!"},
                {"?"},
                {"="},
                {","},
        };
    }
}
