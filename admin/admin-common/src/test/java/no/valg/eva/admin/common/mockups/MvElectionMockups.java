package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.ContestMockups.defaultContest;
import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.ElectionGroupMockups.electionGroup;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.MV_ELECTION_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvElection;

public final class MvElectionMockups {

	public static final long MV_ELECTION_PK_OPERATOR = MV_ELECTION_PK_SERIES + 1;
	public static final long MV_ELECTION_PK_CONTEST = MV_ELECTION_PK_SERIES + 2;
	public static final String MV_ELECTION_PATH_OPERATOR = "730001.01.01";
	public static final String MV_ELECTION_PATH_CONTEST = "730001.01.01.000001";

	public static MvElection mvElection(final long mvElectionPk, final String electionPath, final ElectionEvent electionEvent, final Contest contest) {
		MvElection mvElection = new MvElection();
		mvElection.setPk(mvElectionPk);
		mvElection.setElectionPath(electionPath);
		mvElection.setElectionEvent(electionEvent);
		mvElection.setElectionGroup(electionGroup(electionEvent));
		mvElection.setContest(contest);
		return mvElection;
	}

	public static MvElection operatorMvElection() {
		return mvElection(MV_ELECTION_PK_OPERATOR, MV_ELECTION_PATH_OPERATOR, electionEvent(), null);
	}

	public static MvElection contestMvElection() {
		ElectionEvent electionEvent = electionEvent();
		return mvElection(MV_ELECTION_PK_OPERATOR, MV_ELECTION_PATH_CONTEST, electionEvent, defaultContest(electionEvent));
	}

	public static MvElection testMvElection(Contest contest) {
		ElectionEvent electionEvent = electionEvent();
		return mvElection(MV_ELECTION_PK_OPERATOR, MV_ELECTION_PATH_CONTEST, electionEvent, contest);
	}

	private MvElectionMockups() {
		// no instances allowed
	}
}
