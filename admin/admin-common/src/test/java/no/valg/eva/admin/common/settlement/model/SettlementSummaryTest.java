package no.valg.eva.admin.common.settlement.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.testng.annotations.Test;


public class SettlementSummaryTest {

	public static final int BALLOT_COUNT_BALLOT_1_VO_MODIFIED = 11;
	public static final int BALLOT_COUNT_BALLOT_2_VO_MODIFIED = 47;
	public static final int BALLOT_COUNT_BALLOT_3_VO_MODIFIED = 97;
	public static final int BALLOT_COUNT_ORDINARY_VO_MODIFIED = BALLOT_COUNT_BALLOT_1_VO_MODIFIED + BALLOT_COUNT_BALLOT_2_VO_MODIFIED
			+ BALLOT_COUNT_BALLOT_3_VO_MODIFIED;
	public static final int BALLOT_COUNT_BALLOT_1_VO_UNMODIFIED = 19;
	public static final int BALLOT_COUNT_BALLOT_2_VO_UNMODIFIED = 61;
	public static final int BALLOT_COUNT_BALLOT_3_VO_UNMODIFIED = 107;
	public static final int BALLOT_COUNT_ORDINARY_VO_UNMODIFIED =
			BALLOT_COUNT_BALLOT_1_VO_UNMODIFIED + BALLOT_COUNT_BALLOT_2_VO_UNMODIFIED + BALLOT_COUNT_BALLOT_3_VO_UNMODIFIED;
	public static final int BALLOT_COUNT_BALLOT_1_VS_MODIFIED = 13;
	public static final int BALLOT_COUNT_BALLOT_2_VS_MODIFIED = 53;
	public static final int BALLOT_COUNT_BALLOT_3_VS_MODIFIED = 101;

	@Test
	public void getBallotCountSummaries_givenSettlementSummary_returnsIteratorWithCorrectSequence() throws Exception {
		List<BallotCountSummary> ballotCountSummaries = settlementSummary().getBallotCountSummaries();
		assertThat(ballotCountSummaries).containsSequence(
				ballotCountSummary1(),
				ballotCountSummary2(),
				ballotCountSummary3(),
				ordinaryBallotCountSummary(),
				blankBallotCountSummary(),
				rejectedBallotCountSummary1(),
				rejectedBallotCountSummary2(),
				rejectedBallotCountSummary3(),
				totalRejectedBallotCountSummary(),
				totalBallotCountSummary());
	}

	@Test
	public void getOrdinaryBallotCountSummary_givenSettlementSummary_returnsOrdinaryBallotCountSummary() throws Exception {
		assertThat(settlementSummary().getSumOfOrdinaryBallotCountSummaries()).isEqualTo(ordinaryBallotCountSummary());
	}

	@Test
	public void getBlankBallotCountSummary_givenSettlementSummary_returnsBlankBallotCountSummary() throws Exception {
		assertThat(settlementSummary().getBlankBallotCountSummary()).isEqualTo(blankBallotCountSummary());
	}

	@Test
	public void getTotalBallotCountSummary_givenSettlementSummary_returnsCorrectTotalBallotCountSummary() throws Exception {
		assertThat(settlementSummary().getTotalBallotCountSummary()).isEqualTo(totalBallotCountSummary());
	}

	private SettlementSummary settlementSummary() {
		return new SettlementSummary(countCategories(), ordinaryBallotCountSummaries(), blankBallotCountSummary(), rejectedBallotCountSummaries());
	}

	private List<CountCategory> countCategories() {
		return asList(VO, VS, VB);
	}

	private List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries() {
		return asList(ballotCountSummary1(), ballotCountSummary2(), ballotCountSummary3());
	}

	private BallotInfo ballotInfo1() {
		return ballotInfo("ballotId1", "ballotName1");
	}

	private BallotInfo ballotInfo2() {
		return ballotInfo("ballotId2", "ballotName2");
	}

