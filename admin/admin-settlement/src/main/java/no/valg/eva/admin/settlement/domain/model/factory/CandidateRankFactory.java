package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.CandidateRankConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;
import no.valg.eva.admin.settlement.domain.model.strategy.ProcessCandidateRanksStrategy;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CandidateRankFactory extends EntityFactory<CandidateRankFactory, CandidateRankConsumer> implements CandidateRankEventListener {
	private final Map<CandidateRankKey, CandidateRank> candidateRankMap = new LinkedHashMap<>();
	private final ProcessCandidateRanksStrategy processCandidateRanksStrategy;
	private List<CandidateRank> processedCandidateRanks;

	public CandidateRankFactory(ProcessCandidateRanksStrategy processCandidateRanksStrategy) {
		this.processCandidateRanksStrategy = processCandidateRanksStrategy;
	}

	@Override
	public void candidateRankDelta(CandidateRankEvent event) {
		CandidateRankKey key = CandidateRankKey.from(event);
		if (candidateRankMap.containsKey(key)) {
			CandidateRank candidateRank = candidateRankMap.get(key);
			candidateRank.incrementVotes(event.getVotes());
		} else {
			candidateRankMap.put(key, event.toCandidateRank());
		}
	}

	public void buildCandidateRanks() {
		processedCandidateRanks = processCandidateRanksStrategy.processCandidateRanks(candidateRankMap.values());
		updateConsumers();
		processedCandidateRanks = null;
	}

	@Override
	protected void updateConsumer(CandidateRankConsumer candidateRankConsumer) {
		processedCandidateRanks.forEach(candidateRankConsumer::consume);
	}

	@Override
	protected CandidateRankFactory self() {
		return this;
	}

	private static class CandidateRankKey {
		private final Candidate candidate;
		private final Integer rankNumber;

		private CandidateRankKey(Candidate candidate, Integer rankNumber) {
			this.candidate = candidate;
			this.rankNumber = rankNumber;
		}

		public static CandidateRankKey from(CandidateRankEvent event) {
			return new CandidateRankKey(event.getCandidate(), event.getRankNumber());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CandidateRankKey)) {
				return false;
			}
			CandidateRankKey that = (CandidateRankKey) o;
			return new EqualsBuilder()
					.append(candidate, that.candidate)
					.append(rankNumber, that.rankNumber)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(candidate)
					.append(rankNumber)
					.toHashCode();
		}
	}
}
