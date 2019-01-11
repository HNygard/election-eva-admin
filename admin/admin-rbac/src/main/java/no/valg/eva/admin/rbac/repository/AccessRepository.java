package no.valg.eva.admin.rbac.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Role;

public class AccessRepository extends BaseRepository {
	public AccessRepository() {
	}

	public AccessRepository(EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Returns the access with the corresponding id
	 */
	public Access findAccessByPath(String accessId) {
		Query q = getEm().createNamedQuery("Access.findAccessById").setParameter("accessId", accessId);
		if (q.getResultList().size() < 1) {
			return null;
		}
		return (Access) q.getSingleResult();
	}

	/**
	 * Returns a set of all accesses in all roles included by the parameter, excluding disabled roles. If the included roles include roles the accesses of
	 * theses are also included and so on. Including all descending accesses as described in the method findDecsAccesses.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getIncludedAccessesNoDisabledRoles(Role role) {
		Query query = getEm().createNativeQuery(
				"SELECT a.access_path FROM access a JOIN role_access_all_active ra1 ON a.access_pk = ra1.access_pk WHERE ra1.role_pk = ?1")
				.setParameter(1, role.getPk());
		return new HashSet<>((List<String>) query.getResultList());
	}

	/**
	 * Returns a set of all accesses in all roles included by the parameter, including disabled roles. If the included roles include roles the accesses of
	 * theses are also included and so on. Including all descending accesses as described in the method findDecsAccesses.
	 */
	public Set<Access> getIncludedAccesses(Role role) {
		TypedQuery<Access> query = getEm().createNamedQuery("Role.getIncludedAccesses", Access.class).setParameter(1, role.getPk());
		return new HashSet<>(query.getResultList());
	}

	public List<Access> findAllAccesses() {
		return findAllEntities(Access.class);
	}

	public Access findAccessByPk(Long pk) {
		return findEntityByPk(Access.class, pk);
	}
}
