package no.valg.eva.admin.settlement.test;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Random;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings("unused")
public class CandidateRankTestData {
	private String candidateId;
	private BigDecimal votes;

	public CandidateRank candidateRank(SettlementBuilderTestData.Cache cache, Settlement settlement) {
		Candidate candidate = cache.candidateMap().get(candidateId);
		Affiliation affiliation = candidate.getAffiliation();
		CandidateRank candidateRank = new CandidateRank(candidate, affiliation, votes(), rankNumber(cache, affiliation.getParty().getId()));
		candidateRank.setPk(new Random().nextLong());
		candidateRank.setSettlement(settlement);
		cache.candidateRankMap().put(candidateId, candidateRank);
		return candidateRank;
	}

	private BigDecimal votes() {
		if (votes == null) {
			return ZERO;
		}
		return votes;
	}

	private Integer rankNumber(SettlementBuilderTestData.Cache cache, String id) {
		if (cache.rankNumberMap().containsKey(id)) {
			int rankNumber = cache.rankNumberMap().get(id) + 1;
			cache.rankNumberMap().put(id, rankNumber);
			return rankNumber;
		}
		cache.rankNumberMap().put(id, 1);
		return 1;
	}
}
