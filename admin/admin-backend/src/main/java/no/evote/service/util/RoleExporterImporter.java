package no.evote.service.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.constants.PollingPlaceType;
import no.evote.exception.EvoteException;
import no.evote.service.rbac.LegacyAccessServiceBean;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAreaLevel;
import no.valg.eva.admin.rbac.repository.RoleRepository;

import org.apache.commons.lang3.text.StrTokenizer;

public class RoleExporterImporter {

	@Inject
	private LegacyAccessServiceBean accessService;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private RoleRepository roleRepository;

	/**
	 * Export the given roles to CSV format. Can optionally exclude default roles.
	 * 
	 * @param roles List of roles to be exported
	 * @param excludeDefaultRoles True; will exclude default election event roles
	 */
	public String exportRoles(List<Role> roles, final boolean excludeDefaultRoles) {
		final String sep = "\t";
		StringBuilder exportData = new StringBuilder();
		for (Role role : roles) {
			if (excludeDefaultRoles && isDefaultRole(role)) {
				continue;
			}

			exportData
					.append(role.getId())
					.append(sep)
					.append(role.getName())
					.append(sep)
					.append(role.getSecurityLevel())
					.append(sep)
					.append(role.isMutuallyExclusive())
					.append(sep)
					.append(role.isActive())
					.append(sep);

			exportIncludedRoles(exportData, role);
			exportData.append(sep);
			exportAccesses(exportData, role.getAccesses());
			exportData.append(sep);
			exportAreaLevels(exportData, role.getRoleAreaLevels());
			exportData.append('\n');
		}

		return exportData.toString();
	}

	private void exportAreaLevels(StringBuilder exportData, Set<RoleAreaLevel> roleAreaLevels) {
		for (RoleAreaLevel areaLevel : roleAreaLevels) {
			exportData
					.append(areaLevel.getAreaLevel().getId())
					.append(':')
					.append(areaLevel.getPollingPlaceType())
					.append(';');
		}
	}

	private boolean isDefaultRole(final Role role) {
		return Arrays.asList(EvoteConstants.DEFAULT_ROLES).contains(role.getId());
	}

	private void exportAccesses(final StringBuilder exportData, final Set<Access> accesses) {
		for (Access access : accesses) {
			exportData.append(access.getPath()).append(';');
		}
	}

	private void exportIncludedRoles(final StringBuilder exportData, final Role role) {
		for (Role includedRole : role.getIncludedRoles()) {
			exportData.append(includedRole.getId()).append(';');
		}
	}

	/**
	 * Given a list of roles to import as a CSV string, will build a list of roles that can be created.
	 * @param electionEvent The election event we are importing roles to
	 * @param importData List of roles and accesses as CSV data
	 */
	public List<Role> buildRoleListFromImportData(final ElectionEvent electionEvent, final String importData) {
		List<Role> roles = new ArrayList<>();

		Map<String, ArrayList<Role>> dependentRoles = new HashMap<>();
		String[] lines = importData.split("\\n");
		for (String line : lines) {
			roles.add(buildRoleEntity(dependentRoles, line, electionEvent));
		}

		fixIncludedRoles(electionEvent, roles, dependentRoles);

		return sortRolesInCreationOrder(roles);
	}

	/**
	 * Sort roles in the order that is required to create them. Roles that have other roles that depend on them must be created first - this is done in two
	 * steps: first roles are sorted with the roles that have the most included roles first. Second, each role is inserted into a new list, before the first
	 * role we can find that depends on the role we are inserting.
	 * 
	 * @param roles List of unsorted roles
	 * @return Sorted list of roles in the order that they need to be created
	 */
	private List<Role> sortRolesInCreationOrder(final List<Role> roles) {
		Collections.sort(roles, new RoleExporterImporter.IncludedRolesCountComparator());

		List<Role> sortedRoles = new ArrayList<>();
		for (Role r : roles) {
			boolean added = false;
			for (int i = 0; i < sortedRoles.size(); ++i) {
				if (sortedRoles.get(i).includesRole(r)) {
					sortedRoles.add(i, r);
					added = true;
					break;
				}
			}

			if (!added) {
				sortedRoles.add(r);
			}
		}
		return sortedRoles;
	}

