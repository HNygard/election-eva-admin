package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class KommunerTabell extends ValggeografiTabell<KommuneSti, Kommune> {
	private final ValghierarkiSti valghierarkiSti;

	public KommunerTabell(ValggeografiPanel panel, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		super(panel, KOMMUNE, countCategory);
		this.valghierarkiSti = valghierarkiSti;
	}

	@Override
	public void oppdater() {
		FylkeskommunerTabell fylkeskommunerTabell = getPanel().getFylkeskommunerTabell();
		if (fylkeskommunerTabell == null || fylkeskommunerTabell.isRadValgt()) {
			FylkeskommuneSti valgtFylkeskommuneSti = fylkeskommunerTabell != null ? fylkeskommunerTabell.valgtSti() : null;
			initFlereRader(kommuner(valgtFylkeskommuneSti));
		} else {
			initIngenRader();
		}
	}

	private List<Kommune> kommuner(FylkeskommuneSti valgtFylkeskommuneSti) {
		UserData userData = getUserData();
		return getValggeografiService().kommuner(userData, valgtFylkeskommuneSti, valghierarkiSti, countCategory());
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterBydelerTabell();
	}
}
