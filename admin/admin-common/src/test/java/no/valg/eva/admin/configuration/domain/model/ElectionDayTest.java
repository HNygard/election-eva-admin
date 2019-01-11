package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class ElectionDayTest {

	public static final Integer YEAR = 2017;
	public static final int MONTH_OF_YEAR = 9;
	public static final int DAY_OF_MONTH = 11;

	@Test
	public void electionYear_returnsYearAsString() {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setDate(new LocalDate(YEAR, MONTH_OF_YEAR, DAY_OF_MONTH));

		assertThat(electionDay.electionYear()).isEqualTo(YEAR.toString());
	}
}
