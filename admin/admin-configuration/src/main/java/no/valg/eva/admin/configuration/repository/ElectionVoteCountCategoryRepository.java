package no.valg.eva.admin.configuration.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;

public class ElectionVoteCountCategoryRepository extends BaseRepository {
	public ElectionVoteCountCategoryRepository() {
	}

	protected ElectionVoteCountCategoryRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public List<ElectionVoteCountCategory> findElectionVoteCountCategories(ElectionGroup electionGroup) {
		TypedQuery<ElectionVoteCountCategory> query =
				getEm().createNamedQuery("ElectionVoteCountCategory.findElectionVoteCountCategories", ElectionVoteCountCategory.class);
		query.setParameter("electionGroupPk", electionGroup.getPk());
		return query.getResultList();
	}

	public void update(final UserData userData, final List<ElectionVoteCountCategory> electionVoteCountCategories) {
		for (ElectionVoteCountCategory electionVoteCountCategory : electionVoteCountCategories) {
			super.updateEntity(userData, electionVoteCountCategory);
		}
	}

	public void create(final UserData userData, final ElectionVoteCountCategory electionVoteCountCategory) {
		createEntity(userData, electionVoteCountCategory);
	}

	public List<ElectionVoteCountCategory> findElectionVoteCountCategories(ElectionGroup electionGroup, boolean detachEntities) {
		if (detachEntities) {
			return detach(findElectionVoteCountCategories(electionGroup));
		}
		return findElectionVoteCountCategories(electionGroup);
	}

	public List<ElectionVoteCountCategory> findElectionVoteCountCategories(ElectionGroup electionGroup, CountCategory... excludedCategories) {
		if (excludedCategories.length == 0) {
			return findElectionVoteCountCategories(electionGroup);
		}
		Set<CountCategory> excludedCategorySet = new HashSet<>();
		Collections.addAll(excludedCategorySet, excludedCategories);
		List<ElectionVoteCountCategory> result = new ArrayList<>();
		List<ElectionVoteCountCategory> electionVoteCountCategories = findElectionVoteCountCategories(electionGroup);
		for (ElectionVoteCountCategory electionVoteCountCategory : electionVoteCountCategories) {
			CountCategory countCategory = CountCategory.fromId(electionVoteCountCategory.getVoteCountCategory().getId());
			if (excludedCategorySet.contains(countCategory)) {
				continue;
			}
			result.add(electionVoteCountCategory);
		}
		return result;
	}
}
