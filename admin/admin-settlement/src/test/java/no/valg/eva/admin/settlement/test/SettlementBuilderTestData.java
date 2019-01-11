package no.valg.eva.admin.settlement.test;

import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.baseline;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.personal;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.renumber;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.strikeout;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.writein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings({ "unused" })
public class SettlementBuilderTestData {
	private final Cache cache = new Cache();
	private ElectionTestData election;
	private List<ContestReportTestData> contestReports;
	private List<AffiliationVoteCountTestData> expectedAffiliationVoteCounts;
	private List<CandidateVoteCountTestData> expectedCandidateVoteCounts;
	private List<CandidateRankTestData> expectedCandidateRanks;
	private List<CandidateSeatTestData> expectedCandidateSeats;

	public Contest contest() {
		if (cache.contest == null) {
			cache.contest = election.contest(cache);
		}
		return cache.contest;
	}

	public List<ContestReport> contestReports() {
		if (cache.contestReports == null) {
			cache.contestReports = new ArrayList<>();
			for (ContestReportTestData contestReportTestData : this.contestReports) {
				cache.contestReports.add(contestReportTestData.contestReport(cache, contest()));
			}
		}
		return cache.contestReports;
	}

	public AffiliationVoteCount[] expectedAffiliationVoteCounts(Settlement settlement) {
		if (cache.expectedAffiliationVoteCounts == null) {
			cache.expectedAffiliationVoteCounts = new ArrayList<>();
			for (AffiliationVoteCountTestData affiliationVoteCount : this.expectedAffiliationVoteCounts) {
				cache.expectedAffiliationVoteCounts.add(affiliationVoteCount.affiliationVoteCount(cache, settlement));
			}
		}
		return cache.expectedAffiliationVoteCounts.toArray(new AffiliationVoteCount[cache.expectedAffiliationVoteCounts.size()]);
	}

	public CandidateVoteCount[] expectedCandidateVoteCounts(Settlement settlement) {
		if (cache.expectedCandidateVoteCounts == null) {
			cache.expectedCandidateVoteCounts = new ArrayList<>();
			for (CandidateVoteCountTestData candidateVoteCount : this.expectedCandidateVoteCounts) {
				cache.expectedCandidateVoteCounts.add(candidateVoteCount.candidateVoteCount(cache, settlement));
			}
		}
		return cache.expectedCandidateVoteCounts.toArray(new CandidateVoteCount[cache.expectedCandidateVoteCounts.size()]);
	}

	public CandidateRank[] expectedCandidateRanks(Settlement settlement) {
		if (cache.expectedCandidateRanks == null) {
			cache.expectedCandidateRanks = new ArrayList<>();
			for (CandidateRankTestData candidateRank : this.expectedCandidateRanks) {
				cache.expectedCandidateRanks.add(candidateRank.candidateRank(cache, settlement));
			}
		}
		return cache.expectedCandidateRanks.toArray(new CandidateRank[cache.expectedCandidateRanks.size()]);
	}

	public CandidateSeat[] expectedCandidateSeats(Settlement settlement) {
		if (cache.expectedCandidateSeats == null) {
			cache.expectedCandidateSeats = new ArrayList<>();
			int lastIndex = -1;
			for (CandidateSeatTestData candidateSeat : this.expectedCandidateSeats) {
				cache.expectedCandidateSeats.add(candidateSeat.candidateSeat(cache, settlement, ++lastIndex));
			}
		}
		return cache.expectedCandidateSeats.toArray(new CandidateSeat[cache.expectedCandidateSeats.size()]);
	}

	public Map<VoteCategoryValues, VoteCategory> voteCategoryMap() {
		return cache.voteCategoryMap;
	}

	public class Cache {
		private final Map<String, Affiliation> affiliationMap = new HashMap<>();
		private final Map<String, Ballot> ballotMap = new HashMap<>();
		private final Map<String, Candidate> candidateMap = new HashMap<>();
		private final Map<VoteCategoryValues, VoteCategory> voteCategoryMap = new HashMap<>();
		private final Map<CountCategory, VoteCountCategory> voteCountCategoryMap = new HashMap<>();
		private final Map<String, AffiliationVoteCount> affiliationVoteCountMap = new HashMap<>();
		private final Map<String, CandidateRank> candidateRankMap = new HashMap<>();
		private final Map<String, Integer> rankNumberMap = new HashMap<>();
		private Contest contest;
		private List<ContestReport> contestReports;
		private List<AffiliationVoteCount> expectedAffiliationVoteCounts;
		private List<CandidateVoteCount> expectedCandidateVoteCounts;
		private List<CandidateRank> expectedCandidateRanks;
		private List<CandidateSeat> expectedCandidateSeats;

		private Cache() {
			voteCategoryMap.put(baseline, voteCategory(baseline));
			voteCategoryMap.put(personal, voteCategory(personal));
			voteCategoryMap.put(writein, voteCategory(writein));
			voteCategoryMap.put(renumber, voteCategory(renumber));
			voteCategoryMap.put(strikeout, voteCategory(strikeout));
		}

		private VoteCategory voteCategory(VoteCategoryValues voteCategoryValue) {
			VoteCategory voteCategory = new VoteCategory();
			voteCategory.setPk((long) voteCategoryValue.hashCode());
			voteCategory.setId(voteCategoryValue.name());
			return voteCategory;
		}

		public Contest contest() {
			return contest;
		}

		public Map<String, Affiliation> affiliationMap() {
			return affiliationMap;
		}

		public Map<String, Ballot> ballotMap() {
			return ballotMap;
		}

		public Map<String, Candidate> candidateMap() {
			return candidateMap;
		}

		public Map<VoteCategoryValues, VoteCategory> voteCategoryMap() {
			return voteCategoryMap;
		}

		public Map<CountCategory, VoteCountCategory> voteCountCategoryMap() {
			return voteCountCategoryMap;
		}

		public Map<String, AffiliationVoteCount> affiliationVoteCountMap() {
			return affiliationVoteCountMap;
		}

		public Map<String, CandidateRank> candidateRankMap() {
			return candidateRankMap;
		}
		
		public Map<String, Integer> rankNumberMap() {
			return rankNumberMap;
		}
	}
}
