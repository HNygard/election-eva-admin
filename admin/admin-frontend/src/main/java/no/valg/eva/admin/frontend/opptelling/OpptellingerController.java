package no.valg.eva.admin.frontend.opptelling;

import static java.lang.String.format;

import javax.inject.Inject;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;

public abstract class OpptellingerController extends KontekstAvhengigController {
	@Inject
	private ContestInfoService contestInfoService;

	@Override
	public void initialized(Kontekst kontekst) {
		CountCategory countCategory = kontekst.getCountCategory();
		ValgSti valgSti = kontekst.getValghierarkiSti().tilValgSti();
		ValggeografiSti valggeografiSti = kontekst.getValggeografiSti();
		ElectionPath contestPath = contestInfoService.findContestPathByElectionAndArea(getUserData(), valgSti.electionPath(), valggeografiSti.areaPath());
		redirectTo(format(url(), countCategory, contestPath, valggeografiSti));
	}

	protected abstract String url();

	public String getSideTittel() {
		return "dummy";
	}
}
