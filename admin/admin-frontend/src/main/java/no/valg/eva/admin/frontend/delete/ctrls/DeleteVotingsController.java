package no.valg.eva.admin.frontend.delete.ctrls;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class DeleteVotingsController extends KontekstAvhengigController {

	@Inject
	@EjbProxy
	private VotingService votingService;

	private List<String> selectedVotingCategoryPks;
	private List<VotingCategory> votingCategoryList;
	private MvElection mvElection;
	private MvArea mvArea;
	private boolean deleted = false;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(getAreaLevels()));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		votingCategoryList = votingService.findAllVotingCategories(getUserData());
		setSelectedVotingCategoryPks(new ArrayList<>());
		mvElection = getMvElectionService().findSingleByPath(kontekst.valggruppeSti());
		mvArea = getMvAreaService().findSingleByPath(kontekst.getValggeografiSti());
		MessageUtil.buildDetailMessage("@delete.votings.confirmText", new String[] { mvElection.toString(), mvArea.toString() },
				FacesMessage.SEVERITY_INFO);
		MessageUtil.buildDetailMessage("@common.message.choose_category", FacesMessage.SEVERITY_INFO);

	}

	/**
	 * Delete votings for selected mvElection and mvArea
	 */
	public void deleteVotings() {
		execute(() -> {
			if (getSelectedVotingCategoryPks().isEmpty()) {
				votingService.deleteVotings(getUserData(), mvElection, mvArea, null);
				if (mvArea.getAreaLevel() <= AreaLevelEnum.MUNICIPALITY.getLevel()) {
					votingService.deleteSeqVotingNumber(getUserData(), mvElection, mvArea);
				}
			} else {
				for (String s : getSelectedVotingCategoryPks()) {
					votingService.deleteVotings(getUserData(), mvElection, mvArea, Integer.parseInt(s));
				}
			}
			MessageUtil.buildDetailMessage("@delete.votings.confirmation", new String[] { mvElection.toString(), mvArea.toString() },
					FacesMessage.SEVERITY_INFO);
			deleted = true;
		});
	}

	public List<String> getSelectedVotingCategoryPks() {
		return selectedVotingCategoryPks;
	}

	public void setSelectedVotingCategoryPks(final List<String> selectedVotingCategoryPks) {
		this.selectedVotingCategoryPks = selectedVotingCategoryPks;
	}

	public List<VotingCategory> getVotingCategoryList() {
		return votingCategoryList;
	}

	public boolean isDeleted() {
		return deleted;
	}

	private ValggeografiNivaa[] getAreaLevels() {
		List<ValggeografiNivaa> result = new ArrayList<>(asList(BYDEL, STEMMEKRETS, STEMMESTED));
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
