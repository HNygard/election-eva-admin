package no.valg.eva.admin.settlement.test;

import static java.math.BigDecimal.ONE;

import java.math.BigDecimal;
import java.util.Random;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings("unused")
public class CandidateSeatTestData {
	private String candidateId;

	public CandidateSeat candidateSeat(SettlementBuilderTestData.Cache cache, Settlement settlement, int index) {
		Candidate candidate = cache.candidateMap().get(candidateId);
		Affiliation affiliation = candidate.getAffiliation();
		int seatNumber = index + 1;
		BigDecimal divisor = divisor(cache);
		boolean elected = seatNumber <= cache.contest().getNumberOfPositions();
		int dividend = cache.affiliationVoteCountMap().get(affiliation.getParty().getId()).getVotes();
		CandidateSeat candidateSeat = new CandidateSeat(candidate, affiliation, seatNumber, dividend, divisor, elected);
		candidateSeat.setPk(new Random().nextLong());
		candidateSeat.setSettlement(settlement);
		return candidateSeat;
	}

	private BigDecimal divisor(SettlementBuilderTestData.Cache cache) {
		int rankNumber = cache.candidateRankMap().get(candidateId).getRankNumber();
		if (rankNumber == 1) {
			return cache.contest().getSettlementFirstDivisor();
		}
		return BigDecimal.valueOf(rankNumber - 1).multiply(BigDecimal.valueOf(2)).add(ONE);
	}
}
