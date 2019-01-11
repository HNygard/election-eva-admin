package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.model.modifiedballots.RegisterModifiedBallotCountStatus;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.counting.view.TopInfoProvider;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.REJECTED_BALLOTS_PROCESS;
import static no.valg.eva.admin.frontend.counting.ctrls.ModifiedBallotsStatusController.Referrer.APPROVE_MANUAL_REJECTED_COUNT;
import static no.valg.eva.admin.frontend.counting.ctrls.ModifiedBallotsStatusController.Referrer.APPROVE_SCANNED_REJECTED_COUNT;
import static no.valg.eva.admin.frontend.counting.ctrls.ModifiedBallotsStatusController.Referrer.COUNTING;

@Named
@ConversationScoped
public class ModifiedBallotsStatusController extends ConversationScopedController implements TopInfoProvider {

	private static final int DEFAULT_POLL_INTERVAL_IN_SECONDS = 10;
	private static final int POLL_INTERVAL_ON_ERROR_IN_SECONDS = 60;

	@Inject
	protected CountingService countingService;
	@Inject
	private ModifiedBallotBatchService modifiedBallotBatchService;
	@Inject
	private Instance<StartCountingController> startCountingControllerInstance;
	@Inject
	private Instance<FinalCountController> finalCountControllerInstance;
	@Inject
	private Instance<CountyFinalCountController> countyFinalCountControllerInstance;
	@Inject
	private Instance<ApproveManualRejectedCountController> approveManualRejectedCountControllerInstance;
	@Inject
	private Instance<ApproveScannedRejectedCountController> approveScannedRejectedCountControllerInstance;
	@Inject
	private UserData userData;
	@Inject
	private MessageProvider messageProvider;
	private Referrer referrer;
	private ModifiedBallotBatchProcess process;

	private RegisterModifiedBallotCountStatus registerModifiedBallotCountStatus;
	private Integer pollInterval;

	public void preRender(ComponentSystemEvent event) {
		registerModifiedBallotCountStatus = new RegisterModifiedBallotCountStatus(
				modifiedBallotBatchService.buildModifiedBallotStatuses(userData, getFinalCount(), process));
		if (!registerModifiedBallotCountStatus.isHasNoUnfinishedBatches()) {
			FacesMessage message = new FacesMessage(messageProvider.get("@modified.ballots.must.complete.batch"));
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			getFacesContext().addMessage(null, message);
		}
	}

	@Override
	protected void doInit() {
		if (getBooleanRequestParameter("fromApproveManualRejectedCount", false)) {
			referrer = APPROVE_MANUAL_REJECTED_COUNT;
			process = REJECTED_BALLOTS_PROCESS;
		} else if (getBooleanRequestParameter("fromApproveScannedRejectedCount", false)) {
			referrer = APPROVE_SCANNED_REJECTED_COUNT;
			process = REJECTED_BALLOTS_PROCESS;
		} else {
			referrer = COUNTING;
			process = MODIFIED_BALLOTS_PROCESS;
		}
		pollInterval = DEFAULT_POLL_INTERVAL_IN_SECONDS;
	}

	private boolean getBooleanRequestParameter(String requestParameterName, boolean defaultValue) {
		String value = getRequestParameter(requestParameterName);
		if (value != null) {
			return Boolean.valueOf(value);
		}
		return defaultValue;
	}

	public RegisterModifiedBallotCountStatus getRegisterModifiedBallotCountStatus() {
		return registerModifiedBallotCountStatus;
	}

