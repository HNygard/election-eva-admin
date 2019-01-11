package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.voting.domain.model.Voting;

@Named
@ViewScoped
public class RejectedVotingsReportController extends KontekstAvhengigController {

	@Inject
	@EjbProxy
	private transient VotingService votingService;

	private ValggruppeSti valggruppeSti;
	private KommuneSti kommuneSti;
	private List<Voting> rejectedVotingsList;
	private Voting selectedRejectedVotings;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(KOMMUNE));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		valggruppeSti = kontekst.valggruppeSti();
		kommuneSti = kontekst.kommuneSti();
		rejectedVotingsList = votingService.getRejectedVotingsByElectionGroupAndMunicipality(getUserData(), valggruppeSti, kommuneSti);
		if (!rejectedVotingsList.isEmpty()) {
			selectedRejectedVotings = rejectedVotingsList.get(0);
		}
	}

	public void removeRejectionForVotings() {
		if (selectedRejectedVotings == null) {
			showMessage(getMessageProvider().get(MessageUtil.FIELD_IS_REQUIRED, getMessageProvider().get("@common.person")), FacesMessage.SEVERITY_ERROR);
		} else {
			selectedRejectedVotings.setVotingRejection(null);
			selectedRejectedVotings.setValidationTimestamp(null);
			votingService.update(getUserData(), selectedRejectedVotings);
			showMessage(getMessageProvider().get("@voting.approveBallot.undoRejectionResponse", selectedRejectedVotings.getVoter().getNameLine()),
					FacesMessage.SEVERITY_INFO);
			rejectedVotingsList = votingService.getRejectedVotingsByElectionGroupAndMunicipality(getUserData(), valggruppeSti, kommuneSti);
		}
	}

	public List<Voting> getRejectedVotingsList() {
		return rejectedVotingsList;
	}

	public void setRejectedVotingsList(List<Voting> rejectedVotingsList) {
		this.rejectedVotingsList = rejectedVotingsList;
	}

	public Voting getSelectedRejectedVotings() {
		return selectedRejectedVotings;
	}

	public void setSelectedRejectedVotings(Voting selectedRejectedVotings) {
		this.selectedRejectedVotings = selectedRejectedVotings;
	}

	void showMessage(final String message, final FacesMessage.Severity severityInfo) {
		getFacesContext().addMessage("form:", new FacesMessage(severityInfo, message, message));
	}

}
