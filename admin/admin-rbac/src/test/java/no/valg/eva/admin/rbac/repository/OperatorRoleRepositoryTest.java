package no.valg.eva.admin.rbac.repository;

import no.evote.security.UserData;
import no.evote.service.backendmock.RepositoryBackedRBACTestFixture;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.service.backendmock.RBACTestFixture.PATH_ADMIN_EVENT;
import static no.evote.service.backendmock.RBACTestFixture.PATH_ELECTION_EVENT_VALG07;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;



@Test(groups = TestGroups.REPOSITORY)
public class OperatorRoleRepositoryTest extends AbstractJpaTestBase {
    private static final String OPERATOR_ID_030115000292 = "03011500292";
	private OperatorRoleRepository operatorRoleRepository;
	private OperatorRepository operatorRepository;
	private GenericTestRepository genericRepository;
	private RepositoryBackedRBACTestFixture rbacTestFixture;
	private MvAreaRepository mvAreaRepository;
	private AccessRepository accessRepository;

	@BeforeMethod(alwaysRun = true)
	public void initRepositories() {
		rbacTestFixture = new RepositoryBackedRBACTestFixture(getEntityManager());
		rbacTestFixture.init();

		operatorRoleRepository = new OperatorRoleRepository(getEntityManager());

		operatorRepository = new OperatorRepository(getEntityManager());
		genericRepository = new GenericTestRepository(getEntityManager());
		mvAreaRepository = new MvAreaRepository(getEntityManager());
		accessRepository = new AccessRepository(getEntityManager());
	}

