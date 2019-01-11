package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

public class ValggrupperTabell extends ValghierarkiTabell<ValggruppeSti, Valggruppe> {
	public ValggrupperTabell(ValghierarkiPanel panel, ValghierarkiService valghierarkiService, UserData userData) {
		super(panel, VALGGRUPPE, valghierarkiService, userData);
	}

	@Override
	public void oppdater() {
		initFlereRader(getValghierarkiService().valggrupper(getUserData()));
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterValgTabell();
	}
}
