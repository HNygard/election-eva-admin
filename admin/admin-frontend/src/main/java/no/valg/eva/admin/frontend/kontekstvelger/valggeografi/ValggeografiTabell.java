package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad.kontekstvelgerRader;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.FORHAND_ORDINAERE;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.VALGTING_ORDINAERE;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.filter.ForhandOrdinaereFilter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.filter.ValgtingOrdinaereFilter;

public abstract class ValggeografiTabell<S extends ValggeografiSti, V extends Valggeografi<S>>
		extends KontekstvelgerTabell<ValggeografiPanel, ValggeografiRad<S>, ValggeografiNivaa> {
	private final ValggeografiNivaa nivaa;
	private ValggeografiFilter filter;
	private CountCategory countCategory;

	public ValggeografiTabell(ValggeografiPanel panel, ValggeografiNivaa nivaa, CountCategory countCategory) {
		super(panel, true);
		this.nivaa = nivaa;
		this.countCategory = countCategory;
	}

	@Override
	public ValggeografiNivaa getId() {
		return nivaa;
	}

	@Override
	public String getNavn() {
		return nivaa.visningsnavn();
	}

	@Override
	public boolean isVisKnapp() {
		return getPanel().visKnapp(nivaa);
	}

	public S valgtSti() {
		ValggeografiRad<S> valgtRad = getValgtRad();
		if (valgtRad != null) {
			return valgtRad.getSti();
		}
		return null;
	}

	public void setFilter(ValggeografiFilter filter) {
		this.filter = filter;
	}

	ValggeografiService getValggeografiService() {
		return getPanel().getValggeografiService();
	}

	UserData getUserData() {
		return getPanel().getUserData();
	}

	CountCategory countCategory() {
		return countCategory;
	}

	void initIngenRader() {
		setRader(emptyList());
	}

	void initEnRad(V valggeografi) {
		setRader(singletonList(new ValggeografiRad<>(valggeografi)));
	}

	void initFlereRader(List<V> valggeografiListe) {
		if (filter != null) {
			if (filter == FORHAND_ORDINAERE) {
				valggeografiListe = new ForhandOrdinaereFilter<S, V>().filter(valggeografiListe);
			} else if (filter == VALGTING_ORDINAERE) {
				valggeografiListe = new ValgtingOrdinaereFilter<S, V>().filter(valggeografiListe);
			}
		}
		setRader(kontekstvelgerRader(valggeografiListe, ValggeografiRad::new));
	}
}
