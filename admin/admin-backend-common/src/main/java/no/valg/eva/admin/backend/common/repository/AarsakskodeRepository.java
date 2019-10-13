package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.valg.eva.admin.configuration.domain.model.Aarsakskode;

@Default
@ApplicationScoped
public class AarsakskodeRepository extends BaseRepository {
	public AarsakskodeRepository() {
	}

	public AarsakskodeRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<Aarsakskode> findAll() {
		return super.findAllEntities(Aarsakskode.class);
	}
}
