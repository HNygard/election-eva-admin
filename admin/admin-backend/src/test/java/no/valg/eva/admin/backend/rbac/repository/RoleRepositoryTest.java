package no.valg.eva.admin.backend.rbac.repository;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



@Test(groups = TestGroups.REPOSITORY)
public class RoleRepositoryTest extends AbstractJpaTestBase {

	private static final String ELECTION_EVENT_VALG2007 = "200701";
	private static final String TEST_ROLE_TEST_LITERAL = "test_role";
	private final Logger logger = Logger.getLogger(RoleRepositoryTest.class);
	private AccessRepository accessRepository;
	private ElectionEventRepository electionEventRepository;
	private RoleRepository roleRepository;
	private LegacyUserDataServiceBean userDataService;
	private BackendContainer backend;

	@BeforeMethod(alwaysRun = true)
	public void initServices() {
		backend = new BackendContainer(getEntityManager());
		backend.initServices();

		accessRepository = new AccessRepository(getEntityManager());
		electionEventRepository = backend.getElectionEventRepository();
		userDataService = backend.getUserDataService();

		roleRepository = new RoleRepository(getEntityManager());
	}

	@Test
	public void testGetRoleWithIncludedRoles() {
		RBACTestFixture fixture = createRbacTestFixture();

		Set<Role> roles = roleRepository.getRoleWithIncludedRoles(fixture.getRolePartyTVoteCountVotingCountElectionDayAll());
		assertEquals(roles.size(), 4);
		assertTrue(roles.contains(fixture.getRolePartyTVoteCountVotingCountElectionDayAll()));
		assertTrue(roles.contains(fixture.getRoleVotingCountElectionDayAll()));
		assertTrue(roles.contains(fixture.getRoleVotingCountElectionDayApprove()));
		assertTrue(roles.contains(fixture.getRoleVotingCountElectionDayRead()));
	}

