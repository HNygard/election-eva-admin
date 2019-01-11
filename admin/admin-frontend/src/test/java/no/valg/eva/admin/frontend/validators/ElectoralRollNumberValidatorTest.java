package no.valg.eva.admin.frontend.validators;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ElectoralRollNumberValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "gyldigManntallsnummer")
	public void validate_forGyldigManntallsnummer_girIngenException(String manntallsnummer) throws Exception {
		ElectoralRollNumberValidator validator = initializeMocks(ElectoralRollNumberValidator.class);
		validator.validate(getFacesContextMock(), createMock(UIComponent.class), manntallsnummer);
	}

	@Test(dataProvider = "ugyldigManntallsnummer", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.manntallsnummer.ugyldig")
	public void validate_forUgyldigManntallsnummer_kasterException(String input) throws Exception {
		ElectoralRollNumberValidator validator = initializeMocks(ElectoralRollNumberValidator.class);
		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@DataProvider
	public Object[][] gyldigManntallsnummer() {
		return new Object[][]{
			{"123456789080"},	
			{"123456789072"}
		};
	}

	@DataProvider
	public Object[][] ugyldigManntallsnummer() {
		return new Object[][]{
			{"123456789081"},
			{"18027533221"},
			{"aaaaa"},
			{"180275asdfg"},
			{"asdfgh49911"}
		};
	}
}
