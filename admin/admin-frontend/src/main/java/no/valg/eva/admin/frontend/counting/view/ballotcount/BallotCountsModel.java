package no.valg.eva.admin.frontend.counting.view.ballotcount;

import java.io.Serializable;
import java.util.ArrayList;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.frontend.counting.ctrls.BaseFinalCountController;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class BallotCountsModel extends ArrayList<BallotCountsModelRow> implements Serializable {

	private CountController ctrl;
	private boolean showProtocolCount;

	public BallotCountsModel(CountController ctrl) {
		this.ctrl = ctrl;
		this.showProtocolCount = ctrl.isIncludeProtocolCount();
		boolean split = ctrl.isSplitBallotCounts();
		// BallotCounts
		for (BallotCount bc : ctrl.getCount().getBallotCounts()) {
			if (split) {
				add(new BallotCountWithSplitRow(bc));
			} else {
				add(new BallotCountRow(bc));
			}
		}

		// Total ballots
		add(new TotalBallotCountRow(ctrl));

		// Blank
		add(new BlankBallotCountRow(ctrl));

		if (ctrl.getCount().hasRejectedBallotCounts()) {
			// Rejected
			// Header
			add(new HeaderRow("@count.ballot.approve.rejected.proposed"));
			// Iterate the rejected
			for (RejectedBallotCount rbc : ctrl.getCount().getRejectedBallotCounts()) {
				add(new RejectedBallotCountRow(ctrl, rbc));
			}
			// Total
			add(new TotalRejectedBallotCountRow(ctrl));
		} else {
			// Questionable
			add(new QuestionableBallotCountRow(ctrl));
		}
	}

	public boolean isShowProtocolCount() {
		return showProtocolCount;
	}

	public int getTotalBallotCountForProtocolCounts() {
		return ctrl.getCounts().getTotalBallotCountForProtocolCounts();
	}

	public int getTotalBallotCount() {
		return ctrl.getCount().getTotalBallotCount();
	}

	public int getTotalBallotCountDifferenceFromPreviousCount() {
		return ctrl.getTotalBallotCountDifferenceFromPreviousCount();
	}

	public String getTabTitle() {
		String result = ctrl.getTab().getTitle();
		if (ctrl instanceof BaseFinalCountController) {
			result = ctrl.getMessageProvider().get(result) + " #" + ((BaseFinalCountController) ctrl).getFinalCount().getIndex();
		}
		return result;
	}
}
