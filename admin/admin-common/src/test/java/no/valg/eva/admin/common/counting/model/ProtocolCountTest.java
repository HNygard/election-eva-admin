package no.valg.eva.admin.common.counting.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;


public class ProtocolCountTest {

	private static final LocalDate ELECTION_DAY_1 = new LocalDate(2014, 1, 1);
	private static final LocalDate ELECTION_DAY_2 = new LocalDate(2014, 1, 2);

	@Test
	public void isForeignSpecialCoversEnabled_isTrue_whenElectronicMarkOffsFalse() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setElectronicMarkOffs(false);

		assertThat(protocolCount.isForeignSpecialCoversEnabled()).isTrue();
	}

	@Test
	public void isForeignSpecialCoversEnabled_isFalse_whenElectronicMarkOffsTrue() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setElectronicMarkOffs(true);

		assertThat(protocolCount.isForeignSpecialCoversEnabled()).isFalse();
	}

	@Test
	public void validate_whenOrdinaryContestAndBallotCountMatchesProtocolCount_isValid() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setOrdinaryBallotCount(200);

		protocolCount.validateForApproval();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.missing_comment")
	public void validate_whenOrdinaryContestAndBallotCountsDontMatchProtocolCount_isInvalid() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setOrdinaryBallotCount(201);

		protocolCount.validateForApproval();
	}

	@Test
	public void validate_whenBoroughContestAndBallotCountMatchesProtocolCount_isValid() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 50),
				createDailyMarkoffCount(ELECTION_DAY_2, 50))));
		protocolCount.setOrdinaryBallotCount(200);
		protocolCount.setBallotCountForOtherContests(100);

		protocolCount.validateForApproval();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.missing_comment")
	public void validate_whenBoroughContestAndBallotCountForOtherContestsDontMatchProtocolCountForOtherContests_isInvalid() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 50),
				createDailyMarkoffCount(ELECTION_DAY_2, 50))));
		protocolCount.setOrdinaryBallotCount(200);
		protocolCount.setBallotCountForOtherContests(101);

		protocolCount.validateForApproval();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.missing_comment")
	public void validate_whenBoroughContestAndBallotCountForOtherContestsDontMatchProtocolCount_isInvalid() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 50),
				createDailyMarkoffCount(ELECTION_DAY_2, 50))));
		protocolCount.setOrdinaryBallotCount(201);
		protocolCount.setBallotCountForOtherContests(100);

		protocolCount.validateForApproval();
	}

	@Test
	public void getTotalBallotCount_sumsVotesForOwnContestButNotForOtherContests() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setOrdinaryBallotCount(3);
		protocolCount.setBlankBallotCount(2);
		protocolCount.setQuestionableBallotCount(4);
		protocolCount.setBallotCountForOtherContests(8);

		assertThat(protocolCount.getTotalBallotCount()).isEqualTo(7);
	}

	@Test
	public void getTotalBallotCount_whenBlankQuestionableAndBallotsFromOtherContestsAreNull_usesZeroAsDefaultValueInSum() {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setOrdinaryBallotCount(1);
		protocolCount.setBlankBallotCount(null);
		protocolCount.setQuestionableBallotCount(null);
		protocolCount.setBallotCountForOtherContests(null);

		assertThat(protocolCount.getTotalBallotCount()).isEqualTo(1);
	}

	@Test
	public void isIncludeMarkOffsFromOtherContests_isTrue_whenDailyMarkOffsFromOtherContestsIsIncluded() {
		ProtocolCount protocolCount = createProtocolCount();
		protocolCount.setDailyMarkOffCountsForOtherContests(new DailyMarkOffCounts());

		assertTrue(protocolCount.isIncludeMarkOffsFromOtherContests());
	}

	@Test
	public void isIncludeMarkOffsFromOtherContests_isFalse_whenDailyMarkOffsFromOtherContestsIsNotIncluded() {
		ProtocolCount protocolCount = createProtocolCount();
		protocolCount.setDailyMarkOffCountsForOtherContests(null);

		assertFalse(protocolCount.isIncludeMarkOffsFromOtherContests());
	}

	@Test
	public void isIncludeBallotCountFromOtherContests_isTrue_whenBallotCountsFromOtherContestsIsIncluded() {
		ProtocolCount protocolCount = createProtocolCount();
		protocolCount.setBallotCountForOtherContests(1);

		assertTrue(protocolCount.isIncludeBallotCountFromOtherContests());
	}

	@Test
	public void isIncludeBallotCountFromOtherContests_isFalse_whenBallotCountsFromOtherContestsIsNotIncluded() {
		ProtocolCount protocolCount = createProtocolCount();
		protocolCount.setBallotCountForOtherContests(null);

		assertFalse(protocolCount.isIncludeBallotCountFromOtherContests());
	}

	@Test
	public void equals_forTwoProtocolCountsWithTheSameValues_returnsTrue() {
		ProtocolCount aProtocolCount = makeProtocolCount();
		ProtocolCount theSameProtocolCount = makeProtocolCount();

		boolean result = aProtocolCount.equals(theSameProtocolCount);

		assertThat(result).isTrue();
	}

	@Test
	public void equals_forTwoProtocolCountsWithDifferentId_returnsFalse() {
		ProtocolCount aProtocolCount = makeProtocolCount();
		ProtocolCount aDifferentProtocolCount = makeProtocolCount();
		aDifferentProtocolCount.setId("aDifferentId");

		boolean result = aProtocolCount.equals(aDifferentProtocolCount);

		assertThat(result).isFalse();
	}

	private ProtocolCount createProtocolCount() {
		ProtocolCount protocolCount = new ProtocolCount(null, null, null, null, true);
		protocolCount.setStatus(CountStatus.APPROVED);
		return protocolCount;
	}

	private DailyMarkOffCount createDailyMarkoffCount(LocalDate date, int markOffCount) {
		return new DailyMarkOffCount(date, markOffCount);
	}

	private ProtocolCount makeProtocolCount() {
		return new ProtocolCount("someId", new AreaPath("000000"), "someAreaName", "someReportingUnitName", true);
	}

	private DailyMarkOffCounts getDailyMarkOffCounts(List<DailyMarkOffCount> dailyMarkOffCountList) {
		return new DailyMarkOffCounts(dailyMarkOffCountList);
	}

	@Test
	public void isCommentRequired_whenProtocolCountWithDifferenceBetweenMarkOffsAndTotalBallotCount_returnsTrue() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setOrdinaryBallotCount(201);
		assertThat(protocolCount.isCommentRequired()).isTrue();
	}

	@Test
	public void isCommentRequired_whenProtocolCountWithNoDifferenceBetweenMarkOffsAndTotalBallotCount_returnsFalse() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setOrdinaryBallotCount(200);
		assertThat(protocolCount.isCommentRequired()).isFalse();
	}

	@Test
	public void isCommentRequired_whenBoroughProtocolCountWithDifferenceBetweenMarkOffsAndTotalBallotCount_returnsTrue() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 50),
				createDailyMarkoffCount(ELECTION_DAY_2, 50))));
		protocolCount.setOrdinaryBallotCount(200);
		protocolCount.setBallotCountForOtherContests(101);
		assertThat(protocolCount.isCommentRequired()).isTrue();
	}

	@Test
	public void isCommentRequired_whenBoroughProtocolCountWithNoDifferenceBetweenMarkOffsAndTotalBallotCount_returnsFalse() throws Exception {
		ProtocolCount protocolCount = makeProtocolCount();
		protocolCount.setDailyMarkOffCounts(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 100),
				createDailyMarkoffCount(ELECTION_DAY_2, 100))));
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(
				createDailyMarkoffCount(ELECTION_DAY_1, 50),
				createDailyMarkoffCount(ELECTION_DAY_2, 50))));
		protocolCount.setOrdinaryBallotCount(200);
		protocolCount.setBallotCountForOtherContests(100);
		assertThat(protocolCount.isCommentRequired()).isFalse();
	}
}

