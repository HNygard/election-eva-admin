package no.valg.eva.admin.frontend.counting.ctrls;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.EvoteConstants.BALLOT_BLANK;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.REJECTED_BALLOTS_PROCESS;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.common.Button.renderedAndEnabled;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.ValgdistriktOpptellingskategoriOgValggeografiHolder;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

public abstract class BaseApproveRejectedCountController extends ConversationScopedController {
	@Inject
	protected ValgdistriktOpptellingskategoriOgValggeografiHolder holder;
	@Inject
	protected UserData userData;
	@Inject
	protected CountingService countingService;
	@Inject
	protected UserDataController userDataController;
	@Inject
	protected MessageProvider messageProvider;
	
	protected FinalCount finalCount;
	protected List<BallotCount> newBallotCounts;
	protected ReportingUnitTypeId reportingUnitTypeId;
	@Inject
	private PageTitleMetaBuilder pageTitleMetaBuilder;
	@Inject
	private ModifiedBallotBatchService modifiedBallotBatchService;

	@Override
	protected void doInit() {
		initRejectedCount();
	}

	protected void refreshRejectedCount() {
		finalCount = findApprovedFinalCount();
		newBallotCounts = buildNewBallotCounts(finalCount);
		resolveMessagesWhenNoError(finalCount);
	}

	private void initRejectedCount() {
		reportingUnitTypeId = getRequestParameter("reportingUnitType", ReportingUnitTypeId::valueOf);
		if (reportingUnitTypeId != null && reportingUnitTypeId != FYLKESVALGSTYRET) {
			throw new IllegalArgumentException(
					format("expected <reportingUnitType> to be <%s>, but was <%s>", FYLKESVALGSTYRET, reportingUnitTypeId));
		}
		finalCount = findApprovedFinalCount();
		if (resolveErrorStateAndMessages(finalCount)) {
			finalCount = null;
		} else {
			newBallotCounts = buildNewBallotCounts(finalCount);
			resolveMessagesWhenNoError(finalCount);
		}
	}

	private FinalCount findApprovedFinalCount() {
		CountContext countContext = buildCountContext();
		ApprovedFinalCountRef ref = new ApprovedFinalCountRef(reportingUnitTypeId, countContext, holder.getSelectedAreaPath());
		return countingService.findApprovedFinalCount(userData, ref);
	}

	protected abstract boolean resolveErrorStateAndMessages(FinalCount finalCount);

	private List<BallotCount> buildNewBallotCounts(FinalCount finalCount) {
		List<BallotCount> result = new ArrayList<>();
		if (finalCount.isModifiedBallotsProcessed()) {
			for (BallotCount ballotCount : finalCount.getBallotCounts()) {
				result.add(new BallotCount(ballotCount.getId(), ballotCount.getName(), 0, 0));
			}
			result.add(new BallotCount(BALLOT_BLANK, messageProvider.get("@count.label.blancs"), 0, 0));
			return result;
		}
		List<ModifiedBallotsStatus> modifiedBallotStatuses = modifiedBallotBatchService.buildModifiedBallotStatuses(userData, getFinalCount(),
				REJECTED_BALLOTS_PROCESS);
		for (ModifiedBallotsStatus modifiedBallotStatus : modifiedBallotStatuses) {
			String ballotName = findBallotName(finalCount, modifiedBallotStatus.getBallotId());
			int newModifiedCount = modifiedBallotStatus.getRemaining() + modifiedBallotStatus.getInProgress();
			result.add(new BallotCount(modifiedBallotStatus.getBallotId(), ballotName, 0, newModifiedCount));
		}
		result.add(new BallotCount(BALLOT_BLANK, messageProvider.get("@count.label.blancs"), 0, 0));
		return result;
	}

	protected abstract boolean isScanned();

	private String findBallotName(FinalCount finalCount, String ballotId) {
		String ballotName = null;
		for (BallotCount ballotCount : finalCount.getBallotCounts()) {
			if (ballotCount.getId().equals(ballotId)) {
				ballotName = ballotCount.getName();
				break;
			}
		}
		return ballotName;
	}

