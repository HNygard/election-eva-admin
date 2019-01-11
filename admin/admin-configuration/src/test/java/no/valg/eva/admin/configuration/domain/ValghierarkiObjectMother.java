package no.valg.eva.admin.configuration.domain;

import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.contestArea;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.randomPk;

import java.util.HashSet;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

public final class ValghierarkiObjectMother {
	
	public static final String ELECTION_EVENT_PATH_DEFAULT = "123456";
	public static final String ELECTION_GROUP_PATH_DEFAULT = "123456.12";
	public static final String ELECTION_PATH_DEFAULT = "123456.12.12";
	public static final String ELECTION_PATH_1 = "123456.12.01";
	public static final String ELECTION_PATH_2 = "123456.12.02";
	public static final String ELECTION_PATH_3 = "123456.12.03";
	public static final String CONTEST_PATH_DEFAULT = "123456.12.12.123456";
	public static final String CONTEST_PATH_1 = "123456.12.12.000001";
	public static final String CONTEST_PATH_2 = "123456.12.12.000002";

	public static final String ELECTION_EVENT_NAME_DEFAULT = "electionEventName";
	public static final String ELECTION_GROUP_NAME_DEFAULT = "electionGroupName";
	public static final String ELECTION_NAME_DEFAULT = "electionNameDefault";
	public static final String ELECTION_NAME_1 = "electionName1";
	public static final String ELECTION_NAME_2 = "electionName2";
	public static final String ELECTION_NAME_3 = "electionName3";
	public static final String CONTEST_NAME_1 = "contestName1";
	public static final String CONTEST_NAME_2 = "contestName2";

	private ValghierarkiObjectMother() { /* Not possible to instantiate */ }
	
	public static Contest contest() {
		return new Contest();
	}

	public static Contest contest(MvArea mvArea) {
		Contest contest = new Contest();
		contest.setPk(randomPk());
		if (mvArea != null) {
			Set<ContestArea> contestAreaSet = new HashSet<>();
			contestAreaSet.add(contestArea(mvArea));
			contest.setContestAreaSet(contestAreaSet);
		}
		return contest;
	}

	public static UserData userDataOpptellingsvalgstyret() {
		UserData userData = userData(ELECTION_EVENT_PATH_DEFAULT);

		userData.getOperatorMvArea().setAreaLevel(AreaLevelEnum.ROOT.getLevel());

		MvElection mvElection = new MvElection();
		mvElection.setElectionPath(CONTEST_PATH_DEFAULT);
		mvElection.setContest(ValghierarkiObjectMother.contest());
		userData.getOperatorRole().setMvElection(mvElection);

		return userData;
	}

	@SuppressWarnings("deprecation")
	public static UserData userData(String operatorAreaPath) {
		UserData userData = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		Operator operator = new Operator();
		ElectionEvent electionEvent = new ElectionEvent();
		operator.setElectionEvent(electionEvent);
		operatorRole.setOperator(operator);
		MvArea operatorMvArea = mvArea(operatorAreaPath);
		operatorRole.setMvArea(operatorMvArea);
		operatorRole.setMvElection(mvElection(ELECTION_EVENT_PATH_DEFAULT, ELECTION_EVENT_NAME_DEFAULT));
		userData.setOperatorRole(operatorRole);

		return userData;
	}

	public static MvElection mvElection(String electionPath, String name) {
		return mvElection(electionPath, name, null, null);
	}

	public static MvElection mvElection(String electionPath, String name, AreaLevelEnum areaLevelEnum) {
		return mvElection(electionPath, name, null, areaLevelEnum);
	}

	public static MvElection mvElection(String electionPath, String name, Election election, AreaLevelEnum areaLevelEnum) {
		MvElection mvElection = new MvElection();
		mvElection.setElectionPath(electionPath);
		ElectionLevelEnum electionLevelEnum = ElectionPath.from(electionPath).getLevel();
		mvElection.setElectionLevel(electionLevelEnum.getLevel());
		switch (electionLevelEnum) {
			case ELECTION_GROUP:
				mvElection.setElectionGroupName(name);
				break;
			case ELECTION:
				mvElection.setElectionName(name);
				break;
			case CONTEST:
				mvElection.setContestName(name);
				break;
			default:
		}
		mvElection.setElection(election);
		if (areaLevelEnum != null) {
			mvElection.setAreaLevel(areaLevelEnum.getLevel());
		}
		return mvElection;
	}
	
	public static MvElection mvElection(Contest contest) {
		MvElection mvElection = new MvElection();
		mvElection.setContest(contest);
		return mvElection;
	}
}
