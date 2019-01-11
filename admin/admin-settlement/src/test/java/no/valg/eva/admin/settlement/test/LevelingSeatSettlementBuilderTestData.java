package no.valg.eva.admin.settlement.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings({ "unused" })
public class LevelingSeatSettlementBuilderTestData {
	private final Cache cache = new Cache();
	private ElectionTestData election;
	private List<SettlementTestData> settlements = new ArrayList<>();

	public Election election() {
		if (cache.election == null) {
			cache.election = election.election();
		}
		return cache.election;
	}

	public List<Settlement> settlements() {
		if (cache.settlements == null) {
			cache.settlements = new ArrayList<>();
			for (SettlementTestData settlementTestData : this.settlements) {
				cache.settlements.add(settlementTestData.settlement(cache));
			}
		}
		return cache.settlements;
	}

	public class Cache {
		private final Map<String, Contest> contestMap = new HashMap<>();
		private List<Settlement> settlements;
		private Election election;

		public Map<String, Contest> contestMap() {
			return contestMap;
		}
	}
}
