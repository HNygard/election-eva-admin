package no.valg.eva.admin.common.counting.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.List;
import java.util.Map;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



public class FinalCountTest {
	private static final String DEFAULT_ID = "EVO1";
	private static final AreaPath DEFAULT_AREA_PATH = new AreaPath("730071.47.01.0101.010100.0001");
	private static final String DEFAULT_AREA_NAME = "Halden";
	private static final String DEFAULT_REPORTING_UNIT_AREA_NAME = DEFAULT_AREA_NAME;
	private static final boolean MANUAL_COUNT_FALSE = false;
	private static final Integer DEFAULT_BLANK_BALLOT_COUNT = 0;

	private FinalCount count;

	@BeforeMethod
	public void setUp() throws Exception {
		count = defaultCount();
	}

	private FinalCount defaultCount() {
		return defaultCount(DEFAULT_ID);
	}

	private FinalCount defaultCount(String id) {
		FinalCount count = count(
				id,
				DEFAULT_AREA_PATH,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				MANUAL_COUNT_FALSE,
				DEFAULT_BLANK_BALLOT_COUNT);
		List<BallotCount> ballotCounts = asList(ballotCount("AP", 0, 0), ballotCount("DEM", 0, 0));
		count.setBallotCounts(ballotCounts);
		List<RejectedBallotCount> rejectedBallotCounts = asList(rejectedBallotCount("VA", 0), rejectedBallotCount("VB", 0));
		count.setRejectedBallotCounts(rejectedBallotCounts);
		return count;
	}

	private FinalCount count(
			String id,
			AreaPath areaPath,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			Integer blankBallotCount) {

		return new FinalCount(id, areaPath, VO, areaName, reportingUnitAreaName, manualCount, blankBallotCount);
	}

	@Test
	public void getModifiedDateShouldReturnWhatSetModifiedDateSets() throws Exception {
		DateTime modifiedDate = DateTime.now();
		count.setModifiedDate(modifiedDate);

		assertThat(count.getModifiedDate()).isEqualTo(modifiedDate);
	}

	@Test
	public void isRevokedShouldReturnTrueWhenStatusIsRejected() throws Exception {
		count.setStatus(CountStatus.REVOKED);

		boolean rejected = count.isRevoked();

		assertThat(rejected).isTrue();
	}

	@Test
	public void isRevokedShouldReturnFalseWhenStatusIsNotRejected() throws Exception {
		count.setStatus(CountStatus.APPROVED);

		boolean rejected = count.isRevoked();

		assertThat(rejected).isFalse();
	}

	@Test
	public void getIndex_noId_returnZero() throws Exception {
		assertThat(defaultCount(null).getIndex()).isEqualTo(1);
	}

	@Test
	public void getIndexShouldReturnNumberInId() throws Exception {
		count.setId("EVO2");

		int index = count.getIndex();

		assertThat(index).isEqualTo(2);
	}

	@Test
	public void getBallotCountsShouldReturnWhatSetBallotCountsSets() throws Exception {
		List<BallotCount> ballotCounts = asList(ballotCount("AP", 0, 0), ballotCount("DEM", 0, 0));

		count.setBallotCounts(ballotCounts);

		assertThat(count.getBallotCounts()).isSameAs(ballotCounts);
	}

