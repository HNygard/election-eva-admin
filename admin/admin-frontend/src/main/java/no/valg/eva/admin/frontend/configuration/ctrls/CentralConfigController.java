package no.valg.eva.admin.frontend.configuration.ctrls;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.common.configuration.model.central.CentralConfigurationSummary;
import no.valg.eva.admin.common.configuration.service.CentralConfigurationService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

/**
 * Used to retrieve all required data for the central configuration view.
 */
@Named
@ViewScoped
public class CentralConfigController extends BaseController {
	// Injected
	private UserDataController userDataController;
	private CentralConfigurationService centralConfigurationService;

	private CentralConfigurationSummary summary;

	public CentralConfigController() {
	}

	@Inject
	public CentralConfigController(UserDataController userDataController, CentralConfigurationService centralConfigurationService) {
		this.userDataController = userDataController;
		this.centralConfigurationService = centralConfigurationService;
	}

	@PostConstruct
	public void init() {
		summary = centralConfigurationService.getCentralConfigurationSummary(userDataController.getUserData());
	}

	public String getConfigurationStatus() {
		return userDataController.getElectionEvent().getElectionEventStatus().getName();
	}

	public boolean hasLocalConfiguration() {
		return (userDataController.getElectionEvent().getElectionEventStatus().getId() >= ElectionEventStatusEnum.LOCAL_CONFIGURATION.id());
	}

	public CentralConfigurationSummary getSummary() {
		return summary;
	}

}
