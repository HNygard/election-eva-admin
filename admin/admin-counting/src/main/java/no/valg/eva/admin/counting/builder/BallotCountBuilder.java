package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;

public class BallotCountBuilder {
	private final BallotCount ballotCount = new BallotCount();

	public BallotCountBuilder applyEntity(no.valg.eva.admin.counting.domain.model.BallotCount ballotCountEntity) {
		Ballot ballot = ballotCountEntity.getBallot();
		this.ballotCount.setId(ballot.getId());
		this.ballotCount.setName(ballot.getAffiliation().getParty().getName());
		this.ballotCount.setModifiedCount(ballotCountEntity.getModifiedBallots());
		this.ballotCount.setUnmodifiedCount(ballotCountEntity.getUnmodifiedBallots());
		this.ballotCount.setBallotCountRef(new BallotCountRef(ballotCountEntity.getPk()));
		return this;
	}

	public BallotCountBuilder applyAffiliation(Affiliation affiliation) {
		String id = affiliation.getBallot().getId();
		String name = affiliation.getParty().getName();
		this.ballotCount.setId(id);
		this.ballotCount.setName(name);
		return this;
	}

	public BallotCountBuilder applyBallot(Ballot ballot) {
		return applyAffiliation(ballot.getAffiliation());
	}

	public BallotCount build() {
		return ballotCount;
	}
}
