package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

public class ValghendelseTabell extends ValghierarkiTabell<ValghendelseSti, Valghendelse> {
	public ValghendelseTabell(ValghierarkiPanel panel, ValghierarkiService valghierarkiService, UserData userData) {
		super(panel, VALGHENDELSE, valghierarkiService, userData);
	}

	@Override
	public void oppdater() {
		initEnRad(getValghierarkiService().valghendelse(getUserData()));
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterValggrupperTabell();
	}
}
