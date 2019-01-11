package no.valg.eva.admin.rbac;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

/**
 * This is a factory class for creating RBAC and related objects for use in tests.
 */
public class RbacTestdataFactory {

	private final long electionEventPk;
	private final Random random = new Random();

	public RbacTestdataFactory(long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	public ElectionEvent createElectionEvent(long electionEventPk) {
		ElectionEvent electionEvent = new ElectionEvent(electionEventPk);
		electionEvent.setPk(random.nextLong());
		return electionEvent;
	}

	public UserData createUserData(MvArea selectedMvArea) {
		ElectionEvent electionEvent = new ElectionEvent(electionEventPk);
		OperatorRole selectedOperatorRole = createOperatorRole(
				createOperator("2412195012345", "Admin", "adminsen", "admin@norge.no", "44455444", electionEvent), "a-random-role", selectedMvArea, false);
		UserData userData = new UserData();
		userData.setOperatorRole(selectedOperatorRole);
		return userData;
	}

	public UserData createUserDataWithAccess(MvArea selectedMvArea, String access) {
		ElectionEvent electionEvent = new ElectionEvent(electionEventPk);
		OperatorRole selectedOperatorRole = createOperatorRole(
				createOperator("2412195012345", "Admin", "adminsen", "admin@norge.no", "44455444", electionEvent), "a-random-role", selectedMvArea, false);
		UserData userData = new UserData();
		userData.setOperatorRole(selectedOperatorRole);
		Set<String> secObj = new HashSet<>();
		secObj.add(access);
		AccessCache accessCache = new AccessCache(secObj, null);
		userData.setAccessCache(accessCache);
		return userData;
	}

	public MvArea createMvArea(AreaPath areaPath, ElectionEvent electionEvent) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(random.nextLong());
		mvArea.setAreaPath(areaPath.path());
		mvArea.setCountyName("Testland");
		mvArea.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());
		mvArea.setElectionEvent(electionEvent);
		return mvArea;
	}

	public Role createRole(String roleId, ElectionEvent electionEvent, boolean isUserSupport) {
		Role role = new Role();
		role.setPk(random.nextLong());
		role.setId(roleId);
		role.setName(roleId);
		role.setElectionEvent(electionEvent);
		role.setUserSupport(isUserSupport);
		return role;
	}

	public Operator createOperator(String operatorId, String firstName, String lastName, String email, String telephoneNumber, ElectionEvent electionEvent) {
		Operator operator = new Operator();
		operator.setElectionEvent(electionEvent);

		operator.setPk(random.nextLong());
		operator.setId(operatorId);
		operator.setFirstName(firstName);
		operator.setLastName(lastName);
		operator.setEmail(email);
		operator.setTelephoneNumber(telephoneNumber);

		return operator;
	}

	public OperatorRole createOperatorRole(Operator operator, String roleId, MvArea mvArea, boolean isUserSupport) {
		Role role = createRole(roleId, requireNonNull(operator.getElectionEvent(), "Operator must have ElectionEvent"), isUserSupport);

		MvElection mvElection = new MvElection();
		mvElection.setPk(random.nextLong());
		mvElection.setElectionEvent(operator.getElectionEvent());

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setPk(random.nextLong());
		operatorRole.setOperator(operator);
		operatorRole.setMvArea(mvArea);
		operatorRole.setRole(role);
		operatorRole.setMvElection(mvElection);

		return operatorRole;
	}
}
