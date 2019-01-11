package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;

/**
 * Defines services for storing and retrieving ElectionVoteCountCategory.
 */
public interface ElectionVoteCountCategoryService extends Serializable {
	/**
	 * @return election vote count categories for election group, optionally without excluded categories
	 */
	List<ElectionVoteCountCategory> findElectionVoteCountCategories(UserData userData, ElectionGroup electionGroup, CountCategory... excludedCategories);

	/**
	 * Updates instances in database.
	 * @param userData
	 *            userData.
	 * @param categories
	 *            instances to update
	 */
	void update(UserData userData, List<ElectionVoteCountCategory> categories);
}
