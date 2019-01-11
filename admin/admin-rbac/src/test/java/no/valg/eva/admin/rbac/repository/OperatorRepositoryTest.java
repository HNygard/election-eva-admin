package no.valg.eva.admin.rbac.repository;

import com.google.common.base.Predicate;
import no.evote.security.UserData;
import no.evote.service.backendmock.RepositoryBackedRBACTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.transform;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.service.backendmock.RBACTestFixture.PATH_ELECTION_EVENT_VALG07;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.REPOSITORY)
public class OperatorRepositoryTest extends AbstractJpaTestBase {

	private static final long EXISTING_ELECTION_EVENT_PK = 1L;
	private static final long ELECTION_EVENT_PK_VALG2007 = 2L;
	private static final String NAME_LINE = "Ola Normann";
	private static final String ID = "11111111111";
	private static final String NON_EXISTING_NAME_LINE = "Finn Esikke";
	private static final AreaPath AREA_PATH_OSLO = AreaPath.from("200701.47.03.0301");
	private static final AreaPath AREA_PATH_HALDEN = AreaPath.from("200701.47.07.0701");
	private static final String NAME_EXISTING_USER_OSLO = "Per Pilot";

	private static final Function<OperatorRole, Role> OPERATOR_ROLE_ROLE_FUNCTION = OperatorRole::getRole;

	private GenericTestRepository genericTestRepository;
	private RoleRepository roleRepository;
	private ElectionEventRepository electionEventRepository;
	private OperatorRoleRepository operatorRoleRepository;
	private OperatorRepository operatorRepository;
	private MvAreaRepository mvAreaRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		genericTestRepository = new GenericTestRepository(getEntityManager());
		RepositoryBackedRBACTestFixture rbacTestFixture = new RepositoryBackedRBACTestFixture(getEntityManager());
		roleRepository = new RoleRepository(getEntityManager());
		electionEventRepository = new ElectionEventRepository(getEntityManager());
		operatorRoleRepository = new OperatorRoleRepository(getEntityManager());
		operatorRepository = new OperatorRepository(getEntityManager());
		mvAreaRepository = new MvAreaRepository(getEntityManager());

		rbacTestFixture.init();
	}

	@Test
	public void findOperatorsByName_givenExistingNameLine_returnsOperatorWithNameLine() {
		OperatorRepository operatorRepository = new OperatorRepository(getEntityManager());
		UserData stubUserData = stubUserData();
		operatorRepository.create(stubUserData, operator());

		List<Operator> result = operatorRepository.findOperatorsByName(EXISTING_ELECTION_EVENT_PK, NAME_LINE);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(ID);
	}

	@Test
	public void findOperatorsWithRoles() {
		ElectionEvent electionEvent = electionEventRepository.findById(PATH_ELECTION_EVENT_VALG07);
		Role countyAdminRole = roleRepository.findByElectionEventAndId(electionEvent, "stemmemottak_valgting");
		Set<Role> roleFilter = of(countyAdminRole);
		List<Operator> operatorsWithRoles = operatorRepository.findOperatorsWithRolesIn(roleFilter);
		assertThat(operatorsWithRoles).hasSize(1);

		for (Operator operator : operatorsWithRoles) {
			List<OperatorRole> operatorRoles = operatorRoleRepository.getOperatorRoles(operator);
			assertThat(tryFind(rolesFromOperatorRoles(operatorRoles), thatEquals(countyAdminRole)).isPresent());
		}
	}

	@Test
	public void findOperatorsByName_givenNonExistingNameLine_returnsEmptyList() {
		OperatorRepository operatorRepository = new OperatorRepository(getEntityManager());
		UserData stubUserData = stubUserData();
		operatorRepository.create(stubUserData, operator());

		List<Operator> result = operatorRepository.findOperatorsByName(EXISTING_ELECTION_EVENT_PK, NON_EXISTING_NAME_LINE);

		assertThat(result).isEmpty();
	}

	@Test(dataProvider = "findOperatorsByNameAndAreaTestData")
	public void findOperatorsByNameAndArea(AreaPath areaPath, String nameSearchString, List<String> expectedNamesReturned) {
		OperatorRepository operatorRepository = new OperatorRepository(getEntityManager());
		UserData stubUserData = stubUserData();
		when(stubUserData.getElectionEventPk()).thenReturn(ELECTION_EVENT_PK_VALG2007);
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		operatorRepository.create(stubUserData, operator());

		List<Operator> actualOperators = operatorRepository.findOperatorsByNameAndArea(stubUserData, mvArea, nameSearchString);

		assertThat(actualOperators.size()).isEqualTo(expectedNamesReturned.size());
		for (int i = 0; i < actualOperators.size(); i++) {
			assertThat(actualOperators.get(i).getNameLine()).isEqualTo(expectedNamesReturned.get(i));
		}
	}
	
	@DataProvider
	private Object[][] findOperatorsByNameAndAreaTestData() {
		return new Object[][] {
			{ AREA_PATH_OSLO, NAME_EXISTING_USER_OSLO, singletonList(NAME_EXISTING_USER_OSLO) },
			{ AREA_PATH_HALDEN, NAME_EXISTING_USER_OSLO, emptyList() }
		};
	}
	
	private UserData stubUserData() {
		UserData stubUserData = mock(UserData.class);
		OperatorRole stubOperatorRole = mock(OperatorRole.class);
		when(stubUserData.getOperatorRole()).thenReturn(stubOperatorRole);
		Role stubRole = mock(Role.class);
		when(stubOperatorRole.getRole()).thenReturn(stubRole);
		when(stubRole.isUserSupport()).thenReturn(false);
		return stubUserData;
	}

	private Operator operator() {
		Operator operator = new Operator();
		operator.setId(ID);
		operator.setFirstName("Ola");
		operator.setLastName("Normann");
		operator.setNameLine(NAME_LINE);
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "pk", EXISTING_ELECTION_EVENT_PK);
		operator.setElectionEvent(electionEvent);
		return operator;
	}

	private List<Role> rolesFromOperatorRoles(List<OperatorRole> operatorRoles) {
		return transform(operatorRoles, OPERATOR_ROLE_ROLE_FUNCTION::apply);
	}

	private Predicate<Role> thatEquals(final Role role) {
		return new Predicate<Role>() {
			@Override
			public boolean apply(Role input) {
				return input.equals(role);
			}
		};
	}

}
