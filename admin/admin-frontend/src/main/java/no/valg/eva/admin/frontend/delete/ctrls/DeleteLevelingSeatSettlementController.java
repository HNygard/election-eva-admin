package no.valg.eva.admin.frontend.delete.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class DeleteLevelingSeatSettlementController extends BaseController {

	// Injected
	private UserData userData;
	private LevelingSeatSettlementService levelingSeatSettlementService;

	private boolean deleted;

	public DeleteLevelingSeatSettlementController() {
		// CDI
	}

	@Inject
	public DeleteLevelingSeatSettlementController(UserData userData, LevelingSeatSettlementService levelingSeatSettlementService) {
		this.userData = userData;
		this.levelingSeatSettlementService = levelingSeatSettlementService;
	}

	@PostConstruct
	public void init() {
		MessageUtil.buildDetailMessage("@delete.levelingSeatSettlement.confirmText", SEVERITY_INFO);
	}

	public void deleteLevelingSeatSettlement() {
		execute(() -> {
			levelingSeatSettlementService.deleteLevelingSeatSettlement(userData);
			deleted = true;
			MessageUtil.buildDetailMessage("@delete.levelingSeatSettlement.confirmation", SEVERITY_INFO);
		});
	}

	public boolean isDeleted() {
		return deleted;
	}
}
