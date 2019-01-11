package no.valg.eva.admin.backend.common.repository;

import javax.persistence.EntityManager;

import no.evote.model.BaseEntity;
import no.evote.security.UserData;

public class GenericRepository extends BaseRepository {
	public GenericRepository() {
	}

	public GenericRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public BaseEntity findByPk(Class<?> clazz, Long pk) {
		return (BaseEntity) super.findEntityByPk(clazz, pk);
	}

	public <T> T create(UserData userData, T entity) {
		return super.createEntity(userData, entity);
	}

	public void delete(UserData userData, Class<?> entityClass, Long pk) {
		super.deleteEntity(userData, entityClass, pk);
	}

	public <T> T update(UserData userData, T entity) {
		return super.updateEntity(userData, entity);
	}
}
