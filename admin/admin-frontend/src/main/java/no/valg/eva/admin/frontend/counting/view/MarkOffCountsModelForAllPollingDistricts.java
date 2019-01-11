package no.valg.eva.admin.frontend.counting.view;

import java.io.Serializable;
import java.util.ArrayList;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.frontend.common.MarkupUtils;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

public class MarkOffCountsModelForAllPollingDistricts extends ArrayList<MarkOffCountsModelForAllPollingDistricts.MarkOffCountsRow> implements Serializable {
	public MarkOffCountsModelForAllPollingDistricts(PreliminaryCountController ctrl) {
		add(new MarkOffCountsRow(ctrl));
	}

	public static class MarkOffCountsRow {
		private PreliminaryCountController ctrl;

		public MarkOffCountsRow(PreliminaryCountController ctrl) {
			this.ctrl = ctrl;
		}

		public int getTotalMarkOffCount() {
			return ctrl.getTotalMarkOffCount();
		}

		public Integer getTotalBallotCountForOtherPollingDistricts() {
			PreliminaryCount preliminaryCount = ctrl.getPreliminaryCount();
			return preliminaryCount.getTotalBallotCountForOtherPollingDistricts() == null ? 0 : preliminaryCount.getTotalBallotCountForOtherPollingDistricts();
		}

		public Integer getTotalBallotCountForAllPollingDistricts() {
			PreliminaryCount preliminaryCount = ctrl.getPreliminaryCount();
			return preliminaryCount.getTotalBallotCountForAllPollingDistricts();
		}

		public Integer getTotalBallotCountDifferenceForAllPollingDistricts() {
			PreliminaryCount preliminaryCount = ctrl.getPreliminaryCount();
			return preliminaryCount.getTotalBallotCountForAllPollingDistricts() - preliminaryCount.getMarkOffCount()
					+ preliminaryCount.getLateValidationCovers();
		}

		public String getDiffStyleClass() {
			return MarkupUtils.getClass(getTotalBallotCountDifferenceForAllPollingDistricts());
		}
	}
}
