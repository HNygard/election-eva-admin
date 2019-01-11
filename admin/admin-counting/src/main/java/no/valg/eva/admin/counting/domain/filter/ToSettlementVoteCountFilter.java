package no.valg.eva.admin.counting.domain.filter;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.filter.Filter;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public final class ToSettlementVoteCountFilter implements Filter<VoteCount> {
	public static final ToSettlementVoteCountFilter INSTANCE = new ToSettlementVoteCountFilter();

	private ToSettlementVoteCountFilter() {
	}

	@Override
	public boolean filter(VoteCount voteCount) {
		CountQualifier countQualifier = CountQualifier.fromId(voteCount.getCountQualifierId());
		return FINAL.equals(countQualifier) && TO_SETTLEMENT.equals(voteCount.getCountStatus());
	}
}
