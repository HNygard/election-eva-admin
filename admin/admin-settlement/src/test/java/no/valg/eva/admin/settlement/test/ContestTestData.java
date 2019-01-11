package no.valg.eva.admin.settlement.test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;

@SuppressWarnings({ "unused" })
public class ContestTestData {
	private String id;
	private int numberOfPositions;
	private List<BallotTestData> ballots;

	public Contest contest(SettlementBuilderTestData.Cache cache, Election election) {
		Contest contest = new Contest();
		contest.setId(id);
		contest.setElection(election);
		contest.setNumberOfPositions(numberOfPositions);
		contest.setBallots(ballots(cache, contest));
		return contest;
	}

	private Set<Ballot> ballots(SettlementBuilderTestData.Cache cache, Contest contest) {
		Set<Ballot> ballots = new LinkedHashSet<>();
		for (BallotTestData ballotTestData : this.ballots) {
			ballots.add(ballotTestData.ballot(cache, contest));
		}
		return ballots;
	}
}
