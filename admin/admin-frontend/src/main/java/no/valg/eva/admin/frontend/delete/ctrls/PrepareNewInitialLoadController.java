package no.valg.eva.admin.frontend.delete.ctrls;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class PrepareNewInitialLoadController extends BaseController {

	// Injected
	private UserData userData;
	private MvElectionService mvElectionService;
	private MvAreaService mvAreaService;
	private VoterService voterService;

	private boolean deleted;

	public PrepareNewInitialLoadController() {
		// For CDI
	}

	@Inject
	public PrepareNewInitialLoadController(UserData userData, MvElectionService mvElectionService, MvAreaService mvAreaService, VoterService voterService) {
		this.userData = userData;
		this.mvElectionService = mvElectionService;
		this.mvAreaService = mvAreaService;
		this.voterService = voterService;
	}

	/**
	 * The method calls deleteVoters with the root as parameter for both mvElection and mvArea. The psql function will therefore also delete the row in
	 * voter_import_batch for this election event. A new initial load can be performed when all voters and the row in voter_import_batch is deleted.
	 */
	public void prepareForNewInitialLoad() {
		execute(() -> {
			MvElection rootMvElection = mvElectionService.findRoot(userData, userData.getElectionEventPk());
			MvArea rootMvArea = mvAreaService.findRoot(userData.getElectionEventPk());
			voterService.prepareNewInitialLoad(userData, rootMvElection, rootMvArea);
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@delete.prepareInitialLoad.confirmation", new String[] { rootMvArea.toString() },
					FacesMessage.SEVERITY_INFO);
			deleted = true;
		});
	}

	public boolean isDeleted() {
		return deleted;
	}
}
