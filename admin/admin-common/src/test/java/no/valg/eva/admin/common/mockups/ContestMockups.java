package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.CONTEST_PK_SERIES;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public final class ContestMockups {

	public static final long CONTEST_PK = CONTEST_PK_SERIES + 1;

	private ContestMockups() {
		// no instances allowed
	}

	public static Contest contest(final Election election) {
		Contest contest = new Contest();
		contest.setPk(CONTEST_PK);
		contest.setElection(election);
		contest.getContestAreaSet().add(contestArea());
		return contest;
	}

	private static ContestArea contestArea() {
		return mock(ContestArea.class, RETURNS_DEEP_STUBS);
	}

	public static Contest contestWithOutContestReport(final Election election) {
		Contest contest = new Contest();
		contest.setPk(CONTEST_PK);
		contest.setId("111111");
		contest.setElection(election);
		return contest;
	}

	public static Contest defaultContest(final ElectionEvent electionEvent) {
		return contest(ElectionMockups.defaultElection(electionEvent));
	}

	public static Contest defaultContestWithoutContestReport(final ElectionEvent electionEvent) {
		return contestWithOutContestReport(ElectionMockups.defaultElection(electionEvent));
	}

}
