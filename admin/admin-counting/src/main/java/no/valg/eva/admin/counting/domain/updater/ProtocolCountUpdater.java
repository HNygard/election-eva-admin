package no.valg.eva.admin.counting.domain.updater;

import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

public class ProtocolCountUpdater implements CountUpdater<ProtocolCount> {

	@Override
	public void applyUpdates(VoteCount voteCount, ProtocolCount protocolCount, VoteCountStatus voteCountStatus) {
		int approvedBallots = protocolCount.getOrdinaryBallotCount();
		if (protocolCount.getBlankBallotCount() != null) {
			approvedBallots += protocolCount.getBlankBallotCount();
		}
		voteCount.setApprovedBallots(approvedBallots);
		voteCount.setRejectedBallots(protocolCount.getQuestionableBallotCount());
		voteCount.setForeignSpecialCovers(protocolCount.getForeignSpecialCovers());
		voteCount.setSpecialCovers(protocolCount.getSpecialCovers());
		voteCount.setEmergencySpecialCovers(protocolCount.getEmergencySpecialCovers());
		voteCount.setInfoText(protocolCount.getComment());
		voteCount.setVoteCountStatus(voteCountStatus);
		voteCount.setBallotsForOtherContests(protocolCount.getBallotCountForOtherContests());
	}
}
