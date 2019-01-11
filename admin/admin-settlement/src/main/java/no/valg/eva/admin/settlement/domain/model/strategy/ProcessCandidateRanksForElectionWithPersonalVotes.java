package no.valg.eva.admin.settlement.domain.model.strategy;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.consumer.AffiliationVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

public class ProcessCandidateRanksForElectionWithPersonalVotes implements ProcessCandidateRanksStrategy, AffiliationVoteCountConsumer {
	private final BigDecimal candidateRankVoteShareThreshold;
	private final Map<Affiliation, AffiliationVoteCount> affiliationVoteCountMap = new HashMap<>();

	public ProcessCandidateRanksForElectionWithPersonalVotes(BigDecimal candidateRankVoteShareThreshold) {
		this.candidateRankVoteShareThreshold = candidateRankVoteShareThreshold;
	}

	@Override
	public void consume(AffiliationVoteCount affiliationVoteCount) {
		affiliationVoteCountMap.put(affiliationVoteCount.getAffiliation(), affiliationVoteCount);
	}

	@Override
	public List<CandidateRank> processCandidateRanks(Collection<CandidateRank> candidateRanks) {
		List<CandidateRank> result = candidateRanks
				.stream()
				.sorted(this::orderByAffiliationAndDesendingVotesAndDisplayOrder)
				.collect(toList());
		long lastAffiliationPk = -1;
		int lastRankNumber = 0;
		for (CandidateRank candidateRank : result) {
			long affiliationPk = candidateRank.getAffiliation().getPk();
			if (lastAffiliationPk != affiliationPk) {
				lastAffiliationPk = affiliationPk;
				lastRankNumber = 0;
			}
			candidateRank.setRankNumber(++lastRankNumber);
		}
		return result;
	}

	private int orderByAffiliationAndDesendingVotesAndDisplayOrder(CandidateRank candidateRank1, CandidateRank candidateRank2) {
		if (!candidateRank1.getAffiliation().getPk().equals(candidateRank2.getAffiliation().getPk())) {
			return (int) (candidateRank1.getAffiliation().getPk() - candidateRank2.getAffiliation().getPk());
		}
		BigDecimal candidateVotes1 = candidateVotes(candidateRank1);
		BigDecimal candidateVotes2 = candidateVotes(candidateRank2);
		if (candidateVotes1.compareTo(candidateVotes2) != 0) {
			return candidateVotes2.compareTo(candidateVotes1);
		}
		return candidateRank1.getCandidate().getDisplayOrder() - candidateRank2.getCandidate().getDisplayOrder();
	}

	private BigDecimal candidateVotes(CandidateRank candidateRank) {
		BigDecimal candidateVotes = candidateRank.getVotes();
		AffiliationVoteCount affiliationVoteCount = affiliationVoteCountMap.get(candidateRank.getAffiliation());
		BigDecimal minimumCandidateVotes = BigDecimal.valueOf(affiliationVoteCount.getVotes()).multiply(candidateRankVoteShareThreshold);
		if (candidateVotes.compareTo(minimumCandidateVotes) >= 0) {
			return candidateVotes;
		}
		return ZERO;
	}
}