	private BallotInfo ballotInfo3() {
		return ballotInfo("ballotId3", "ballotName3");
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary(BallotInfo ballotInfo, SplitBallotCount... ballotCounts) {
		return new BallotCountSummary<>(ballotInfo, asList(ballotCounts));
	}

	private BallotCountSummary<SimpleBallotCount> ballotCountSummary(BallotInfo ballotInfo, SimpleBallotCount... ballotCounts) {
		return new BallotCountSummary<>(ballotInfo, asList(ballotCounts));
	}

	private BallotInfo ballotInfo(String ballotId, String ballotName) {
		return new BallotInfo(ballotId, ballotName);
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary1() {
	return ballotCountSummary(
				ballotInfo1(),
				splitBallotCount(VO, BALLOT_COUNT_BALLOT_1_VO_MODIFIED, BALLOT_COUNT_BALLOT_1_VO_UNMODIFIED),
				splitBallotCount(VS, BALLOT_COUNT_BALLOT_1_VS_MODIFIED, 23),
				splitBallotCount(VB, 17, 29));
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary2() {
		return ballotCountSummary(
				ballotInfo2(),
				splitBallotCount(VO, BALLOT_COUNT_BALLOT_2_VO_MODIFIED, BALLOT_COUNT_BALLOT_2_VO_UNMODIFIED),
				splitBallotCount(VS, BALLOT_COUNT_BALLOT_2_VS_MODIFIED, 67),
				splitBallotCount(VB, 59, 71));
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary3() {
		return ballotCountSummary(
				ballotInfo3(),
				splitBallotCount(VO, BALLOT_COUNT_BALLOT_3_VO_MODIFIED, BALLOT_COUNT_BALLOT_3_VO_UNMODIFIED),
				splitBallotCount(VS, BALLOT_COUNT_BALLOT_3_VS_MODIFIED, 109),
				splitBallotCount(VB, 103, 113));
	}

	private BallotCountSummary<SplitBallotCount> ordinaryBallotCountSummary() {
		return ballotCountSummary(
				ballotInfo(null, "@count.ballot.totalOrdinary"),
				splitBallotCount(VO, BALLOT_COUNT_ORDINARY_VO_MODIFIED, BALLOT_COUNT_ORDINARY_VO_UNMODIFIED),
				splitBallotCount(VS, 167, 199),
				splitBallotCount(VB, 179, 213));
	}

	private BallotCountSummary<SimpleBallotCount> blankBallotCountSummary() {
		return ballotCountSummary(
				ballotInfo(EvoteConstants.BALLOT_BLANK, "BlankBallots"),
				simpleBallotCount(VO, 9),
				simpleBallotCount(VS, 14),
				simpleBallotCount(VB, 18));
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary1() {
		return ballotCountSummary(
				rejectedBallotInfo1(),
				simpleBallotCount(VO, 3),
				simpleBallotCount(VS, 5),
				simpleBallotCount(VB, 7));
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary2() {
		return ballotCountSummary(
				rejectedBallotInfo2(),
				simpleBallotCount(VO, 11),
				simpleBallotCount(VS, 13),
				simpleBallotCount(VB, 17));
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary3() {
		return ballotCountSummary(
				rejectedBallotInfo3(),
				simpleBallotCount(VO, 19),
				simpleBallotCount(VS, 23),
				simpleBallotCount(VB, 29));
	}

	private BallotCountSummary totalRejectedBallotCountSummary() {
		return ballotCountSummary(
				ballotInfo(null, "@count.ballot.totalRejected"),
				simpleBallotCount(VO, 3 + 11 + 19),
				simpleBallotCount(VS, 5 + 13 + 23),
				simpleBallotCount(VB, 7 + 17 + 29));
	}

	private BallotCountSummary totalBallotCountSummary() {
		return ballotCountSummary(
				ballotInfo(null, "@count.ballot.total"),
				simpleBallotCount(VO, 155 + 187 + 2 + 7 + 3 + 11 + 19),
				simpleBallotCount(VS, 167 + 199 + 3 + 11 + 5 + 13 + 23),
				simpleBallotCount(VB, 179 + 213 + 5 + 13 + 7 + 17 + 29));
	}

	private BallotInfo rejectedBallotInfo1() {
		return ballotInfo("rejectedBallotId1", "rejectedBallotName1");
	}

	private BallotInfo rejectedBallotInfo2() {
		return ballotInfo("rejectedBallotId2", "rejectedBallotName2");
	}

	private BallotInfo rejectedBallotInfo3() {
		return ballotInfo("rejectedBallotId3", "rejectedBallotName3");
	}

	private List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries() {
		return asList(rejectedBallotCountSummary1(), rejectedBallotCountSummary2(), rejectedBallotCountSummary3());
	}

	private SplitBallotCount splitBallotCount(CountCategory countCategory, int modifiedBallotCount, int unmodifiedBallotCount) {
		return new SplitBallotCount(countCategory, modifiedBallotCount, unmodifiedBallotCount);
	}

	private SimpleBallotCount simpleBallotCount(CountCategory countCategory, int ballotCount) {
		return new SimpleBallotCount(countCategory, ballotCount);
	}
}

