package no.valg.eva.admin.counting.domain.filter;

import no.valg.eva.admin.common.filter.Filter;
import no.valg.eva.admin.counting.domain.model.BallotCount;

public final class BallotsOnlyBallotCountFilter implements Filter<BallotCount> {
	public static final BallotsOnlyBallotCountFilter INSTANCE = new BallotsOnlyBallotCountFilter();

	private BallotsOnlyBallotCountFilter() {
	}

	@Override
	public boolean filter(BallotCount ballotCount) {
		return ballotCount.getBallot() != null;
	}
}
