package no.valg.eva.admin.counting.domain.updater;

import java.util.Map;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class ProtocolBallotUpdater implements BallotUpdater<ProtocolCount> {
	@Override
	public void applyUpdates(VoteCount voteCount, ProtocolCount count, Map<String, Ballot> ballotMap, Map<String, BallotRejection> ballotRejectionMap) {
		BallotCountUpdater.updateBlankBallotCount(voteCount, count.getBlankBallotCount(), ballotMap.get(EvoteConstants.BALLOT_BLANK));
	}
}
