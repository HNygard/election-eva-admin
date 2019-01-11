package no.valg.eva.admin.rbac.repository;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAreaLevel;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



@Test(groups = TestGroups.REPOSITORY)
public class RoleRepositoryTest extends AbstractJpaTestBase {

	private static final String ACCESS_NAME = "test";
	private static final boolean ADVANCE_POLLING_PLACE = false;
	private static final String ELECTION_EVENT_VALG2007 = "200701";
	private static final String ROLE_ID = "role";
	private static final String ANOTHER_ROLE_ID = "another_role";
	private static final String INCLUDED_ROLE_ID = "included_role";
	private static final String ROLE_WITH_HIGER_LEVEL_ID = "higher_level_role";
	private static final String ADMIN = "000000";
	private static final String VALG2007 = "200701";
	private static final String ROOT_LITERAL = "root";
	private static final String TEST_OPERATOR_LITERAL = "operator";
	private static final String TEST_OPERATOR_TEST_LITERAL = "test";
	private static final String TEST_OPERATOR_NL = "test test";
	private static final String TEST_OPERATOR_E = "test@test.com";
	private static final String TEST_OPERATOR_PC = "0000";
	private static final String TEST_OPERATOR_TN = "654456645";
	private RoleRepository roleRepository;

	private UserData userData;
	private Role role;
	private Role includedRole;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod
	public void setUp() {
		genericTestRepository = new GenericTestRepository(getEntityManager());

		roleRepository = new RoleRepository(getEntityManager());
		userData = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		Role udRole = new Role();
		udRole.setUserSupport(false);
		udRole.setElectionEvent(new ElectionEvent());
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		operatorRole.setRole(udRole);
		operatorRole.setOperator(operator);
		userData.setOperatorRole(operatorRole);

		// Set up role and included role
		role = role(ROLE_ID, true);
		includedRole = roleRepository.create(userData, role(INCLUDED_ROLE_ID, false));
		Set<Role> includedRoles = new HashSet<>();
		includedRoles.add(includedRole);
		role.setIncludedRoles(includedRoles);
		roleRepository.create(userData, role);
	}

	@Test
	public void create() {
		Role role = role(ANOTHER_ROLE_ID, false);
		role = roleRepository.create(userData, role);

		assertNotNull(role.getPk());
		assertNotNull(role.getAuditTimestamp());
		assertEquals("I", role.getAuditOperation());
		assertEquals(0, role.getAuditOplock());
	}

	@Test
	public void update() {
		DateTime createdTimestamp = role.getAuditTimestamp();

		role.setId("test1");
		role.setName("test1");
		role.setElectionEvent(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ROOT_ELECTION_EVENT_ID));
		role.setSecurityLevel(4);
		role = roleRepository.update(userData, role);

		DateTime updatedTimestamp = role.getAuditTimestamp();

