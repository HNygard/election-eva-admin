package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "VoteCountCategoryService")



@Default
@Remote(VoteCountCategoryService.class)
public class VoteCountCategoryServiceEjb implements VoteCountCategoryService {
	@Inject
	private VoteCountCategoryServiceBean voteCountCategoryService;

	@Override
	@Security(accesses = {Beskyttet_Slett_Opptelling, Konfigurasjon_Opptellingsmåter}, type = READ)
	public List<VoteCountCategory> findAll(UserData userData, CountCategory... excludedCategories) {
		return voteCountCategoryService.findAll(excludedCategories);
	}
}
