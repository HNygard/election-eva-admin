package no.evote.service.util;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.evote.service.rbac.LegacyAccessServiceBean;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAreaLevel;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static no.evote.constants.PollingPlaceType.ELECTION_DAY_VOTING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class RoleExporterImporterTest {

	private static final String[] EMPTY = new String[] {};
	@Mock
	private LegacyAccessServiceBean accessService;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private MvAreaRepository mvAreaRepository;

	@BeforeSuite
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	private String createRoleRow(final String roleId, final String[] includesRoles, final String[] accesses, final String[] areaLevels) {
		return roleId + "\t" + roleId + "\t0\ttrue\ttrue\t" + StringUtils.join(includesRoles, ';') + "\t" + StringUtils.join(accesses, ';')
				+ "\t" + StringUtils.join(areaLevels, ';') + "\n";
	}

	@Test
	public void rolesAreAddedInTheCorrectOrder() {
		StringBuilder testData = new StringBuilder();
		testData.append(createRoleRow("r8", toArray("r5", "r6"), EMPTY, EMPTY));
		testData.append(createRoleRow("r6", toArray("r3"), EMPTY, EMPTY));
		testData.append(createRoleRow("r4", toArray("r1"), EMPTY, EMPTY));
		testData.append(createRoleRow("r1", toArray("r2"), EMPTY, EMPTY));
		testData.append(createRoleRow("r5", toArray("r4", "r1", "r3"), toArray("a1", "a2"), EMPTY));
		testData.append(createRoleRow("r2", EMPTY, EMPTY, EMPTY));
		testData.append(createRoleRow("r3", EMPTY, EMPTY, EMPTY));
		testData.append(createRoleRow("r7", toArray("r2", "r8"), EMPTY, EMPTY));

		addAccess("a1");
		addAccess("a2");

		RoleExporterImporter rei = new RoleExporterImporter();
		rei.setAccessService(accessService);
		when(roleRepository.findByElectionEventAndId(any(), anyString())).thenReturn(null);
		rei.setRoleRepository(roleRepository);

		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(1L);

		List<Role> rolesToImport = rei.buildRoleListFromImportData(electionEvent, testData.toString());

		assertEquals(rolesToImport.size(), 8);
		assertEquals(rolesToImport.get(0).getId(), "r2");
		assertEquals(rolesToImport.get(1).getId(), "r1");
		assertEquals(rolesToImport.get(2).getId(), "r4");
		assertEquals(rolesToImport.get(3).getId(), "r3");
		assertEquals(rolesToImport.get(4).getId(), "r5");
		assertEquals(rolesToImport.get(5).getId(), "r6");
		assertEquals(rolesToImport.get(6).getId(), "r8");
		assertEquals(rolesToImport.get(7).getId(), "r7");

		Role r5 = rolesToImport.get(4);
		assertEquals(r5.getId(), "r5");
		assertEquals(r5.getAccesses().size(), 2);
		assertEquals(r5.getIncludedRoles().size(), 3);
	}

	@Test
	public void shouldUseExistingRole() {
		StringBuilder testData = new StringBuilder();
		testData.append(createRoleRow("r1", EMPTY, EMPTY, EMPTY));

		RoleExporterImporter rei = new RoleExporterImporter();
		rei.setAccessService(accessService);

		Role existingRole = new Role();
		existingRole.setPk(9000L);
		existingRole.setId("r1");

		when(roleRepository.findByElectionEventAndId(any(), eq("r1"))).thenReturn(existingRole);
		rei.setRoleRepository(roleRepository);

		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(1L);

		List<Role> rolesToImport = rei.buildRoleListFromImportData(electionEvent, testData.toString());

		assertEquals(rolesToImport.size(), 1);
		Role importedRole = rolesToImport.get(0);
		assertEquals(importedRole.getId(), "r1");
		assertEquals(importedRole.getPk(), Long.valueOf(9000L));
	}

	@Test
	public void importShouldIncludeAreaLevels() {
		StringBuilder testData = new StringBuilder();
		PollingPlaceType expectedPollingPlaceType = PollingPlaceType.ADVANCE_VOTING;
		AreaLevelEnum expectedAreaLevel = AreaLevelEnum.MUNICIPALITY;
		testData.append(createRoleRow("r1", EMPTY, EMPTY, new String[] { expectedAreaLevel.getLevel() + ":" + expectedPollingPlaceType.name() }));
		RoleExporterImporter rei = new RoleExporterImporter();
		rei.setAccessService(accessService);
		rei.setMvAreaRepository(mvAreaRepository);
		when(mvAreaRepository.findAreaLevelById(Integer.toString(expectedAreaLevel.getLevel()))).thenReturn(new AreaLevel(expectedAreaLevel));

		Role existingRole = new Role();
		existingRole.setPk(9000L);
		existingRole.setId("r1");
		existingRole.setElectionEvent(new ElectionEvent(1000L));

		when(roleRepository.findByElectionEventAndId(any(), eq("r1"))).thenReturn(existingRole);
		rei.setRoleRepository(roleRepository);

		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(1L);

		List<Role> rolesToImport = rei.buildRoleListFromImportData(electionEvent, testData.toString());

		assertEquals(rolesToImport.size(), 1);
		Role importedRole = rolesToImport.get(0);
		assertEquals(importedRole.getId(), "r1");
		assertEquals(importedRole.getPk(), Long.valueOf(9000L));
		assertEquals(importedRole.getRoleAreaLevels().size(), 1);
		RoleAreaLevel areaLevel = importedRole.getRoleAreaLevels().iterator().next();
		assertEquals(areaLevel.getPollingPlaceType(), expectedPollingPlaceType);
		assertEquals(areaLevel.getAreaLevel().getId(), expectedAreaLevel.getLevel());
	}

	@Test
	public void exportShouldIncludeAreaLevels() {
		RoleExporterImporter rei = new RoleExporterImporter();
		rei.setAccessService(accessService);
		rei.setMvAreaRepository(mvAreaRepository);
		Role role = new Role();
		role.setId("roleId");
		role.setSecurityLevel(0);
		ElectionEvent electionEvent = new ElectionEvent("950002", "Electionevent", new Locale());
		electionEvent.setPk(1L);
		role.setElectionEvent(electionEvent);
		HashSet<RoleAreaLevel> roleAreaLevels = new HashSet<>();
		roleAreaLevels.add(new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.COUNTRY), ELECTION_DAY_VOTING));
		role.setRoleAreaLevels(roleAreaLevels);
		role.setName("@roleName");
		HashSet<Access> accesses = new HashSet<>();
		Access access = new Access();
		access.setPath("accessPath");
		accesses.add(access);
		role.setAccesses(accesses);
		String exportRoles = rei.exportRoles(newArrayList(role), true);
		assertEquals(exportRoles, "roleId\t@roleName\t0\tfalse\tfalse\t\taccessPath;\t" + AreaLevelEnum.COUNTRY.getLevel() + ":" + ELECTION_DAY_VOTING + ";\n");
	}

	private void addAccess(final String accessName) {
		Access access = new Access();
		access.setPath(accessName);
		access.setName(accessName);
		when(accessService.findAccessByPath(accessName)).thenReturn(access);
	}

	private String[] toArray(final String... strings) {
		return strings;
	}
}

