package no.valg.eva.admin.common.counting.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class PreliminaryCountTest {
	private static final String DEFAULT_ID = "EVO1";
	private static final AreaPath DEFAULT_AREA_PATH = new AreaPath("730071.47.01.0101.010100.0001");
	private static final String DEFAULT_AREA_NAME = "Halden";
	private static final String DEFAULT_REPORTING_UNIT_AREA_NAME = DEFAULT_AREA_NAME;
	private static final boolean MANUAL_COUNT_FALSE = false;
	private static final Integer DEFAULT_BLANK_BALLOT_COUNT = 0;

	private PreliminaryCount defaultCount() {
		PreliminaryCount count = count(
				VO,
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				MANUAL_COUNT_FALSE,
				DEFAULT_BLANK_BALLOT_COUNT
				);
		count.setBallotCounts(new ArrayList<>());
		count.getBallotCounts().add(new BallotCount());
		return count;
	}

	private PreliminaryCount defaultCount(CountCategory category) {
		PreliminaryCount count = count(
				category,
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				MANUAL_COUNT_FALSE,
				DEFAULT_BLANK_BALLOT_COUNT
				);
		count.setBallotCounts(new ArrayList<>());
		count.getBallotCounts().add(new BallotCount());
		return count;
	}

	private PreliminaryCount count(
			CountCategory category,
			String id,
			AreaPath areaPath,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			Integer blankBallotCount) {

		return new PreliminaryCount(id, areaPath, category, areaName, reportingUnitAreaName, manualCount, blankBallotCount);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "ILLEGAL_ARGUMENT_BLANK_IS_NULL")
	public void validate_whenBlankBallotCountsIsNull_throwsException() throws Exception {
		PreliminaryCount count = defaultCount();
		count.setBlankBallotCount(null);

		count.validate();
	}

	@Test
	public void getTotalBallotCountForAllPollingDistricts_whenBallotCountForOtherPollingDistricts_returnForAll() throws Exception {
		int totalBallotCountForOtherPollingDistricts = 57;
		int blankBallotCount = 3;
		int questionableBallotCount = 7;
		int aModifiedCount = 33;
		int aUnmodifiedCount = 44;
		PreliminaryCount count = defaultCount();
		count.setTotalBallotCountForOtherPollingDistricts(totalBallotCountForOtherPollingDistricts);
		count.setBlankBallotCount(blankBallotCount);
		count.setQuestionableBallotCount(questionableBallotCount);
		count.setBallotCounts(singletonList(ballotCount("BALLOT", aModifiedCount, aUnmodifiedCount)));

		Integer totalBallotCountForAllPollingDistricts = count.getTotalBallotCountForAllPollingDistricts();

		int expectedTotalBallotCountForAllPollingDistricts = 144;
		assertThat(totalBallotCountForAllPollingDistricts).isEqualTo(expectedTotalBallotCountForAllPollingDistricts);
	}

	@Test
	public void getTotalBallotCountForAllPollingDistricts_whenNoTotalBallotCountForOtherPollingDistricts_returnsNull() throws Exception {
		PreliminaryCount count = defaultCount();
		assertThat(count.getTotalBallotCountForAllPollingDistricts()).isNull();
	}

	private BallotCount ballotCount(String ballotCountId, int modifiedCount, int unmodifiedCount) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setId(ballotCountId);
		ballotCount.setModifiedCount(modifiedCount);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		return ballotCount;
	}

	@Test
	public void setExpectedBallotCount_whenNullAndPreviouslySet_setZeroValue() throws Exception {
		int initialExpectedBallotCount = 1;
		int blankExpectedBallotCount = 0;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setExpectedBallotCount(initialExpectedBallotCount);
		preliminaryCount.setExpectedBallotCount(null);

		assertThat(preliminaryCount.getExpectedBallotCount()).isEqualTo(blankExpectedBallotCount);
	}

	@Test
	public void setExpectedBallotCount_whenNullAndNotPreviouslySet_setNullValue() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setExpectedBallotCount(null);

		assertThat(preliminaryCount.getExpectedBallotCount()).isNull();
	}

	@Test
	public void setExpectedBallotCount_givenValue_setValue() throws Exception {
		int expectedBallotCount = 1;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setExpectedBallotCount(expectedBallotCount);

		assertThat(preliminaryCount.getExpectedBallotCount()).isEqualTo(expectedBallotCount);
	}

	@Test
	public void isCommentRequired_givenMarkOffCountDifference_returnsTrue() throws Exception {
		int markOffCount = 7;
		int blankBallotCount = 3;
		int aBallotCount = 5;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(aBallotCount);

		assertThat(preliminaryCount.isCommentRequired()).isTrue();
	}

	@Test
	public void isCommentRequired_givenNoMarkOffCountDifference_returnsFalse() throws Exception {
		int markOffCount = 7;
		int blankBallotCount = 3;
		int unmodifiedCount = 4;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(unmodifiedCount);

		assertThat(preliminaryCount.isCommentRequired()).isFalse();
	}

	@Test
	public void isCommentRequired_givenExpectedBallotCountAndMarkOffCountDifference_returnsTrue() throws Exception {
		int expectedBallotCount = 10;
		int markOffCount = 30;
		int blankBallotCount = 3;
		int aBallotCount = 7;
		int totalBallotCountForOtherPollingDistricts = 21;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setExpectedBallotCount(expectedBallotCount);
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(aBallotCount);
		preliminaryCount.setTotalBallotCountForOtherPollingDistricts(totalBallotCountForOtherPollingDistricts);

		assertThat(preliminaryCount.isCommentRequired()).isTrue();
	}

	@Test
	public void isCommentRequired_givenExpectedBallotCountAndNoMarkOffCountDifference_returnsFalse() throws Exception {
		int expectedBallotCount = 10;
		int markOffCount = 30;
		int blankBallotCount = 3;
		int aBallotCount = 7;
		int totalBallotCountForOtherPollingDistricts = 20;

		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setExpectedBallotCount(expectedBallotCount);
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(aBallotCount);
		preliminaryCount.setTotalBallotCountForOtherPollingDistricts(totalBallotCountForOtherPollingDistricts);

		assertThat(preliminaryCount.isCommentRequired()).isFalse();
	}

	@Test
	public void getMarkOffCountDifferenceWithTotalBallotCount_whenFoPreliminaryCount_returnsDifferencePlusLateValidationCovers() throws Exception {
		int markOffCount = 21;
		int lateValidationCovers = 11;
		int blankBallotCount = 8;
		int aBallotCount = 12;
		int difference = blankBallotCount + aBallotCount - markOffCount;

		PreliminaryCount preliminaryCount = defaultCount(FO);
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setLateValidationCovers(lateValidationCovers);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(aBallotCount);

		assertThat(preliminaryCount.getMarkOffCountDifferenceWithTotalBallotCount()).isEqualTo(difference + lateValidationCovers);
	}

	@Test
	public void getMarkOffCountDifferenceWithTotalBallotCount_whenFsPreliminaryCount_returnsDifferenceMinusLateValidationCovers() throws Exception {
		int markOffCount = 21;
		int lateValidationCovers = 11;
		int blankBallotCount = 8;
		int aBallotCount = 12;
		int difference = blankBallotCount + aBallotCount - markOffCount;

		PreliminaryCount preliminaryCount = defaultCount(FS);
		preliminaryCount.setMarkOffCount(markOffCount);
		preliminaryCount.setLateValidationCovers(lateValidationCovers);
		preliminaryCount.setBlankBallotCount(blankBallotCount);
		preliminaryCount.getBallotCounts().get(0).setUnmodifiedCount(aBallotCount);

		assertThat(preliminaryCount.getMarkOffCountDifferenceWithTotalBallotCount()).isEqualTo(difference - lateValidationCovers);
	}

	@Test(
			expectedExceptions = ValidateException.class,
			expectedExceptionsMessageRegExp = PreliminaryCount.COUNT_ERROR_VALIDATION_NEGATIVE_QUESTIONABLE_BALLOT_COUNT)
	public void validate_whenNegativeQuestionableBallotCount_throwValidateException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setQuestionableBallotCount(-1);
		preliminaryCount.validate();
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "ILLEGAL_ARGUMENT_BALLOT_COUNTS_IS_NULL")
	public void validate_whenNullBallotCounts_throwsIllegalArgumentException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setBallotCounts(null);
		preliminaryCount.validate();
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "ILLEGAL_ARGUMENT_BALLOT_COUNTS_IS_EMPTY")
	public void validate_whenEmptyBallotCounts_throwsIllegalArgumentException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.getBallotCounts().clear();
		preliminaryCount.validate();
	}

	@Test
	public void validate_whenNoErrors_returnWithoutException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.validate();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.negative.ballot_count")
	public void validate_gittCountMedNegativBallotCount_kasterException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		List<BallotCount> ballotCounts = asList(ballotCount("AP", 0, -1), ballotCount("DEM", 0, 0));
		preliminaryCount.setBallotCounts(ballotCounts);

		preliminaryCount.validate();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = PreliminaryCount.COUNT_ERROR_VALIDATION_MISSING_COMMENT)
	public void validateForApproval_whenCommentRequired_throwValidateException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.setMarkOffCount(1);
		preliminaryCount.validateForApproval();
	}

	@Test
	public void validateForApproval_whenNoCommentRequired_returnWithoutException() throws Exception {
		PreliminaryCount preliminaryCount = defaultCount();
		preliminaryCount.validateForApproval();
	}

	@Test(dataProvider = "useDailyMarkOffCounts")
	public void useDailyMarkOffCounts_withDataProvider_verifyExpected(CountCategory category, boolean electronicMarkOffs, boolean requiredProtocolCount,
			boolean expected) throws Exception {
		PreliminaryCount count = defaultCount(category);
		count.setRequiredProtocolCount(requiredProtocolCount);
		count.setElectronicMarkOffs(electronicMarkOffs);

		assertThat(count.useDailyMarkOffCounts()).isEqualTo(expected);
	}

	@DataProvider(name = "useDailyMarkOffCounts")
	public Object[][] useDailyMarkOffCounts() {
		return new Object[][] {
				{ CountCategory.BF, true, true, false },
				{ CountCategory.VO, false, false, true },
				{ CountCategory.VO, false, true, false },
				{ CountCategory.VO, true, false, false },
				{ CountCategory.VO, true, true, false }
		};
	}
}

