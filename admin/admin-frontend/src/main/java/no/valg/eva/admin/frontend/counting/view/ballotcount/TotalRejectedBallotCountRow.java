package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class TotalRejectedBallotCountRow extends HeaderRow {

	private CountController ctrl;

	public TotalRejectedBallotCountRow(CountController ctrl) {
		super("@count.ballot.totalRejected");
		this.ctrl = ctrl;
	}

	@Override
	public String getAft() {
		return "total_rejected_ballot_count";
	}

	@Override
	public String getRowStyleClass() {
		return "row_total_rejected_ballot_count";
	}

	@Override
	public int getCount() {
		return ctrl.getCount().getTotalRejectedBallotCount();
	}
}
