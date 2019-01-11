package no.valg.eva.admin.test;

import lombok.NoArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Contains convenience methods for testing repositories.
 */
@NoArgsConstructor
public class GenericTestRepository {

	@PersistenceContext(unitName = "evotePU")
	private EntityManager entityManager;

	public GenericTestRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public <T> T findEntityByProperty(final Class<T> entityClass, final String idName, final Object idValue) {
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> query = cb.createQuery(entityClass);
			Root<T> root = query.from(entityClass);
			query.select(root).where(cb.equal(root.get(idName), idValue));
			TypedQuery<T> q = entityManager.createQuery(query);

			return q.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}

	public <T> List<T> findEntitiesByProperty(final Class<T> entityClass, final String idName, final Object idValue) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(cb.equal(root.get(idName), idValue));
		TypedQuery<T> q = entityManager.createQuery(query);

		return q.getResultList();
	}

	public <T> T createEntity(final T entity) {
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.refresh(entity);
		return entity;
	}

	/**
	 * Update an entity and synchronize with database
	 */
	public <T> T updateEntity(final T entity) {
		T updatedEntity = entityManager.merge(entity);
		entityManager.flush();
		entityManager.refresh(entity);
		return updatedEntity;
	}

}
