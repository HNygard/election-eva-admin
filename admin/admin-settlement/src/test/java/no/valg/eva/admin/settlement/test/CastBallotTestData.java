package no.valg.eva.admin.settlement.test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;

@SuppressWarnings({ "unused" })
public class CastBallotTestData {
	private List<CandidateVoteTestData> candidateVotes;

	public CastBallot castBallot(SettlementBuilderTestData.Cache cache, long pk, BallotCount ballotCount) {
		CastBallot castBallot = new CastBallot();
		castBallot.setBallotCount(ballotCount);
		castBallot.setPk(pk);
		castBallot.setId(String.valueOf(pk));
		castBallot.setCandidateVotes(candidateVotes(cache, castBallot));
		return castBallot;
	}

	private Set<CandidateVote> candidateVotes(SettlementBuilderTestData.Cache cache, CastBallot castBallot) {
		Set<CandidateVote> candidateVotes = new LinkedHashSet<>();
		long lastPk = 0;
		for (CandidateVoteTestData candidateVoteTestData : this.candidateVotes) {
			candidateVotes.add(candidateVoteTestData.candidateVote(cache, ++lastPk, castBallot));
		}
		return candidateVotes;
	}
}
