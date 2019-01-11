package no.valg.eva.admin.frontend.converters;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.configuration.model.Manntallsnummer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("unused")
public class ManntallsnummerConverterTest {

	@Test(dataProvider = "formatterteManntallsnumre")
	public void getAsString_returnererManntallsnummeretMedSpaceFørValgårssiffer(String gyldigManntallsnummer, String forventetFormattertResultat) {
		Manntallsnummer manntallsnummer = gyldigManntallsnummer != null ? new Manntallsnummer(gyldigManntallsnummer) : null;
		ManntallsnummerConverter manntallsnummerConverter = new ManntallsnummerConverter();
		assertThat(manntallsnummerConverter.getAsString(null, null, manntallsnummer)).isEqualTo(forventetFormattertResultat);
	}

	@DataProvider
	private Object[][] formatterteManntallsnumre() {
		return new Object[][]{
			{ "000001234582", "0000012345 82" },
			{ "000000012344", "0000000123 44" },
			{ null, null }
		};
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getAsString_gittIllegalInputType_kasterException() {
		ManntallsnummerConverter manntallsnummerConverter = new ManntallsnummerConverter();
		manntallsnummerConverter.getAsString(null, null, 1L);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void getAsObject_kasterException() {
		ManntallsnummerConverter manntallsnummerConverter = new ManntallsnummerConverter();
		manntallsnummerConverter.getAsObject(null, null, null);
	}

}
