package no.evote.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import no.valg.eva.admin.common.validator.LuhnChecksumValidation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("unused")
public class LuhnChecksumValidationTest {

	@Test(dataProvider = "gyldigKontrollsiffer")
	public void isGyldigKontrollsiffer_forEnSekvensMedTallMedKorrektLuhnSifferTilSlutt_returnererTrue(String strengMedSifre) {
		assertThat(LuhnChecksumValidation.isGyldigKontrollsiffer(strengMedSifre)).isTrue();
	}

	@DataProvider
	private Object[][] gyldigKontrollsiffer() {
		return new Object[][]{
			{ "18" },
			{ "26" },
			{ "125" },
			{ "1230" }
		};
	}

	@Test(dataProvider = "ugyldigKontrollsiffer")
	public void isGyldigKontrollsiffer_forEnSekvensMedTallMedUgyldigLuhnSifferTilSlutt_returnererFalse(String strengMedSifre) {
		assertThat(LuhnChecksumValidation.isGyldigKontrollsiffer(strengMedSifre)).isFalse();
	}

	@DataProvider
	private Object[][] ugyldigKontrollsiffer() {
		return new Object[][]{
			{ "79927398710" },
			{ "79927398711" },
			{ "79927398712" },
			{ "79927398714" }
		};
	}

	@Test(dataProvider = "luhnBeregneKontrollsiffer")
	public void beregnKontrollsiffer_forEnSekvensMedTall_beregnerLuhnKontrollsiffer(String strengMedSifre, int forventetKontrollsiffer) {
		assertEquals(LuhnChecksumValidation.beregnKontrollsiffer(strengMedSifre), forventetKontrollsiffer);
	}

	@DataProvider
	
	private Object[][] luhnBeregneKontrollsiffer() {
		return new Object[][]{
			{ "0123456", 	6 }, // modulo 10 av 9 * (6*2 -> 3 + 5 + 4*2 + 3 + 2*2 + 1 + 0*2) = modulo 10 av (24 * 9) = 6 
			{ "123456", 	6 }, // modulo 10 av 9 * (6*2 -> 3 + 5 + 4*2 + 3 + 2*2 + 1) = modulo 10 av (24 * 9) = 6 
			{ "012345", 	5 } // modulo 10 av 9 * (5*2 -> 1 + 4 + 3*2 + 2 + 1*2 + 0) = modulo 10 av (15 * 9) = 5
		};
	}
	

	@Test(dataProvider = "strengMedTallUtenKontrollsiffer")
	public void beregnKontrollsifffer_returnererEtSvarSomErGyldigIFølge_isGyldigKontrollsiffer(String strengMedTallUtenKontrollsiffer) {
		int beregnetKontrollsiffer = LuhnChecksumValidation.beregnKontrollsiffer(strengMedTallUtenKontrollsiffer);
		String strengMedTallMedKontrollsiffer = strengMedTallUtenKontrollsiffer + beregnetKontrollsiffer;
		assertTrue(LuhnChecksumValidation.isGyldigKontrollsiffer(strengMedTallMedKontrollsiffer));
	}

	@DataProvider
	private Object[][] strengMedTallUtenKontrollsiffer() {
		return new Object[][]{
			{ "1" }, 
			{ "12" }, 
			{ "123" }, 
			{ "1234" }, 
			{ "12345678901" }, 
			{ "02153123456" }, 
			{ "70732467819" }, 
		};
	}
}
