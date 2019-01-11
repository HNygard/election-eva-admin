package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.common.counting.model.BallotCount;

public class BallotCountWithSplitRow extends BallotCountRow implements BallotCountsWithSplitModelRow {

	public BallotCountWithSplitRow(BallotCount ballotCount) {
		super(ballotCount);
	}

	@Override
	public boolean isCountInput() {
		return false;
	}

	@Override
	public void setCount(int count) {
		// do nothing
	}

	@Override
	public boolean isModifiedCountInput() {
		return true;
	}

	@Override
	public int getModifiedCount() {
		return getBallotCount().getModifiedCount();
	}

	@Override
	public void setModifiedCount(int modifiedCount) {
		getBallotCount().setModifiedCount(modifiedCount);
	}

	@Override
	public boolean isUnmodifiedCountInput() {
		return true;
	}

	@Override
	public int getUnmodifiedCount() {
		return getBallotCount().getUnmodifiedCount();
	}

	@Override
	public void setUnmodifiedCount(int unmodifiedCount) {
		getBallotCount().setUnmodifiedCount(unmodifiedCount);
	}
}
