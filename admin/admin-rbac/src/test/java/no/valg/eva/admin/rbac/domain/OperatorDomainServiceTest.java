package no.valg.eva.admin.rbac.domain;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.RbacTestdataFactory;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OperatorDomainServiceTest extends MockUtilsTestCase {
	private static final long ELECTION_EVENT_PK = 42L;
	private static final AreaPath AREA_PATH = AreaPath.from("424242.01");
	private static final String OPERATOR_ID = "010119598765";

	@Mock
	private OperatorRepository operatorRepositoryMock;
	@Mock
	private OperatorRoleRepository operatorRoleRepositoryMock;
	@Mock
	private MvAreaRepository mvAreaRepositoryMock;

	private OperatorDomainService service;
	private RbacTestdataFactory testdataFactory;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		//service = new OperatorDomainService(operatorRepositoryMock, operatorRoleRepositoryMock);
		service = null;
		testdataFactory = new RbacTestdataFactory(ELECTION_EVENT_PK);
	}

	@Test
	public void operatorRolesInArea_userCanSeeBelow_findsOperatorRolesForAllOperatorsOnAndBelow() {
		MvArea mvArea = mock(MvArea.class);
		when(mvArea.getAreaPath()).thenReturn(AREA_PATH.path());
		UserData userData = createMock(UserData.class);
		when(userData.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå)).thenReturn(true);

		service.operatorRolesInArea(mvArea, userData);

		verify(operatorRoleRepositoryMock).findDescOperatorsRoles(mvArea);
	}

	@Test
	public void operatorRolesInArea_userCanNotSeeBelow_findsAtOwnLevel() {
		MvArea mvArea = mock(MvArea.class);
		when(mvArea.getAreaPath()).thenReturn(AREA_PATH.path());
		UserData fakeUserData = createMock(UserData.class);
		when(fakeUserData.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå)).thenReturn(false);
		service.operatorRolesInArea(mvArea, fakeUserData);
		verify(operatorRoleRepositoryMock).operatorRolesAtArea(mvArea);
	}

	@Test
	public void operatorRolesInArea_whenCountyUser_returnAllOperatorsExceptUserSupport() {
		UserData userDataMock = createMock(UserData.class);
		ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
		MvArea mvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
		OperatorRole operatorRole1 = testdataFactory.createOperatorRole(
				testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", "ola-new@norge.no", "99988777", electionEvent),
				"a-random-role", mvArea, false);
		OperatorRole operatorRole2 = testdataFactory.createOperatorRole(
				testdataFactory.createOperator(OPERATOR_ID, "Kari", "Nordmann", "kari-new@norge.no", "99988777", electionEvent),
				"a-random-role", mvArea, true);
		when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(mvArea);
		when(userDataMock.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå)).thenReturn(false); // administrator is a county
																																	// user
		when(operatorRoleRepositoryMock.operatorRolesAtArea(eq(mvArea))).thenReturn(asList(operatorRole1, operatorRole2));

		List<OperatorRole> allOperatorsInArea = service.operatorRolesInArea(mvArea, userDataMock);

		assertThat(allOperatorsInArea).hasSize(1);
		assertThat(allOperatorsInArea.get(0).getOperator().getFirstName()).isEqualTo("Ola");
	}

	@Test
	public void operatorRolesInArea_whenMunicipalityUser_returnAllOperatorsExceptUserSupport() {
		UserData userDataMock = createMock(UserData.class);
		ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
		MvArea mvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
		OperatorRole operatorRole1 = testdataFactory.createOperatorRole(
				testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", "ola-new@norge.no", "99988777", electionEvent),
				"a-random-role", mvArea, false);
		OperatorRole operatorRole2 = testdataFactory.createOperatorRole(
				testdataFactory.createOperator(OPERATOR_ID, "Kari", "Nordmann", "kari-new@norge.no", "99988777", electionEvent),
				"a-random-role", mvArea, true);

		when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(mvArea);
		when(userDataMock.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå)).thenReturn(true); // administrator is a
																																	// municipality user
		when(operatorRoleRepositoryMock.findDescOperatorsRoles(eq(mvArea))).thenReturn(asList(operatorRole1, operatorRole2));

		List<OperatorRole> allOperatorsInArea = service.operatorRolesInArea(mvArea, userDataMock);

		assertThat(allOperatorsInArea).hasSize(1);
		assertThat(allOperatorsInArea.get(0).getOperator().getFirstName()).isEqualTo("Ola");
	}

	@Test
	public void findAllOperatorRolesForOperatorInArea_userCanSeeBelow_findsOperatorRolesForOneOperator() {
		Operator operator = mock(Operator.class);
		MvArea mvArea = mock(MvArea.class);
		UserData fakeUserData = createMock(UserData.class);
		when(fakeUserData.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå)).thenReturn(true);

		service.findAllOperatorRolesForOperatorInArea(operator, mvArea, fakeUserData);

		verify(operatorRoleRepositoryMock).operatorRolesForOperatorAtOrBelowMvArea(operator, mvArea);
	}

	@Test
	public void findsOperatorRolesForOneOperatorWhenUserCanNotSeeBelow() {
		Operator operator = mock(Operator.class);
		MvArea mvArea = mock(MvArea.class);
		UserData fakeUserData = createMock(UserData.class);
		service.findAllOperatorRolesForOperatorInArea(operator, mvArea, fakeUserData);
		verify(operatorRoleRepositoryMock).operatorRolesForOperatorAtOwnLevel(operator, mvArea);
	}

	@Test
	public void deleteOperatorInArea_operatorWithAllRolesWithinUsersPath_operatorEntityShouldBeDeleted() {
		ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
		MvArea mvArea = testdataFactory.createMvArea(AreaPath.from("424242.01"), electionEvent);
		Operator operatorEntity = testdataFactory.createOperator("010119598765", "Ola", "Nordmann", "ola@norge.no", "11122333", electionEvent);
		UserData userData = testdataFactory.createUserDataWithAccess(mvArea, "");

        List<OperatorRole> operatorRoles = singletonList(testdataFactory.createOperatorRole(operatorEntity, "role1", mvArea, false));
		when(operatorRoleRepositoryMock.operatorRolesForOperatorAtOwnLevel(operatorEntity, mvArea)).thenReturn(operatorRoles);
        when(operatorRoleRepositoryMock.getOperatorRoles(operatorEntity)).thenReturn(emptyList());

		service.deleteOperatorInArea(userData, operatorEntity, mvArea); // false

		verify(operatorRoleRepositoryMock).delete(userData, operatorRoles);
		verify(operatorRepositoryMock).delete(userData, operatorEntity);
	}

	@Test
	public void deleteKnownOperatorWithSomeRolesOutsideUsersPathShouldNotDeleteEntity() {
		ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
		MvArea mvArea = testdataFactory.createMvArea(AreaPath.from("424242.01"), electionEvent);
		Operator operatorEntity = testdataFactory.createOperator("010119598765", "Ola", "Nordmann", "ola@norge.no", "11122333", electionEvent);
		UserData userData = testdataFactory.createUserDataWithAccess(mvArea, "");

        List<OperatorRole> operatorRoles = singletonList(testdataFactory.createOperatorRole(operatorEntity, "role1", mvArea, false));
		when(operatorRoleRepositoryMock.operatorRolesForOperatorAtOwnLevel(operatorEntity, mvArea)).thenReturn(operatorRoles);
		when(operatorRoleRepositoryMock.getOperatorRoles(operatorEntity)).thenReturn(
                singletonList(testdataFactory.createOperatorRole(operatorEntity, "role2", mvArea, false)));

		service.deleteOperatorInArea(userData, operatorEntity, mvArea);

		verify(operatorRoleRepositoryMock).delete(userData, operatorRoles);
		verify(operatorRepositoryMock, never()).delete(userData, operatorEntity);
	}
}
