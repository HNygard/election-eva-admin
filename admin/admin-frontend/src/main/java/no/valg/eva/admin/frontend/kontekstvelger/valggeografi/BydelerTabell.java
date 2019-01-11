package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class BydelerTabell extends ValggeografiTabell<BydelSti, Bydel> {
	private final ValghierarkiSti valghierarkiSti;

	public BydelerTabell(ValggeografiPanel panel, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		super(panel, BYDEL, countCategory);
		this.valghierarkiSti = valghierarkiSti;
	}

	@Override
	public void oppdater() {
		KommunerTabell kommunerTabell = getPanel().getKommunerTabell();
		if (kommunerTabell == null || kommunerTabell.isRadValgt()) {
			KommuneSti valgtKommuneSti = kommunerTabell != null ? kommunerTabell.valgtSti() : null;
			initFlereRader(bydeler(valgtKommuneSti));
		} else {
			initIngenRader();
		}
	}

	private List<Bydel> bydeler(KommuneSti valgtKommuneSti) {
		UserData userData = getUserData();
		return getValggeografiService().bydeler(userData, valgtKommuneSti, valghierarkiSti, countCategory());
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterStemmekretserTabell();
	}
}
