package no.evote.service.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.rbac.OperatorServiceBean;
import no.evote.service.rbac.RoleServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class RoleServiceTest extends AbstractJpaTestBase {

	private static final String ELECTION_EVENT_VALG2007 = "200701";
	private static final String TEST_ROLE_TEST_LITERAL = "test_role";

	private AccessRepository accessRepository;
	private ElectionEventRepository electionEventRepository;
	private OperatorServiceBean operatorService;
	private RoleServiceBean roleService;
	private LegacyUserDataServiceBean userDataService;
	private BackendContainer backend;

	@BeforeMethod(alwaysRun = true)
	public void initServices() {
		backend = new BackendContainer(getEntityManager());
		backend.initServices();

		accessRepository = backend.getAccessRepository();
		electionEventRepository = backend.getElectionEventRepository();
		operatorService = backend.getOperatorService();
		roleService = backend.getRoleService();
		userDataService = backend.getUserDataService();
	}

	@Test
	public void create() {
		BaseTestFixture fixture = new BaseTestFixture(userDataService, accessRepository);
		fixture.init();

		UserData createUser = fixture.getUserData("create");

		Role role = new Role();
		role.setId(TEST_ROLE_TEST_LITERAL);
		role.setName(TEST_ROLE_TEST_LITERAL);
		role.setElectionEvent(electionEventRepository.findById(ELECTION_EVENT_VALG2007));
		
		role.setSecurityLevel(3);
		
		role = roleService.create(createUser, role, false);

		assertNotNull(role.getPk());
		assertNotNull(role.getAuditTimestamp());
		assertEquals("I", role.getAuditOperation());
		assertEquals(0, role.getAuditOplock());
	}

	@Test(groups = { TestGroups.SLOW })
	public void testGetCollisonsIfSetMutEx() {
		RBACTestFixture fixture = new ServiceBackedRBACTestFixture(backend);
		fixture.init();

		assertEquals(operatorService.getCollisionsIfSetMutEx(fixture.getRoleVotingCount()).size(), 2);
		assertTrue(operatorService.getCollisionsIfSetMutEx(fixture.getRoleVotingCount()).contains(fixture.getOperator2()));
		assertTrue(operatorService.getCollisionsIfSetMutEx(fixture.getRoleVotingCount()).contains(fixture.getOperator()));
		assertEquals(operatorService.getCollisionsIfSetMutEx(fixture.getRoleVotingCountElectionDayApprove()).size(), 1);
		assertTrue(operatorService.getCollisionsIfSetMutEx(fixture.getRoleVotingCountElectionDayApprove()).contains(fixture.getOperator2()));
	}
}
