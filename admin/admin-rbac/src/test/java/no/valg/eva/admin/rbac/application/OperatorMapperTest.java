package no.valg.eva.admin.rbac.application;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.testng.annotations.Test;

public class OperatorMapperTest {

	@Test
	public void shallConvertListFromDomainModelToApiModel() {
		no.valg.eva.admin.rbac.domain.model.Operator operator1 = new no.valg.eva.admin.rbac.domain.model.Operator();
		operator1.setId("1");
		operator1.setFirstName("John");
		operator1.setLastName("Doe");

		no.valg.eva.admin.rbac.domain.model.Operator operator2 = new no.valg.eva.admin.rbac.domain.model.Operator();
		operator2.setId("2");
		operator2.setFirstName("Ola");
		operator2.setLastName("Nordmann");

		MvArea area1 = new MvArea();
		area1.setAreaPath("424242.01");
		area1.setCountyName("Oslo");
		area1.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());

		MvArea area2 = new MvArea();
		area2.setAreaPath("424242.17");
		area2.setCountyName("Nordland");
		area2.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());

		
		ElectionEvent electionEvent = new ElectionEvent(42L);
		

		Role role1 = new Role();
		role1.setElectionEvent(electionEvent);
		role1.setId("role1");
		role1.setName("@role[role1].name");

		Role role2 = new Role();
		role2.setElectionEvent(electionEvent);
		role2.setId("role2");
		role2.setName("@role[role2].name");

		OperatorRole operatorRole1ForOperator1 = createOperatorRole(operator1, area1, role1);
		OperatorRole operatorRole2ForOperator1 = createOperatorRole(operator1, area1, role2);
		OperatorRole operatorRole1ForOperator2 = createOperatorRole(operator2, area2, role1);

		List<OperatorRole> operatorRoles = asList(operatorRole1ForOperator1, operatorRole2ForOperator1, operatorRole1ForOperator2);
		List<Operator> operatorWithRoleAssociations =
				OperatorViewToDomainMapper.toOperatorWithRoleAssociations(operatorRoles);

		assertEquals(operatorWithRoleAssociations.size(), 2);

		for (Operator operatorWithRoleAssociation : operatorWithRoleAssociations) {
			if ("1".equals(operatorWithRoleAssociation.getPersonId().getId())) {
				assertOperatorEquals(operator1, operatorWithRoleAssociation);
			} else if ("2".equals(operatorWithRoleAssociation.getPersonId().getId())) {
				assertOperatorEquals(operator2, operatorWithRoleAssociation);
			} else {
				fail();
			}
		}
	}

	private void assertOperatorEquals(no.valg.eva.admin.rbac.domain.model.Operator operator, Operator operatorWithRoleAssociation) {
		assertEquals(operatorWithRoleAssociation.getFirstName(), operator.getFirstName());
		assertEquals(operatorWithRoleAssociation.getLastName(), operator.getLastName());
		assertEquals(operatorWithRoleAssociation.getEmail(), operator.getEmail());
		assertEquals(operatorWithRoleAssociation.getTelephoneNumber(), operator.getTelephoneNumber());
	}

	private OperatorRole createOperatorRole(no.valg.eva.admin.rbac.domain.model.Operator operator1, MvArea area1, Role role1) {
		OperatorRole operatorRoleForOperator = new OperatorRole();

		operatorRoleForOperator.setOperator(operator1);
		operatorRoleForOperator.setRole(role1);
		operatorRoleForOperator.setMvArea(area1);

		return operatorRoleForOperator;
	}

}
