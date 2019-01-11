package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;

import no.valg.eva.admin.felles.sti.valggeografi.RodeSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.valggeografi.model.Rode;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class RoderTabell extends ValggeografiTabell<RodeSti, Rode> {
	public RoderTabell(ValggeografiPanel panel) {
		super(panel, RODE, null);
	}

	@Override
	public void oppdater() {
		StemmestederTabell stemmestederTabell = getPanel().getStemmestederTabell();
		if (stemmestederTabell == null || stemmestederTabell.isRadValgt()) {
			StemmestedSti valgtStemmestedSti = stemmestederTabell != null ? stemmestederTabell.valgtSti() : null;
			initFlereRader(getValggeografiService().roder(valgtStemmestedSti));
		} else {
			initIngenRader();
		}
	}

	@Override
	protected void valgtRadSatt() {
		// gjør ingenting
	}
}
