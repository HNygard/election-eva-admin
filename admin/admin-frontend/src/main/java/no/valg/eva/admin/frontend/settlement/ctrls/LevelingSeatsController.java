package no.valg.eva.admin.frontend.settlement.ctrls;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.settlement.model.LevelingSeat;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary;
import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ViewScoped
public class LevelingSeatsController extends BaseController {

	// Injects
	private UserData userData;
	private MessageProvider messageProvider;
	private LevelingSeatSettlementService levelingSeatSettlementService;

	private LevelingSeatSettlementSummary summary;

	public LevelingSeatsController() {
	}

	@Inject
	public LevelingSeatsController(UserData userData, MessageProvider messageProvider, LevelingSeatSettlementService levelingSeatSettlementService) {
		this.userData = userData;
		this.messageProvider = messageProvider;
		this.levelingSeatSettlementService = levelingSeatSettlementService;
	}

	@PostConstruct
	public void init() {
		summary = levelingSeatSettlementService.levelingSeatSettlementSummary(userData);
	}

	public void distributeLevelingSeats() {
		execute(() -> {
			summary = levelingSeatSettlementService.distributeLevelingSeats(userData);
			MessageUtil.buildDetailMessage(messageProvider.get("@leveling_seats.performed"), FacesMessage.SEVERITY_INFO);
		});
	}

	public boolean isStatusReady() {
		return summary.getStatus() == LevelingSeatSettlementSummary.Status.READY;
	}

	public boolean isStatusDone() {
		return summary.getStatus() == LevelingSeatSettlementSummary.Status.DONE;
	}

	public List<LevelingSeat> getLevelingSeats() {
		return summary.getLevelingSeats();
	}
}