		assertNotNull(updatedTimestamp);
		assertThat(updatedTimestamp.compareTo(createdTimestamp) > 0).isTrue();
		assertEquals("U", role.getAuditOperation());
		assertEquals(1, role.getAuditOplock());
	}

	@Test
	public void testGetRoleWithIncludedRoles() {
		Set<Role> roles = roleRepository.getRoleWithIncludedRoles(role);

		assertThat(roles.size()).isEqualTo(2);
		assertThat(roles).contains(role, includedRole);
	}

	@Test
	public void testGetAccumulatedSecLevelDefaultRoles() {
		assertThat(roleRepository.getAccumulatedSecLevelFor(role)).isEqualTo(3);
	}

	@Test
	public void testGetAccumulatedSecLevelWhenThereIsARoleWithHigherLevel() {
		Role roleWithHigherLevel = role(ROLE_WITH_HIGER_LEVEL_ID, false);
		int expectedLevel = 4;
		roleWithHigherLevel.setSecurityLevel(expectedLevel);
		role.getIncludedRoles().add(roleRepository.create(userData, roleWithHigherLevel));
		assertThat(roleRepository.getAccumulatedSecLevelFor(role)).isEqualTo(expectedLevel);
	}

	@Test
	public void testFindAllRolesInElectionEvent() {

		int listSize = roleRepository.findAllRolesInElectionEvent(
				genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007)).size();
		Role role = role(ANOTHER_ROLE_ID, false);

		role = roleRepository.create(userData, role);
		assertNotNull(role);
		assertThat(
				roleRepository.findAllRolesInElectionEvent(
						genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007)).size())
								.isEqualTo(
										listSize + 1);
	}

	private Role role(final String id, boolean withAccesses) {
		Role role = new Role();
		role.setId(id);
		role.setName(ROLE_ID);
		role.setElectionEvent(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007));
		role.setSecurityLevel(3);
		if (withAccesses) {
			role.setAccesses(accesses());
		}
		return role;
	}

	private Set<Access> accesses() {
		Set<Access> accesses = new HashSet<>();
		accesses.add(genericTestRepository.createEntity(access()));
		return accesses;
	}

	private Access access() {
		Access access = new Access();
		access.setName(ACCESS_NAME);
		access.setPath("test.trallala");
		return access;
	}

	@Test
	public void testFindByElectionEventAndId() {
		assertThat(
				roleRepository.findByElectionEventAndId(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007),
						ROLE_ID))
								.isEqualTo(role);
	}

	@Test
	public void testFindByElectionEventAndIdWhenNoResult() {
		assertThat(
				roleRepository.findByElectionEventAndId(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "event som ikke finnes"),
						ROLE_ID))
								.isNull();
	}

	@Test
	public void testFindByPk() {
		assertThat(roleRepository.findByPk(role.getPk())).isEqualTo(role);
	}

	@Test
	public void testDeleteRole() {
		roleRepository.delete(userData, role);
		assertThat(roleRepository.findByPk(role.getPk())).isNull();
	}

	@Test
	public void testDeleteExistingRoles() {
		roleRepository.deleteExistingRoles(role.getElectionEvent());
		getEntityManager().flush();
		getEntityManager().clear();
		assertThat(roleRepository.findByPk(role.getPk())).isNull();
	}

	@Test
	public void getAccesses() {
		Set<Access> accesses = roleRepository.getAccesses(role);
		assertThat(accesses.size()).isEqualTo(1);
		assertThat(accesses.iterator().next().getName()).isEqualTo(ACCESS_NAME);
	}

	@Test
	public void getIncludedRoles() {
		Set<Role> includedRoles = roleRepository.getIncludedRoles(role);
		assertThat(includedRoles.size()).isEqualTo(1);
		assertThat(includedRoles).contains(includedRole);
	}

	/*
	* The root role has every role included in some way and can therefore assign all roles
	*/
	@Test(enabled = false, description = "This is old code and does not work anymore")
	public void findAssignableRolesForOperatorRoleRoot() {

		MvArea mvArea = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", ADMIN);
		MvElection mvElection = genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", ADMIN);
		Operator operator = createOperator("12058711150", TEST_OPERATOR_LITERAL, VALG2007);
		Role roleForOperator = role(ROOT_LITERAL, false);

		OperatorRole op = createOperatorRole(mvArea, mvElection, operator, roleForOperator);

		Set<Role> expectedRoles = new HashSet<>();
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", VALG2007);
		expectedRoles.add(roleRepository.findByElectionEventAndId(electionEvent, "reserve"));

		Set<Role> assignableRoles = new HashSet<>(roleRepository.findAssignableRolesForOperatorRole(op));

		assertThat(assignableRoles).isEqualTo(expectedRoles);
	}

	@Test
	public void assignableRolesForAreaCountyReturnsNoRolesWhenRoleAreaLevelCountyIsNotSetOnAnyRole() {
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007);
		AreaPath anyCountyPath = new AreaPath(ELECTION_EVENT_VALG2007 + ".47.03");
		assertThat(roleRepository.assignableRolesForArea(anyCountyPath.getLevel(), electionEvent.getPk())).hasSize(2);
	}

	@Test
	public void assignableRolesForAreaCountyReturnsRolesWithRoleAreaLevelCountySet() {
		// set up
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007);
		// set first role to level county
		Role role = roleRepository.findByElectionEventAndId(electionEvent, ROLE_ID);
		AreaLevel areaLevel = genericTestRepository.findEntityByProperty(AreaLevel.class, "id", AreaLevelEnum.COUNTY.getLevel());
		RoleAreaLevel roleAreaLevel = new RoleAreaLevel(role, areaLevel, null);
		roleAreaLevel.setAuditOperator("test"); // dette burde vært gjort av en entity listener
		role.getRoleAreaLevels().add(roleAreaLevel);

		AreaPath anyCountyPath = new AreaPath(ELECTION_EVENT_VALG2007 + ".47.03");

		List<RoleItem> result = roleRepository.assignableRolesForArea(anyCountyPath.getLevel(), electionEvent.getPk());

		assertThat(result).hasSize(3);
	}

	@Test
	public void add_roleAreaLevelToRoleWithoutAreaLevels_areaLevelElectionDayPollingPlaceIsAdded() {
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007);
		Role role = roleRepository.findByElectionEventAndId(electionEvent, ROLE_ID);
		AreaLevel areaLevel = genericTestRepository.findEntityByProperty(AreaLevel.class, "id", AreaLevelEnum.POLLING_PLACE.getLevel());
		RoleAreaLevel roleAreaLevel = new RoleAreaLevel(role, areaLevel, PollingPlaceType.ELECTION_DAY_VOTING);
		roleAreaLevel.setAuditOperator("test"); // dette burde vært gjort av en entity listener

		role.getRoleAreaLevels().add(roleAreaLevel);
		Role updatedRole = roleRepository.findByPk(role.getPk());

		Set<RoleAreaLevel> roleAreaLevels = updatedRole.getRoleAreaLevels();
		assertThat(roleAreaLevels).hasSize(1);
		RoleAreaLevel persistedRoleAreaLevel = roleAreaLevels.iterator().next();
		assertThat(persistedRoleAreaLevel.getPollingPlaceType()).isEqualTo(PollingPlaceType.ELECTION_DAY_VOTING);
	}

	@Test
	public void updateAssignableAreaLevels_roleWithAreaLevelElectionDayPollingPlace_updatedToAdvancePollingPlace() {
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007);
		List<Role> roles = roleRepository.findAllRolesInElectionEvent(electionEvent);
		Role role = roles.get(0);
		AreaLevel areaLevel = genericTestRepository.findEntityByProperty(AreaLevel.class, "id", AreaLevelEnum.POLLING_PLACE.getLevel());
		RoleAreaLevel roleAreaLevel = new RoleAreaLevel(role, areaLevel, PollingPlaceType.ELECTION_DAY_VOTING);
		roleAreaLevel.setAuditOperator("test"); // dette burde vært gjort av en entity listener
		role.getRoleAreaLevels().add(roleAreaLevel);

		Role updatedRole = roleRepository.findByPk(role.getPk()); // trigger flush

		Set<AreaLevelEnum> assignableAreaLevels = new HashSet<>();
		assignableAreaLevels.add(AreaLevelEnum.POLLING_PLACE);
		Map<AreaLevel, AreaLevel> mapWithPersistedAreaLevels = new HashMap<>();
		mapWithPersistedAreaLevels.put(new AreaLevel(AreaLevelEnum.POLLING_PLACE), areaLevel);

		// endre fra election day til advance
		updatedRole.updateAssignableAreaLevels(assignableAreaLevels, mapWithPersistedAreaLevels, ADVANCE_POLLING_PLACE);

		Role resultRole = roleRepository.findByPk(role.getPk());
		Set<RoleAreaLevel> resultRoleAreaLevels = resultRole.getRoleAreaLevels();
		assertThat(resultRoleAreaLevels).hasSize(1);
		RoleAreaLevel updatedRoleAreaLevel = resultRoleAreaLevels.iterator().next();
		assertThat(updatedRoleAreaLevel.getPollingPlaceType()).isEqualTo(PollingPlaceType.ADVANCE_VOTING);
	}

	@Test
	public void updateAssignableAreaLevels_roleWithAreaLevelElectionDayPollingPlaceAndMunicipality_updatedToAdvancePollingPlaceAndNotMunicipality() {
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", ELECTION_EVENT_VALG2007);
		Role role = roleRepository.findByElectionEventAndId(electionEvent, ROLE_ID);
		AreaLevel areaLevelPollingPlace = genericTestRepository.findEntityByProperty(AreaLevel.class, "id", AreaLevelEnum.POLLING_PLACE.getLevel());
		RoleAreaLevel roleAreaLevelPollingPlace = new RoleAreaLevel(role, areaLevelPollingPlace, PollingPlaceType.ELECTION_DAY_VOTING);
		roleAreaLevelPollingPlace.setAuditOperator("test"); // dette burde vært gjort av en entity listener
		role.getRoleAreaLevels().add(roleAreaLevelPollingPlace);
		AreaLevel areaLevelMunicipality = genericTestRepository.findEntityByProperty(AreaLevel.class, "id", AreaLevelEnum.MUNICIPALITY.getLevel());
		RoleAreaLevel roleAreaLevelMunicipality = new RoleAreaLevel(role, areaLevelMunicipality, PollingPlaceType.NOT_APPLICABLE);
		roleAreaLevelMunicipality.setAuditOperator("test"); // dette burde vært gjort av en entity listener
		role.getRoleAreaLevels().add(roleAreaLevelMunicipality);

		Role updatedRole = roleRepository.findByPk(role.getPk()); // trigger flush

		assertThat(updatedRole.getRoleAreaLevels()).hasSize(2);

		Set<AreaLevelEnum> assignableAreaLevels = new HashSet<>();
		assignableAreaLevels.add(AreaLevelEnum.POLLING_PLACE);
		Map<AreaLevel, AreaLevel> mapWithPersistedAreaLevels = new HashMap<>();
		mapWithPersistedAreaLevels.put(new AreaLevel(AreaLevelEnum.POLLING_PLACE), areaLevelPollingPlace);
		mapWithPersistedAreaLevels.put(new AreaLevel(AreaLevelEnum.MUNICIPALITY), areaLevelMunicipality);

		// endre fra election day til advance
		updatedRole.updateAssignableAreaLevels(assignableAreaLevels, mapWithPersistedAreaLevels, ADVANCE_POLLING_PLACE);

		Role resultRole = roleRepository.findByPk(role.getPk());
		Set<RoleAreaLevel> resultRoleAreaLevels = resultRole.getRoleAreaLevels();
		assertThat(resultRoleAreaLevels).hasSize(1);
		RoleAreaLevel updatedRoleAreaLevel = resultRoleAreaLevels.iterator().next();
		assertThat(updatedRoleAreaLevel.getAreaLevel().getId()).isEqualTo(AreaLevelEnum.POLLING_PLACE.getLevel());
		assertThat(updatedRoleAreaLevel.getPollingPlaceType()).isEqualTo(PollingPlaceType.ADVANCE_VOTING);
	}

	protected Operator createOperator(final String id, final String firstName, final String electionEventId) {
		Operator tempOperator = new Operator();
		tempOperator.setId(id);
		tempOperator.setFirstName(firstName);
		tempOperator.setActive(true);
		tempOperator.setMiddleName(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setLastName(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setNameLine(TEST_OPERATOR_NL);
		tempOperator.setAddressLine1(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setAddressLine2(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setAddressLine3(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setEmail(TEST_OPERATOR_E);
		tempOperator.setPostalCode(TEST_OPERATOR_PC);
		tempOperator.setPostTown(TEST_OPERATOR_TEST_LITERAL);
		tempOperator.setTelephoneNumber(TEST_OPERATOR_TN);
		tempOperator.setElectionEvent(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", electionEventId));
		return tempOperator;
	}

	protected OperatorRole createOperatorRole(final MvArea mvArea, final MvElection mvElection, final Operator operator, final Role role) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setMvArea(mvArea);
		operatorRole.setMvElection(mvElection);
		operatorRole.setOperator(genericTestRepository.createEntity(operator));
		operatorRole.setRole(genericTestRepository.createEntity(role));
		genericTestRepository.createEntity(operatorRole);
		return operatorRole;
	}
}

