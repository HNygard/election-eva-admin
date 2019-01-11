package no.valg.eva.admin.frontend.validators;

import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VotingCardTextValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "gyldigTekst")
	public void validate_forGyldigTekst_girIngenException(String manntallsnummer) throws Exception {
		VotingCardTextValidator validator = initializeMocks(VotingCardTextValidator.class);
		validator.validate(getFacesContextMock(), createMock(UIComponent.class), manntallsnummer);
	}

	@Test(dataProvider = "ugyldigTekst",
			expectedExceptions = ValidatorException.class,
			expectedExceptionsMessageRegExp = "@config.local.election_card.infoText_invalidSize")
	public void validate_forUgyldigTekst_kasterException(String input) throws Exception {
		VotingCardTextValidator validator = initializeMocks(VotingCardTextValidator.class);
		validator.validate(getFacesContextMock(), createMock(UIComponent.class), input);
	}

	@DataProvider
	public Object[][] gyldigTekst() {
		return new Object[][] {
				{ null },
				{ "" },
				{ "Dette er en" },
				{ "Dette er en\ngyldig\ntekst" },
				{ "30tegnAAAAAAAAAAAAAAAAAAAAAAAA\n30tegnAAAAAAAAAAAAAAAAAAAAAAAA\n30tegnAAAAAAAAAAAAAAAAAAAAAAAA" + "\n30tegnAAAAAAAAAAAAAAAAAAAAAAAA\n30tegnAAAAAAAAAAAAAAAAAAAAAAAA" },
				{ "150tegnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" },
				{ "147tegnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + "\n" + ":)" },
				{ "første linje:\nandre:\ntredje:\nfjerde:\nfemte: 110tegnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" }
		};
	}

	@DataProvider
	public Object[][] ugyldigTekst() {
		return new Object[][] {
				{ "151tegnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" },
				{ "149tegnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + "\n" + "2T" },
				{ "11\n88\n77\n66\n55\ne4" },
				{ "\n\n\n\n\n\n" },
				{ "sdfsdfsdf\n\n\n\n\nsdfs" }
		};
	}

}
