package no.valg.eva.admin.frontend.listproposal.ctrls;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.PartyService;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.primefaces.event.CloseEvent;

/**
 * PartyController is used in editPartyList.xhtml and for creating new list proposals.
 */
@Named
@ViewScoped
public class PartyController extends BaseController {

	@Inject
	private MessageProvider mms;
	@Inject
	private PartyService partyService;
	@Inject
	private UserData userData;

	private List<Parti> parties;
	private Parti selectedParty;

	@PostConstruct
	protected void doInit() {
		selectedParty = initNewParty();
		parties = new ArrayList<>();
		repopulatePartiesList(parties);
	}

	private Parti initNewParty() {
		return new Parti(Partikategori.LOKALT, "");
	}

	private void repopulatePartiesList(List<Parti> parties) {
		parties.clear();
		List<Parti> loaded = partyService.findAllPartiesButNotBlank(userData, userData.getElectionEventPk());
		parties.addAll(loaded
				.stream()
				.sorted((party1, party2) -> party1.getOversattNavn().compareTo(party2.getOversattNavn()))
				.collect(Collectors.toList()));
	}

	public void createOrUpdateParty() {
		if (isPartyValid(selectedParty)) {
			execute(() -> {
				String msgTxt = "@party.edited";
				if (selectedParty.getPartyPk() == null) {
					selectedParty = partyService.create(userData, selectedParty);
					msgTxt = "@party.created";
				} else {
					selectedParty = partyService.update(userData, selectedParty);
				}
				repopulatePartiesList(parties);
				MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, mms.get(msgTxt, selectedParty.getOversattNavn()));
				FacesUtil.executeJS("PF('newPartyWidget').hide()");
				FacesUtil.updateDom("editPartyForm");
			});

		}
	}

	public void deleteParty(Parti parti) {
		if (isPartyValidForDeletion(parti)) {
			partyService.delete(userData, parti);
			repopulatePartiesList(parties);
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, mms.get("@party.deleted", parti.getOversattNavn()));
		}
	}

	public void dialogHandleClose(CloseEvent event) {
		repopulatePartiesList(parties);
	}

	private boolean isPartyValid(Parti party) {
		return isValidated(partyService.validateParty(userData, party));
	}

	private boolean isPartyValidForDeletion(Parti parti) {
		return isValidated(partyService.validatePartyForDelete(userData, parti));

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

	public List<Parti> getParties() {
		return parties;
	}

	public void setParties(List<Parti> parties) {
		this.parties = parties;
	}

	public Parti getSelectedParty() {
		return selectedParty;
	}

	public void setSelectedParty(Parti party) {
		selectedParty = party;
	}

	public String getSelectedPartyName() {
		return selectedParty.getOversattNavn();
	}

	public void setSelectedPartyName(String selectedPartyName) {
		selectedParty.setOversattNavn(selectedPartyName);
	}

	public void setNewSelectedParty() {
		selectedParty = initNewParty();
	}

	public Partikategori[] getPartyCategories() {
		return Partikategori.values();
	}
}
