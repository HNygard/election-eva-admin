package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.valg.eva.admin.configuration.domain.model.Locale;

@Default
@ApplicationScoped

public class LocaleRepository extends BaseRepository {
	public LocaleRepository() {
	}

	public LocaleRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Locale findByPk(Long pk) {
		return super.findEntityByPk(Locale.class, pk);
	}

	public List<Locale> findAllLocales() {
		return super.findAllEntities(Locale.class);
	}

	public Locale findById(String id) {
		return super.findEntityById(Locale.class, id);
	}
}
