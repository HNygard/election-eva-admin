package no.valg.eva.admin.counting.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

/**
 * Repo for finding different constant code value entities in the counting module like VoteCountStatus, CountQualifer etc. We intend to have one
 * CodeValueRepository per module in the system.
 */
@Default
@ApplicationScoped
public class CountingCodeValueRepository extends BaseRepository {

	public CountingCodeValueRepository() {
	}

	CountingCodeValueRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VoteCountStatus findVoteCountStatusByCountStatus(CountStatus countStatus) {
		return findVoteCountStatusById(countStatus.getId());
	}

	public VoteCountStatus findVoteCountStatusById(int id) {
		return super.findEntityById(VoteCountStatus.class, id);
	}

	public CountQualifier findCountQualifierById(String id) {
		return super.findEntityById(CountQualifier.class, id);
	}

	public VoteCountCategory findVoteCountCategoryById(String id) {
		return super.findEntityById(VoteCountCategory.class, id);
	}

	public VoteCategory findVoteCategoryById(VoteCategory.VoteCategoryValues voteCategoryId) {
		return super.findEntityById(VoteCategory.class, voteCategoryId.name());
	}
}
