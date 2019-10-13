package no.valg.eva.admin.configuration.application;

import static java.lang.String.format;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.service.ValghierarkiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ValghierarkiService")


@Default
@Remote(ValghierarkiService.class)
public class ValghierarkiApplicationService implements ValghierarkiService {
	@Inject
	private ValghierarkiDomainService domainService;

	@Override
	@SecurityNone
	public Valghierarki valghierarki(ValghierarkiSti valghierarkiSti) {
		switch (valghierarkiSti.nivaa()) {
		case VALGHENDELSE:
			return domainService.valghendelse((ValghendelseSti) valghierarkiSti);
		case VALGGRUPPE:
			return domainService.valggruppe((ValggruppeSti) valghierarkiSti);
		case VALG:
			return domainService.valg((ValgSti) valghierarkiSti);
		case VALGDISTRIKT:
			return domainService.valgdistrikt((ValgdistriktSti) valghierarkiSti);
		default:
			throw new IllegalArgumentException(format("ukjent nivå: %s", valghierarkiSti.nivaa()));
		}
	}

	@Override
	@SecurityNone
	public Valghendelse valghendelse(UserData userData) {
		return domainService.valghendelse(valghendelseSti(userData));
	}

	@Override
	@SecurityNone
	public List<Valggruppe> valggrupper(UserData userData) {
		return domainService.valggrupper(valghendelseSti(userData));
	}

	@Override
	@SecurityNone
	public Valg valg(ValgSti valgSti) {
		return domainService.valg(valgSti);
	}

	@Override
	@SecurityNone
	public List<Valg> valg(UserData userData, ValggruppeSti valggruppeSti, CountCategory countCategory) {
		return domainService.valg(valggruppeSti, userData.operatorValggeografiSti(), false, countCategory);
	}

	@Override
	@SecurityNone
	public List<Valgdistrikt> valgdistrikter(UserData userData, ValgSti valgSti) {
		return domainService.valgdistrikter(valgSti, userData.operatorValggeografiSti());
	}

	@Override
	@SecurityNone
	public List<Valgdistrikt> valgdistrikter(UserData userData, ValgSti valgSti, ValggeografiSti valggeografiSti) {
		return domainService.valgdistrikterFiltrertPaaGeografi(valgSti, valggeografiSti);
	}

	private ValghendelseSti valghendelseSti(UserData userData) {
		return userData.operatorValghierarkiSti().valghendelseSti();
	}
}