	@Test
	public void create() {
		BaseTestFixture fixture = createBaseTestFixture();

		UserData createUser = fixture.getUserData("create");

		Role role = new Role();
		role.setId(TEST_ROLE_TEST_LITERAL);
		role.setName(TEST_ROLE_TEST_LITERAL);
		role.setElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007));
		role.setSecurityLevel(3);
		role = roleRepository.create(createUser, role);

		assertNotNull(role.getPk());
		assertNotNull(role.getAuditTimestamp());
		assertEquals("I", role.getAuditOperation());
		assertEquals(0, role.getAuditOplock());
	}

	@Test
	public void update() {
		BaseTestFixture fixture = createBaseTestFixture();

		UserData createUser = fixture.getUserData("create");
		UserData updateUser = fixture.getUserData("update");

		Role role = new Role();
		role.setId(TEST_ROLE_TEST_LITERAL);
		role.setName(TEST_ROLE_TEST_LITERAL);
		role.setElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007));
		role.setSecurityLevel(3);
		role = roleRepository.create(createUser, role);
		DateTime createdTimestamp = role.getAuditTimestamp();
		String oldName = role.getName();

		// wait a bit
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}

		role.setId("test1");
		role.setName("test1");
		role.setElectionEvent(electionEventRepository.findById(ROOT_ELECTION_EVENT_ID));
		role.setSecurityLevel(4);
		role = roleRepository.update(updateUser, role);

		DateTime updatedTimestamp = role.getAuditTimestamp();

		assertNotNull(updatedTimestamp);
		assertTrue(updatedTimestamp.compareTo(createdTimestamp) > 0);
		assertEquals("U", role.getAuditOperation());
		assertEquals(1, role.getAuditOplock());

		role.setId(TEST_ROLE_TEST_LITERAL);
		role.setName(oldName);
		role.setElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007));
		roleRepository.update(createUser, role);
	}

	@Test
	public void testGetAccumulatedSecLevel() {
		RBACTestFixture fixture = createRbacTestFixture();

		assertEquals(roleRepository.getAccumulatedSecLevelFor(fixture.getRoleVotingCountElectionDayAll()).intValue(), 4);
		assertEquals(roleRepository.getAccumulatedSecLevelFor(fixture.getRolePartyTVoteCountVotingCountElectionDayAll()).intValue(), 4);
		assertEquals(roleRepository.getAccumulatedSecLevelFor(fixture.getRoleVotingCountElectionDayRead()).intValue(), 3);
	}

	@Test
	public void testFindAllRolesInElectionEvent() {
		BaseTestFixture fixture = createBaseTestFixture();

		int listSize = roleRepository.findAllRolesInElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007)).size();
		Role role = new Role();
		role.setId(TEST_ROLE_TEST_LITERAL);
		role.setName(TEST_ROLE_TEST_LITERAL);
		role.setElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007));
		role.setSecurityLevel(3);

		role = roleRepository.create(fixture.getUserData(), role);
		assertNotNull(role);
		assertEquals(roleRepository.findAllRolesInElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007)).size(), listSize + 1);
	}

	@Test
	public void testFindByElectionEventAndId() {
		RBACTestFixture fixture = createRbacTestFixture();

		assertEquals(roleRepository.findByElectionEventAndId(electionEventRepository.findById(ELECTION_EVENT_VALG2007),
				RBACTestFixture.ROLE_NAME_VOTING_COUNT_ELECTION_DAY_ALL), fixture.getRoleVotingCountElectionDayAll());
		assertEquals(
				roleRepository.findByElectionEventAndId(electionEventRepository.findById(ELECTION_EVENT_VALG2007), RBACTestFixture.ROLE_NAME_VOTING_COUNT),
				fixture.getRoleVotingCount());
		assertEquals(roleRepository.findByElectionEventAndId(electionEventRepository.findById(ELECTION_EVENT_VALG2007), "NoRoleWhatSoEver"), null);
	}

	@Test
	public void testFindByPk() {
		RBACTestFixture fixture = createRbacTestFixture();

		assertEquals(roleRepository.findByPk(fixture.getRoleEmpty().getPk()), fixture.getRoleEmpty());
		assertEquals(roleRepository.findByPk(fixture.getRolePartyTVoteCountVotingCountElectionDayAll().getPk()),
				fixture.getRolePartyTVoteCountVotingCountElectionDayAll());
	}

	@Test
	public void getAccesses() {
		RBACTestFixture fixture = createRbacTestFixture();

		Set<Access> accesses = roleRepository.getAccesses(fixture.getRoleVotingCount());
		assertEquals(accesses.size(), 1);
		assertTrue(accesses.contains(fixture.getAccessVotingCount()));
	}

	@Test
	public void getIncludedRoles() {
		RBACTestFixture fixture = createRbacTestFixture();

		Set<Role> includedRoles = roleRepository.getIncludedRoles(fixture.getRoleVotingCount());
		assertEquals(includedRoles.size(), 1);
		assertTrue(includedRoles.contains(fixture.getRoleEmpty()));
	}

	/*
	 * The root role has every role included in some way and can therefore assign all roles
	 */
	@Test(enabled = false, description = "This is old code and does not work anymore")
	public void findAssignableRolesForOperatorRoleRoot() {
		RBACTestFixture fixture = createRbacTestFixture();

		Set<Role> expectedRoles = new HashSet<>();
		expectedRoles.add(fixture.getRoleVotingCount());
		expectedRoles.add(fixture.getRoleEmpty());
		expectedRoles.add(fixture.getRolePartyTVoteCountVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayRead());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayApprove());
		expectedRoles.add(fixture.getRoleRoot());
		expectedRoles.add(getReserveRole());

		Set<Role> assignableRoles = new HashSet<>(roleRepository.findAssignableRolesForOperatorRole(fixture.getOperatorRoleOperatorRoot()));

		assertEquals(assignableRoles, expectedRoles);
	}

	/*
	 * The root role has every role included in some way and can therefore assign all roles except inactive ones
	 */
	@Test(enabled = false, description = "This is old code and does not work anymore")
	public void findAssignableRolesForOperatorRoleNotIncludingDisablesRoles() {
		RBACTestFixture fixture = createRbacTestFixture();

		fixture.getRoleVotingCount().setActive(false);
		roleRepository.update(fixture.getUserData(), fixture.getRoleVotingCount());

		Set<Role> expectedRoles = new HashSet<>();
		expectedRoles.add(fixture.getRoleEmpty());
		expectedRoles.add(fixture.getRolePartyTVoteCountVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayRead());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayApprove());
		expectedRoles.add(fixture.getRoleRoot());
		expectedRoles.add(getReserveRole());

		Set<Role> assignableRoles = new HashSet<>(roleRepository.findAssignableRolesForOperatorRole(fixture.getOperatorRoleOperatorRoot()));

		assertEquals(assignableRoles, expectedRoles);
	}

	/*
	 * The role2 can assign all roles in the 2 tree because it includes those roles. In addition it can assign role11 because it doesn't have any access objects
	 */
	@Test(enabled = false, description = "This is old code and does not work anymore")
	public void findAssignableRolesForOperatorRole2() {
		RBACTestFixture fixture = createRbacTestFixture();

		Set<Role> expectedRoles = new HashSet<>();
		expectedRoles.add(fixture.getRolePartyTVoteCountVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayAll());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayRead());
		expectedRoles.add(fixture.getRoleVotingCountElectionDayApprove());
		expectedRoles.add(fixture.getRoleEmpty());
		expectedRoles.add(getReserveRole());

		Set<Role> assignableRoles = new HashSet<>(roleRepository.findAssignableRolesForOperatorRole(
				fixture.getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag()));

		assertEquals(assignableRoles, expectedRoles);
	}

	private Role getReserveRole() {
		return roleRepository.findByElectionEventAndId(electionEventRepository.findById(ELECTION_EVENT_VALG2007), "reserve");
	}

	private RBACTestFixture createRbacTestFixture() {
		RBACTestFixture testFixture = new ServiceBackedRBACTestFixture(backend);
		testFixture.init();
		return testFixture;
	}

	private BaseTestFixture createBaseTestFixture() {
		BaseTestFixture testFixture = new BaseTestFixture(userDataService, accessRepository);
		testFixture.init();
		return testFixture;
	}
}

