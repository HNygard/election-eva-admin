package no.valg.eva.admin.configuration.application;


import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.OpptellingskategoriService;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.service.OpptellingskategoriDomainService;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "OpptellingskategoriService")


@Default
@Remote(OpptellingskategoriService.class)
public class OpptellingskategoriApplicationService implements OpptellingskategoriService {
	@Inject
	private OpptellingskategoriDomainService domainService;

	@Override
	@SecurityNone
	public List<CountCategory> countCategoriesForValgSti(UserData userData, ValgSti valgSti) {
		return domainService.countCategoriesForValgSti(userData.operatorValggeografiSti(), valgSti);
	}

	@Override
	@SecurityNone
	public List<CountCategory> countCategories(UserData userData) {
		return domainService.countCategories(userData.operatorValggeografiSti());
	}
}
