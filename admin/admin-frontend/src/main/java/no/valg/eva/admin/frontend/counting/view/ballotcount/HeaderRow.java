package no.valg.eva.admin.frontend.counting.view.ballotcount;

public class HeaderRow implements BallotCountsWithSplitModelRow {

	private String title;

	public HeaderRow(String title) {
		this.title = title;
	}

	@Override
	public String getAft() {
		return getId() == null ? "header" : "header_" + getId();
	}

	@Override
	public String getRowStyleClass() {
		return getId() == null ? "row_header" : "row_header row_header_" + getId();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getStyleClass() {
		return "bold";
	}

	@Override
	public boolean isModifiedCountInput() {
		return false;
	}

	@Override
	public int getModifiedCount() {
		return 0;
	}

	@Override
	public void setModifiedCount(int modifiedCount) {
		// do nothing
	}

	@Override
	public boolean isUnmodifiedCountInput() {
		return false;
	}

	@Override
	public int getUnmodifiedCount() {
		return 0;
	}

	@Override
	public void setUnmodifiedCount(int unmodifiedCount) {
		// do nothing
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public Integer getProtocolCount() {
		return null;
	}

	@Override
	public Integer getDiff() {
		return null;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public void setCount(int count) {
		// do nothing
	}

	@Override
	public boolean isCountInput() {
		return false;
	}
}
