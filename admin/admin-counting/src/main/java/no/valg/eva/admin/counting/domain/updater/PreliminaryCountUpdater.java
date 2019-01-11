package no.valg.eva.admin.counting.domain.updater;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

public class PreliminaryCountUpdater implements CountUpdater<PreliminaryCount> {

	@Override
	public void applyUpdates(VoteCount voteCount, PreliminaryCount preliminaryCount, VoteCountStatus voteCountStatus) {
		voteCount.setApprovedBallots(preliminaryCount.getOrdinaryBallotCount() + preliminaryCount.getBlankBallotCount());
		voteCount.setRejectedBallots(preliminaryCount.getQuestionableBallotCount());
		voteCount.setInfoText(preliminaryCount.getComment());
		voteCount.setVoteCountStatus(voteCountStatus);
		if (preliminaryCount.getExpectedBallotCount() != null) {
			voteCount.setTechnicalVotings(preliminaryCount.getExpectedBallotCount());
		}
	}
}
