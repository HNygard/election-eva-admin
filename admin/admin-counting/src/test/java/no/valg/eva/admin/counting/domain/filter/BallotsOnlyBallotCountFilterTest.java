package no.valg.eva.admin.counting.domain.filter;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.counting.domain.model.BallotCount;

import org.testng.annotations.Test;

public class BallotsOnlyBallotCountFilterTest {

	@Test
	public void filter_givenBallotCountWithBallot_returnsTrue() throws Exception {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallot(new Ballot());
		assertThat(BallotsOnlyBallotCountFilter.INSTANCE.filter(ballotCount)).isTrue();
	}

	@Test
	public void filter_givenBallotCountWithoutBallot_returnsFalse() throws Exception {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setBallot(null);
		assertThat(BallotsOnlyBallotCountFilter.INSTANCE.filter(ballotCount)).isFalse();
	}
}
