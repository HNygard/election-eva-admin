package no.evote.presentation.validation;

import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GpsCoordinatesValidatorTest extends BaseFrontendTest {

	@Test(dataProvider = "invalidValues", expectedExceptions = ValidatorException.class, expectedExceptionsMessageRegExp = "@validation.gps")
	public void validate_withInvalidValue_throwsException(String value) throws Exception {
		GpsCoordinatesValidator validator = initializeMocks(GpsCoordinatesValidator.class);

		validator.validate(null, null, value);
	}

	@DataProvider
	public Object[][] invalidValues() {
		return new Object[][] {
				{ "invalid" },
				{ "1212121.123123123" },
				{ "00.123123123123123123123123123, 00.123123123123123123123123123" }
		};
	}

	@Test
	public void validate_withValidValue_nothingHappens() throws Exception {
		GpsCoordinatesValidator validator = initializeMocks(GpsCoordinatesValidator.class);

		validator.validate(null, null, "12.12, 13.13");
	}

	@DataProvider
	public Object[][] validValues() {
		return new Object[][] {
				{ "12.1212,12.1212" },
				{ "12.1212, 12.1212" },
				{ "12.121211212112121, 12.121211212112121" },
			    { "-90.121211212112121, +12.121211212112121" }
		};
	}

}
