package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;


public class DailyMarkOffCountsTest {

	@Test
	public void getMarkOffCount_whenContainingSeveralDailyMarkups_returnsTheSumOfMarkOffs() throws Exception {
		DailyMarkOffCounts dailyMarkOffCounts = new DailyMarkOffCounts();
		dailyMarkOffCounts.add(getDailyMarkOffCount(new LocalDate(2014, 1, 2), 1));
		dailyMarkOffCounts.add(getDailyMarkOffCount(new LocalDate(2014, 1, 3), 2));

		int markOffCount = dailyMarkOffCounts.getMarkOffCount();

		assertThat(markOffCount).isEqualTo(3);
	}

	@Test
	public void getMarkOffCount_whenEmpty_returnsZero() throws Exception {
		DailyMarkOffCounts dailyMarkOffCounts = new DailyMarkOffCounts();

		int markOffCount = dailyMarkOffCounts.getMarkOffCount();

		assertThat(markOffCount).isEqualTo(0);
	}

	private DailyMarkOffCount getDailyMarkOffCount(LocalDate date, int markOffCount) {
		return new DailyMarkOffCount(date, markOffCount);
	}
}

