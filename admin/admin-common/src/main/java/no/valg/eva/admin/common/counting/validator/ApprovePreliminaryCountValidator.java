package no.valg.eva.admin.common.counting.validator;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;

public abstract class ApprovePreliminaryCountValidator {
	public static ApprovePreliminaryCountValidator forVo(int totalBallotCountForProtocolCounts) {
		return new ApprovePreliminaryCountValidatorForVo(totalBallotCountForProtocolCounts);
	}

	public static ApprovePreliminaryCountValidator forOtherCategoriesThanVo() {
		return new ApprovePreliminaryCountValidatorForOtherCategories();
	}

	public abstract void validate(PreliminaryCount preliminaryCount);

	public abstract boolean isCommentRequired(PreliminaryCount preliminaryCount);
}
