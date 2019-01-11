package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

import org.apache.commons.lang3.ObjectUtils;

public class ValgdistrikterTabell extends ValghierarkiTabell<ValgdistriktSti, Valgdistrikt> {
	private ValgSti valgtValgSti;

	public ValgdistrikterTabell(ValghierarkiPanel panel, ValghierarkiService valghierarkiService, UserData userData) {
		super(panel, VALGDISTRIKT, valghierarkiService, userData);
	}

	@Override
	public void oppdater() {
		ValgTabell valgTabell = getPanel().getValgTabell();
		if (valgTabell == null || valgTabell.isRadValgt()) {
			ValgSti nyValgtValgSti = valgTabell != null ? valgTabell.valgtSti() : null;
			if (ObjectUtils.equals(nyValgtValgSti, valgtValgSti)) {
				return;
			}
			valgtValgSti = nyValgtValgSti;
			initFlereRader(getValghierarkiService().valgdistrikter(getUserData(), valgtValgSti));
		} else {
			initIngenRader();
		}
	}

	@Override
	protected void valgtRadSatt() {
		// gjør ingenting
	}
}
