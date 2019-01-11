package no.valg.eva.admin.frontend.counting.view;

import java.io.Serializable;
import java.util.ArrayList;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.frontend.common.MarkupUtils;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

public class MarkOffCountsModel extends ArrayList<MarkOffCountsModel.MarkOffCountsRow> implements Serializable {

	private CountController ctrl;

	public MarkOffCountsModel(CountController ctrl) {
		this.ctrl = ctrl;
		add(new MarkOffCountsRow(ctrl));
	}

	public boolean isShowProtocolCount() {
		return ctrl.isIncludeProtocolCount();
	}

	public boolean isShowExpectedBallotCount() {
		return ctrl instanceof PreliminaryCountController && ((PreliminaryCountController) ctrl).getPreliminaryCount().getExpectedBallotCount() != null;
	}

	public boolean isShowTotalMarkOffCount() {
		if (ctrl instanceof PreliminaryCountController) {
			PreliminaryCountController preliminaryCountController = (PreliminaryCountController) ctrl;
			PreliminaryCount preliminaryCount = preliminaryCountController.getPreliminaryCount();
			return ctrl.getTotalMarkOffCount() != null && preliminaryCount.getExpectedBallotCount() == null;
		}
		return ctrl.getTotalMarkOffCount() != null;
	}

	public class MarkOffCountsRow {
		private CountController ctrl;

		public MarkOffCountsRow(CountController ctrl) {
			this.ctrl = ctrl;
		}

		public int getTotalProtocolCount() {
			return ctrl.getCounts().getTotalBallotCountForProtocolCounts();
		}

		public int getTotalMarkOffCount() {
			return ctrl.getTotalMarkOffCount();
		}

		public Integer getExpectedBallotCount() {
			if (ctrl instanceof PreliminaryCountController) {
				return ((PreliminaryCountController) ctrl).getPreliminaryCount().getExpectedBallotCount();
			}
			return null;
		}

		public void setExpectedBallotCount(Integer expectedBallotCount) {
			if (ctrl instanceof PreliminaryCountController) {
				((PreliminaryCountController) ctrl).getPreliminaryCount().setExpectedBallotCount(expectedBallotCount);
			}
		}

		public Integer getExpectedBallotCountDifference() {
			if (ctrl instanceof PreliminaryCountController) {
				PreliminaryCount preliminaryCount = ((PreliminaryCountController) ctrl).getPreliminaryCount();
				return preliminaryCount.getTotalBallotCount() - preliminaryCount.getExpectedBallotCount();
			}
			return null;
		}

		public int getTotalBallotCount() {
			return ctrl.getCount().getTotalBallotCount();
		}

		public int getTotalBallotCountDifferenceFromPreviousCount() {
			return ctrl.getTotalBallotCountDifferenceFromPreviousCount();
		}

		public String getDiffStyleClass() {
			if (MarkOffCountsModel.this.isShowExpectedBallotCount()) {
				return MarkupUtils.getClass(getExpectedBallotCountDifference());
			}
			return MarkupUtils.getClass(getTotalBallotCountDifferenceFromPreviousCount());
		}
	}
}
