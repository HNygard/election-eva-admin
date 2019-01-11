package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.BallotCount;

public class RejectedBallotCountBuilder {
	private final RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();

	public RejectedBallotCountBuilder applyEntity(final BallotCount ballotCountEntity) {
		BallotRejection ballotRejection = ballotCountEntity.getBallotRejection();
		this.rejectedBallotCount.setId(ballotRejection.getId());
		this.rejectedBallotCount.setName(ballotRejection.getName());
		this.rejectedBallotCount.setCount(ballotCountEntity.getUnmodifiedBallots());
		return this;
	}

	public RejectedBallotCount build() {
		return rejectedBallotCount;
	}
}
