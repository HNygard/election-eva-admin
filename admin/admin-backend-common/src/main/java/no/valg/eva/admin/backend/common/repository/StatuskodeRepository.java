package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.evote.model.Statuskode;

@Default
@ApplicationScoped
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
