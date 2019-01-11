package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.persistence.EntityManager;

import no.evote.model.SpesRegType;

public class SpesRegTypeRepository extends BaseRepository {
	public SpesRegTypeRepository() {
	}

	public SpesRegTypeRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<SpesRegType> findAll() {
		return super.findAllEntities(SpesRegType.class);
	}
}
