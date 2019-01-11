package no.valg.eva.admin.frontend.delete.ctrls;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class DeleteVotersWithoutMvAreaController extends BaseController {

	// Injected
	private VoterService voterService;
	private UserData userData;

	private boolean deleted;

	public DeleteVotersWithoutMvAreaController() {
		// For CDI
	}

	@Inject
	public DeleteVotersWithoutMvAreaController(VoterService voterService, UserData userData) {
		this.voterService = voterService;
		this.userData = userData;
	}

	/**
	 * Delete voters not attached to an mvArea
	 */
	public void deleteVotersWithoutMvArea() {
		execute(() -> {
			voterService.deleteVotersWithoutMvArea(userData, userData.getElectionEventPk());
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@delete.votersWithoutMvArea.confirmation", null, FacesMessage.SEVERITY_INFO);
			deleted = true;
		});
	}

	public boolean isDeleted() {
		return deleted;
	}
}
