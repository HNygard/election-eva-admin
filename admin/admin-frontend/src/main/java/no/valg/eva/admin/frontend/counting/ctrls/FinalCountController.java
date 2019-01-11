package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;

@Named
@ConversationScoped
public class FinalCountController extends BaseFinalCountController {
	@Override
	int getFinalCountIndex() {
		return getCounts().getFinalCountIndex();
	}

	@Override
	void updateCounts(int index, FinalCount finalCount) {
		if (index == getFinalCountIndex()) {
			getCounts().getFinalCounts().set(index, finalCount);
		} else {
			getCounts().getFinalCounts().add(finalCount);
			getCounts().setFinalCountIndex(getFinalCounts().size() - 1);
		}
		updateHasModifiedBallotBatchForCurrentCount();
	}

	public List<FinalCount> getFinalCounts() {
		return getCounts().getFinalCounts();
	}

	@Override
	public boolean isApproved() {
		return getCounts().hasApprovedFinalCount();
	}

	@Override
	public boolean isCountEditable() {
		if (isUserOnCountyLevel()) {
			return false;
		}
		return super.isCountEditable();
	}

	@Override
	public Button button(ButtonType type) {
		if (isUserOnCountyLevel()) {
			return notRendered();
		}
		FinalCount count = getFinalCount();
		switch (type) {
		case REGISTER_CORRECTIONS:
			if (!count.isManualCount() || !hasOpprellingRettelserRediger() || startCountingController.isContestOnCountyLevel()) {
				return notRendered();
			}
			if (count.isEditable() && !count.isModifiedBallotsProcessed() && !isApproved() && isPreviousApproved()) {
				return enabled(true);
			}
			return notRendered();
		case REVIEW_CORRECTIONS:
			if (!count.isManualCount() || !hasOpprellingRettelserLes() || !hasCorrections() || startCountingController.isContestOnCountyLevel()) {
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
}
