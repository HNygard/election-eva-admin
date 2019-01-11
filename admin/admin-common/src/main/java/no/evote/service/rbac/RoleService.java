package no.evote.service.rbac;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import no.evote.security.UserData;
import no.valg.eva.admin.common.FindByIdRequest;
import no.valg.eva.admin.common.rbac.CircularReferenceCheckRequest;
import no.valg.eva.admin.common.rbac.PersistRoleResponse;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Role;

public interface RoleService extends Serializable {
	/**
	 * @return all roles as view objects for user/election event
	 */
	List<no.valg.eva.admin.common.rbac.Role> findAllRolesWithoutAccessesForView(UserData userData);

	Integer getAccumulatedSecLevelFor(Role role);

	Integer getAccumulatedSecLevelFor(UserData userData, FindByIdRequest findByIdRequest);

	@Deprecated
	Role findByElectionEventAndId(ElectionEvent e, String id);

	/**
	 * Validates and updates Role
	 * @return list of validation feedback
	 */
	List<String> updateRole(UserData userData, no.valg.eva.admin.common.rbac.Role role);

	/**
	 * Validates and saves Role
	 * @return list of validation feedback and persisted role
	 */
	PersistRoleResponse persistRole(UserData userData, no.valg.eva.admin.common.rbac.Role role);

	void delete(UserData userData, Long pk);

	/**
	 * @return accesses for given role
	 */
	no.valg.eva.admin.common.rbac.Role findRoleWithAccessesForView(UserData userData, FindByIdRequest findByIdRequest);

	/**
	 * @return included roles for given role pk
	 */
	Set<no.valg.eva.admin.common.rbac.Role> findIncludedRolesForView(UserData userData, FindByIdRequest findByIdRequest);

	/**
	 * Export roles and accesses. The resulting string contains one line for each role, on the following format:
	 * 
	 * <pre>
	 * ROLE_ID	ROLE_NAME	SECURITY_LEVEL	MUTUALLY_EXCLUSIVE	ACTIVE	INCLUDED_ROLE1;INCLUDED_ROLE2;...	ACCESS_ID1;ACCESS_ID2;...
	 * </pre>
	 * 
	 * @return result string
	 */
	String exportRoles(UserData userData, boolean excludeDefaultRoles);

	/**
	 * Import roles given a string on the format as exported by <code>exportRoles</code>
	 * @return imported roles count
	 */
	int importRoles(UserData userData, String string, boolean deleteExisting);

	/**
	 * @return true if circular reference, else false
	 */
	boolean isCircularReference(UserData userData, CircularReferenceCheckRequest circularReferenceCheckRequest);

	/**
	 * @return view object for role with given id
	 */
	no.valg.eva.admin.common.rbac.Role findForViewById(UserData userData, FindByIdRequest findByIdRequest);
}
