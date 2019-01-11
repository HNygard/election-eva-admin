package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.counting.mockup.BallotCountMockups.UNMODIFIED_BALLOT_COUNT_BLANK;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.dtoExistingBallotCounts;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.dtoNewBallotCounts;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.INFO_TEXT;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.MANUAL_COUNT_TRUE;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.REJECTED_BALLOTS;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;

public final class PreliminaryCountMockups {

	public static final int BLANK_BALLOT_COUNT = UNMODIFIED_BALLOT_COUNT_BLANK;
	public static final int BLANK_BALLOT_COUNT_ZERO = 0;
	public static final int QUESTIONABLE_BALLOT_COUNT = REJECTED_BALLOTS;
	public static final int QUESTIONABLE_BALLOT_COUNT_ZERO = 0;
	public static final String COMMENT = INFO_TEXT;
	public static final String COMMENT_NULL = null;

	private PreliminaryCountMockups() {
	}

	public static PreliminaryCount preliminaryCount(
			final AreaPath areaPath,
			final CountStatus status,
			final int blankBallotCount,
			final int questionableBallotCount,
			final List<BallotCount> ballotCounts,
			final boolean manualCount,
			final String comment) {

		PreliminaryCount preliminaryCount = new PreliminaryCount("", areaPath, CountCategory.VO, "", "", manualCount);
		preliminaryCount.setStatus(status);

		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.setQuestionableBallotCount(questionableBallotCount);
		preliminaryCount.setBallotCounts(ballotCounts);

		preliminaryCount.setComment(comment);

		return preliminaryCount;
	}

	public static PreliminaryCount blankPreliminaryCount() {
		return preliminaryCount(
				AreaPath.from("730001.47.01.0101.010100.0001"),
				CountStatus.NEW,
				BLANK_BALLOT_COUNT_ZERO,
				QUESTIONABLE_BALLOT_COUNT_ZERO,
				dtoNewBallotCounts(),
				MANUAL_COUNT_TRUE,
				COMMENT_NULL);
	}

	public static PreliminaryCount loadedPreliminaryCount() {
		return preliminaryCount(
				AreaPath.from("730001.47.01.0101.010100.0001"),
				CountStatus.APPROVED,
				BLANK_BALLOT_COUNT,
				QUESTIONABLE_BALLOT_COUNT,
				dtoExistingBallotCounts(),
				MANUAL_COUNT_TRUE,
				COMMENT);
	}
}
