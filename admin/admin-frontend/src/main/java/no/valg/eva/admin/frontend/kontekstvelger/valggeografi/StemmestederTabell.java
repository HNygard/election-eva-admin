package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;

import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class StemmestederTabell extends ValggeografiTabell<StemmestedSti, Stemmested> {
	public StemmestederTabell(ValggeografiPanel panel) {
		super(panel, STEMMESTED, null);
	}

	@Override
	public void oppdater() {
		StemmekretserTabell stemmekretserTabell = getPanel().getStemmekretserTabell();
		if (stemmekretserTabell == null || stemmekretserTabell.isRadValgt()) {
			StemmekretsSti valgtStemmekretsSti = stemmekretserTabell != null ? stemmekretserTabell.valgtSti() : null;
			initFlereRader(getValggeografiService().stemmesteder(getUserData(), valgtStemmekretsSti));
		} else {
			initIngenRader();
		}
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterRoderTabell();
	}
}
