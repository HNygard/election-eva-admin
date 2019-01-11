package no.valg.eva.admin.backend.common.repository;

import javax.persistence.EntityManager;

import no.valg.eva.admin.configuration.domain.model.Responsibility;

public class ResponsibilityRepository extends BaseRepository {
	public ResponsibilityRepository() {
	}

	public ResponsibilityRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Responsibility findByPk(Long pk) {
		return super.findEntityByPk(Responsibility.class, pk);
	}
}
