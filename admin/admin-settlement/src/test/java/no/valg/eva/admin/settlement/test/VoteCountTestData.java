package no.valg.eva.admin.settlement.test;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

@SuppressWarnings({ "unused" })
public class VoteCountTestData {
	private CountCategory countCategory;
	private List<BallotCountTestData> ballotCounts;

	public VoteCount voteCount(SettlementBuilderTestData.Cache cache, long pk, ContestReport contestReport) {
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(pk);
		voteCount.setContestReport(contestReport);
		voteCount.setCountQualifier(finalCountQualifier());
		voteCount.setVoteCountStatus(toSettlementVoteCountStatus());
		voteCount.setVoteCountCategory(voteCountCategory(cache.voteCountCategoryMap()));
		voteCount.setBallotCountSet(ballotCounts(cache, voteCount));
		return voteCount;
	}

	private CountQualifier finalCountQualifier() {
		CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(FINAL.getId());
		return countQualifier;
	}

	private VoteCountStatus toSettlementVoteCountStatus() {
		VoteCountStatus voteCountStatus = new VoteCountStatus();
		voteCountStatus.setId(TO_SETTLEMENT.getId());
		return voteCountStatus;
	}

	private VoteCountCategory voteCountCategory(Map<CountCategory, VoteCountCategory> voteCountCategoryMap) {
		if (!voteCountCategoryMap.containsKey(countCategory)) {
			VoteCountCategory voteCountCategory = new VoteCountCategory();
			voteCountCategory.setEarlyVoting(countCategory.isEarlyVoting());
			voteCountCategoryMap.put(countCategory, voteCountCategory);
		}
		return voteCountCategoryMap.get(countCategory);
	}

	private Set<BallotCount> ballotCounts(SettlementBuilderTestData.Cache cache, VoteCount voteCount) {
		Set<BallotCount> ballotCounts = new LinkedHashSet<>();
		long lastPk = 0;
		for (BallotCountTestData ballotCountTestData : this.ballotCounts) {
			ballotCounts.add(ballotCountTestData.ballotCount(cache, ++lastPk, voteCount));
		}
		return ballotCounts;
	}
}
