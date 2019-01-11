package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Se;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;

public abstract class BaseFinalCountController extends CountController {
	protected static final String COUNTING_PATH = "/secure/counting/";

	@Inject
	private ModifiedBallotBatchService modifiedBallotBatchService;

	private boolean hasModifiedBallotBatchForCurrentCount;

	abstract List<FinalCount> getFinalCounts();

	abstract int getFinalCountIndex();

	abstract void updateCounts(int index, FinalCount finalCount);

	@Override
	public boolean isCommentRequired() {
		return false; // NOT IN USE
	}

	@Override
	public void initCountController() {
		updateHasModifiedBallotBatchForCurrentCount();
		if (isUserOnCountyLevel() && this instanceof FinalCountController && !isApproved()) {
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, "Kommunen har ikke godkjent endelig telling!");
		}
	}

	@Override
	public void saveCount() {
		saveCount(true);
	}

	public void saveCount(boolean message) {
		try {
			FinalCount count = getFinalCount();
			count.validate();
			count = getCountingService().saveCount(userData, getCountContext(), count);
			updateCounts(getFinalCountIndex(), count);
			if (message) {
				MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isSaved"));
			}
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	@Override
	public void modifiedBallotProcessed() {
		saveCount();
		FinalCount finalCount = getFinalCount();
		finalCount.setModifiedBallotsProcessed(true);
		finalCount = countingService.saveCount(userData, getContext(), finalCount);
		updateCounts(getFinalCountIndex(), finalCount);
	}

	public String saveCountAndRegisterCountCorrections() {
		// Lagrer først telling da stikke-prosessen er avhengig av at tellingen faktisk er lagra i databasen
		saveCount(false);
		return showCountCorrections();
	}

	public String showCountCorrections() {
		return addRedirect(COUNTING_PATH + "modifiedBallotsStatus.xhtml");
	}

	@Override
	public Count getCount() {
		if (getFinalCounts() == null || getFinalCounts().isEmpty()) {
			return null;
		}
		return getFinalCounts().get(getFinalCountIndex());
	}

	public FinalCount getFinalCount() {
		return (FinalCount) getCount();
	}

	public boolean isReferendum() {
		return getCount() != null;
	}

	public void newFinalCount() {
		int newIndex = getFinalCounts().size();
		String id = "E" + getContext().getCategory().getId() + (newIndex + 1);
		FinalCount newFinalCount = createNewManualFinalCount();
		newFinalCount.setId(id);
		updateCounts(newIndex, newFinalCount);
	}

	public FinalCount createNewManualFinalCount() {
		FinalCount originalFinalCount = getFinalCounts().get(0);
		FinalCount newFinalCount = new FinalCount(
				null,
				originalFinalCount.getAreaPath(),
				originalFinalCount.getCategory(),
				originalFinalCount.getAreaName(),
				originalFinalCount.getReportingUnitTypeId(),
				originalFinalCount.getReportingUnitAreaName(), true);
		List<BallotCount> newBallotCounts = new ArrayList<>();
		for (BallotCount ballotCount : originalFinalCount.getBallotCounts()) {
			BallotCount newBallotCount = new BallotCount(ballotCount.getId(), ballotCount.getName(), 0, 0);
			newBallotCounts.add(newBallotCount);
		}
		newFinalCount.setBallotCounts(newBallotCounts);
		List<RejectedBallotCount> newRejectedBallotCounts = new ArrayList<>();
		for (RejectedBallotCount rejectedBallotCount : originalFinalCount.getRejectedBallotCounts()) {
			RejectedBallotCount newRejectedBallotCount = new RejectedBallotCount(rejectedBallotCount.getId(), rejectedBallotCount.getName(), 0);
			newRejectedBallotCounts.add(newRejectedBallotCount);
		}
		newFinalCount.setRejectedBallotCounts(newRejectedBallotCounts);
		return newFinalCount;
	}

	@Override
	public Button button(ButtonType type) {
		FinalCount count = getFinalCount();
		switch (type) {
		case REVOKE:
			return notRendered();
		case APPROVE:
			return notRendered();
		case REGISTER_CORRECTIONS:
			if (!count.isManualCount() || !hasOpprellingRettelserRediger()) {
				return notRendered();
			}
			if (count.isEditable() && !count.isModifiedBallotsProcessed() && !isApproved() && isPreviousApproved()) {
				return enabled(true);
			}
			return notRendered();
		case REVIEW_CORRECTIONS:
			if (!count.isManualCount() || !hasOpprellingRettelserLes() || !hasCorrections()) {
				return notRendered();
			}
			if (count.isEditable() && count.isModifiedBallotsProcessed()) {
				return enabled(true);
			}
			return notRendered();
		case MODIFIED_BALLOT_PROCESSED:
			if (!count.isManualCount() || !hasWriteAccess()) {
				return notRendered();
			}
			if (count.isEditable() && !count.isModifiedBallotsProcessed() && !isApproved() && isPreviousApproved()) {
				return enabled(true);
			}
			return notRendered();
		default:
			return super.button(type);
		}
	}

	boolean hasOpprellingRettelserLes() {
		return hasOpprellingRettelserRediger() || userAccess.hasAccess(Opptelling_Rettelser_Se);
	}

	boolean hasOpprellingRettelserRediger() {
		return userAccess.hasAccess(Opptelling_Rettelser_Rediger);
	}

	@Override
	public boolean isIncludeMarkOffCount() {
		return false;
	}

	@Override
	public boolean isSplitBallotCounts() {
		return true;
	}

	@Override
	public int getOrdinaryBallotCountDifferenceFromPreviousCount() {
		return getCounts().getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, getFinalCount().getId());
	}

	@Override
	public int getBlankBallotCountDifferenceFromPreviousCount() {
		return getCounts().getBlankBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, getFinalCount().getId());
	}

	@Override
	public int getQuestionableBallotCountDifferenceFromPreviousCount() {
		return getCounts().getQuestionableBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, getFinalCount().getId());
	}

	@Override
	public int getTotalBallotCountDifferenceFromPreviousCount() {
		return getCounts().getTotalBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, getFinalCount().getId());
	}

	@Override
	public boolean hasCorrections() {
		for (BallotCount count : getFinalCount().getBallotCounts()) {
			if (count.getModifiedCount() > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCountEditable() {
		return super.isCountEditable() && !hasModifiedBallotBatchForCurrentCount && !getFinalCount().isModifiedBallotsProcessed();
	}

	void updateHasModifiedBallotBatchForCurrentCount() {
		if (getFinalCount() == null || getFinalCount().isNew()) {
			hasModifiedBallotBatchForCurrentCount = false;
			return;
		}
		List<BallotCountRef> pks = new ArrayList<>();
		for (BallotCount bc : getFinalCount().getBallotCounts()) {
			BallotCountRef ballotCountRef = bc.getBallotCountRef();
			if (ballotCountRef != null) {
				// on a system in test mode a count may have both new and saved ballot counts and this excludes ballot counts not yet saved
				pks.add(ballotCountRef);
			}
		}
		hasModifiedBallotBatchForCurrentCount = modifiedBallotBatchService.hasModifiedBallotBatchForBallotCountPks(userData, pks);
	}
}
