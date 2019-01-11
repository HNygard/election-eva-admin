package no.evote.service.rbac;

import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Administrere;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Roller_Import;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.FindByIdRequest;
import no.valg.eva.admin.common.rbac.CircularReferenceCheckRequest;
import no.valg.eva.admin.common.rbac.PersistRoleResponse;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "RoleService")
@Remote(RoleService.class)
public class RoleServiceEjb implements RoleService {
	@Inject
	private RoleServiceBean roleService;
	@Inject
	private RoleRepository roleRepository;

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public List<no.valg.eva.admin.common.rbac.Role> findAllRolesWithoutAccessesForView(UserData userData) {
		return roleService.findAllRolesWithoutAccessesForView(userData);
	}

	/**
	 * Gets the highest security level in all recursively included roles and the role parameter
	 */
	@Override
	@SecurityNone
	public Integer getAccumulatedSecLevelFor(Role role) {
		return roleRepository.getAccumulatedSecLevelFor(role);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public Integer getAccumulatedSecLevelFor(UserData userData, FindByIdRequest findByIdRequest) {
		Role role = roleRepository.findByElectionEventAndId(userData.electionEvent(), findByIdRequest.getId());
		return getAccumulatedSecLevelFor(role);
	}

	@Override
	@Deprecated
	@SecurityNone // Brukes bare i test
	public Role findByElectionEventAndId(ElectionEvent ee, String id) {
		return roleRepository.findByElectionEventAndId(ee, id);
	}

	@Override
	@Security(accesses = Tilgang_Brukere_Administrere, type = WRITE)
	public List<String> updateRole(UserData userData, no.valg.eva.admin.common.rbac.Role roleViewObject) {
		return roleService.updateRole(userData, roleViewObject);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = WRITE)
	public PersistRoleResponse persistRole(UserData userData, no.valg.eva.admin.common.rbac.Role roleViewObject) {
		return roleService.persistRole(userData, roleViewObject);
	}

	/**
	 * This metod deletes a role, and removes the role entity from the objects that depend on it.
	 */
	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = WRITE)
	public void delete(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT, entity = Role.class) final Long pk) {
		roleService.delete(userData, pk);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public no.valg.eva.admin.common.rbac.Role findRoleWithAccessesForView(UserData userData, FindByIdRequest findByIdRequest) {
		return roleService.findRoleWithAccessesForView(userData, findByIdRequest);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public Set<no.valg.eva.admin.common.rbac.Role> findIncludedRolesForView(UserData userData, FindByIdRequest findByIdRequest) {
		return roleService.findIncludedRolesForView(userData, findByIdRequest);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Import, type = READ)
	public String exportRoles(UserData userData, boolean excludeDefaultRoles) {
		return roleService.exportRoles(userData, excludeDefaultRoles);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Import, type = WRITE)
	public int importRoles(UserData userData, String importData, boolean deleteExistingRoles) {
		return roleService.importRoles(userData, importData, deleteExistingRoles);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public boolean isCircularReference(UserData userData, CircularReferenceCheckRequest circularReferenceCheckRequest) {
		return roleService.isCircularReference(userData, circularReferenceCheckRequest);
	}

	@Override
	@Security(accesses = Tilgang_Roller_Administrere, type = READ)
	public no.valg.eva.admin.common.rbac.Role findForViewById(final UserData userData, final FindByIdRequest findByIdRequest) {
		return roleRepository.findByElectionEventAndId(userData.electionEvent(), findByIdRequest.getId()).toViewObject(false);
	}
}
