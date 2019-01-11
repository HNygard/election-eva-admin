package no.evote.presentation.validation;

import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import static org.mockito.Mockito.when;

public class OnlyLettersAndSeparatorsValidatorTest extends BaseFrontendTest {

    @Test(dataProvider = "validInputs")
    public void validate_withValidInput_verifyNoException(String input) throws Exception {
        OnlyLettersAndSeparatorsValidator validator = initializeMocks(OnlyLettersAndSeparatorsValidator.class);

        validator.validate(getFacesContextMock(), uiComponent(null), input);
    }

    @DataProvider
    private Object[][] validInputs() {
        return new Object[][]{
            {"abcdef"},
            {"ABCDEF"},
            {"ÆØÅáÁàÀéÉèÈ"},
            {"A B C D"},
            {"A.B.C"},
            {"A-B"},
            {"A'en"},
            {"ÖöËëÜüÄä ÔôÊêÛûÂâ"},
            {"/"}
        };
    }

    private UIComponent uiComponent(String extraChars) {
        UIComponent uiComponent = createMock(UIComponent.class);
        when(uiComponent.getAttributes().get("extrachars")).thenReturn(extraChars);
        return uiComponent;
    }

    @Test(dataProvider = "invalidInputs", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.letters.*")
    public void validate_withInvalidInput_verifyException(String input) throws Exception {
        OnlyLettersAndSeparatorsValidator validator = initializeMocks(OnlyLettersAndSeparatorsValidator.class);

        validator.validate(getFacesContextMock(), uiComponent(null), input);
    }

    @DataProvider
    private Object[][] invalidInputs() {
        return new Object[][]{
                {"%"},
                {"!"},
                {"?"},
                {"="},
                {"A,B,C"}
        };
    }
    
    @Test(dataProvider = "inputsWithExtraChars")
    public void validate_withExtraChars_verifiesWithBaseCharsPlusExtraChars(String extraChars, String validInput, String invalidInput) throws Exception {
        OnlyLettersAndSeparatorsValidator validator = initializeMocks(OnlyLettersAndSeparatorsValidator.class);

        validator.validate(getFacesContextMock(), uiComponent(extraChars), validInput);
        expectException(ValidatorException.class, () -> validator.validate(getFacesContextMock(), uiComponent(extraChars), invalidInput));
    }

    @DataProvider
    private Object[][] inputsWithExtraChars() {
        return new Object[][]{
                {"%", "ASDF%", "ASDF!"},
                {"?", "ÆØÅ?", "ÆØÅ%"},
                {",", "A,B-D", "A,B&C"}
        };
    }
}
