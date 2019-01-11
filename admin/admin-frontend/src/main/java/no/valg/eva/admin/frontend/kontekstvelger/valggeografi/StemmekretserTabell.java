package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class StemmekretserTabell extends ValggeografiTabell<StemmekretsSti, Stemmekrets> {
	private final ValghierarkiSti valghierarkiSti;

	public StemmekretserTabell(ValggeografiPanel panel, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		super(panel, STEMMEKRETS, countCategory);
		this.valghierarkiSti = valghierarkiSti;
	}

	@Override
	public void oppdater() {
		BydelerTabell bydelerTabell = getPanel().getBydelerTabell();
		if (bydelerTabell == null || bydelerTabell.isRadValgt()) {
			BydelSti valgtBydelSti = bydelerTabell != null ? bydelerTabell.valgtSti() : null;
			initFlereRader(stemmekretser(valgtBydelSti));
		} else {
			initIngenRader();
		}
	}

	private List<Stemmekrets> stemmekretser(BydelSti valgtBydelSti) {
		UserData userData = getUserData();
		return getValggeografiService().stemmekretser(userData, valgtBydelSti, valghierarkiSti, countCategory());
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterStemmestederTabell();
	}
}
