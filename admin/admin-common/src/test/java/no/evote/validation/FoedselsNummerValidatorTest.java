package no.evote.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import no.valg.eva.admin.test.MockUtilsTestCase;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;


public class FoedselsNummerValidatorTest extends MockUtilsTestCase {

	@Test
	public void asLocalDate_withInvalidSsn_returnsNull() throws Exception {
		assertThat(FoedselsNummerValidator.asLocalDate("sdfsd", Locale.ENGLISH)).isNull();
	}

	@Test
	public void asLocalDate_withValidSsn_returnsLocalDate() throws Exception {
		LocalDate localDate = FoedselsNummerValidator.asLocalDate("24036518886", Locale.ENGLISH);

		assertThat(localDate.get(DateTimeFieldType.dayOfMonth())).isEqualTo(24);
		assertThat(localDate.get(DateTimeFieldType.monthOfYear())).isEqualTo(3);
		assertThat(localDate.get(DateTimeFieldType.year())).isEqualTo(1965);
	}

	@Test
	public void asFormattedLocalDate_withInvalidSsn_returnsNull() throws Exception {
		assertThat(FoedselsNummerValidator.asFormattedLocalDate("sdfsd", Locale.ENGLISH)).isNull();
	}

	@Test
	public void asFormattedLocalDate_withValidSsn_returnsNull() throws Exception {
		String result = FoedselsNummerValidator.asFormattedLocalDate("24036518886", Locale.ENGLISH);

		assertThat(result.toLowerCase()).isEqualTo("24 mar 1965");
	}
}

