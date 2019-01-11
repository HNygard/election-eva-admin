package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.common.mockups.GeneralMockups.ELECTRONIC_MARK_OFFS_TRUE;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.UNMODIFIED_BALLOT_COUNT_BLANK;
import static no.valg.eva.admin.counting.mockup.DailyMarkOffCountMockups.blankDailyMarkOffCounts;
import static no.valg.eva.admin.counting.mockup.DailyMarkOffCountMockups.loadedDailyMarkOffCounts;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.APPROVED_BALLOTS;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.EMERGENCY_SPECIAL_COVERS;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.EMERGENCY_SPECIAL_COVERS_ZERO;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.FOREIGN_SPECIAL_COVERS;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.FOREIGN_SPECIAL_COVERS_ZERO;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.INFO_TEXT;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.INFO_TEXT_NULL;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.REJECTED_BALLOTS;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.SPECIAL_COVERS;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.SPECIAL_COVERS_ZERO;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;

public final class ProtocolCountMockups {

	public static final int ORDINARY_BALLOT_COUNT = APPROVED_BALLOTS;
	public static final int ORDINARY_BALLOT_COUNT_ZERO = 0;
	public static final int BLANK_BALLOT_COUNT = UNMODIFIED_BALLOT_COUNT_BLANK;
	public static final int BLANK_BALLOT_COUNT_ZERO = 0;
	public static final int QUESTIONABLE_BALLOT_COUNT = REJECTED_BALLOTS;
	public static final int QUESTIONABLE_BALLOT_COUNT_ZERO = 0;
	public static final String COMMENT = INFO_TEXT;
	public static final String COMMENT_NULL = INFO_TEXT_NULL;

	private ProtocolCountMockups() {
	}

	public static ProtocolCount protocolCount(
			AreaPath areaPath,
			CountStatus status,
			boolean electronicMarkOffs,
			DailyMarkOffCounts dailyMarkOffCounts,
			int ordinaryBallotCount,
			int blankBallotCount,
			int questionableBallotCount,
			int foreignSpecialCovers,
			int specialCovers,
			Integer emergencySpecialCovers,
			String comment) {

		ProtocolCount protocolCount = new ProtocolCount("", areaPath, "", "", true);
		protocolCount.setStatus(status);

		protocolCount.setElectronicMarkOffs(electronicMarkOffs);
		protocolCount.setDailyMarkOffCounts(dailyMarkOffCounts);

		protocolCount.setOrdinaryBallotCount(ordinaryBallotCount);
		protocolCount.setBlankBallotCount(blankBallotCount);
		protocolCount.setQuestionableBallotCount(questionableBallotCount);

		protocolCount.setForeignSpecialCovers(foreignSpecialCovers);
		protocolCount.setSpecialCovers(specialCovers);
		if (electronicMarkOffs) {
			protocolCount.setEmergencySpecialCovers(emergencySpecialCovers);
		} else {
			protocolCount.setEmergencySpecialCovers(0);
		}

		protocolCount.setComment(comment);

		return protocolCount;
	}

	public static ProtocolCount loadedProtocolCount(AreaPath areaPath) {
		return protocolCount(
				areaPath, CountStatus.APPROVED,
				ELECTRONIC_MARK_OFFS_TRUE,
				loadedDailyMarkOffCounts(),
				ORDINARY_BALLOT_COUNT,
				BLANK_BALLOT_COUNT,
				QUESTIONABLE_BALLOT_COUNT,
				FOREIGN_SPECIAL_COVERS,
				SPECIAL_COVERS,
				EMERGENCY_SPECIAL_COVERS,
				COMMENT);
	}

	public static ProtocolCount blankProtocolCount() {
		return protocolCount(
				null, CountStatus.SAVED,
				ELECTRONIC_MARK_OFFS_TRUE,
				blankDailyMarkOffCounts(),
				ORDINARY_BALLOT_COUNT_ZERO,
				BLANK_BALLOT_COUNT_ZERO,
				QUESTIONABLE_BALLOT_COUNT_ZERO,
				FOREIGN_SPECIAL_COVERS_ZERO,
				SPECIAL_COVERS_ZERO,
				EMERGENCY_SPECIAL_COVERS_ZERO,
				COMMENT_NULL);
	}

	public static ProtocolCount newProtocolCount() {
		return protocolCount(
				null, CountStatus.NEW,
				ELECTRONIC_MARK_OFFS_TRUE,
				blankDailyMarkOffCounts(),
				ORDINARY_BALLOT_COUNT_ZERO,
				BLANK_BALLOT_COUNT_ZERO,
				QUESTIONABLE_BALLOT_COUNT_ZERO,
				FOREIGN_SPECIAL_COVERS_ZERO,
				SPECIAL_COVERS_ZERO,
				EMERGENCY_SPECIAL_COVERS_ZERO,
				COMMENT_NULL);
	}
}
