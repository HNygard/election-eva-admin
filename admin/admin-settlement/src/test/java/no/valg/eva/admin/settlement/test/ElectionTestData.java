package no.valg.eva.admin.settlement.test;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.SettlementConfig;

@SuppressWarnings("unused")
public class ElectionTestData {
	private String id;
	private SettlementConfig settlementConfig;
	private BigDecimal baselineVoteFactor;
	private BigDecimal candidateRankVoteShareThreshold = ZERO;
	private BigDecimal settlementFirstDivisor;
	private ContestTestData contest;

	public Election election() {
		Election election = new Election();
		election.setWritein(settlementConfig.isWriteIn());
		election.setRenumber(settlementConfig.isRenumber());
		election.setBaselineVoteFactor(baselineVoteFactor);
		election.setCandidateRankVoteShareThreshold(candidateRankVoteShareThreshold);
		election.setSettlementFirstDivisor(settlementFirstDivisor);
		return election;
	}

	public Contest contest(SettlementBuilderTestData.Cache cache) {
		return contest.contest(cache, election());
	}
}
