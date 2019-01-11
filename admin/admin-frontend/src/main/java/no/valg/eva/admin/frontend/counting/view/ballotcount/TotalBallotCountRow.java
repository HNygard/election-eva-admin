package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class TotalBallotCountRow extends HeaderRow {

	private CountController ctrl;

	public TotalBallotCountRow(CountController ctrl) {
		super("@count.label.totalBallotCounts");
		this.ctrl = ctrl;
	}

	@Override
	public String getAft() {
		return "total_ballot_count";
	}

	@Override
	public String getRowStyleClass() {
		return "row_total_ballot_count";
	}

	@Override
	public Integer getProtocolCount() {
		return ctrl.getCounts().getOrdinaryBallotCountForProtocolCounts();
	}

	@Override
	public int getCount() {
		return ctrl.getCount().getOrdinaryBallotCount();
	}

	@Override
	public Integer getDiff() {
		return ctrl.getOrdinaryBallotCountDifferenceFromPreviousCount();
	}

	@Override
	public int getModifiedCount() {
		return ctrl.getCount().getModifiedBallotCount();
	}

	@Override
	public int getUnmodifiedCount() {
		return ctrl.getCount().getUnmodifiedBallotCount();
	}
}
