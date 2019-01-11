package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;

import no.valg.eva.admin.felles.sti.valggeografi.LandSti;
import no.valg.eva.admin.felles.valggeografi.model.Land;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class LandTabell extends ValggeografiTabell<LandSti, Land> {
	public LandTabell(ValggeografiPanel panel) {
		super(panel, LAND, null);
	}

	@Override
	public void oppdater() {
		initEnRad(getValggeografiService().land(getUserData()));
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterFylkeskommunerTabell();
	}
}
