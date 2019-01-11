package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class BlankBallotCountRow extends HeaderRow {

	private CountController ctrl;

	public BlankBallotCountRow(CountController ctrl) {
		super("@count.label.blancs");
		this.ctrl = ctrl;
	}

	@Override
	public String getAft() {
		return "blank";
	}

	@Override
	public String getRowStyleClass() {
		return "row_blank";
	}

	@Override
	public Integer getProtocolCount() {
		return ctrl.getCounts().getBlankBallotCountForProtocolCounts();
	}

	@Override
	public boolean isCountInput() {
		return true;
	}

	@Override
	public int getCount() {
		return ctrl.getCount().getBlankBallotCount();
	}

	@Override
	public void setCount(int count) {
		ctrl.getCount().setBlankBallotCount(count);
	}

	@Override
	public Integer getDiff() {
		return ctrl.getBlankBallotCountDifferenceFromPreviousCount();
	}

	@Override
	public String getStyleClass() {
		return "";
	}
}
