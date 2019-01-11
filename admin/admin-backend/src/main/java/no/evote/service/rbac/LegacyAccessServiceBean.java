package no.evote.service.rbac;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class LegacyAccessServiceBean {
	@Inject
	private AccessRepository accessRepository;

	/**
	 * Returns the access with the corresponding id
	 */
	public Access findAccessByPath(String accessId) {
		return accessRepository.findAccessByPath(accessId);
	}

	public Access findAccessByPk(Long pk) {
		return accessRepository.findAccessByPk(pk);
	}

	/**
	 * Returns a set of all accesses in all roles included by the parameter, including disabled roles. If the included roles include roles the accesses of
	 * theses are also included and so on. Including all descending accesses as described in the method findDecsAccesses.
	 */
	public Set<Access> getIncludedAccesses(Role role) {
		return accessRepository.getIncludedAccesses(role);
	}

	/**
	 * Method for recursively including roles as described in getIncludedAccessesNoDisabledRoles
	 */
	public List<Access> findAll() {
		return accessRepository.findAllAccesses();
	}
}
