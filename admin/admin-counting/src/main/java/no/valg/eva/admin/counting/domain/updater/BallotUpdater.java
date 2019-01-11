package no.valg.eva.admin.counting.domain.updater;

import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public interface BallotUpdater<T> {
	void applyUpdates(VoteCount voteCount, T count, Map<String, Ballot> ballotMap, Map<String, BallotRejection> ballotRejectionMap);
}
