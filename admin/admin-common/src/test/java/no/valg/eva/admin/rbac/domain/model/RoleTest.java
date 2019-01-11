package no.valg.eva.admin.rbac.domain.model;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class RoleTest {

	public static final long ELECTION_EVENT_PK = 1L;
	public static final long ROLE_PK = 2L;
	public static final String ACCESS_E = "E";
	public static final boolean ANY_BOOLEAN = false;
	public static final int SECURITY_LEVEL_3 = 3;
	private Role role;

	@BeforeMethod
	public void setUp() {
		role = role();
	}

	private Role role() {
		Role role = roleWithoutAreaLevels();
		role.setRoleAreaLevels(roleAreaLevels(role));
		return role;
	}

	private Role roleWithoutAreaLevels() {
		Role role = new Role();
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(ELECTION_EVENT_PK);
		role.setElectionEvent(electionEvent);
		role.setId("");
		role.setPk(ROLE_PK);
		role.setAccesses(accesses());
		return role;
	}

	private Set<Access> accesses() {
		Set<Access> accesses = new HashSet<>();
		Access access = new Access();
		access.setPath("");
		accesses.add(access);
		return accesses;
	}

	@Test
	public void oneLevelIncludedRoleIsHighest() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(0L);

		Role includedRole = new Role();
		includedRole.setActive(true);
		includedRole.setSecurityLevel(SECURITY_LEVEL_3);
		includedRole.setElectionEvent(electionEvent);
		includedRole.setId("roleId");

		Role role = new Role();
		role.setActive(true);
		role.setSecurityLevel(2);
		role.getIncludedRoles().add(includedRole);

		assertEquals(role.getAccumulatedSecLevel(), SECURITY_LEVEL_3);
	}

	@Test
	public void twoLevelIncludedRoleIsHighest() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(0L);

		Role includedRole = new Role();
		includedRole.setActive(true);
		includedRole.setSecurityLevel(2);
		includedRole.setElectionEvent(electionEvent);
		includedRole.setId("roleId");

		Role includedRole2 = new Role();
		includedRole2.setActive(true);
		includedRole2.setSecurityLevel(SECURITY_LEVEL_3);
		includedRole2.setElectionEvent(electionEvent);
		includedRole2.setId("roleId");
		includedRole.getIncludedRoles().add(includedRole2);

		Role role = new Role();
		role.setActive(true);
		role.setSecurityLevel(2);
		role.getIncludedRoles().add(includedRole);

		assertEquals(role.getAccumulatedSecLevel(), SECURITY_LEVEL_3);
	}

	@Test
	public void baseRoleIsHighest() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(0L);

		Role includedRole = new Role();
		includedRole.setActive(true);
		includedRole.setSecurityLevel(2);
		includedRole.setElectionEvent(electionEvent);
		includedRole.setId("roleId");

		Role role = new Role();
		role.setActive(true);
		role.setSecurityLevel(SECURITY_LEVEL_3);
		role.getIncludedRoles().add(includedRole);

		assertEquals(role.getAccumulatedSecLevel(), SECURITY_LEVEL_3);
	}

	@Test
	public void testRemoveRoleAreaLevel() {
		role.removeAreaLevel(COUNTY);
		assertEquals(1, role.getRoleAreaLevels().size());
		assertEquals(MUNICIPALITY.getLevel(), role.getRoleAreaLevels().iterator().next().getAreaLevel().getId());
	}

	@Test
	public void addAssignableAreaLevel_withElectionDayPollingPlace_makesRoleBeAssignableToElectionDayPollingPlace() {
		Role role = roleWithoutAreaLevels();
		AreaLevel areaLevelPollingPlace = new AreaLevel();
		areaLevelPollingPlace.setId(POLLING_PLACE.getLevel());

		role.addOrUpdateAssignableAreaLevel(areaLevelPollingPlace, PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(role.getRoleAreaLevels()).hasSize(1);
		assertThat(role.canBeAssignedToPollingPlace(PollingPlaceType.ELECTION_DAY_VOTING)).isTrue();
	}

	@Test
	public void canBeAssignedToPollingPlace_whenRoleCanBeAssignedToAdvanceVotePollingPlace_returnsFalse() {
		Role role = roleWithoutAreaLevels();
		AreaLevel areaLevelPollingPlace = new AreaLevel();
		areaLevelPollingPlace.setId(POLLING_PLACE.getLevel());
		role.addOrUpdateAssignableAreaLevel(areaLevelPollingPlace, PollingPlaceType.ADVANCE_VOTING);

		boolean result = role.canBeAssignedToPollingPlace(PollingPlaceType.ELECTION_DAY_VOTING);

		assertThat(result).isFalse();
	}

	@Test
	public void canBeAssignedToArea_whenRoleIsForAdvancePollingPlaceAndPollingPlaceTypeIsAdvance_returnsTrue() {
		Role role = roleWithoutAreaLevels();
		AreaLevel areaLevelPollingPlace = new AreaLevel();
		areaLevelPollingPlace.setId(POLLING_PLACE.getLevel());
		role.addOrUpdateAssignableAreaLevel(areaLevelPollingPlace, PollingPlaceType.ADVANCE_VOTING);

		boolean result = role.canBeAssignedToArea(POLLING_PLACE, PollingPlaceType.ADVANCE_VOTING);

		assertThat(result).isTrue();
	}

	@Test
	public void canBeAssignedToArea_whenRoleIsForAdvancePollingPlaceAndPollingPlaceTypeIsElectionDay_returnsFalse() {
		Role role = roleWithoutAreaLevels();
		AreaLevel areaLevelPollingPlace = new AreaLevel();
		areaLevelPollingPlace.setId(POLLING_PLACE.getLevel());
		role.addOrUpdateAssignableAreaLevel(areaLevelPollingPlace, PollingPlaceType.ELECTION_DAY_VOTING);

		boolean result = role.canBeAssignedToArea(POLLING_PLACE, PollingPlaceType.ADVANCE_VOTING);

		assertThat(result).isFalse();
	}

	@Test
	public void testCanBeAssignedToAreaLevel() {
		assertTrue(role.canBeAssignedToAreaLevel(MUNICIPALITY));
	}

	@Test
	public void toViewObject_withoutAccesses_returnsViewObjectWithoutAccesses() {
		Role role = role();

		no.valg.eva.admin.common.rbac.Role viewObject = role.toViewObject(false);

		assertThat(viewObject.getElectionEventPk()).isEqualTo(ELECTION_EVENT_PK);
		assertThat(viewObject.getPk()).isEqualTo(ROLE_PK);
		assertThat(viewObject.getAccesses()).isEmpty();
		assertThat(viewObject.isMunicipalityAreaLevel()).isTrue();
	}

	@Test
	public void toViewObject_whenRoleAreaLevelIsAdvancePollingPlace_returnsViewObjectWithElectionDayVotingPollingPlaceFalse() {
		Role role = role();
		role.getRoleAreaLevels().add(roleAreaLevelForAdvancePollingPlace(role));

		no.valg.eva.admin.common.rbac.Role viewObject = role.toViewObject(ANY_BOOLEAN);

		assertThat(viewObject.isPollingPlaceAreaLevel()).isTrue();
		assertThat(viewObject.getElectionDayVotingPollingPlaceType()).isFalse();
	}

	@Test
	public void toViewObject_whenRoleAreaLevelIsElectionDayPollingPlace_returnsViewObjectWithElectionDayVotingPollingPlaceTrue() {
		Role role = role();
		role.getRoleAreaLevels().add(roleAreaLevelForElectionDayPollingPlace(role));

		no.valg.eva.admin.common.rbac.Role viewObject = role.toViewObject(ANY_BOOLEAN);

		assertThat(viewObject.isPollingPlaceAreaLevel()).isTrue();
		assertThat(viewObject.getElectionDayVotingPollingPlaceType()).isTrue();
	}

	@Test
	public void toViewObjectWithAccesses() {
		no.valg.eva.admin.common.rbac.Role viewObject = role.toViewObject(true);
		assertThat(viewObject.getElectionEventPk()).isEqualTo(ELECTION_EVENT_PK);
		assertThat(viewObject.getPk()).isEqualTo(ROLE_PK);
		assertThat(viewObject.getAccesses()).hasSize(1);
	}

	@Test
	public void updateAssignableAreaLevels() {
		Set<AreaLevelEnum> enumLevels = new HashSet<>();
		enumLevels.add(COUNTY);
		enumLevels.add(MUNICIPALITY);

		role.updateAssignableAreaLevels(enumLevels, allLevels(), null);
		assertThat(role.canBeAssignedToAreaLevel(COUNTY)).isTrue();
		assertThat(role.canBeAssignedToAreaLevel(MUNICIPALITY)).isTrue();
	}

	@Test
	public void updateAssignableAreaLevels_PollingPlaceAndFalseElectionDayPollingPlaceType_PollingPlaceTypeAdvanceVoting() {
		Role role = roleWithoutAreaLevels();
		Set<AreaLevelEnum> enumLevels = new HashSet<>();
		enumLevels.add(POLLING_PLACE);

		role.updateAssignableAreaLevels(enumLevels, allLevels(), Boolean.FALSE);

		assertEquals(PollingPlaceType.ADVANCE_VOTING, role.getRoleAreaLevels().iterator().next().getPollingPlaceType());
	}

	@Test
	public void updateAssignableAreaLevels_PollingPlaceAndTrueElectionDayPollingPlaceType_PollingPlaceTypeElectionDayVoting() {
		Role role = roleWithoutAreaLevels();
		Set<AreaLevelEnum> enumLevels = new HashSet<>();
		enumLevels.add(POLLING_PLACE);

		role.updateAssignableAreaLevels(enumLevels, allLevels(), Boolean.TRUE);

		assertEquals(PollingPlaceType.ELECTION_DAY_VOTING, role.getRoleAreaLevels().iterator().next().getPollingPlaceType());

	}

	@Test
	public void updateAssignableAreaLevels_PollingPlaceAndTrueElectionDayPollingPlaceType_PollingPlaceTypeNotApplicable() {
		Role role = roleWithoutAreaLevels();
		Set<AreaLevelEnum> enumLevels = new HashSet<>();
		enumLevels.add(POLLING_DISTRICT);

		role.updateAssignableAreaLevels(enumLevels, allLevels(), Boolean.TRUE);

		assertEquals(PollingPlaceType.NOT_APPLICABLE, role.getRoleAreaLevels().iterator().next().getPollingPlaceType());
	}

	@Test
	public void levelsAsEnums_withRoleAreaLevels_returnsAllLevels() throws Exception {
		Role role = role();

		List<AreaLevelEnum> levels = role.levelsAsEnums();
		assertThat(levels).hasSize(2);
		assertThat(levels.get(0)).isSameAs(AreaLevelEnum.MUNICIPALITY);
		assertThat(levels.get(1)).isSameAs(AreaLevelEnum.COUNTY);
	}

	private Map<AreaLevel, AreaLevel> allLevels() {
		Map<AreaLevel, AreaLevel> allLevels = new HashMap<>();
		allLevels.put(new AreaLevel(COUNTY), new AreaLevel(COUNTY));
		allLevels.put(new AreaLevel(MUNICIPALITY), new AreaLevel(MUNICIPALITY));
		allLevels.put(new AreaLevel(POLLING_DISTRICT), new AreaLevel(POLLING_DISTRICT));
		allLevels.put(new AreaLevel(POLLING_PLACE), new AreaLevel(POLLING_PLACE));
		return allLevels;
	}

	private Set<RoleAreaLevel> roleAreaLevels(Role role1) {
		Set<RoleAreaLevel> roleAreaLevels = new HashSet();
		roleAreaLevels.add(roleAreaLevel(MUNICIPALITY, role1));
		roleAreaLevels.add(roleAreaLevel(COUNTY, role1));
		return roleAreaLevels;
	}

	private RoleAreaLevel roleAreaLevel(final AreaLevelEnum level, Role role1) {
		AreaLevel areaLevel = new AreaLevel();
		areaLevel.setId(level.getLevel());
		return new RoleAreaLevel(role1, areaLevel, null);
	}

	private RoleAreaLevel roleAreaLevelForAdvancePollingPlace(Role role1) {
		AreaLevel areaLevel = new AreaLevel();
		areaLevel.setId(POLLING_PLACE.getLevel());
		return new RoleAreaLevel(role1, areaLevel, PollingPlaceType.ADVANCE_VOTING);
	}

	private RoleAreaLevel roleAreaLevelForElectionDayPollingPlace(Role role1) {
		AreaLevel areaLevel = new AreaLevel();
		areaLevel.setId(POLLING_PLACE.getLevel());
		return new RoleAreaLevel(role1, areaLevel, PollingPlaceType.ELECTION_DAY_VOTING);
	}
}
