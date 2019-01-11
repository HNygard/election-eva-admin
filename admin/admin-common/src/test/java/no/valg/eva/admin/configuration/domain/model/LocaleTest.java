package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test cases for Locale
 */
public class LocaleTest {
	
	@Test
	public void bokmaalHasJavaLocale_nb_NO() {
		Locale locale = new Locale();
		locale.setId("nb-NO");
		assertEquals(new java.util.Locale("nb", "NO"), locale.toJavaLocale());
	}

	@Test
	public void soerSamiskHasJavaLocale_sma_NO() {
		Locale locale = new Locale();
		locale.setId("sma-NO");
		assertEquals(new java.util.Locale("sma", "NO"), locale.toJavaLocale());
	}
	
	@Test
	public void isNynorsk_localeIdIsNbNo_returnsFalse() {
		Locale locale = new Locale();
		locale.setId("nb-NO");
		assertThat(locale.isNynorsk()).isFalse();
	}

	@Test
	public void isNynorsk_localeIdIsNnNo_returnsTrue() {
		Locale locale = new Locale();
		locale.setId(Locale.NN_NO);
		assertThat(locale.isNynorsk()).isTrue();
	}
}
