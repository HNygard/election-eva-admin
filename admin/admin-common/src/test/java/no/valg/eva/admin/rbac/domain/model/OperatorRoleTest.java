package no.valg.eva.admin.rbac.domain.model;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.annotations.Test;


public class OperatorRoleTest extends BaseTakeTimeTest {

	@Test
	public void isEnabledForSecurityLevelTrueForSameSecLevelRoleActive() {
		OperatorRole operatorRole = createBasicOperatorRole();

		assertTrue(operatorRole.isEnabledForSecurityLevel(2));
	}

	@Test
	public void isEnabledForSecurityLevelTrueForHigherSecLevelRoleActive() {
		OperatorRole operatorRole = createBasicOperatorRole();

		assertTrue(operatorRole.isEnabledForSecurityLevel(3));
	}

	@Test
	public void isEnabledForSecurityLevelFalseForLowerSecLevelRoleActive() {
		OperatorRole operatorRole = createBasicOperatorRole();

		assertFalse(operatorRole.isEnabledForSecurityLevel(1));
	}

	@Test
	public void isEnabledForSecurityLevelFalseRoleNotActive() {
		OperatorRole operatorRole = createBasicOperatorRole();

		operatorRole.getRole().setActive(false);

		assertFalse(operatorRole.isEnabledForSecurityLevel(1));
	}

	@Test
	public void isEnabledForSecurityLevelFalseForLowerSecLevelThanIncludedRoles() {
		OperatorRole operatorRole = createBasicOperatorRole();

		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(0L);

		Role includedRole = new Role();
		includedRole.setActive(true);
		includedRole.setSecurityLevel(3);
		includedRole.setElectionEvent(electionEvent);
		includedRole.setId("roleId");

		operatorRole.getRole().getIncludedRoles().add(includedRole);

		assertFalse(operatorRole.isEnabledForSecurityLevel(2));
	}

	@Test
	public void isEnabledForSecurityLevelFalseOperatorNotActive() {
		OperatorRole operatorRole = createBasicOperatorRole();

		operatorRole.getOperator().setActive(false);

		assertFalse(operatorRole.isEnabledForSecurityLevel(5));
	}

	private OperatorRole createBasicOperatorRole() {
		OperatorRole operatorRole = new OperatorRole();

		Role role = new Role();
		role.setActive(true);
		role.setSecurityLevel(2);

		Operator operator = new Operator();
		operator.setActive(true);

		operatorRole.setRole(role);
		operatorRole.setOperator(operator);

		return operatorRole;
	}
}

