package no.valg.eva.admin.backend.configuration.repository;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ElectionTestFixture;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import javax.persistence.Query;
import java.util.List;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = TestGroups.REPOSITORY)
public class ElectionEventRepositoryTest extends AbstractJpaTestBase {

	private static final int PARTY_NUMBER_START_LOCAL_PARTY = 1800;
	private static final int PARTY_NUMBER_START_REGISTERED_PARTY = 5600;
	
	private ElectionEventDomainService electionEventService;
	private ElectionEventRepository electionEventRepository;
	private ContestRepository contestRepository;
	private LegacyUserDataServiceBean userDataService;
	private ElectionGroupRepository electionGroupRepository;
	private ElectionRepository electionRepository;
	private LocaleRepository localeRepository;
	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;
	private AccessRepository accessRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void initializeDependencies() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();

		electionEventRepository = backend.getElectionEventRepository();
		contestRepository = backend.getContestRepository();
		userDataService = backend.getUserDataService();
		accessRepository = new AccessRepository(getEntityManager());
		electionEventService = backend.getElectionEventService();
		electionGroupRepository = backend.getElectionGroupRepository();
		electionRepository = backend.getElectionRepository();
		localeRepository = backend.getLocaleRepository();
		mvAreaRepository = backend.getMvAreaRepository();
		mvElectionRepository = backend.getMvElectionRepository();
		genericTestRepository = backend.getGenericTestRepository();
	}

	@Test
	public void testFindAllActiveElectionEvents() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		assertTrue(electionEventRepository.findAllActiveElectionEvents().size() > 0);
		for (ElectionEvent electionEvent : electionEventRepository.findAllActiveElectionEvents()) {
			assertFalse(electionEvent.getId().equals(ROOT_ELECTION_EVENT_ID));
		}
	}

	@Test
	public void testFindElectionEventById() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		ElectionEvent retrievedElectionEvent = electionEventRepository.findById(fixture.getElectionEvent().getId());
		assertNotNull(retrievedElectionEvent);
	}

	@Test
	public void testFindAllElectionEvents() {
		List<ElectionEvent> electionEventList = electionEventRepository.findAll();
		assertTrue(!electionEventList.isEmpty());
	}

	@Test
	public void testFindLatestElectionDay() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		fixture.setElectionDay(fixture.buildElectionDay(fixture.getElectionEvent()));
		fixture.createElectionDay(fixture.getElectionDay());
		assertNotNull(electionEventRepository.findLatestElectionDay(electionEventRepository.findById(fixture.getElectionEvent().getId())));
	}

	@Test
	public void testDemoElection_withDefault_returnsFalse() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());
		ElectionEvent retrievedElectionEvent = electionEventRepository.findById(fixture.getElectionEvent().getId());
		assertEquals(retrievedElectionEvent.isDemoElection(), false);
	}

	@Test
	public void testDemoElection_withTrue_returnsTrue() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.getElectionEvent().setDemoElection(true);
		fixture.createElectionEvent(fixture.getElectionEvent());
		ElectionEvent retrievedElectionEvent = electionEventRepository.findById(fixture.getElectionEvent().getId());
		assertEquals(retrievedElectionEvent.isDemoElection(), true);
	}

	private ElectionTestFixture createElectionTestFixture() {
		ElectionTestFixture fixture = new ElectionTestFixture(userDataService, accessRepository, electionGroupRepository,
				electionRepository, contestRepository, localeRepository, electionEventRepository, electionEventService);
		fixture.init();

		return fixture;
	}

	@Test(groups = TestGroups.SLOW)
	public void testCopyAreas() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		// 0 is the default number of municipalities in an election event
		assertTrue(mvAreaRepository.findByPathAndLevel(fixture.getElectionEvent().getId(), AreaLevelEnum.MUNICIPALITY.getLevel()).isEmpty());

		electionEventRepository.copyAreas(electionEventRepository.findById("200901"), fixture.getElectionEvent());
		assertFalse(mvAreaRepository.findByPathAndLevel(fixture.getElectionEvent().getId(), AreaLevelEnum.MUNICIPALITY.getLevel()).isEmpty());
	}

	@Test
	public void testCopyElections() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		assertTrue(mvElectionRepository.findByPathAndLevel(fixture.getElectionEvent().getId(), AreaLevelEnum.MUNICIPALITY.getLevel()).isEmpty());

		electionEventRepository.copyElections(electionEventRepository.findById("200901"), fixture.getElectionEvent());

		assertFalse(mvElectionRepository.findByPathAndLevel(fixture.getElectionEvent().getId(), AreaLevelEnum.MUNICIPALITY.getLevel()).isEmpty());
	}

	@Test
	public void copyElections_copiesPartiesPartyContestAreasAndPartyNumber() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		electionEventRepository.copyElections(electionEventRepository.findById("200701"), fixture.getElectionEvent());
		
		// VL - ett innslag i party_contest_area.txt
		assertThat(findParty(fixture, "VL").getPartyContestAreas()).hasSize(1);
		// SFP - to innslag i party_contest_area.txt
		assertThat(findParty(fixture, "SFP").getPartyContestAreas()).hasSize(2);
	}

	@Test
	public void copyPartyNumberConfiguration_copiesPartyNumber() {
		ElectionTestFixture fixture = createElectionTestFixture();

		fixture.setElectionEvent(fixture.buildElectionEvent());
		fixture.createElectionEvent(fixture.getElectionEvent());

		electionEventRepository.copyPartyNumberConfiguration(electionEventRepository.findById("200701"), fixture.getElectionEvent());

		Query partyNumberQuery = getEntityManager().createNativeQuery("select last_party_number from admin.party_number where election_event_pk = ?");
		partyNumberQuery.setParameter(1, fixture.getElectionEvent().getPk());
		// ref party_number.txt
		assertThat(partyNumberQuery.getResultList()).contains(PARTY_NUMBER_START_LOCAL_PARTY, PARTY_NUMBER_START_REGISTERED_PARTY);
	}

	private Party findParty(ElectionTestFixture fixture, String id) {
		return genericTestRepository.findEntitiesByProperty(Party.class, "id", id)
				.stream()
				.filter(party -> party.getElectionEvent().equals(fixture.getElectionEvent()))
				.findFirst().get();
	}
}
