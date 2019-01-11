package no.evote.service;

import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleServiceBean;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class OperatorRoleServiceBeanTest {

    private static final String DEFAULT_ELECTION_EVENT_ID = "201301";
    private static final String ELECTION_EVENT_ID_201302 = "201302";

	@Test
	public void getOperatorRolesOneElectionEvent() {
		MockOperatorRoleServiceBean mockOperatorRoleService = new MockOperatorRoleServiceBean();

		mockOperatorRoleService.addOperatorRole(createOperatorRole(true));
		mockOperatorRoleService.addOperatorRole(createOperatorRole(true));

		UserData userData = new UserData();
		userData.setSecurityLevel(5);
		userData.setUid("");
		Map<ElectionEvent, List<OperatorRole>> selectableOperatorRoles = mockOperatorRoleService.getOperatorRolesPerElectionEvent(userData);

		assertEquals(selectableOperatorRoles.size(), 1);
		List<OperatorRole> operatorRoles = new ArrayList<>(selectableOperatorRoles.get(selectableOperatorRoles.keySet().iterator().next()));

		assertEquals(operatorRoles.size(), 2);
	}

	@Test
	public void getOperatorRolesTwoElectionEvent() {
		MockOperatorRoleServiceBean mockOperatorRoleService = new MockOperatorRoleServiceBean();

		mockOperatorRoleService.addOperatorRole(createOperatorRole(true));

		OperatorRole operatorRole = createOperatorRole(true);
		ElectionEvent electionEvent = electionEvent(ELECTION_EVENT_ID_201302);
		operatorRole.getOperator().setElectionEvent(electionEvent);
		mockOperatorRoleService.addOperatorRole(operatorRole);

		UserData userData = new UserData();
		userData.setSecurityLevel(5);
		userData.setUid("");
		Map<ElectionEvent, List<OperatorRole>> selectableOperatorRoles = mockOperatorRoleService.getOperatorRolesPerElectionEvent(userData);

		assertEquals(selectableOperatorRoles.size(), 2);
	}

	private OperatorRole createOperatorRole(Boolean active) {
		OperatorRole operatorRole = mock(OperatorRole.class);
		when(operatorRole.isEnabledForSecurityLevel(anyInt())).thenReturn(active);

		Operator operator = new Operator();

		operator.setElectionEvent(electionEvent());

		when(operatorRole.getOperator()).thenReturn(operator);
		return operatorRole;
	}

	private ElectionEvent electionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId(DEFAULT_ELECTION_EVENT_ID);

		return electionEvent;
	}

	private ElectionEvent electionEvent(String electionEventID) {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId(electionEventID);

		return electionEvent;
	}

	class MockOperatorRoleServiceBean extends OperatorRoleServiceBean {
		private List<OperatorRole> operatorRoles = new ArrayList<>();
		private List<Operator> operators = new ArrayList<>();

		public MockOperatorRoleServiceBean() {
			OperatorRepository operatorRepository = mock(OperatorRepository.class);
			Operator operator = new Operator();
			operator.setElectionEvent(new ElectionEvent());
			when(operatorRepository.findOperatorsById(anyString())).thenReturn(operators);
			setOperatorRepository(operatorRepository);
		}

		@Override
		public List<OperatorRole> getOperatorRoles(Operator operator) {
			return operatorRoles;
		}

		public void addOperatorRole(OperatorRole operatorRole) {
			operatorRoles.add(operatorRole);
			operators.add(operatorRole.getOperator());
		}
	}
}

