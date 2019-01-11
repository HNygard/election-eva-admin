package no.valg.eva.admin.rbac.repository;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAccess;
import no.valg.eva.admin.rbac.domain.model.RoleInclude;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleRepository extends BaseRepository {
	private RoleRepository() {
	}

	/**
	 * Constructor for use in tests.
	 */
	public RoleRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public List<Role> findAllRolesInElectionEvent(ElectionEvent event) {
		return getEm()
				.createNamedQuery("Role.findAllInElectionEvent", Role.class)
				.setParameter("event", event)
				.getResultList();
	}

	/**
	 * Get the role together with all included roles
	 */
	public Set<Role> getRoleWithIncludedRoles(final Role role) {
		TypedQuery<Role> query = getEm().createNamedQuery("Role.getRoleWithIncludedRoles", Role.class).setParameter(1, role.getPk());
		return new HashSet<>(query.getResultList());
	}

	/**
	 * Gets the highest security level in all recursively included roles and the role parameter
	 */
	public Integer getAccumulatedSecLevelFor(Role role) {
		Query q = getEm().createNativeQuery(
				"SELECT MAX(r.security_level) FROM role r JOIN role_include_all ri ON r.role_pk = ri.included_role_pk WHERE ri.role_pk = :rolePk");
		q.setParameter("rolePk", role.getPk());
		return (Integer) q.getSingleResult();
	}

	public Role findByElectionEventAndId(final ElectionEvent ee, final String id) {
		TypedQuery<Role> query = getEm().createNamedQuery("Role.findByElectionEventAndId", Role.class).setParameter("event", ee).setParameter("id", id);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Role findByPk(final Long pk) {
		return super.findEntityByPk(Role.class, pk);
	}

	public Role create(final UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) final Role role) {
		return createEntity(userData, role);
	}

	/**
	 * Returns only the enabled roles that the operatorRole itself has access to. Ignoring access objects descending from t
	 */
	public List<Role> findAssignableRolesForOperatorRole(final OperatorRole operatorRole) {
		TypedQuery<Role> query = getEm().createNamedQuery("Role.getAssignableRoles", Role.class).setParameter(1, operatorRole.getOperator().getPk());
		setCacheHint(query);
		return query.getResultList();
	}

	public Role update(final UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) final Role role) {
		return updateEntity(userData, role);
	}

	/**
	 * Deletes a {@link Role}, with its {@link OperatorRole}, (this and others) {@link RoleInclude}, and {@link RoleAccess} associations.
	 */
	public void delete(final UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT, entity = Role.class) final Role role) {
		TypedQuery<OperatorRole> operatorRolesQuery = getEm().createNamedQuery("Role.getOperatorRoles", OperatorRole.class).setParameter(Role.ROLE_PARAMETER,
				role);
		deleteEntities(userData, new ArrayList<>(operatorRolesQuery.getResultList()));

		TypedQuery<RoleInclude> includedRolesQuery = getEm().createNamedQuery("Role.getThisRoleIncludes", RoleInclude.class).setParameter(Role.ROLE_PARAMETER,
				role);
		deleteEntities(userData, new ArrayList<>(includedRolesQuery.getResultList()));

		TypedQuery<RoleInclude> includedRolesQuery2 = getEm().createNamedQuery("Role.getOthersRoleIncludes", RoleInclude.class).setParameter(
				Role.ROLE_PARAMETER, role);
		deleteEntities(userData, new ArrayList<>(includedRolesQuery2.getResultList()));

		TypedQuery<RoleAccess> roleAccessesQuery = getEm().createNamedQuery("Role.getRoleAccesses", RoleAccess.class).setParameter(Role.ROLE_PARAMETER, role);
		deleteEntities(userData, new ArrayList<>(roleAccessesQuery.getResultList()));

		deleteEntity(userData, Role.class, role.getPk());
	}

	public Set<Access> getAccesses(final Role role) {
		TypedQuery<Access> query = getEm().createNamedQuery("Role.getAccesses", Access.class).setParameter(Role.ROLE_PARAMETER, role);
		return new HashSet<>(query.getResultList());
	}

	public Set<Role> getIncludedRoles(final Role role) {
		TypedQuery<Role> query = getEm().createNamedQuery("Role.getIncludedRoles", Role.class).setParameter(Role.ROLE_PARAMETER, role);
		return new HashSet<>(query.getResultList());
	}

	/**
	 * Delete all existing roles, except the defaults
	 */
	public void deleteExistingRoles(ElectionEvent electionEvent) {
		getEm().createNamedQuery("Role.deleteAllExcept").setParameter("electionEvent", electionEvent)
				.setParameter("ids", Arrays.asList(EvoteConstants.DEFAULT_ROLES)).executeUpdate();
	}

    public List<Role> findRolesByCheckCandidateConflict(ElectionEvent electionEvent) {
        return getEm()
                .createNamedQuery("Role.findByCheckCandidateConflict", Role.class)
                .setParameter("electionEvent", electionEvent)
                .getResultList();
    }

	/**
	 * Finds assignable roles for given area path and election event.
	 * @param areaLevelEnum
	 *            area level
	 * @param electionEventPk
	 *            pk of election event
	 * @return list of role items
	 */
	public List<RoleItem> assignableRolesForArea(AreaLevelEnum areaLevelEnum, Long electionEventPk) {
		TypedQuery<Role> query = getEm().createNamedQuery("Role.assignableRolesForArea", Role.class);
		query.setParameter("areaLevelId", areaLevelEnum.getLevel());
		query.setParameter("electionEventPk", electionEventPk);
		List<Role> roles = query.getResultList();
		List<RoleItem> roleItems = new ArrayList<>();
		for (Role role : roles) {
			roleItems.add(new RoleItem(role.getId(), role.getName(), role.isUserSupport(), role.getElectionLevel(), role.levelsAsEnums()));
		}
		return roleItems;
	}

}
