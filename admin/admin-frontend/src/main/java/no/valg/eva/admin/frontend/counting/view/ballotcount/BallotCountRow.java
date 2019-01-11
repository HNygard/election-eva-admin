package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.common.counting.model.BallotCount;

public class BallotCountRow implements BallotCountsModelRow {

	private BallotCount ballotCount;
	private String title;
	private String id;

	public BallotCountRow(BallotCount ballotCount) {
		this.ballotCount = ballotCount;
		this.title = ballotCount.getName();
		this.id = ballotCount.getId();
	}

	public BallotCount getBallotCount() {
		return ballotCount;
	}

	@Override
	public String getAft() {
		return "ballot_" + getId();
	}

	@Override
	public String getRowStyleClass() {
		return "row_ballot row_ballot_" + getId();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Integer getProtocolCount() {
		return null;
	}

	@Override
	public boolean isCountInput() {
		return true;
	}

	@Override
	public int getCount() {
		return ballotCount.getCount();
	}

	@Override
	public void setCount(int count) {
		ballotCount.setUnmodifiedCount(count);
	}

	@Override
	public Integer getDiff() {
		return null;
	}

	@Override
	public String getStyleClass() {
		return "";
	}
}
