package no.valg.eva.admin.settlement.test;

import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@SuppressWarnings("unused")
public class AffiliationVoteCountTestData {
	private String affiliationId;
	private Integer earlyVotingBallots;
	private Integer earlyVotingModifiedBallots;
	private Integer electionDayVotingBallots;
	private Integer electionDayVotingModifiedBallots;
	private Integer baselineVotes;
	private Integer addedVotes;
	private Integer subtractedVotes;

	public AffiliationVoteCount affiliationVoteCount(SettlementBuilderTestData.Cache cache, Settlement settlement) {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setSettlement(settlement);
		affiliationVoteCount.setAffiliation(cache.affiliationMap().get(affiliationId));
		affiliationVoteCount.setBallots(earlyVotingBallots() + electionDayVotingBallots());
		affiliationVoteCount.setModifiedBallots(earlyVotingModifiedBallots() + electionDayVotingModifiedBallots());
		affiliationVoteCount.setEarlyVotingBallots(earlyVotingBallots());
		affiliationVoteCount.setEarlyVotingModifiedBallots(earlyVotingModifiedBallots());
		affiliationVoteCount.setElectionDayBallots(electionDayVotingBallots());
		affiliationVoteCount.setElectionDayModifiedBallots(electionDayVotingModifiedBallots());
		affiliationVoteCount.setBaselineVotes(baselineVotes());
		affiliationVoteCount.setAddedVotes(addedVotes());
		affiliationVoteCount.setSubtractedVotes(subtractedVotes());
		cache.affiliationVoteCountMap().put(affiliationId, affiliationVoteCount);
		return affiliationVoteCount;
	}

	private int earlyVotingBallots() {
		return zeroIfNull(earlyVotingBallots);
	}

	private int earlyVotingModifiedBallots() {
		return zeroIfNull(earlyVotingModifiedBallots);
	}

	private int electionDayVotingBallots() {
		return zeroIfNull(electionDayVotingBallots);
	}

	private int electionDayVotingModifiedBallots() {
		return zeroIfNull(electionDayVotingModifiedBallots);
	}

	private int baselineVotes() {
		return zeroIfNull(baselineVotes);
	}

	private int addedVotes() {
		return zeroIfNull(addedVotes);
	}

	private int subtractedVotes() {
		return zeroIfNull(subtractedVotes);
	}
}
