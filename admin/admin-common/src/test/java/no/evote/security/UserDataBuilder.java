package no.evote.security;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

public class UserDataBuilder {
	
	private UserData userData;
	
	public static UserDataBuilder create() {
		UserDataBuilder builder = new UserDataBuilder();
		builder.userData = getDefaultUserData();
		return builder;
	}

	private static UserData getDefaultUserData() {
		UserData userData = new UserData();
		
		OperatorRole operatorRole = new OperatorRole();

		MvArea mvArea = new MvArea();

		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(0L);

		Role role = new Role();
		role.setElectionEvent(electionEvent);

		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());

		operatorRole.setMvArea(mvArea);
		operatorRole.setRole(role);
		operatorRole.setOperator(operator);

		userData.setOperatorRole(operatorRole);
		
		return userData;
	}

	public UserDataBuilder withPollingDistrictAsSelectedArea() {
		PollingDistrict pollingDistrict = new PollingDistrict();

		MvArea pollingDistrictArea = new MvArea();
		pollingDistrictArea.setAreaLevel(AreaLevelEnum.POLLING_DISTRICT.getLevel());
		pollingDistrictArea.setPollingDistrict(pollingDistrict);
		
		userData.getOperatorRole().setMvArea(pollingDistrictArea);
		
		return this;
	}

	public UserDataBuilder withRootLevelAsSelectedArea() {
		MvArea rootMvArea = new MvArea();
		rootMvArea.setAreaLevel(AreaLevelEnum.ROOT.getLevel());
		
		userData.getOperatorRole().setMvArea(rootMvArea);
		
		return this;
	}

	public UserData build() {
		return userData;
	}
}