	/**
	 * Fix the list of included roles on each role. First, loops through the roles defined in the import and adds them. Next, loop through the list of roles
	 * that are included and look them up in the database.
	 */
	private void fixIncludedRoles(final ElectionEvent electionEvent, final List<Role> roles,
			final Map<String, ArrayList<Role>> dependentRoles) {
		for (Role r : roles) {
			if (dependentRoles.containsKey(r.getId())) {
				for (Role dependentRole : dependentRoles.get(r.getId())) {
					dependentRole.getIncludedRoles().add(r);
				}
				dependentRoles.remove(r.getId());
			}
		}

		// for (String roleId : dependentRoles.keySet()) {
		for (Entry<String, ArrayList<Role>> entry : dependentRoles.entrySet()) {
			String roleId = entry.getKey();
			Role role = roleRepository.findByElectionEventAndId(electionEvent, roleId);

			if (role == null) {
				throw new EvoteException(new UserMessage("@rbac.roles_import.error_unknown_included_role", roleId));
			}

			for (Role dependentRole : entry.getValue()) {
				dependentRole.getIncludedRoles().add(role);
			}
		}
	}

	/**
	 * Build a role instance given the supplied data. Will reuse role instance from database if found, but remove included roles and accesses.
	 * 
	 * @param line Data for the role that is to be imported, on CSV format
	 */
	private Role buildRoleEntity(final Map<String, ArrayList<Role>> dependentRoles, final String line, final ElectionEvent electionEvent) {
		StrTokenizer tokenizer = new StrTokenizer(line, '\t');
		tokenizer.setIgnoreEmptyTokens(false);
		String roleId = tokenizer.nextToken();

		Role role = null;
		Role existingRole = roleRepository.findByElectionEventAndId(electionEvent, roleId);
		if (existingRole != null) {
			role = existingRole;
			role.getIncludedRoles().clear();
			role.getAccesses().clear();
		} else {
			role = new Role();
			role.setElectionEvent(electionEvent);
			role.setId(roleId);
			role.setRoleAreaLevels(new HashSet<RoleAreaLevel>());
		}

		role.setName(tokenizer.nextToken());
		role.setSecurityLevel(Integer.parseInt(tokenizer.nextToken()));
		role.setMutuallyExclusive(Boolean.parseBoolean(tokenizer.nextToken()));
		role.setActive(Boolean.parseBoolean(tokenizer.nextToken()));

		String includedRolesList = tokenizer.nextToken();
		if (!includedRolesList.isEmpty()) {
			addIncludedRoles(dependentRoles, role, includedRolesList);
		}

		String accessList = tokenizer.nextToken();
		if (!accessList.isEmpty()) {
			addAccesses(role, accessList);
		}

		String areaLevelList = tokenizer.nextToken();
		if (!areaLevelList.isEmpty()) {
			addAreaLevels(role, areaLevelList);
		}
		return role;
	}

	private void addAreaLevels(Role role, String areaLevelList) {
		for (String areaLevelIdAndPollingPlaceType : areaLevelList.split(";")) {
			String[] parts = areaLevelIdAndPollingPlaceType.split(":");
			String areaLevelId = parts[0];
			PollingPlaceType pollingPlaceType = PollingPlaceType.valueOf(parts[1]);
			AreaLevel areaLevel = mvAreaRepository.findAreaLevelById(areaLevelId);
			RoleAreaLevel roleAreaLevel = new RoleAreaLevel(role, areaLevel, pollingPlaceType);
			role.getRoleAreaLevels().add(roleAreaLevel);
		}
	}

	private void addAccesses(final Role role, final String accessList) {
		Set<Access> accesses = new HashSet<>();
		role.setAccesses(accesses);
		for (String accessPath : accessList.split(";")) {
			Access access = accessService.findAccessByPath(accessPath);
			if (access == null) {
				throw new EvoteException("Unknown access: " + accessPath);
			}
			accesses.add(access);
		}
	}

	private void addIncludedRoles(final Map<String, ArrayList<Role>> dependentRoles, final Role role, final String includedRoleList) {
		for (String roleId : includedRoleList.split(";")) {
			if (!dependentRoles.containsKey(roleId)) {
				dependentRoles.put(roleId, new ArrayList<Role>());
			}

			dependentRoles.get(roleId).add(role);
		}
	}

	public void setAccessService(final LegacyAccessServiceBean accessService) {
		this.accessService = accessService;
	}

	public void setRoleRepository(final RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public void setMvAreaRepository(MvAreaRepository mvAreaRepository) {
		this.mvAreaRepository = mvAreaRepository;
	}

	/**
	 * Used when sorting roles to be created in correct order.
	 */
	private static final class IncludedRolesCountComparator implements Comparator<Role>, Serializable {
		@Override
		public int compare(final Role o1, final Role o2) {
			return Integer.valueOf(o1.getIncludedRoles().size()).compareTo(Integer.valueOf(o2.getIncludedRoles().size())) * -1;
		}
	}
}
