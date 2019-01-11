package no.valg.eva.admin.common.settlement.model;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SplitBallotCountTest {
	public static final int ZERO = 0;
	public static final int MODIFIED_BALLOT_COUNT_11 = 11;
	public static final int MODIFIED_BALLOT_COUNT_13 = 13;
	public static final int UNMODIFIED_BALLOT_COUNT_17 = 17;
	public static final int TOTAL_BALLOT_COUNT = MODIFIED_BALLOT_COUNT_11 + UNMODIFIED_BALLOT_COUNT_17;
	public static final int UNMODIFIED_BALLOT_COUNT_19 = 19;

	@Test
	public void getModifiedBallotCount_givenModifiedCount_returnsCorrectCount() throws Exception {
		assertThat(new SplitBallotCount(VO, MODIFIED_BALLOT_COUNT_11, ZERO).getModifiedBallotCount()).isEqualTo(MODIFIED_BALLOT_COUNT_11);
	}

	@Test
	public void setModifiedBallotCount_givenAnotherModifiedBallotCount_changesModifiedBallotCount() throws Exception {
		SplitBallotCount splitBallotCount = new SplitBallotCount(VO, MODIFIED_BALLOT_COUNT_11, UNMODIFIED_BALLOT_COUNT_17);
		splitBallotCount.setModifiedBallotCount(MODIFIED_BALLOT_COUNT_13);
		assertThat(splitBallotCount.getModifiedBallotCount()).isEqualTo(MODIFIED_BALLOT_COUNT_13);
	}

	@Test
	public void getUnmodifiedBallotCount_givenUnmodifiedCount_returnCorrectCount() throws Exception {
		assertThat(new SplitBallotCount(VO, ZERO, UNMODIFIED_BALLOT_COUNT_17).getUnmodifiedBallotCount()).isEqualTo(UNMODIFIED_BALLOT_COUNT_17);
	}

	@Test
	public void setUnmodifiedBallotCount_givenAnotherUnmodifiedBallotCount_changesUnmodifiedBallotCount() throws Exception {
		SplitBallotCount splitBallotCount = new SplitBallotCount(VO, MODIFIED_BALLOT_COUNT_11, UNMODIFIED_BALLOT_COUNT_17);
		splitBallotCount.setUnmodifiedBallotCount(UNMODIFIED_BALLOT_COUNT_19);
		assertThat(splitBallotCount.getUnmodifiedBallotCount()).isEqualTo(UNMODIFIED_BALLOT_COUNT_19);
	}

	@Test
	public void getBallotCount_givenBallotCount_returnsCorrectTotalBallotCount() throws Exception {
		SplitBallotCount splitBallotCount = new SplitBallotCount(VO, MODIFIED_BALLOT_COUNT_11, UNMODIFIED_BALLOT_COUNT_17);
		assertThat(splitBallotCount.getBallotCount()).isEqualTo(TOTAL_BALLOT_COUNT);
	}
}
