package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;

public interface VoteCountCategoryService extends Serializable {
	@Cacheable
	List<VoteCountCategory> findAll(UserData userData, CountCategory... excludedCategories);
}
