package no.valg.eva.admin.frontend.listproposal.ctrls;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.PartyService;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.common.configuration.service.PartiService;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;

@Named
@ViewScoped
public class OpprettListeforslagController extends ListeforslagBaseController {

	@Inject
	private PartiService partiService;
	@Inject
	private PartyService partyService;

	private int tabActiveIndex = 0;
	private String selectedPartyId;
	private List<Parti> partiesWithoutAffiliations;
	private Parti newParty = new Parti(Partikategori.LOKALT, "");

	public void newListProposal() {
		if (isEmpty(selectedPartyId)) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@common.message.required", getMessageProvider().get("@listProposal.party.choose")),
					FacesMessage.SEVERITY_ERROR);
			return;
		}
		Affiliation newAffiliation = getAffiliationService().createNewAffiliation(getUserData(), getContest(),
				findSelectedParty(), getUserData().getLocale(), BallotStatus.BallotStatusValue.PENDING.getId());
		if (newAffiliation == null) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@common.message.required", getMessageProvider().get("@listProposal.party.choose")),
					FacesMessage.SEVERITY_ERROR);
		} else {
			redirectTilRediger(newAffiliation, "createListProposal.xhtml");
		}
	}

	/**
	 * Creates a new listProposal which is affiliated with a new created party
	 */
	public void createPartyAffiliation() {
		if (isPartyValid(newParty)) {
			Affiliation newAffiliation = getAffiliationService().createNewPartyAndAffiliation(getUserData(), getContest(),
					newParty, getUserData().getLocale());
			redirectTilRediger(newAffiliation, "createListProposal.xhtml");
		}
	}

	public List<Parti> getPartiesWithoutAffiliations() {
		if (partiesWithoutAffiliations == null && getContest() != null) {
			partiesWithoutAffiliations = partiService.partierUtenListeforslag(getUserData(), getContest().electionPath());
		}
		return partiesWithoutAffiliations;
	}

	private boolean isPartyValid(Parti party) {
		boolean valid = true;
		if (isEmpty(newParty.getOversattNavn())) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@common.message.required", getMessageProvider().get("@party.name")),
					FacesMessage.SEVERITY_ERROR);
			valid = false;
		}
		if (isEmpty(newParty.getId())) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@common.message.required", getMessageProvider().get("@party.id")),
					FacesMessage.SEVERITY_ERROR);
			valid = false;
		}
		return valid && isValidated(partyService.validateParty(getUserData(), party));
	}

	private boolean isValidated(List<String> validationFeedbackList) {
		boolean isListValid = validationFeedbackList.isEmpty();
		if (isListValid) {
			return true;
		}
		for (String validationFeedback : validationFeedbackList) {
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, validationFeedback);
		}
		return false;
	}

	private Parti findSelectedParty() {
		Optional<Parti> partiOptional = getPartiesWithoutAffiliations().stream().filter(parti -> parti.getId().equals(selectedPartyId))
				.findFirst();
		return partiOptional.isPresent() ? partiOptional.get() : null;
	}

	public int getTabActiveIndex() {
		return tabActiveIndex;
	}

	public void setTabActiveIndex(int tabActiveIndex) {
		this.tabActiveIndex = tabActiveIndex;
	}

	public String getSelectedPartyId() {
		return selectedPartyId;
	}

	public void setSelectedPartyId(String selectedPartyId) {
		this.selectedPartyId = selectedPartyId;
	}

	public Parti getNewParty() {
		return newParty;
	}

	public void setNewParty(Parti newParty) {
		this.newParty = newParty;
	}
}