	@Test
	public void shallFindOperatorsRolesForOperatorAtOrBelowMvArea() {
		ElectionObjects eo = buildElectionForTest();
		Operator operator1 = createOperator("01010154321", eo.electionEvent);
		Operator operator2 = createOperator("02020298765", eo.electionEvent);

		Role roleA = createRole("roleA", eo.electionEvent);
		Role roleB = createRole("roleB", eo.electionEvent);

		OperatorRole op1HasRoleAForNorge = createOperatorRole(operator1, roleA, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);
		OperatorRole op1HasRoleBForOslo = createOperatorRole(operator1, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForOslo);
		OperatorRole op2HasRoleBForNorge = createOperatorRole(operator2, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);

		assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOrBelowMvArea(operator1, eo.mvAreaForNorge),
				asList(op1HasRoleAForNorge, op1HasRoleBForOslo));
        assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOrBelowMvArea(operator1, eo.mvAreaForOslo), singletonList(op1HasRoleBForOslo));
        assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOrBelowMvArea(operator2, eo.mvAreaForNorge), singletonList(op2HasRoleBForNorge));
        assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOrBelowMvArea(operator1, eo.mvAreaForNordland), emptyList());
	}

	@Test
	public void shallFindOperatorRolesForOperatorAtOwnLevel() {
		ElectionObjects eo = buildElectionForTest();
		Operator operator1 = createOperator("01010154321", eo.electionEvent);

		Role roleA = createRole("roleA", eo.electionEvent);
		Role roleB = createRole("roleB", eo.electionEvent);

		OperatorRole op1HasRoleAForNorge = createOperatorRole(operator1, roleA, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);
		OperatorRole op1HasRoleBForOslo = createOperatorRole(operator1, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForOslo);

        assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOwnLevel(operator1, eo.mvAreaForNorge), singletonList(op1HasRoleAForNorge));
        assertEquals(operatorRoleRepository.operatorRolesForOperatorAtOwnLevel(operator1, eo.mvAreaForOslo), singletonList(op1HasRoleBForOslo));
	}

	@Test
	public void shallFindOperatorsRolesAtOwnLevel() {
		ElectionObjects eo = buildElectionForTest();
		Operator operator1 = createOperator("01010154321", eo.electionEvent);
		Operator operator2 = createOperator("02020298765", eo.electionEvent);

		Role roleA = createRole("roleA", eo.electionEvent);
		Role roleB = createRole("roleB", eo.electionEvent);

		OperatorRole op1HasRoleAForNorge = createOperatorRole(operator1, roleA, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);
		OperatorRole op1HasRoleBForOslo = createOperatorRole(operator1, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForOslo);
		OperatorRole op2HasRoleBForNorge = createOperatorRole(operator2, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);

		assertEquals(operatorRoleRepository.operatorRolesAtArea(eo.mvAreaForNorge), asList(op1HasRoleAForNorge, op2HasRoleBForNorge));
        assertEquals(operatorRoleRepository.operatorRolesAtArea(eo.mvAreaForOslo), singletonList(op1HasRoleBForOslo));
	}

	@Test
	public void shallFindDescOperatorsRoles() {
		ElectionObjects eo = buildElectionForTest();
		Operator operator1 = createOperator("01010154321", eo.electionEvent);
		Operator operator2 = createOperator("02020298765", eo.electionEvent);

		Role roleA = createRole("roleA", eo.electionEvent);
		Role roleB = createRole("roleB", eo.electionEvent);

		OperatorRole op1HasRoleAForNorge = createOperatorRole(operator1, roleA, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);
		OperatorRole op1HasRoleBForOslo = createOperatorRole(operator1, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForOslo);
		OperatorRole op2HasRoleBForNorge = createOperatorRole(operator2, roleB, mvElectionForElectionEvent(eo.electionEvent), eo.mvAreaForNorge);

		List<OperatorRole> actual = operatorRoleRepository.findDescOperatorsRoles(eo.mvAreaForNorge); // nondeterministic order when same role
		List<OperatorRole> expected = asList(op1HasRoleAForNorge, op1HasRoleBForOslo, op2HasRoleBForNorge);
		assertThat(actual).containsAll(expected);
		assertThat(actual).hasSameSizeAs(expected); // since actual may be bigger than expected after the previous assert
        assertEquals(operatorRoleRepository.findDescOperatorsRoles(eo.mvAreaForOslo), singletonList(op1HasRoleBForOslo));
	}

	@Test
	public void create() {
		UserData createUser = rbacTestFixture.getSysAdminUserData();

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setMvArea(genericRepository.findEntityByProperty(MvArea.class, "areaPath", PATH_ADMIN_EVENT));
		operatorRole.setMvElection(genericRepository.findEntityByProperty(MvElection.class, "electionPath", PATH_ADMIN_EVENT));
		operatorRole.setRole(rbacTestFixture.getRoleVotingCount());
		operatorRole.setOperator(operatorRepository.findByElectionEventsAndId(genericRepository.findEntityByProperty(ElectionEvent.class, "id",
				PATH_ELECTION_EVENT_VALG07).getPk(), "03011500292"));

		operatorRole = operatorRoleRepository.create(createUser, operatorRole);

		OperatorRole savedOperatorRole = operatorRoleRepository.findByPk(operatorRole.getPk());

		Assert.assertNotNull(savedOperatorRole.getPk());
		Assert.assertNotNull(savedOperatorRole.getAuditTimestamp());
		Assert.assertEquals("I", savedOperatorRole.getAuditOperation());
		Assert.assertEquals(0, savedOperatorRole.getAuditOplock());

		operatorRoleRepository.delete(rbacTestFixture.getSysAdminUserData(), operatorRole.getPk());
	}

	@Test
	public void update() {
		UserData createUser = rbacTestFixture.getSysAdminUserData();
		UserData updateUser = rbacTestFixture.getSysAdminUserData();

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setMvArea(genericRepository.findEntityByProperty(MvArea.class, "areaPath", PATH_ADMIN_EVENT));
		operatorRole.setMvElection(genericRepository.findEntityByProperty(MvElection.class, "electionPath", PATH_ADMIN_EVENT));
		operatorRole.setRole(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll());
		operatorRole.setOperator(operatorRepository.findByElectionEventsAndId(genericRepository.findEntityByProperty(ElectionEvent.class, "id",
				PATH_ELECTION_EVENT_VALG07).getPk(), OPERATOR_ID_030115000292));

		operatorRoleRepository.create(createUser, operatorRole);
		DateTime createdTimestamp = DateTime.now();

//		// wait a bit
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// No biggie if this one fails. In worst case, the test fails, and this can be investigated more
//		}

		operatorRole.setMvArea(genericRepository.findEntityByProperty(MvArea.class, "areaPath", PATH_ELECTION_EVENT_VALG07));
		operatorRole.setMvElection(genericRepository.findEntityByProperty(MvElection.class, "electionPath", PATH_ELECTION_EVENT_VALG07));
		operatorRole.setRole(rbacTestFixture.getRoleVotingCount());

		operatorRole = operatorRoleRepository.update(updateUser, operatorRole);

		operatorRole = operatorRoleRepository.findByPk(operatorRole.getPk());

		DateTime updatedTimestamp = operatorRole.getAuditTimestamp();

		Assert.assertNotNull(operatorRole.getAuditTimestamp());
		Assert.assertTrue(updatedTimestamp.compareTo(createdTimestamp) > 0);
		Assert.assertEquals("U", operatorRole.getAuditOperation());
		Assert.assertEquals(1, operatorRole.getAuditOplock());

		operatorRoleRepository.delete(updateUser, operatorRole.getPk());
	}

	@Test
	public void canFetchOperatorRolesForOperator() {
		Assert.assertEquals(operatorRoleRepository.getOperatorRoles(rbacTestFixture.getOperator1()).size(), 1);
		Assert.assertEquals(operatorRoleRepository.getOperatorRoles(rbacTestFixture.getOperator1()).get(0),
				rbacTestFixture.getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag());
	}

	@Test
	public void canFindDescendingOperatorsRolesForMvArea() {
		Assert.assertEquals(operatorRoleRepository.findDescOperatorsRoles(
				rbacTestFixture.getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag().getMvArea()).size(), 4);

		Set<OperatorRole> correctOperatorRoles = new HashSet<>();
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag());
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator11Empty());
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator3VotingCountElectionDayReadInSorTrondelag());
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator1111VotingCountElectionDayAllInTydalPollingDistrict());
		Assert.assertTrue(
				operatorRoleRepository.findDescOperatorsRoles(rbacTestFixture.getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag().getMvArea())
						.containsAll(correctOperatorRoles));
		Assert.assertEquals(operatorRoleRepository
				.findDescOperatorsRoles(rbacTestFixture.getOperatorRoleOperator2VotingCountElectionDayApproveInTroms().getMvArea()).size(), 2);

		correctOperatorRoles.clear();
		correctOperatorRoles = new HashSet<>();
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator2VotingCountElectionDayApproveInTroms());
		correctOperatorRoles.add(rbacTestFixture.getOperatorRoleOperator2VotingCountInTroms());
		Assert.assertTrue(
				operatorRoleRepository.findDescOperatorsRoles(rbacTestFixture.getOperatorRoleOperator2VotingCountElectionDayApproveInTroms().getMvArea())
						.containsAll(correctOperatorRoles));
	}

    @Test
    public void findOperatorRolesGivingOperatorAccess_given_returnsOperatorRoles() {
        MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200701"));
        Long electionEventPk = mvArea.getElectionEvent().getPk();
        Operator operator = operatorRepository.findByElectionEventsAndId(electionEventPk, "03011700143");
        Access access = accessRepository.findAccessByPath("tilgang.brukere");

        List<OperatorRole> operatorRolesGivingOperatorAccess = operatorRoleRepository.findOperatorRolesGivingOperatorAccess(2L, mvArea, operator, access);

        assertThat(operatorRolesGivingOperatorAccess.size()).isEqualTo(1);
        assertThat(operatorRolesGivingOperatorAccess.get(0).getRole().getName()).isEqualTo("Valghendelseadministrator");
    }

    @Test
    public void findConflictingOperatorRoles_givenCandidateContestRole_returnsOperatorRoles() {
        Candidate candidate = new Candidate();
        candidate.setId("03011700143");
        MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.03.0301.030104"));
        Role role = new Role();
        role.setId("stemmemottak_valgting");
        
        List<OperatorRole> conflictingOperatorRoles = operatorRoleRepository.findConflictingOperatorRoles(new PersonId(candidate.getId()), mvArea, role);

        Assert.assertEquals(conflictingOperatorRoles.size(), 1);
    }

	private Operator createOperator(final String id, ElectionEvent electionEvent) {
		Operator operator = new Operator();
		operator.setElectionEvent(electionEvent);

		operator.setId(id);
		operator.setFirstName("Ola");
		operator.setLastName("Nordmann");
		operator.setNameLine("Ola Nordmann");

		return genericRepository.createEntity(operator);
	}

	private Role createRole(String roleName, ElectionEvent electionEvent) {
		Role role = new Role();
		role.setElectionEvent(electionEvent);

		role.setId(roleName);
		role.setName("@role[" + roleName + "].name");

		return genericRepository.createEntity(role);
	}

	private OperatorRole createOperatorRole(Operator operator, Role role, MvElection mvElection, MvArea mvArea) {
		OperatorRole operatorRole = new OperatorRole();

		operatorRole.setOperator(operator);
		operatorRole.setRole(role);
		operatorRole.setMvElection(mvElection);
		operatorRole.setMvArea(mvArea);

		return genericRepository.createEntity(operatorRole);
	}

	private ElectionObjects buildElectionForTest() {
		ElectionObjects eo = new ElectionObjects();

		ElectionEventStatus eeConfigurationApproved = (ElectionEventStatus) getEntityManager().createQuery(
				"SELECT ees FROM ElectionEventStatus ees WHERE ees.id = 3").getSingleResult();
		Locale norskBokmaal = (Locale) getEntityManager().createQuery("SELECT locale from Locale locale WHERE locale.id = 'nb-NO'").getSingleResult();

		eo.electionEvent = new ElectionEvent("000042", "Enhetstestvalg", norskBokmaal);
		eo.electionEvent.setElectionEventStatus(eeConfigurationApproved);
		eo.electionEvent.setElectoralRollCutOffDate(new LocalDate(2015, 7, 1));
		eo.electionEvent.setVotingCardDeadline(new LocalDate(2015, 8, 3));
		eo.electionEvent.setVotingCardElectoralRollDate(new LocalDate(2015, 7, 11));
		eo.electionEvent = genericRepository.createEntity(eo.electionEvent);

		eo.norge = new Country("47", "Norge", eo.electionEvent);
		eo.norge = genericRepository.createEntity(eo.norge);
		eo.mvAreaForNorge = mvAreaForCountry(eo.norge);

		eo.nordland = new County("17", "Nordland", eo.norge);
		eo.nordland.setLocale(norskBokmaal);
		eo.nordland.setCountyStatus(genericRepository.findEntityByProperty(CountyStatus.class, "id", 3));
		eo.nordland = genericRepository.createEntity(eo.nordland);
		eo.mvAreaForNordland = mvAreaForCounty(eo.nordland);

		eo.oslo = new County("01", "Oslo", eo.norge);
		eo.oslo.setLocale(norskBokmaal);
		eo.oslo.setCountyStatus(genericRepository.findEntityByProperty(CountyStatus.class, "id", 3));
		eo.oslo = genericRepository.createEntity(eo.oslo);
		eo.mvAreaForOslo = mvAreaForCounty(eo.oslo);

		return eo;
	}

	private MvElection mvElectionForElectionEvent(ElectionEvent electionEvent) {
		Query query = getEntityManager().createNativeQuery("SELECT mve.* FROM mv_election mve WHERE mve.election_event_pk = ?1", MvElection.class)
				.setParameter(1, electionEvent.getPk());
		return (MvElection) query.getSingleResult();
	}

	private MvArea mvAreaForCountry(Country country) {
		Query query = getEntityManager().createNativeQuery("SELECT mva.* FROM mv_area mva WHERE mva.country_pk = ?1 AND mva.county_pk IS NULL", MvArea.class)
				.setParameter(1, country.getPk());
		return (MvArea) query.getSingleResult();
	}

	private MvArea mvAreaForCounty(County county) {
		Query query = getEntityManager().createNativeQuery("SELECT mva.* FROM mv_area mva WHERE mva.county_pk = ?1 AND mva.municipality_pk IS NULL",
				MvArea.class).setParameter(1, county.getPk());
		return (MvArea) query.getSingleResult();
	}

	private static class ElectionObjects {
		private ElectionEvent electionEvent;
		private Country norge;
		private County nordland;
		private County oslo;

		private MvArea mvAreaForNorge;
		private MvArea mvAreaForNordland;
		private MvArea mvAreaForOslo;
	}
}

