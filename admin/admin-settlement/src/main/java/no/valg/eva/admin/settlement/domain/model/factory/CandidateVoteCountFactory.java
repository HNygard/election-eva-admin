package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.LinkedHashMap;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.settlement.domain.consumer.CandidateVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateVoteCountEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CandidateVoteCountFactory extends EntityFactory<CandidateVoteCountFactory, CandidateVoteCountConsumer> implements CandidateVoteCountEventListener {
	private final Map<CandidateVoteCountKey, CandidateVoteCount> candidateVoteCountMap = new LinkedHashMap<>();

	@Override
	public void candidateVoteCountDelta(CandidateVoteCountEvent event) {
		CandidateVoteCountKey key = CandidateVoteCountKey.from(event);
		if (candidateVoteCountMap.containsKey(key)) {
			CandidateVoteCount candidateVoteCount = candidateVoteCountMap.get(key);
			candidateVoteCount.incrementVotes(event.getVotes());
			candidateVoteCount.incrementEarlyVotingVotes(event.getEarlyVotingVotes());
			candidateVoteCount.incrementElectionDayVotes(event.getElectionDayVotes());
		} else {
			candidateVoteCountMap.put(key, event.toCandidateVoteCount());
		}
	}

	public void buildCandidateVoteCounts() {
		updateConsumers();
	}

	@Override
	protected void updateConsumer(CandidateVoteCountConsumer candidateVoteCountConsumer) {
		candidateVoteCountMap.values().forEach(candidateVoteCountConsumer::consume);
	}

	@Override
	protected CandidateVoteCountFactory self() {
		return this;
	}

	private static class CandidateVoteCountKey {
		private final Affiliation affiliation;
		private final Candidate candidate;
		private final VoteCategory voteCategory;
		private final Integer rankNumber;

		private CandidateVoteCountKey(Affiliation affiliation, Candidate candidate, VoteCategory voteCategory, Integer rankNumber) {
			this.affiliation = affiliation;
			this.candidate = candidate;
			this.voteCategory = voteCategory;
			this.rankNumber = rankNumber;
		}

		public static CandidateVoteCountKey from(CandidateVoteCountEvent event) {
			return new CandidateVoteCountKey(event.getAffiliation(), event.getCandidate(), event.getVoteCategory(), event.getRankNumber());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CandidateVoteCountKey)) {
				return false;
			}
			CandidateVoteCountKey that = (CandidateVoteCountKey) o;
			return new EqualsBuilder()
					.append(affiliation, that.affiliation)
					.append(candidate, that.candidate)
					.append(voteCategory, that.voteCategory)
					.append(rankNumber, that.rankNumber)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(affiliation)
					.append(candidate)
					.append(voteCategory)
					.append(rankNumber)
					.toHashCode();
		}
	}
}
