package no.valg.eva.admin.counting.domain.updater;

import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

import java.util.Map;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public final class BallotCountUpdater {

	private BallotCountUpdater() {
		// no instances allowed
	}

	public static void updateBallotCounts(VoteCount voteCount, PreliminaryCount preliminaryCount, Map<String, Ballot> ballotMap) {
		updateBallotCounts(voteCount, preliminaryCount.getBallotCountMap(), preliminaryCount.getBlankBallotCount(), ballotMap);
	}

	public static void updateBallotCounts(VoteCount voteCount, FinalCount finalCount, Map<String, Ballot> ballotMap,
			Map<String, BallotRejection> ballotRejectionMap) {
		updateBallotCounts(voteCount, finalCount.getBallotCountMap(), zeroIfNull(finalCount.getBlankBallotCount()), ballotMap);
		updateRejectedBallotCounts(voteCount, finalCount.getRejectedBallotCountMap(), ballotRejectionMap);
	}

	public static void updateBlankBallotCount(VoteCount voteCount, Integer blankBallotCount, Ballot blankBallot) {
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap = voteCount.getBallotCountMap();
		if (ballotCountMap.containsKey(EvoteConstants.BALLOT_BLANK)) {
			ballotCountMap.get(EvoteConstants.BALLOT_BLANK).setUnmodifiedBallots(zeroIfNull(blankBallotCount));
		} else {
			voteCount.addNewBallotCount(blankBallot, zeroIfNull(blankBallotCount), 0);
		}
	}

	private static void updateBallotCounts(VoteCount voteCount, Map<String, BallotCount> ballotCountMap, Integer blankBallotCount, Map<String, Ballot> ballotMap) {
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> voteCountBallotCountMap = voteCount.getBallotCountMap();
		for (no.valg.eva.admin.counting.domain.model.BallotCount voteCountBallotCount : voteCountBallotCountMap.values()) {
			String ballotId = voteCountBallotCount.getBallot().getId();
			if (EvoteConstants.BALLOT_BLANK.equalsIgnoreCase(ballotId)) {
				voteCountBallotCount.setUnmodifiedBallots(zeroIfNull(blankBallotCount));
			} else {
				BallotCount ballotCount = ballotCountMap.get(ballotId);
				voteCountBallotCount.setModifiedBallots(ballotCount.getModifiedCount());
				voteCountBallotCount.setUnmodifiedBallots(ballotCount.getUnmodifiedCount());
			}
		}
		for (BallotCount ballotCount : ballotCountMap.values()) {
			String ballotId = ballotCount.getId();
			if (voteCountBallotCountMap.containsKey(ballotId)) {
				continue;
			}
			Ballot ballot = ballotMap.get(ballotId);
			voteCount.addNewBallotCount(ballot, ballotCount.getUnmodifiedCount(), ballotCount.getModifiedCount());
		}
	}

	private static void updateRejectedBallotCounts(
			VoteCount voteCount, Map<String, RejectedBallotCount> rejectedBallotCountMap, Map<String, BallotRejection> ballotRejectionMap) {
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> voteCountRejectedBallotCountMap = voteCount.getRejectedBallotCountMap();
		for (no.valg.eva.admin.counting.domain.model.BallotCount voteCountBallotCount : voteCountRejectedBallotCountMap.values()) {
			String ballotRejectionId = voteCountBallotCount.getBallotRejectionId();
			RejectedBallotCount rejectedBallotCount = rejectedBallotCountMap.get(ballotRejectionId);
			voteCountBallotCount.setUnmodifiedBallots(rejectedBallotCount.getCount());
		}
		for (RejectedBallotCount rejectedBallotCount : rejectedBallotCountMap.values()) {
			String ballotRejectionId = rejectedBallotCount.getId();
			if (voteCountRejectedBallotCountMap.containsKey(ballotRejectionId)) {
				continue;
			}
			BallotRejection ballotRejection = ballotRejectionMap.get(ballotRejectionId);
			voteCount.addNewRejectedBallotCount(ballotRejection, rejectedBallotCount.getCount());
		}
	}
}
