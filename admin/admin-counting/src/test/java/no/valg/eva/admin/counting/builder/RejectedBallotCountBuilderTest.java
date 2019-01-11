package no.valg.eva.admin.counting.builder;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.BallotCount;

import org.testng.annotations.Test;

public class RejectedBallotCountBuilderTest {

	@Test
	public void testBuilder() {
		BallotCount ballotCount = new BallotCount();
		BallotRejection ballot = new BallotRejection();
		ballotCount.setBallotRejection(ballot);
		ballotCount.setUnmodifiedBallots(1);
		assertThat(new RejectedBallotCountBuilder().applyEntity(ballotCount).build().getCount()).isEqualTo(1);
	}

}
