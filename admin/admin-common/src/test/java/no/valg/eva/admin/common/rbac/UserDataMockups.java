package no.valg.eva.admin.common.rbac;

import java.util.HashSet;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;


public final class UserDataMockups {

	private UserDataMockups() {
	}

	public static UserData userData(OperatorRole operatorRole) {
		UserData userData = new UserData();
		userData.setOperatorRole(operatorRole);
		return userData;
	}

	public static UserData userData(String uid, AreaLevelEnum areaLevel) {
		UserData userData = new UserData();

		OperatorRole operatorRole = new OperatorRole();
		MvArea mvArea = new MvArea();
		mvArea.setPk(10608L);
		mvArea.setAreaPath("950003");
		Role role = new Role();
		role.setElectionEvent(new ElectionEvent());
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		MvElection mvElection = new MvElection();
		mvElection.setPk(6L);
		mvElection.setElectionPath("950003.01");
		mvArea.setAreaLevel(areaLevel.getLevel());
		mvArea.setCountryId("47");
		operatorRole.setMvArea(mvArea);
		operatorRole.setRole(role);
		operatorRole.setOperator(operator);
		operatorRole.setMvElection(mvElection);
		userData.setUid(uid);
		userData.setOperatorRole(operatorRole);

		userData.setAccessCache(new AccessCache(new HashSet<>(), null) {
			@Override
			public boolean hasAccess(Accesses... accesses) {
				return true;
			}
		});

		return userData;
	}

	public static UserData userOnPollingDistrictLevel() {
		// UserData-objekt for Per Pilot
		return userData("03011700143", AreaLevelEnum.POLLING_DISTRICT);
	}

	public static UserData userOnPollingStation() {
		// UserData-objekt for Per Pilot
		return userData("03011700143", AreaLevelEnum.POLLING_STATION);
	}

	public static UserData userOnMunicipalityLevel() {
		UserData userData = new UserData();

		OperatorRole operatorRole = new OperatorRole();
		MvArea mvArea = new MvArea();
		mvArea.setPk(4265L);
		Role role = new Role();
		role.setElectionEvent(new ElectionEvent());
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		MvElection mvElection = new MvElection();
		mvElection.setPk(2L);
		mvArea.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
		operatorRole.setMvArea(mvArea);
		operatorRole.setRole(role);
		operatorRole.setOperator(operator);
		operatorRole.setMvElection(mvElection);
		userData.setUid("03011700143");
		userData.setOperatorRole(operatorRole);

		return userData;
	}
}

