package no.valg.eva.admin.common.mockups;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.ELECTION_DAY_PK_SERIES;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionDay;

public final class ElectionDayMockups {

	private static final long ELECTION_DAY_PK_1 = ELECTION_DAY_PK_SERIES + 1;
	private static final long ELECTION_DAY_PK_2 = ELECTION_DAY_PK_SERIES + 2;
	public static final String ELECTION_DAY_DATE_STRING_1 = "2013-09-08";
	public static final String ELECTION_DAY_DATE_STRING_2 = "2013-09-09";

	public static List<ElectionDay> electionDays() {
		return asList(electionDay1(), electionDay2());
	}

	public static ElectionDay electionDay1() {
		return electionDay(ELECTION_DAY_PK_1, ELECTION_DAY_DATE_STRING_1);
	}

	public static ElectionDay electionDay2() {
		return electionDay(ELECTION_DAY_PK_2, ELECTION_DAY_DATE_STRING_2);
	}

	public static ElectionDay electionDay(final Long electionDayPk, final String dateString) {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setPk(electionDayPk);
		electionDay.setDate(GeneralMockups.localDate(dateString));
		return electionDay;
	}

	private ElectionDayMockups() {
		// no instances allowed
	}
}
