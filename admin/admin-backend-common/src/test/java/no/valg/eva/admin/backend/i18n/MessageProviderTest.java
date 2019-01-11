package no.valg.eva.admin.backend.i18n;

import no.evote.exception.EvoteException;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageProviderTest {

	@Test
	public void oversett_gittLocaleOgMeldingsnokkel_finnerFremRiktigVerdi() {
		assertThat(MessageProvider.get(new Locale("nb", "NO"), "@test1")).isEqualTo("bokmål");
		assertThat(MessageProvider.get(new Locale("nn", "NO"), "@test1")).isEqualTo("nynorsk");
	}

	@Test
	public void oversett_gittLocaleMeldingsnokkelOgParameter_finnerFremRiktigVerdi() {
		assertThat(MessageProvider.getWithTranslatedParameters(new Locale("nb", "NO"), "@test2", "param")).isEqualTo("bokmål param");
		assertThat(MessageProvider.getWithTranslatedParameters(new Locale("nn", "NO"), "@test2", "param")).isEqualTo("nynorsk param");
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testGet_gittMeldingsnokkelSomIkkeFinnes_returnererNokkelenMedUtropstegnRundt() {
		MessageProvider.get(new Locale("nb", "NO"), "@test3");
		assertThat(MessageProvider.getWithTranslatedParameters(new Locale("nn", "NO"), "@test3", "param")).isEqualTo("!@test3!");
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testGetWithTranslatedParameters_givenMessageKey_verifiesException() {
		MessageProvider.getWithTranslatedParameters(new Locale("nn", "NO"), "@test3", "param");
	}
}
