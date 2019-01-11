package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class QuestionableBallotCountRow extends HeaderRow {

	private CountController ctrl;

	public QuestionableBallotCountRow(CountController ctrl) {
		super("@count.label.questionable");
		this.ctrl = ctrl;
	}

	@Override
	public String getAft() {
		return "questionable";
	}

	@Override
	public String getRowStyleClass() {
		return "row_questionable";
	}

	@Override
	public Integer getProtocolCount() {
		return ctrl.getCounts().getQuestionableBallotCountForProtocolCounts();
	}

	@Override
	public boolean isCountInput() {
		return true;
	}

	@Override
	public int getCount() {
		return ctrl.getCount().getQuestionableBallotCount();
	}

	@Override
	public void setCount(int count) {
		ctrl.getCount().setQuestionableBallotCount(count);
	}

	@Override
	public Integer getDiff() {
		return ctrl.getQuestionableBallotCountDifferenceFromPreviousCount();
	}

	@Override
	public String getStyleClass() {
		return "";
	}
}
