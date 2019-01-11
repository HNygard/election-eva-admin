package no.valg.eva.admin.configuration.application;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.service.ValghierarkiDomainService;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;

@Stateless(name = "ListeforslagValghierarkiService")
@Remote(ValghierarkiService.class)
public class ListeforslagValghierarkiApplicationService extends ValghierarkiApplicationService implements ValghierarkiService {
	@Inject
	private ValghierarkiDomainService domainService;

	@Override
	@SecurityNone
	public List<Valg> valg(UserData userData, ValggruppeSti valggruppeSti, CountCategory countCategory) {
		return domainService.valg(valggruppeSti, userData.operatorValggeografiSti(), true, countCategory);
	}
}