	private BallotCount ballotCount(String id, int unmodifiedCount, int modifiedCount) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setId(id);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		return ballotCount;
	}

	@Test
	public void getBallotCountMap() throws Exception {
		BallotCount apBallotCount = ballotCount("AP", 0, 0);
		BallotCount demBallotCount = ballotCount("DEM", 0, 0);
		count.setBallotCounts(asList(apBallotCount, demBallotCount));

		Map<String, BallotCount> ballotCountMap = count.getBallotCountMap();

		assertThat(ballotCountMap).contains(entry("AP", apBallotCount), entry("DEM", demBallotCount));
	}

	@Test
	public void getRejectedBallotCountsShouldReturnWhatSetRejectedBallotCountsSets() throws Exception {
		List<RejectedBallotCount> rejectedBallotCounts = asList(rejectedBallotCount("VA", 0), rejectedBallotCount("VB", 0));

		count.setRejectedBallotCounts(rejectedBallotCounts);

		assertThat(count.getRejectedBallotCounts()).isSameAs(rejectedBallotCounts);
	}

	private RejectedBallotCount rejectedBallotCount(String id, int count) {
		RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();
		rejectedBallotCount.setId(id);
		rejectedBallotCount.setCount(count);
		return rejectedBallotCount;
	}

	@Test
	public void getRejectedBallotCountMap() throws Exception {
		RejectedBallotCount vaRejectedBallotCount = rejectedBallotCount("VA", 0);
		RejectedBallotCount vbRejectedBallotCount = rejectedBallotCount("VB", 0);
		List<RejectedBallotCount> rejectedBallotCounts = asList(vaRejectedBallotCount, vbRejectedBallotCount);
		count.setRejectedBallotCounts(rejectedBallotCounts);

		Map<String, RejectedBallotCount> rejectedBallotCountMap = count.getRejectedBallotCountMap();

		assertThat(rejectedBallotCountMap).contains(entry("VA", vaRejectedBallotCount), entry("VB", vbRejectedBallotCount));
	}

	@Test
	public void isModifiedBallotsProcessedShouldReturnFalseWhenSetModifiedBallotsProcessedSetsFalseValue() throws Exception {
		count.setModifiedBallotsProcessed(false);

		assertThat(count.isModifiedBallotsProcessed()).isFalse();
	}

	@Test
	public void isModifiedBallotsProcessedShouldReturnTrueWhenSetModifiedBallotsProcessedSetsTrueValue() throws Exception {
		count.setModifiedBallotsProcessed(true);

		assertThat(count.isModifiedBallotsProcessed()).isTrue();
	}

	@Test
	public void isRejectedBallotsProcessedShouldReturnFalseWhenSetRejectedBallotsProcessedSetsFalseValue() throws Exception {
		count.setRejectedBallotsProcessed(false);

		assertThat(count.isRejectedBallotsProcessed()).isFalse();
	}

	@Test
	public void isRejectedBallotsProcessedShouldReturnTrueWhenSetRejectedBallotsProcessedSetsTrueValue() throws Exception {
		count.setRejectedBallotsProcessed(true);

		assertThat(count.isRejectedBallotsProcessed()).isTrue();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "ballot counts cannot be null")
	public void validateShouldThrowExceptionWhenBallotCountsIsNull() throws Exception {
		count.setBallotCounts(null);

		count.validate();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "ballot counts cannot be empty")
	public void validateShouldThrowExceptionWhenBallotCountsIsEmpty() throws Exception {
		List<BallotCount> ballotCounts = emptyList();
		count.setBallotCounts(ballotCounts);

		count.validate();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.negative.(un)?modified_ballot_count",
			dataProvider = "negativeBallotCounts")
	public void validate_gittCountMedNegativBallotCount_kasterException(int urettetAntall, int rettetAntall) throws Exception {
		List<BallotCount> ballotCounts = asList(ballotCount("AP", 0, urettetAntall), ballotCount("DEM", rettetAntall, 0));
		count.setBallotCounts(ballotCounts);
		
		count.validate();
	}

	@DataProvider
	private Object[][] negativeBallotCounts() {
		return new Object[][] {
				new Object[] { -1, 0 },
				new Object[] { 0, -1 }
		};
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "rejected ballot counts cannot be null")
	public void validateShouldThrowExceptionWhenRejectedBallotCountsIsNull() throws Exception {
		count.setRejectedBallotCounts(null);

		count.validate();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "rejected ballot counts cannot be empty")
	public void validateShouldThrowExceptionWhenRejectedBallotCountsIsEmpty() throws Exception {
		List<RejectedBallotCount> rejectedBallotCounts = emptyList();
		count.setRejectedBallotCounts(rejectedBallotCounts);

		count.validate();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "blank ballot counts cannot be null")
	public void validate_whenBlankBallotCountsIsNull_throwsException() throws Exception {
		count.setBlankBallotCount(null);

		count.validate();
	}

	@Test
	public void getOrdinaryBallotCountShouldReturnSumOfBallotCounts() throws Exception {
		count.setBallotCounts(asList(ballotCount("AP", 30, 50), ballotCount("DEM", 10, 20), ballotCount("FRP", 5, 7)));

		int ordinaryBallotCount = count.getOrdinaryBallotCount();

		assertThat(ordinaryBallotCount).isEqualTo(122);
	}

	@Test
	public void getModifiedBallotCountShouldReturnSumOfModifiedBallotCounts() throws Exception {
		count.setBallotCounts(asList(ballotCount("AP", 30, 50), ballotCount("DEM", 10, 20), ballotCount("FRP", 5, 7)));

		int modifiedBallotCount = count.getModifiedBallotCount();

		assertThat(modifiedBallotCount).isEqualTo(77);
	}

	@Test
	public void getUnmodifiedBallotCountShouldReturnSumOfUnmodifiedBallotCounts() throws Exception {
		count.setBallotCounts(asList(ballotCount("AP", 30, 50), ballotCount("DEM", 10, 20), ballotCount("FRP", 5, 7)));

		int unmodifiedBallotCount = count.getUnmodifiedBallotCount();

		assertThat(unmodifiedBallotCount).isEqualTo(45);
	}

	@Test
	public void getTotalRejectedBallotCountShouldReturnSumOfRejectedBallotCounts() throws Exception {
		count.setRejectedBallotCounts(asList(rejectedBallotCount("VA", 3), rejectedBallotCount("VB", 5), rejectedBallotCount("VC", 7), rejectedBallotCount("VD", 11)));

		int totalRejectedBallotCount = count.getTotalRejectedBallotCount();

		assertThat(totalRejectedBallotCount).isEqualTo(26);
	}

	@Test
	public void getTotalBallotCountShouldReturnSumOfOrdinaryBallotCountsAndBlankBallotCountsAndRejectedBallotCounts() throws Exception {
		count.setBlankBallotCount(11);
		count.setBallotCounts(asList(ballotCount("AP", 30, 50), ballotCount("DEM", 10, 20), ballotCount("FRP", 5, 7)));
		count.setRejectedBallotCounts(asList(rejectedBallotCount("VA", 3), rejectedBallotCount("VB", 5), rejectedBallotCount("VC", 7), rejectedBallotCount("VD", 11)));

		int totalBallotCount = count.getTotalBallotCount();

		assertThat(totalBallotCount).isEqualTo(159);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = "final count does not have questionable ballot count")
	public void getQuestionableBallotCount_throwsUnsupported() throws Exception {
		count.getQuestionableBallotCount();
	}

	@Test
	public void isApproved() throws Exception {
		count.setStatus(CountStatus.APPROVED);
		assertThat(count.isApproved()).isTrue();
		count.setStatus(CountStatus.TO_SETTLEMENT);
		assertThat(count.isApproved()).isTrue();
		count.setStatus(CountStatus.NEW);
		assertThat(count.isApproved()).isFalse();
		count.setStatus(CountStatus.SAVED);
		assertThat(count.isApproved()).isFalse();
		count.setStatus(CountStatus.REVOKED);
		assertThat(count.isApproved()).isFalse();
	}
}

