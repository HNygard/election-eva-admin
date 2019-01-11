package no.valg.eva.admin.frontend.counting.ctrls;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.model.FinalCount;

@Named
@ConversationScoped
public class CountyFinalCountController extends BaseFinalCountController {
	@Override
	int getFinalCountIndex() {
		return getCounts().getCountyFinalCountIndex();
	}

	@Override
	void updateCounts(int index, FinalCount finalCount) {
		if (index == getFinalCountIndex()) {
			getCounts().getCountyFinalCounts().set(index, finalCount);
		} else {
			getCounts().getCountyFinalCounts().add(index, finalCount);
			getCounts().setCountyFinalCountIndex(index);
		}
		updateHasModifiedBallotBatchForCurrentCount();
	}

	@Override
	public List<FinalCount> getFinalCounts() {
		return getCounts().getCountyFinalCounts();
	}

	@Override
	public int getBlankBallotCountDifferenceFromPreviousCount() {
		return getCounts().getCountyBlankBallotCountDifference(getFinalCount().getId());
	}

	@Override
	public int getOrdinaryBallotCountDifferenceFromPreviousCount() {
		return getCounts().getCountyOrdinaryBallotCountDifference(getFinalCount().getId());
	}

	@Override
	public int getQuestionableBallotCountDifferenceFromPreviousCount() {
		return getCounts().getCountyQuestionableBallotCountDifference(getFinalCount().getId());
	}

	@Override
	public int getTotalBallotCountDifferenceFromPreviousCount() {
		return getCounts().getCountyTotalBallotCountDifference(getFinalCount().getId());
	}

	@Override
	public boolean isApproved() {
		return getCounts().hasApprovedCountyFinalCount();
	}

	@Override
	public boolean isCountEditable() {
		if (!super.isCountEditable()) {
			return false;
		}

		CountController previousController = getPreviousController();
		if (!(previousController instanceof FinalCountController)) {
			return true;
		}

		FinalCount finalCount = ((FinalCountController) previousController).getFinalCount();
		return finalCount.isRejectedBallotsProcessed();
	}
}
