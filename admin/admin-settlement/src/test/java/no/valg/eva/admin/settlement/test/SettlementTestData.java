package no.valg.eva.admin.settlement.test;

import no.valg.eva.admin.settlement.domain.model.Settlement;

public class SettlementTestData {
	private String contestId;

	public Settlement settlement(LevelingSeatSettlementBuilderTestData.Cache cache) {
		return new Settlement(cache.contestMap().get(contestId));
	}
}
