package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;

public class VoteCountCategoryServiceBean {
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;

	public List<VoteCountCategory> findAll(CountCategory... excludedCategories) {
		if (excludedCategories.length == 0) {
			return voteCountCategoryRepository.findAll();
		}
		Set<CountCategory> excludedCategorySet = new HashSet<>();
		Collections.addAll(excludedCategorySet, excludedCategories);
		List<VoteCountCategory> result = new ArrayList<>();
		for (VoteCountCategory voteCountCategory : voteCountCategoryRepository.findAll()) {
			CountCategory category = CountCategory.fromId(voteCountCategory.getId());
			if (excludedCategorySet.contains(category)) {
				continue;
			}
			result.add(voteCountCategory);
		}
		return result;
	}
}
