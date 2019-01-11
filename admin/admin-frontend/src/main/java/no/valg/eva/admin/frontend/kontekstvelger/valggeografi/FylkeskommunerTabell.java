package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public class FylkeskommunerTabell extends ValggeografiTabell<FylkeskommuneSti, Fylkeskommune> {
	private final ValghierarkiSti valghierarkiSti;

	public FylkeskommunerTabell(ValggeografiPanel panel, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		super(panel, FYLKESKOMMUNE, countCategory);
		this.valghierarkiSti = valghierarkiSti;
	}

	@Override
	public void oppdater() {
		initFlereRader(fylkeskommuner());
	}

	private List<Fylkeskommune> fylkeskommuner() {
		UserData userData = getUserData();
		return getValggeografiService().fylkeskommuner(userData, valghierarkiSti, countCategory());
	}

	@Override
	protected void valgtRadSatt() {
		getPanel().oppdaterKommunerTabell();
	}
}
