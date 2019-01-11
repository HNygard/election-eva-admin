package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.persistence.EntityManager;

import no.evote.model.Statuskode;

public class StatuskodeRepository extends BaseRepository {
	public StatuskodeRepository() {
	}

	public StatuskodeRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<Statuskode> findAll() {
		return super.findAllEntities(Statuskode.class);
	}
}
