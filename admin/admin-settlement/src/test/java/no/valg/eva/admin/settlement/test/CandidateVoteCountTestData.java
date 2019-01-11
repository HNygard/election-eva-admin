package no.valg.eva.admin.settlement.test;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Random;

import no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings("unused")
public class CandidateVoteCountTestData {
	private String affiliationId;
	private String candidateId;
	private VoteCategoryValues voteCategoryId;
	private Integer rankNumber;
	private BigDecimal earlyVotingVotes = ZERO;
	private BigDecimal electionDayVotes = ZERO;

	public CandidateVoteCount candidateVoteCount(SettlementBuilderTestData.Cache cache, Settlement settlement) {
		CandidateVoteCount candidateVoteCount = new CandidateVoteCount();
		candidateVoteCount.setPk(new Random().nextLong());
		candidateVoteCount.setSettlement(settlement);
		candidateVoteCount.setAffiliation(cache.affiliationMap().get(affiliationId));
		candidateVoteCount.setCandidate(cache.candidateMap().get(candidateId));
		candidateVoteCount.setVoteCategory(cache.voteCategoryMap().get(voteCategoryId));
		candidateVoteCount.setRankNumber(rankNumber);
		candidateVoteCount.setVotes(earlyVotingVotes.add(electionDayVotes));
		candidateVoteCount.setEarlyVotingVotes(earlyVotingVotes);
		candidateVoteCount.setElectionDayVotes(electionDayVotes);
		return candidateVoteCount;
	}
}
