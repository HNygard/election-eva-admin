package no.valg.eva.admin.settlement.test;

import no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;

@SuppressWarnings("unused")
public class CandidateVoteTestData {
	private VoteCategoryValues voteCategoryId;
	private String candidateId;

	public CandidateVote candidateVote(SettlementBuilderTestData.Cache cache, long pk, CastBallot castBallot) {
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setPk(pk);
		candidateVote.setCastBallot(castBallot);
		candidateVote.setCandidate(cache.candidateMap().get(candidateId));
		candidateVote.setVoteCategory(cache.voteCategoryMap().get(voteCategoryId));
		return candidateVote;
	}
}
