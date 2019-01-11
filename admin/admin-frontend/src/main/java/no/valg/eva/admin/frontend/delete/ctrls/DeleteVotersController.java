package no.valg.eva.admin.frontend.delete.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class DeleteVotersController extends KontekstAvhengigController {

	// Injected
	private VoterService voterService;

	private boolean deleted;
	private MvArea mvArea;

	@SuppressWarnings("unused")
	public DeleteVotersController() {
		// For CDI
	}

	@Inject
	public DeleteVotersController(VoterService voterService) {
		this.voterService = voterService;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(geografi(valggeografiNivaaer()));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		mvArea = getMvAreaService().findSingleByPath(kontekst.getValggeografiSti());
	}

	/**
	 * Delete voters for selected mvArea
	 */
	public void deleteVoters() {
		execute(() -> {
			MvElection rootMvElection = getMvElectionService().findRoot(getUserData(), getUserData().getElectionEventPk());
			voterService.deleteVoters(getUserData(), rootMvElection, mvArea);
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@delete.voters.confirmation", new String[] { mvArea.toString() },
					FacesMessage.SEVERITY_INFO);
			deleted = true;
		});
	}

	public MvArea getMvArea() {
		return mvArea;
	}

	public boolean isDeleted() {
		return deleted;
	}

	private ValggeografiNivaa[] valggeografiNivaaer() {
		List<ValggeografiNivaa> result = new ArrayList<>();
		result.add(BYDEL);
		result.add(STEMMEKRETS);
		int userLevel = getUserData().getOperatorAreaLevel().getLevel();
		if (userLevel <= KOMMUNE.nivaa()) {
			result.add(0, KOMMUNE);
		}
		if (userLevel <= FYLKESKOMMUNE.nivaa()) {
			result.add(0, FYLKESKOMMUNE);
		}
		if (userLevel <= LAND.nivaa()) {
			result.add(0, LAND);
		}
		return result.toArray(new ValggeografiNivaa[result.size()]);
	}
}