	private void resolveMessagesWhenNoError(FinalCount finalCount) {
		MessageUtil.clearMessages();
		if (finalCount.isReadyForSettlement()) {
			showMessage("@count.ballot.approve.rejected.toSettlement.done", FacesMessage.SEVERITY_INFO);
		} else if (!finalCount.isModifiedBallotsProcessed()) {
			showMessage("@count.ballot.approve.rejected.registerNumbers", FacesMessage.SEVERITY_INFO);
		} else if (finalCount.isRejectedBallotsProcessed() && finalCount.isApproved() && isReportingUnitOnContestLevel()) {
			showMessage("@count.ballot.approve.rejected.rejectedBallotsProcessed", FacesMessage.SEVERITY_WARN);
		} else if (finalCount.isRejectedBallotsProcessed() && finalCount.isApproved()) {
			showMessage("@count.ballot.approve.rejected.finished", FacesMessage.SEVERITY_INFO);
		}
	}

	private void showMessage(String message, FacesMessage.Severity severity) {
		String translatedMessage = messageProvider.get(message);
		getFacesContext().addMessage(null, new FacesMessage(severity, translatedMessage, translatedMessage));
	}

	public boolean isReferendum() {
		return false;
	}

	protected boolean isReportingUnitOnContestLevel() {
		ReportingUnitTypeId theReportingUnitTypeId = reportingUnitTypeId();
		AreaLevelEnum contestAreaLevel = holder.getSelectedContestInfo().getAreaLevel();
		boolean fylkeAndCounty = theReportingUnitTypeId == FYLKESVALGSTYRET && contestAreaLevel == COUNTY;
		boolean valgstyreAndMunicipalityOrBorough = theReportingUnitTypeId == VALGSTYRET && (contestAreaLevel == MUNICIPALITY || contestAreaLevel == BOROUGH);
		return fylkeAndCounty || valgstyreAndMunicipalityOrBorough;
	}

	public FinalCount getFinalCount() {
		return finalCount;
	}
	
	public void setFinalCount(FinalCount finalCount) {
		this.finalCount = finalCount;
	}

	public List<BallotCount> getNewBallotCounts() {
		return newBallotCounts;
	}

	public String goToRegisterCorrectedBallotsRejectionMode() {
		if (!finalCount.isRejectedBallotsProcessed() && !doRegisterRejectedCounts()) {
			return null;
		}
		return format(addRedirect("/secure/counting/modifiedBallotsStatus.xhtml?category=%s&contestPath=%s&areaPath=%s&%s"),
				holder.getSelectedCountCategory(), holder.getSelectedContestInfo().getElectionPath(), holder.getSelectedAreaPath(), buildFromUrlPart());
	}

	protected CountContext buildCountContext() {
		return new CountContext(holder.getSelectedContestInfo().getElectionPath(), holder.getSelectedCountCategory());
	}

	protected abstract String buildFromUrlPart();

	public String getElectionName() {
		return holder.getSelectedContestInfo().getElectionName();
	}

	public String getMunicipalityName() {
		return holder.getSelectedMvArea().getMunicipalityName();
	}

	private ReportingUnitTypeId reportingUnitTypeId() {
		if (userData.isElectionEventAdminUser() && reportingUnitTypeId != null) {
			return reportingUnitTypeId;
		}
		if (userData.getOperatorAreaLevel() == COUNTY) {
			return FYLKESVALGSTYRET;
		}
		return VALGSTYRET;
	}

	public CountContext getCountContext() {
		return buildCountContext();
	}

	public List<PageTitleMetaModel> getAreaPageTitleMeta() {
		List<PageTitleMetaModel> result = pageTitleMetaBuilder.area(holder.getSelectedMvArea());
		if (getFinalCount() != null) {
			result.add(new PageTitleMetaModel(messageProvider.get("@statistic.column.countStatus"), messageProvider.get(getFinalCount().getStatus().getName())));
		}
		return result;
	}

	public int calculateTotalNewBallotCount() {
		int totalNewBallotCount = 0;
		for (BallotCount newBallotCount : newBallotCounts) {
			totalNewBallotCount += newBallotCount.getCount();
		}
		return totalNewBallotCount;
	}

	public void approveToSettlement() {
		if (!finalCount.isRejectedBallotsProcessed() && !doRegisterRejectedCounts()) {
			return;
		}
		ApprovedFinalCountRef ref = new ApprovedFinalCountRef(reportingUnitTypeId, buildCountContext(), holder.getSelectedAreaPath());
		finalCount = countingService.updateFinalCountStatusToSettlement(userData, ref);
		MessageUtil.clearMessages();
		MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.toSettlement.done"), FacesMessage.SEVERITY_INFO);
	}

	public void registerRejectedCounts() {
		doRegisterRejectedCounts();
	}

