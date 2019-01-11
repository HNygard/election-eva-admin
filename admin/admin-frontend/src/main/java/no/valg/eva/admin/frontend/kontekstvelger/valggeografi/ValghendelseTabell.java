package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;

import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class ValghendelseTabell extends ValggeografiTabell<ValghendelseSti, Valghendelse> {
	public ValghendelseTabell(ValggeografiPanel panel) {
		super(panel, VALGHENDELSE, null);
	}

	@Override
	public void oppdater() {
		initEnRad(getValggeografiService().valghendelse(getUserData()));
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterLandTabell();
	}
}
