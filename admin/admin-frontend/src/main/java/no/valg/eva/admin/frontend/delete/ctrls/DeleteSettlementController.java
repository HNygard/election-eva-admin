package no.valg.eva.admin.frontend.delete.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.SLETT_VALGOPPGJOER;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class DeleteSettlementController extends KontekstAvhengigController {

	// Injected
	private SettlementService settlementService;

	private MvElection mvElection;
	private MvArea mvArea;
	private boolean deleted;

	@SuppressWarnings("unused")
	public DeleteSettlementController() {
		// CDI
	}

	@Inject
	public DeleteSettlementController(SettlementService settlementService) {
		this.settlementService = settlementService;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE, VALG, VALGDISTRIKT).medTjeneste(SLETT_VALGOPPGJOER));
		setup.leggTil(geografi(valggeografiNivaaer()).medFilter(HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		mvElection = getMvElectionService().findSingleByPath(kontekst.getValghierarkiSti());
		mvArea = getMvAreaService().findSingleByPath(kontekst.getValggeografiSti());
		MessageUtil.buildDetailMessage("@delete.settlement.confirmText", new String[] { mvElection.toString(), mvArea.toString() },
				FacesMessage.SEVERITY_INFO);
	}

	/**
	 * Delete settlement for selected mvElection and mvArea
	 */
	public void deleteSettlement() {
		execute(() -> {
			settlementService.deleteSettlements(getUserData(), mvElection.electionPath(), mvArea.areaPath());
			MessageUtil.buildDetailMessage("@delete.settlement.confirmation", new String[] { mvElection.toString(), mvArea.toString() },
					FacesMessage.SEVERITY_INFO);
			deleted = true;
		});
	}

	public boolean isDeleted() {
		return deleted;
	}

	private ValggeografiNivaa[] valggeografiNivaaer() {
		List<ValggeografiNivaa> result = new ArrayList<>();
		result.add(BYDEL);
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