	protected boolean doRegisterRejectedCounts() {
		FinalCount originalApprovedFinalCount = findApprovedFinalCount();
		if (!isInputValid(originalApprovedFinalCount)) {
			return false;
		}
		boolean hasNewModifiedBallots = processBallotCounts();
		if (isReportingUnitOnContestLevel() && hasNewModifiedBallots) {
			finalCount.setModifiedBallotsProcessed(false);
		}
		finalCount.setRejectedBallotsProcessed(true);
		finalCount = countingService.processRejectedBallots(userData, buildCountContext(), finalCount);
		if (!isReportingUnitOnContestLevel()) {
			MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.finished"), FacesMessage.SEVERITY_INFO);
		}
		return true;
	}

	private boolean isInputValid(FinalCount originalApprovedFinalCount) {
		int originalTotalRejectedBallotCount = originalApprovedFinalCount.getTotalRejectedBallotCount();
		int totalBallotCountFromInput = calculateTotalBallotCountFromInput();
		if (totalBallotCountFromInput < originalTotalRejectedBallotCount) {
			MessageUtil.buildDetailMessage(
					messageProvider.get("@count.ballot.approve.rejected.validate.toFewBallotsRegistred") + " " + originalTotalRejectedBallotCount,
					FacesMessage.SEVERITY_WARN);
			return false;
		}
		if (totalBallotCountFromInput > originalTotalRejectedBallotCount) {
			MessageUtil.buildDetailMessage(
					messageProvider.get("@count.ballot.approve.rejected.validate.toManyBallotsRegistred") + " " + originalTotalRejectedBallotCount,
					FacesMessage.SEVERITY_WARN);
			return false;
		}
		return true;
	}

	private int calculateTotalBallotCountFromInput() {
		return finalCount.getTotalRejectedBallotCount() + calculateTotalNewBallotCount();
	}

	private boolean processBallotCounts() {
		int newModifiedBallotCounts = 0;
		List<BallotCount> ballotCounts = finalCount.getBallotCounts();
		BallotCount blankBallot = null;
		
		for (BallotCount newBallotCount : newBallotCounts) {
			if (BALLOT_BLANK.equals(newBallotCount.getId())) {
				blankBallot = newBallotCount;
			} else {
				for (BallotCount ballotCount : ballotCounts) {
					if (newBallotCount.getId().equals(ballotCount.getId())) {
						if (newBallotCount.getCount() > 0) {
							ballotCount.setUnmodifiedCount(ballotCount.getUnmodifiedCount() + newBallotCount.getUnmodifiedCount());
							ballotCount.setModifiedCount(ballotCount.getModifiedCount() + newBallotCount.getModifiedCount());
							newModifiedBallotCounts += newBallotCount.getModifiedCount();
						}
						break;
					}
				}
			}
		}
		// Handle blanks
		if (blankBallot != null && blankBallot.getUnmodifiedCount() > 0) {
			finalCount.setBlankBallotCount(finalCount.getBlankBallotCount() + blankBallot.getUnmodifiedCount());
		}
		return newModifiedBallotCounts != 0;
	}

	public abstract boolean isEditMode();

	public Button button(ButtonType type) {
		switch (type) {
		case APPROVE:
			return approveButton();
		case APPROVE_TO_SETTLEMENT:
			return approveToSettlementButton();
		case REGISTER_CORRECTIONS:
			return registerCorrectionsButton();
		default:
			return notRendered();
		}
	}

	private Button approveButton() {
		if (isReportingUnitOnContestLevel()) {
			return notRendered();
		}
		return enabled(!finalCount.isRejectedBallotsProcessed());
	}

	private Button approveToSettlementButton() {
		if (!isReportingUnitOnContestLevel()) {
			return notRendered();
		}
		String name;
		if (finalCount.isRejectedBallotsProcessed()) {
			name = messageProvider.get("@count.ballot.approve.rejected.toSettlement");
		} else {
			name = messageProvider.get("@count.ballot.approve.rejected.ApproveAndToSettlement");
		}
		return enabled(finalCount.getStatus() == APPROVED, name);
	}

	private Button registerCorrectionsButton() {
		if (!isReportingUnitOnContestLevel()) {
			return notRendered();
		}
		return renderedAndEnabled();
	}

	public boolean hasNewModifiedBallotCounts() {
		for (BallotCount newBallotCount : newBallotCounts) {
			if (newBallotCount.getModifiedCount() > 0) {
				return true;
			}
		}
		return false;
	}
}
