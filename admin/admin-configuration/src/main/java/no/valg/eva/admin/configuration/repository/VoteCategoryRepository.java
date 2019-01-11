package no.valg.eva.admin.configuration.repository;

import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;

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
