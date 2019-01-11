package no.valg.eva.admin.counting.domain.updater;

import java.util.Map;

import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class PreliminaryBallotUpdater implements BallotUpdater<PreliminaryCount> {
	@Override
	public void applyUpdates(VoteCount voteCount, PreliminaryCount count, Map<String, Ballot> ballotMap, Map<String, BallotRejection> ballotRejectionMap) {
		BallotCountUpdater.updateBallotCounts(voteCount, count, ballotMap);
	}
}
