package no.valg.eva.admin.counting.mockup;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.ContestMockups.defaultContest;
import static no.valg.eva.admin.common.mockups.ElectionDayMockups.electionDay1;
import static no.valg.eva.admin.common.mockups.ElectionDayMockups.electionDay2;
import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.MunicipalityMockups.municipality;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.pollingDistrictMvArea;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.MANUAL_CONTEST_VOTING_PK_SERIES;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;

public final class ManualContestVotingMockups {

	public static final long MANUAL_CONTEST_VOTING_PK_1 = MANUAL_CONTEST_VOTING_PK_SERIES + 1;
	public static final long MANUAL_CONTEST_VOTING_PK_2 = MANUAL_CONTEST_VOTING_PK_SERIES + 2;

	public static final int VOTINGS_1 = 1300;
	public static final int VOTINGS_2 = 1700;
	public static final int VOTINGS_ZERO = 0;
	public static final boolean ELECTRONIC_MARK_OFFS_FALSE = false;

	public static ManualContestVoting manualContestVoting(
			final Long manualContestVotingPk,
			final MvArea mvArea,
			final Contest contest,
			final ElectionDay electionDay,
			final int votings) {

		ManualContestVoting manualContestVoting = new ManualContestVoting();
		manualContestVoting.setPk(manualContestVotingPk);
		manualContestVoting.setMvArea(mvArea);
		manualContestVoting.setContest(contest);
		manualContestVoting.setElectionDay(electionDay);
		manualContestVoting.setVotings(votings);
		return manualContestVoting;
	}

	public static List<ManualContestVoting> manualContestVotings(final boolean includePrimaryKeys) {
		MvArea mvArea = pollingDistrictMvArea(municipality(ELECTRONIC_MARK_OFFS_FALSE));
		Contest contest = defaultContest(electionEvent());
		if (includePrimaryKeys) {
			return asList(
					manualContestVoting(MANUAL_CONTEST_VOTING_PK_1, mvArea, contest, electionDay1(), VOTINGS_1),
					manualContestVoting(MANUAL_CONTEST_VOTING_PK_2, mvArea, contest, electionDay2(), VOTINGS_2));
		}

		return asList(
				manualContestVoting(null, mvArea, contest, electionDay1(), VOTINGS_1),
				manualContestVoting(null, mvArea, contest, electionDay2(), VOTINGS_2));
	}

	public static List<ManualContestVoting> defaultManualContestVotings() {
		MvArea mvArea = pollingDistrictMvArea(municipality(ELECTRONIC_MARK_OFFS_FALSE));
		Contest contest = defaultContest(electionEvent());
		return asList(
				manualContestVoting(MANUAL_CONTEST_VOTING_PK_1, mvArea, contest, electionDay1(), VOTINGS_ZERO),
				manualContestVoting(MANUAL_CONTEST_VOTING_PK_2, mvArea, contest, electionDay2(), VOTINGS_ZERO));
	}

	private ManualContestVotingMockups() {
		// no instances allowed
	}
}
