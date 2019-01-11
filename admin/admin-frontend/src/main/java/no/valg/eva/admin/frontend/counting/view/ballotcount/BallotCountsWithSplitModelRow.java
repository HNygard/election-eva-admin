package no.valg.eva.admin.frontend.counting.view.ballotcount;

public interface BallotCountsWithSplitModelRow extends BallotCountsModelRow {

	boolean isModifiedCountInput();

	int getModifiedCount();

	void setModifiedCount(int modifiedCount);

	boolean isUnmodifiedCountInput();

	int getUnmodifiedCount();

	void setUnmodifiedCount(int unmodifiedCount);

}
