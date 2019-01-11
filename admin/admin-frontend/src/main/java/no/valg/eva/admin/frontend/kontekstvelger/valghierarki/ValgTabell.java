package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

public class ValgTabell extends ValghierarkiTabell<ValgSti, Valg> {
	private final CountCategory valgtCountCategory;

	public ValgTabell(ValghierarkiPanel panel, ValghierarkiService valghierarkiService, UserData userData, CountCategory valgtCountCategory) {
		super(panel, VALG, valghierarkiService, userData);
		this.valgtCountCategory = valgtCountCategory;
	}

	@Override
	public void oppdater() {
		ValggrupperTabell valggrupperTabell = getPanel().getValggrupperTabell();
		if (valggrupperTabell == null || valggrupperTabell.isRadValgt()) {
			ValggruppeSti valgtValggruppeSti = valggrupperTabell != null ? valggrupperTabell.valgtSti() : null;
			initFlereRader(getValghierarkiService().valg(getUserData(), valgtValggruppeSti, valgtCountCategory));
		} else {
			initIngenRader();
		}
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterValgdistrikterTabell();
	}
}
