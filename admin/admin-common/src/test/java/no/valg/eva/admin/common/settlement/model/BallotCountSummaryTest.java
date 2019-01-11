package no.valg.eva.admin.common.settlement.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;


public class BallotCountSummaryTest {

	@Test
	public void getBallotCount_givenCountCategory_returnsBallotCount() throws Exception {
		BallotCount ballotCount = new SplitBallotCount(VO, 0, 0);
		BallotCountSummary ballotCountSummary = ballotCountSummary(ballotCount);
		assertThat(ballotCountSummary.getBallotCount(VO)).isSameAs(ballotCount);
	}

	@Test
	public void getBallotCount_givenUnknownCountCategory_returnsNull() throws Exception {
		BallotCount ballotCount = new SplitBallotCount(VO, 0, 0);
		BallotCountSummary ballotCountSummary = ballotCountSummary(ballotCount);
		assertThat(ballotCountSummary.getBallotCount(VS)).isNull();
	}

	private BallotCountSummary ballotCountSummary(BallotCount... ballotCounts) {
		return new BallotCountSummary<>(new BallotInfo(null, null), asList(ballotCounts));
	}

	@Test
	public void getTotalBallotCount_givenBallotCountSummaryWithSplitBallotCounts_returnsSplitTotal() throws Exception {
		BallotCount ballotCount1 = new SplitBallotCount(VO, 11, 19);
		BallotCount ballotCount2 = new SplitBallotCount(VS, 13, 23);
		BallotCount ballotCount3 = new SplitBallotCount(VB, 17, 29);
		BallotCountSummary ballotCountSummary = ballotCountSummary(ballotCount1, ballotCount2, ballotCount3);
		assertThat(ballotCountSummary.getTotalBallotCount()).isEqualTo(new SplitBallotCount(null, 41, 71));
	}

	@Test
	public void getTotalBallotCount_givenBallotCountSummaryWithSimpleBallotCounts_returnsSimpleTotal() throws Exception {
		BallotCount ballotCount1 = new SimpleBallotCount(VO, 11);
		BallotCount ballotCount2 = new SimpleBallotCount(VS, 13);
		BallotCount ballotCount3 = new SimpleBallotCount(VB, 17);
		BallotCountSummary ballotCountSummary = ballotCountSummary(ballotCount1, ballotCount2, ballotCount3);
		assertThat(ballotCountSummary.getTotalBallotCount()).isEqualTo(new SimpleBallotCount(null, 41));
	}

	@Test
	public void getTotalBallotCount_givenEmptyBallotCountSummary_returnsNull() throws Exception {
		assertThat(ballotCountSummary().getTotalBallotCount()).isNull();
	}

	@Test
	public void getBallotCounts_givenEmptyBallotCountSummary_returnsEmptyList() throws Exception {
		BallotCountSummary ballotCountSummary = new BallotCountSummary(new BallotInfo(null, null));
		assertThat(ballotCountSummary.getBallotCounts()).isEmpty();
	}
}

