package no.valg.eva.admin.common.counting.validator;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;

public class ApprovePreliminaryCountValidatorForVo extends ApprovePreliminaryCountValidator {
	private int totalBallotCountForProtocolCounts;

	ApprovePreliminaryCountValidatorForVo(int totalBallotCountForProtocolCounts) {
		this.totalBallotCountForProtocolCounts = totalBallotCountForProtocolCounts;
	}

	@Override
	public void validate(PreliminaryCount preliminaryCount) {
		preliminaryCount.validate();
		if (!preliminaryCount.hasComment() && isCommentRequired(preliminaryCount)) {
			throw new ValidateException("@count.error.validation.missing_comment.preliminary_count");
		}
	}

	@Override
	public boolean isCommentRequired(PreliminaryCount preliminaryCount) {
		int countDifference = preliminaryCount.getTotalBallotCount() - totalBallotCountForProtocolCounts;
		return countDifference != 0;
	}
}