	public String registrationCompleted() {
		RegisterModifiedBallotCountStatus theRegisterModifiedBallotCountStatus = getRegisterModifiedBallotCountStatus();
		if (!theRegisterModifiedBallotCountStatus.isRegistrationOfAllModifiedBallotsCompleted()) {
			throw new IllegalStateException("Registration of modified ballots is not completed!");
		}
		FinalCount activeFinalCount = getFinalCount();
		if (!activeFinalCount.isModifiedBallotsProcessed()) {
			activeFinalCount.setModifiedBallotsProcessed(true);
			if (!execute(() -> {
				setFinalCount(countingService.saveCount(userData, getContext(), activeFinalCount));
			})) {
				pollInterval = POLL_INTERVAL_ON_ERROR_IN_SECONDS;
				return null;
			}
		}
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			BaseApproveRejectedCountController ctrl = resolveApproveRejectedCountController();
			ctrl.refreshRejectedCount();
		}
		return addRedirect(referrer.getPath());
	}

	private BaseApproveRejectedCountController resolveApproveRejectedCountController() {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT) {
			return approveManualRejectedCountControllerInstance.get();
		}
		return approveScannedRejectedCountControllerInstance.get();
	}

	public FinalCount getFinalCount() {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			return resolveApproveRejectedCountController().getFinalCount();
		}
		return getBaseFinalCountController().getFinalCount();
	}

	private void setFinalCount(FinalCount finalCount) {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			resolveApproveRejectedCountController().setFinalCount(finalCount);
			return;
		}
		BaseFinalCountController controller = getBaseFinalCountController();
		controller.updateCounts(controller.getFinalCountIndex(), finalCount);
	}

	private BaseFinalCountController getBaseFinalCountController() {
		if (userData.isCountyLevelUser() || userData.isSamiElectionCountyUser()) {
			return countyFinalCountControllerInstance.get();
		} else {
			return finalCountControllerInstance.get();
		}
	}

	public CountContext getContext() {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			return resolveApproveRejectedCountController().getCountContext();
		}
		return startCountingControllerInstance.get().getCountContext();
	}

	@Override
	public String getDisplayAreaName() {
		FinalCount finalCount = getFinalCount();
		if (finalCount.getAreaPath().isMunicipalityPollingDistrict()) {
			return messageProvider.get("@area.polling_district.municipality");
		} else {
			return (finalCount.getAreaPath().isPollingDistrictLevel() ? finalCount.getAreaPath().getPollingDistrictId()
					: finalCount.getAreaPath().getBoroughId())
					+ " " + finalCount.getAreaName();
		}
	}

	@Override
	public String getMunicipalityName() {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			return resolveApproveRejectedCountController().getMunicipalityName();
		}
		return startCountingControllerInstance.get().getCounts().getMunicipalityName();
	}

	@Override
	public String getElectionName() {
		if (referrer == APPROVE_MANUAL_REJECTED_COUNT || referrer == APPROVE_SCANNED_REJECTED_COUNT) {
			return resolveApproveRejectedCountController().getElectionName();
		}
		return startCountingControllerInstance.get().getCounts().getElectionName();
	}

	@Override
	public String getCategoryName() {
		return getContext().getCategoryMessageProperty();
	}

	public boolean showCreateBatchLink(ModifiedBallotsStatus status) {
		return status.isCanCreateNewBatch() && status.getBallotCountStatus().isHasNoUnfinishedBatches();
	}

	public boolean showGotoReviewLink(ModifiedBallotsStatus status) {
		return status.hasModifiedBallotsAndRegistrationIsDone() && status.getBallotCountStatus().isHasNoUnfinishedBatches();
	}

	public String getBreadCrumbText() {
		switch (referrer) {
		case APPROVE_MANUAL_REJECTED_COUNT:
			return messageProvider.get("@menu.counting.approve_rejected.manual");
		case APPROVE_SCANNED_REJECTED_COUNT:
			return messageProvider.get("@menu.counting.approve_rejected.scan");
		case COUNTING:
			return messageProvider.get("@count.tab.type[E]");
		default:
			return "NA";
		}
	}

	public String getBreadCrumbAction() {
		if (referrer != APPROVE_MANUAL_REJECTED_COUNT && referrer != APPROVE_SCANNED_REJECTED_COUNT) {
			return referrer.getPath() + "?cid=" + getCid();
		}
		CountContext context = getContext();
		String categoryId = context.getCategory().getId();
		ElectionPath contestPath = context.getContestPath();
		AreaPath areaPath = getFinalCount().getAreaPath();
		return referrer.getPath() + "?category=" + categoryId + "&contestPath=" + contestPath + "&areaPath=" + areaPath;
	}

	public boolean doRenderCountingBreadCrumb() {
		return referrer == COUNTING;
	}

	public ModifiedBallotBatchProcess getProcess() {
		return process;
	}

	public Integer getPollInterval() {
		return pollInterval;
	}

	enum Referrer {
		APPROVE_MANUAL_REJECTED_COUNT("/secure/counting/approveManualRejectedCount.xhtml"), APPROVE_SCANNED_REJECTED_COUNT(
				"/secure/counting/approveScannedRejectedCount.xhtml"), COUNTING("/secure/counting/counting.xhtml");

		private String path;

		Referrer(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}
}
