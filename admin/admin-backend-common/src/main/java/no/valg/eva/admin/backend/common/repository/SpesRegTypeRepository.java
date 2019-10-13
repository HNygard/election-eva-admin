package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.evote.model.SpesRegType;

@Default
@ApplicationScoped
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
