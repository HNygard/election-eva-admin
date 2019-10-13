package no.valg.eva.admin.configuration.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;

@Default
@ApplicationScoped
public class VoteCategoryRepository extends BaseRepository {
	public VoteCategoryRepository() {
	}

	public VoteCategoryRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VoteCategory findVoteCategoryById(String id) {
		return super.findEntityById(VoteCategory.class, id);
	}
}
