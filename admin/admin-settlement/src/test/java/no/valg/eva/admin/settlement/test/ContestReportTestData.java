package no.valg.eva.admin.settlement.test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;

@SuppressWarnings({ "unused" })
public class ContestReportTestData {
	private List<VoteCountTestData> voteCounts;

	public ContestReport contestReport(SettlementBuilderTestData.Cache cache, Contest contest) {
		ContestReport contestReport = new ContestReport();
		contestReport.setContest(contest);
		contestReport.setVoteCountSet(voteCounts(cache, contestReport));
		return contestReport;
	}

	private Set<VoteCount> voteCounts(SettlementBuilderTestData.Cache cache, ContestReport contestReport) {
		Set<VoteCount> voteCounts = new LinkedHashSet<>();
		long lastPk = 0;
		for (VoteCountTestData voteCountTestData : this.voteCounts) {
			voteCounts.add(voteCountTestData.voteCount(cache, ++lastPk, contestReport));
		}
		return voteCounts;
	}
}
