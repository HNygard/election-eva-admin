package no.valg.eva.admin.common.counting.validator;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;

public class ApprovePreliminaryCountValidatorForOtherCategories extends ApprovePreliminaryCountValidator {
	public ApprovePreliminaryCountValidatorForOtherCategories() {
	}

	public void validate(PreliminaryCount preliminaryCount) {
		preliminaryCount.validateForApproval();
	}

	@Override
	public boolean isCommentRequired(PreliminaryCount preliminaryCount) {
		return preliminaryCount.isCommentRequired();
	}
}
