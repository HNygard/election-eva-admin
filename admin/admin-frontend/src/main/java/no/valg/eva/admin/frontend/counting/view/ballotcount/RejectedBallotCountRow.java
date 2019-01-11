package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class RejectedBallotCountRow extends HeaderRow {

	private RejectedBallotCount rejectedBallotCount;
	private String rowStyleClass;
	private String aft;

	public RejectedBallotCountRow(CountController ctrl, RejectedBallotCount rejectedBallotCount) {
		super(ctrl.getMessageProvider().get(rejectedBallotCount.getName()) + " (" + rejectedBallotCount.getId() + ")");
		this.rejectedBallotCount = rejectedBallotCount;
		this.rowStyleClass = "row_rejected row_rejected_" + rejectedBallotCount.getId();
		this.aft = "rejected_" + rejectedBallotCount.getId();
	}

	@Override
	public String getAft() {
		return aft;
	}

	@Override
	public String getRowStyleClass() {
		return rowStyleClass;
	}

	@Override
	public boolean isCountInput() {
		return true;
	}

	@Override
	public int getCount() {
		return rejectedBallotCount.getCount();
	}

	@Override
	public void setCount(int count) {
		rejectedBallotCount.setCount(count);
	}

	@Override
	public String getStyleClass() {
		return "";
	}
}
