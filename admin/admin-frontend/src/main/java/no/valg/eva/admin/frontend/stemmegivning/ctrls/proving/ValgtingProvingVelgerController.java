package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import static no.valg.eva.admin.common.counting.model.CountCategory.VS;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;

/**
 * Controller for prøving av valgting for velger.
 */
@Named
@ViewScoped
public class ValgtingProvingVelgerController extends VotingConfirmationVoterController {

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		manntallsSokWidget.setStemmekategori(VS.getId());
	}
}
