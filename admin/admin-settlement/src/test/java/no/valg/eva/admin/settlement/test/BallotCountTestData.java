package no.valg.eva.admin.settlement.test;

import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.VoteCount;

@SuppressWarnings({ "unused" })
public class BallotCountTestData {
	private String ballotId;
	private Integer unmodifiedBallots;
	private List<CastBallotTestData> castBallots = new ArrayList<>();

	public BallotCount ballotCount(SettlementBuilderTestData.Cache cache, long pk, VoteCount voteCount) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(pk);
		ballotCount.setVoteCount(voteCount);
		ballotCount.setBallot(cache.ballotMap().get(ballotId));
		ballotCount.setUnmodifiedBallots(zeroIfNull(unmodifiedBallots));
		ballotCount.setModifiedBallots(castBallots.size());
		ballotCount.setCastBallots(castBallots(cache, ballotCount));
		return ballotCount;
	}

	private Set<CastBallot> castBallots(SettlementBuilderTestData.Cache cache, BallotCount ballotCount) {
		Set<CastBallot> castBallots = new LinkedHashSet<>();
		long lastPk = 0;
		for (CastBallotTestData castBallotTestData : this.castBallots) {
			castBallots.add(castBallotTestData.castBallot(cache, ++lastPk, ballotCount));
		}
		return castBallots;
	}
}
