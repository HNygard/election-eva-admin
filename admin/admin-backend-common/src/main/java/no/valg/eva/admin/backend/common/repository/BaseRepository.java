package no.valg.eva.admin.backend.common.repository;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ReadOnlyPrivilegeException;
import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import org.hibernate.Session;

import javax.persistence.Cacheable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for repositories
 */
public abstract class BaseRepository {
    @PersistenceContext(unitName = "evotePU")
    private EntityManager em;

    protected BaseRepository() {
    }

    /**
     * Constructor for use in tests.
     */
    protected BaseRepository(EntityManager entityManager) {
        this.em = entityManager;
    }

    public Session getSession() {
        return (Session) getEm().getDelegate();
    }

    /**
     * Create an entity and synchronize with database
     */
    protected <T> T createEntity(UserData userData, T entity) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }
        getEm().persist(entity);
        getEm().flush();
        getEm().refresh(entity);
        return entity;
    }

    /**
     * Create entities and synchronize with database
     */
    protected <T> List<T> createEntities(UserData userData, List<T> entities) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }
        for (T entity : entities) {
            getEm().persist(entity);
        }
        getEm().flush();
        for (T entity : entities) {
            getEm().refresh(entity);
        }
        return entities;
    }

    /**
     * Update an entity and synchronize with database
     */
    protected <T> T updateEntity(UserData userData, T entity) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }
        T updatedEntity = getEm().merge(entity);
        getEm().flush();
        getEm().refresh(updatedEntity);
        return updatedEntity;
    }

    /**
     * Update entities and synchronize with database
     */
    protected <T> List<T> updateEntities(UserData userData, List<T> entities) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }
        List<T> updatedEntities = new ArrayList<>();
        for (final T entity : entities) {
            T updatedEntity = getEm().merge(entity);
            updatedEntities.add(updatedEntity);
        }
        getEm().flush();
        for (final T updatedEntity : updatedEntities) {
            getEm().refresh(updatedEntity);
        }
        return updatedEntities;
    }

    /**
     * Delete an entity and synchronize with database
     */
    protected <T> void deleteEntity(UserData userData, Class<T> entityClass, Long pk) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }
        getEm().remove(getEm().getReference(entityClass, pk));
        getEm().flush();
    }

    protected <T extends BaseEntity> void deleteEntities(UserData userData, List<T> entities) {
        if (userData.getOperatorRole().getRole().isUserSupport()) {
            throw new ReadOnlyPrivilegeException(EvoteConstants.USER_SUPPORT_ERROR_MSG);
        }

        for (T entity : entities) {
            if (entity.getPk() != null) {
                getEm().remove(getEm().getReference(entity.getClass(), entity.getPk()));
            }
        }
        getEm().flush();
    }

    /**
     * Generic method for finding an entity by pk field
     */
    protected <T> T findEntityByPk(Class<T> entityClass, Long pk) {
        return getEm().find(entityClass, pk);
    }

    /**
     * Generic method for finding an entity by id field
     */
    protected <T> T findEntityById(Class<T> entityClass, Object id) {
        try {
            CriteriaBuilder cb = getEm().getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root).where(cb.equal(root.get("id"), id));
            TypedQuery<T> q = getEm().createQuery(query);

            setCacheHintIfCacheable(entityClass, q);

            return q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    private <T> void setCacheHintIfCacheable(Class<T> entityClass, TypedQuery<T> q) {
        if (isCacheable(entityClass)) {
            setCacheHint(q);
        }
    }

    private <T> boolean isCacheable(Class<T> entityClass) {
        return entityClass.isAnnotationPresent(Cacheable.class);
    }

    protected <T> void setCacheHint(TypedQuery<T> q) {
        q.setHint("org.hibernate.cacheable", true);
    }

    protected EntityManager getEm() {
        return em;
    }

    /**
     * used only for unit tests
     */
    protected void setEm(EntityManager em) {
        this.em = em;
    }

    /**
     * Generic method for finding all entities of a type
     */
    protected <T> List<T> findAllEntities(Class<T> entityClass) {
        CriteriaQuery<T> query = getEm().getCriteriaBuilder().createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        TypedQuery<T> q = getEm().createQuery(query);
        setCacheHintIfCacheable(entityClass, q);
        return q.getResultList();
    }

    /**
     * Generic method for finding entities filtered on election event
     */
    protected <T> List<T> findEntitiesByElectionEvent(Class<T> entityClass, Long electionEventPk) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root).where(cb.equal(root.get("electionEvent"), electionEventPk));
        TypedQuery<T> q = getEm().createQuery(query);
        setCacheHintIfCacheable(entityClass, q);
        return q.getResultList();
    }

    protected <T> List<T> detach(List<T> entities) {
        for (T entity : entities) {
            em.detach(entity);
        }
        return entities;
    }

    public <T> void detach(T entity) {
        getEm().detach(entity);
    }

    public <T> void refresh(T entity) {
        getEm().refresh(entity);
    }

    public <T> T getReference(Class<T> clazz, Long primaryKey) {
        return getEm().getReference(clazz, primaryKey);
    }
}
