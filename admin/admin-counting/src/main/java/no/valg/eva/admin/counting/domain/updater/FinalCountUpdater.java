package no.valg.eva.admin.counting.domain.updater;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

public class FinalCountUpdater implements CountUpdater<FinalCount> {

	@Override
	public void applyUpdates(VoteCount voteCount, FinalCount finalCount, VoteCountStatus voteCountStatus) {
		voteCount.setApprovedBallots(finalCount.getOrdinaryBallotCount() + finalCount.getBlankBallotCount());
		voteCount.setRejectedBallots(finalCount.getTotalRejectedBallotCount());
		voteCount.setInfoText(finalCount.getComment());
		voteCount.setVoteCountStatus(voteCountStatus);
		voteCount.setModifiedBallotsProcessed(finalCount.isModifiedBallotsProcessed());
		voteCount.setRejectedBallotsProcessed(finalCount.isRejectedBallotsProcessed());
	}
}
