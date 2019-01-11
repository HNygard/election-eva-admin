package no.valg.eva.admin.counting.mockup;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.ElectionDayMockups.ELECTION_DAY_DATE_STRING_1;
import static no.valg.eva.admin.common.mockups.ElectionDayMockups.ELECTION_DAY_DATE_STRING_2;
import static no.valg.eva.admin.common.mockups.GeneralMockups.localDate;
import static no.valg.eva.admin.counting.mockup.VotingMockups.VOTING_COUNT_1;
import static no.valg.eva.admin.counting.mockup.VotingMockups.VOTING_COUNT_2;

import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;

public final class DailyMarkOffCountMockups {

	public static final String DAILY_MARK_OFF_DATE_STRING_1 = ELECTION_DAY_DATE_STRING_1;
	public static final String DAILY_MARK_OFF_DATE_STRING_2 = ELECTION_DAY_DATE_STRING_2;
	public static final int ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_1 = VOTING_COUNT_1;
	public static final int ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_2 = VOTING_COUNT_2;
	public static final int ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_ZERO = 0;

	private DailyMarkOffCountMockups() {
		// no instances allowed
	}

	public static DailyMarkOffCount dailyMarkOffCount(final String dateString, int markOffCount) {
		DailyMarkOffCount dailyMarkOffCount = new DailyMarkOffCount(localDate(dateString));
		dailyMarkOffCount.setMarkOffCount(markOffCount);
		return dailyMarkOffCount;
	}

	public static DailyMarkOffCounts loadedDailyMarkOffCounts() {
		return new DailyMarkOffCounts(asList(
				dailyMarkOffCount(DAILY_MARK_OFF_DATE_STRING_1, ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_1),
				dailyMarkOffCount(DAILY_MARK_OFF_DATE_STRING_2, ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_2)));
	}

	public static DailyMarkOffCounts blankDailyMarkOffCounts() {
		return new DailyMarkOffCounts(asList(
				dailyMarkOffCount(DAILY_MARK_OFF_DATE_STRING_1, ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_ZERO),
				dailyMarkOffCount(DAILY_MARK_OFF_DATE_STRING_2, ELECTRONIC_DAILY_MARK_OFF_MARK_OFFS_ZERO)));
	}
}
