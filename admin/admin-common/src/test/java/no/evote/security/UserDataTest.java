package no.evote.security;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML_Behandle;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Overstyre;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test cases for UserData
 */
public class UserDataTest extends MockUtilsTestCase {

	public static final String AN_ELECTION_EVENT_ID = "150001";
	private static final String AN_ELECTION_PATH = "150001.01.01.000001";

	private UserData userData;
	private OperatorRole operatorRole;
	private Operator operator;

	@BeforeMethod
	public void setUp() {
		userData = new UserData();
		Role role1 = role(1L);
		operator = operator(2L);
		operatorRole = operatorRole(role1, operator);
	}

	private Operator operator(final long pk) {
		Operator operator = new Operator();
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(pk);
		operator.setElectionEvent(electionEvent);
		return operator;
	}

	private OperatorRole operatorRole(final Role role, final Operator operator) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setRole(role);
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
		operatorRole.setMvArea(mvArea);
		MvElection mvElection = new MvElection();
		mvElection.setPk(1L);
		operatorRole.setMvElection(mvElection);
		operatorRole.setOperator(operator);
		return operatorRole;
	}

	private Role role(final long pk) {
		Role role = new Role();
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(pk);
		role.setElectionEvent(electionEvent);
		role.setId("");
		role.setPk(pk);
		return role;
	}

	@Test
	public void userHasAccess() {
		userData.setAccessCache(accessCache(accesses()));
		assertThat(userData.hasAccess(Opptelling_Forhånd_Rediger)).isTrue();
	}

	@Test
	public void userDoesNotHaveAccess() {
		userData.setAccessCache(accessCache(accesses()));
		assertThat(userData.hasAccess(Konfigurasjon_Overstyre)).isFalse();
	}

	@Test
	public void getElectionEventPkReturnsOperatorElectionEventPkAfterSetOperatorRole() {
		userData.setOperatorRole(operatorRole);
		assertThat(userData.getElectionEventPk()).isEqualTo(operator.getElectionEvent().getPk());
	}

	@Test
	public void isCountyLevelUser_operatorAreaOnCountyLevel_returnsTrue() {
		stubCountyLevelRole(true);

		assertThat(userData.isCountyLevelUser()).isTrue();
	}

	@Test
	public void isCountyLevelUser_operatorAreaNotOnCountyLevel_returnsFalse() {
		stubCountyLevelRole(false);

		assertThat(userData.isCountyLevelUser()).isFalse();
	}

	@Test
	public void isSamiElectionCountyUser_samiUser_returnsTrue() {
		stubSamiCountyLevelRole();

		assertThat(userData.isSamiElectionCountyUser()).isTrue();
	}

	@Test
	public void isSamiElectionCountyUser_normalCountyUser_returnsFalse() {
		stubNonSamiCountyLevelRole();

		assertThat(userData.isSamiElectionCountyUser()).isFalse();
	}

	private void stubCountyLevelRole(boolean value) {
		OperatorRole operatorRoleStub = mock(OperatorRole.class, RETURNS_DEEP_STUBS);
		when(operatorRoleStub.getMvArea().isCountyLevel()).thenReturn(value);
		userData.setOperatorRole(operatorRoleStub);
	}

	private void stubSamiCountyLevelRole() {
		OperatorRole operatorRoleStub = mock(OperatorRole.class, RETURNS_DEEP_STUBS);
		when(operatorRoleStub.getMvArea().getAreaPath()).thenReturn(AN_ELECTION_EVENT_ID);
		when(operatorRoleStub.getMvElection().getElectionPath()).thenReturn(AN_ELECTION_PATH);
		userData.setOperatorRole(operatorRoleStub);
	}

	private void stubNonSamiCountyLevelRole() {
		OperatorRole operatorRoleStub = mock(OperatorRole.class, RETURNS_DEEP_STUBS);
		when(operatorRoleStub.getMvArea().getAreaPath()).thenReturn(AN_ELECTION_EVENT_ID);
		when(operatorRoleStub.getMvElection().getElectionPath()).thenReturn(AN_ELECTION_EVENT_ID);
		userData.setOperatorRole(operatorRoleStub);
	}

	private Set<Accesses> accesses() {
		Set<Accesses> accesses = new HashSet<>();
		accesses.add(Opptelling_Forhånd_Rediger);
		accesses.add(Konfigurasjon_EML_Behandle);
		return accesses;
	}

	private AccessCache accessCache(Set<Accesses> accesses) {
		Set<String> accessPaths = new HashSet<>();
		for (Accesses access : accesses) {
			for (String path : access.paths()) {
				accessPaths.add(path);
			}
		}
		return new AccessCache(accessPaths, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "user not in municipality: 222222.22.22.2222")
	public void sjekkAtBrukerTilhørerKommune_givenMunicipalityPathAndUserNotInMunicipality_throwException() throws Exception {
		UserData userData = new UserData();
		OperatorRole operatorRole = createMock(OperatorRole.class);
		when(operatorRole.getMvArea().areaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		userData.setOperatorRole(operatorRole);

		userData.sjekkAtBrukerTilhørerKommune(AreaPath.from("222222.22.22.2222"));
	}

	@Test
	public void sjekkAtBrukerTilhørerKommune_givenMunicipalityPathAndUserInPollingDistrictInMunicipality_doNothing() throws Exception {
		UserData userData = new UserData();
		OperatorRole operatorRole = createMock(OperatorRole.class);
		when(operatorRole.getMvArea().areaPath()).thenReturn(AreaPath.from("111111.11.11.1111.111111.1111"));
		userData.setOperatorRole(operatorRole);

		userData.sjekkAtBrukerTilhørerKommune(AreaPath.from("111111.11.11.1111"));
	}

	@Test
	public void sjekkAtBrukerTilhørerKommune_givenMunicipalityPathAndUserInMunicipality_doNothing() throws Exception {
		UserData userData = new UserData();
		OperatorRole operatorRole = createMock(OperatorRole.class);
		when(operatorRole.getMvArea().areaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		userData.setOperatorRole(operatorRole);

		userData.sjekkAtBrukerTilhørerKommune(AreaPath.from("111111.11.11.1111"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "illegal municipality path: 111111.11.11")
	public void sjekkAtBrukerTilhørerKommune_givenNotMunicipalityPath_throwException() throws Exception {
		UserData userData = new UserData();
		userData.sjekkAtBrukerTilhørerKommune(AreaPath.from("111111.11.11"));
	}
}
