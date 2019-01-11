package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.model.ModelTestConstants;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ElectionTestFixture;
import no.evote.service.configuration.VoterServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAreaLevel;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

@Test(groups = "repository")
public class ElectionEventDomainServiceTest extends AbstractJpaTestBase {

	private ElectionEventDomainService electionEventService;
	private ElectionEventRepository electionEventRepository;
	private ContestRepository contestRepository;
	private RoleRepository roleRepository;
	private VoterServiceBean voterService;
	private ReportingUnitRepository reportingUnitRepository;
	private LegacyUserDataServiceBean userDataService;
	private AccessRepository accessRepository;
	private ElectionGroupRepository electionGroupRepository;
	private ElectionRepository electionRepository;
	private LocaleRepository localeRepository;
	private EntityManager em;

	@BeforeMethod(alwaysRun = true)
	public void initializeDependencies() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();

		electionEventService = backend.getElectionEventService();
		contestRepository = backend.getContestRepository();
		roleRepository = backend.getRoleRepository();
		voterService = backend.getVoterService();
		reportingUnitRepository = backend.getReportingUnitRepository();
		userDataService = backend.getUserDataService();
		accessRepository = new AccessRepository(getEntityManager());
		electionGroupRepository = backend.getElectionGroupRepository();
		electionRepository = backend.getElectionRepository();
		localeRepository = backend.getLocaleRepository();
		electionEventRepository = backend.getElectionEventRepository();
		em = backend.getEm();
	}

	@Test
	public void testCreateElectionEvent() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
	}

	@Test
	public void testCreateElectionEventIdTooLong() {
		ElectionTestFixture fixture = createElectionTestFixture();

		ElectionEvent electionEvent = fixture.buildElectionEvent();
		electionEvent.setId("2099012");

		Set<ConstraintViolation<ElectionEvent>> constraintViolations = fixture.getValidator().validate(electionEvent);

		assertEquals(constraintViolations.size(), 1);
		assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateElectionEventDuplicateId() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.getElectionEvent().setId(ROOT_ELECTION_EVENT_ID);
		electionEventService.create(fixture.getUserData(), fixture.getElectionEvent(), false, VotingHierarchy.NONE, CountingHierarchy.NONE, null,
				null);
	}

	@Test
	public void create_withDemoElection_returnsTrue() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.getElectionEvent().setDemoElection(true);
		ElectionEvent event = fixture.createElectionEvent(fixture.getElectionEvent());
		Assert.assertEquals(event.isDemoElection(), true);
	}

	@Test
	public void testUpdateElectionEvent() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		fixture.getElectionEvent().setName("Updated name");

		fixture.setElectionEvent(electionEventService.update(fixture.getUserData(), fixture.getElectionEvent(), null));

		ElectionEvent electionEventAfterUpdate = electionEventRepository.findByPk(fixture.getElectionEvent().getPk());
		assertNotNull(electionEventAfterUpdate);
		assertEquals(electionEventAfterUpdate.getName(), "Updated name");
	}

	@Test
	public void getVotersWithoutPollingDistricts() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		List<String> voters = voterService.getVotersWithoutPollingDistricts(fixture.getElectionEvent().getPk());

		assertNotNull(voters);
	}

	@Test
	public void testCopyRoles() {
		ElectionTestFixture fixture = createElectionTestFixture();
		int numberOfRolesWithUserSupport = 0;

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		// modify first role to be user Support
		Role userSupportToBe = roleRepository.findAllRolesInElectionEvent(fixture.getElectionEvent()).get(0);
		userSupportToBe.setUserSupport(true);
		roleRepository.update(fixture.getUserData(), userSupportToBe);

		List<Role> allRolesInElectionEvent = roleRepository.findAllRolesInElectionEvent(fixture.getElectionEvent());
		for (Role role : allRolesInElectionEvent) {
			if (role.isUserSupport()) {
				numberOfRolesWithUserSupport++;
			}
		}
		Assert.assertTrue(numberOfRolesWithUserSupport > 0);

		assertEquals(allRolesInElectionEvent.size(), 2);
		electionEventRepository.copyRoles(electionEventRepository.findById(ROOT_ELECTION_EVENT_ID), fixture.getElectionEvent());
		allRolesInElectionEvent = roleRepository.findAllRolesInElectionEvent(fixture.getElectionEvent());
		assertTrue(allRolesInElectionEvent.size() >= 5);
		// check that at least one role now has area level
		int rolesHavingAreaLevel = 0;
		for (Role role : allRolesInElectionEvent) {
			if (role.isUserSupport()) {
				numberOfRolesWithUserSupport--;
			}
			rolesHavingAreaLevel += role.getRoleAreaLevels().size();
			for (RoleAreaLevel roleAreaLevel : role.getRoleAreaLevels()) {
				assertNotNull(roleAreaLevel.getAreaLevel());
				assertNotNull(roleAreaLevel.getPollingPlaceType());
			}
		}
		Assert.assertTrue(rolesHavingAreaLevel > 0);
		Assert.assertEquals(numberOfRolesWithUserSupport, 0);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCopyRolesFail() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		// 1 is the default number of roles in an election event (admin, webservice, scheduled import x 2, public guest)
		assertEquals(roleRepository.findAllRolesInElectionEvent(fixture.getElectionEvent()).size(), 2);

		electionEventRepository.copyRoles(electionEventRepository.findById(ROOT_ELECTION_EVENT_ID), new ElectionEvent(99999L));
	}

	@Test
	public void testCopyElectoralRollSameNumberOfVotersAndMvAreaPkIsUpdated() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		ElectionEvent electionEventCopied = fixture.createElectionEventCopyElectoralRoll(fixture.getElectionEvent());
		List<Voter> electoralRollFrom, electoralRollTo;
		electoralRollFrom = findVoterByElectionEvent(electionEventRepository.findById("200701").getPk());
		electoralRollTo = findVoterByElectionEvent(electionEventCopied.getPk());
		assertEquals(electoralRollTo.size(), electoralRollFrom.size());
		for (Voter voter : electoralRollTo) {
			assertEquals(voter.getMvArea().getElectionEvent().getPk(), electionEventCopied.getPk());
		}
	}

	@SuppressWarnings("unchecked")
	private List<Voter> findVoterByElectionEvent(Long electionEventPk) {
		String sql = "select * from voter where election_event_pk = ?1 order by country_id, county_id, municipality_id, borough_id, polling_district_id;";
		Query query = em.createNativeQuery(sql, Voter.class);
		query.setParameter(1, electionEventPk);
		return query.getResultList();
	}

	@Test(groups = TestGroups.SLOW)
	public void testCopyReportingUnits() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		ElectionEvent electionEventCopied = fixture.createElectionEventCopyElectoralRoll(fixture.getElectionEvent());

		List<ReportingUnit> reportingUnitsFrom = reportingUnitRepository.findAllForElectionEvent(electionEventRepository.findById("200701").getPk());
		List<ReportingUnit> reportingUnitsTo = reportingUnitRepository.findAllForElectionEvent(electionEventCopied.getPk());

		assertEquals(reportingUnitsFrom.size(), reportingUnitsTo.size());
	}

	private ElectionTestFixture createElectionTestFixture() {
		ElectionTestFixture fixture = new ElectionTestFixture(
				userDataService, accessRepository, electionGroupRepository, electionRepository,
				contestRepository, localeRepository, electionEventRepository, electionEventService);
		fixture.init();

		return fixture;
	}

}

