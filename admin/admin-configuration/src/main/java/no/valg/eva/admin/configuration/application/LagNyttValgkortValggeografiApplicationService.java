package no.valg.eva.admin.configuration.application;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.service.ValggeografiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;

@Stateless(name = "LagNyttValgkortValggeografiService")
@Remote(ValggeografiService.class)
public class LagNyttValgkortValggeografiApplicationService extends ValggeografiApplicationService implements ValggeografiService {
	@Inject
	private ValggeografiDomainService domainService;

	@Override
	@SecurityNone
	public List<Fylkeskommune> fylkeskommuner(UserData userData, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		return domainService.fylkeskommuner(valghendelseSti(userData), valghierarkiSti);
	}

	@Override
	@SecurityNone
	public List<Kommune> kommuner(UserData userData, FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		return domainService.kommuner(fylkeskommuneSti, valghierarkiSti);
	}
}
