package no.valg.eva.admin.settlement.domain.model.strategy;

import static com.codepoetics.protonpack.StreamUtils.groupRuns;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;



public class ProcessCandidateRanksForElectionWithRenumbering implements ProcessCandidateRanksStrategy {
	@Override
	public List<CandidateRank> processCandidateRanks(Collection<CandidateRank> candidateRanks) {
		List<CandidateRank> candidateRanksSortedByAffiliation = getCandidateRanksSortedByAffiliation(candidateRanks);

		return candidateRanksGroupedByAffiliation(candidateRanksSortedByAffiliation)
				.map(this::processCandidateRanksForAffilation)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private List<CandidateRank> getCandidateRanksSortedByAffiliation(Collection<CandidateRank> candidateRanks) {
		List<CandidateRank> candidateRanksSortedByAffiliation = new ArrayList<>(candidateRanks);
		Collections.sort(candidateRanksSortedByAffiliation, (c1, c2) -> c1.getAffiliation().getPk().intValue() - c2.getAffiliation().getPk().intValue());
		return candidateRanksSortedByAffiliation;
	}

	private Stream<List<CandidateRank>> candidateRanksGroupedByAffiliation(Collection<CandidateRank> candidateRanks) {
		//NB! sjekker kun om elementer ved siden av hverandre skal grupperes, lista maa mao vaere sortert på affiliation for riktig håndtering av candidateRank
		return groupRuns(candidateRanks.stream(), this::groupByAffiliation);
	}

	private int groupByAffiliation(CandidateRank candidateRank1, CandidateRank candidateRank2) {
		return (int) (candidateRank1.getAffiliation().getPk() - candidateRank2.getAffiliation().getPk());
	}

	private List<CandidateRank> processCandidateRanksForAffilation(List<CandidateRank> candidateRanks) {
		Set<Candidate> rankedCandidates = new HashSet<>();
		Integer[] currentRankNumber = new Integer[1];
		return candidateRanks
				.stream()
				.sorted(this::orderByRankNumberAndDescendingVotesAndDisplayOrder)
				.filter(candidateRank -> includeCandidateRank(rankedCandidates, currentRankNumber, candidateRank))
				.collect(toList());
	}

	private int orderByRankNumberAndDescendingVotesAndDisplayOrder(CandidateRank candidateRank1, CandidateRank candidateRank2) {
		if (!candidateRank1.getRankNumber().equals(candidateRank2.getRankNumber())) {
			return candidateRank1.getRankNumber().compareTo(candidateRank2.getRankNumber());
		}
		if (!candidateRank1.getVotes().equals(candidateRank2.getVotes())) {
			return candidateRank2.getVotes().compareTo(candidateRank1.getVotes());
		}
		return candidateRank1.getCandidate().getDisplayOrder() - candidateRank2.getCandidate().getDisplayOrder();
	}

	private boolean includeCandidateRank(Set<Candidate> rankedCandidates, Integer[] currentRankNumber, CandidateRank candidateRank) {
		if (candidateRank.getRankNumber().equals(currentRankNumber[0]) || rankedCandidates.contains(candidateRank.getCandidate())) {
			return false;
		}
		currentRankNumber[0] = candidateRank.getRankNumber();
		rankedCandidates.add(candidateRank.getCandidate());
		return true;
	}
}
