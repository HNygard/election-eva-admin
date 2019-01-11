package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.mockups.BallotMockups;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.mockup.VoteCountMockups;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;


public class ProtocolCountBuilderTest {

	public static final String AREA_NAME = "areaName";
	public static final String REPORTING_UNIT_NAME = "reportingUnitName";
	public static final LocalDate ELECTION_DAY_1 = LocalDate.parse("2015-09-06");
	public static final LocalDate ELECTION_DAY_2 = LocalDate.parse("2015-09-07");
	public static final LocalDate BEFORE_ELECTION_DAY_1 = LocalDate.parse("2005-09-05");
	public static final LocalDate AFTER_ELECTION_DAY_2 = LocalDate.parse("2005-09-08");
	public static final boolean MANUAL_COUNT_FALSE = false;
	private MvArea countingMvArea;
	private ProtocolCountBuilder protocolCountBuilder;
	private ProtocolCount protocolCount;
	private VoteCount protocolVoteCount;

	@BeforeMethod
	public void setUp() throws Exception {
		Municipality municipality = MunicipalityMockups.municipality(true);
		countingMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);

		protocolCountBuilder = buildProtocolCountBuilder(false);

		protocolCount = protocolCountBuilder.initCount(
				VO,
				new AreaPath(countingMvArea.getPath()),
				AREA_NAME,
				REPORTING_UNIT_NAME,
				false);

		Ballot blankBallot = BallotMockups.blankBallot();
		protocolVoteCount = VoteCountMockups.defaultProtocolVoteCount();
		protocolVoteCount.addNewBallotCount(blankBallot, 1, 0);
	}

	@Test
	public void applyProtocolVoteCountShouldSetId() {
		protocolCountBuilder.applyProtocolVoteCount(protocolVoteCount);
		protocolCount = protocolCountBuilder.build();
		String id = protocolCount.getId();
		assertThat(id).isNotNull();
	}

	@Test
	public void applyNullValuedProtocolVoteCountShouldSetNothing() {
		protocolCountBuilder.applyProtocolVoteCount(null);
		protocolCount = protocolCountBuilder.build();

		String id = protocolCount.getId();
		assertThat(id).isNull();
	}

	@Test
	public void applyProtocolVoteCountWithoutBallotCounts_always_setsBlankCountToNull() {
		protocolVoteCount.setBallotCountSet(new HashSet<>());
		protocolCountBuilder.applyProtocolVoteCount(protocolVoteCount);
		protocolCount = protocolCountBuilder.build();

		Integer blankBallots = protocolCount.getBlankBallotCount();
		assertThat(blankBallots).isEqualTo(null);
	}

	@Test
	public void applyProtocolVoteCount_gittBlankBallotCount_setterBlankCount() {
		protocolCountBuilder.applyProtocolVoteCount(protocolVoteCount);
		protocolCount = protocolCountBuilder.build();

		Integer blankBallots = protocolCount.getBlankBallotCount();
		assertThat(blankBallots).isEqualTo(1);
	}

	private ProtocolCountBuilder buildProtocolCountBuilder(boolean includeBallotCountForOtherContest) {
		return ProtocolCountBuilder.create(
				new AreaPath(countingMvArea.getPath()),
				AREA_NAME,
				REPORTING_UNIT_NAME,
				true,
				true,
				includeBallotCountForOtherContest, true);
	}

	@Test
	public void newProtocolCountBuilder_shouldSetManualCount() {
		ProtocolCountBuilder protocolCountBuilder = ProtocolCountBuilder.create(
				new AreaPath(countingMvArea.getPath()),
				AREA_NAME,
				REPORTING_UNIT_NAME,
				true,
				true,
				false, MANUAL_COUNT_FALSE);

		VoteCount voteCount = VoteCountMockups.defaultProtocolVoteCount();
		ProtocolCount protocolCount = protocolCountBuilder.applyProtocolVoteCount(voteCount).build();
		
		assertThat(protocolCount.isManualCount()).isFalse();
	}
	
	@Test
	public void applyVotings_givenVotingsWhereSomeIsNotOnElectionDay_createsProtocolCountWithDailyMarkOffCounts() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(false);
		protocolCountBuilder.applyElectionDays(asList(electionDay1(), electionDay2()));

		protocolCountBuilder.applyVotings(asList(voting1(), voting2(), voting3(), voting4(), voting5(), voting6()));
		ProtocolCount result = protocolCountBuilder.build();

		DailyMarkOffCounts dailyMarkOffCounts = result.getDailyMarkOffCounts();
		assertThat(dailyMarkOffCounts.getMarkOffCount()).isEqualTo(6);
		assertThat(dailyMarkOffCounts).containsExactly(dailyMarkOffCount1(), dailyMarkOffCount2());
	}

	@Test
	public void applyVotingsForAnotherContest_givenVotingsWhereSomeIsNotOnElectionDay_createsProtocolCountWithDailyMarkOffCounts() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(false);
		protocolCountBuilder.applyElectionDaysForOtherContests(asList(electionDay1(), electionDay2()));

		protocolCountBuilder.applyVotingsForAnotherContest(asList(voting1(), voting2(), voting3(), voting4(), voting5(), voting6()));
		ProtocolCount result = protocolCountBuilder.build();

		DailyMarkOffCounts dailyMarkOffCounts = result.getDailyMarkOffCountsForOtherContests();
		assertThat(dailyMarkOffCounts.getMarkOffCount()).isEqualTo(6);
		assertThat(dailyMarkOffCounts).containsExactly(dailyMarkOffCount1(), dailyMarkOffCount2());
	}

	@Test
	public void initCount_givenIncludeBallotCountForOtherContestTrue_returnsProtocolCountWithZeroBallotCountsForOtherContests() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(true);
		ProtocolCount protocolCount = protocolCountBuilder.build();
		assertThat(protocolCount.getBallotCountForOtherContests()).isZero();
	}

	@Test
	public void initCount_givenIncludeBallotCountForOtherContestFalse_returnsProtocolCountWithoutBallotCountsForOtherContests() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(false);
		ProtocolCount protocolCount = protocolCountBuilder.build();
		assertThat(protocolCount.getBallotCountForOtherContests()).isNull();
	}

	@Test
	public void initCount_givenIncludeBallotCountForOtherContestTrue_returnsProtocolCountWithZeroSpecialCovers() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(true);
		ProtocolCount protocolCount = protocolCountBuilder.build();
		assertThat(protocolCount.getSpecialCovers()).isZero();
		assertThat(protocolCount.getForeignSpecialCovers()).isNull();
		assertThat(protocolCount.getEmergencySpecialCovers()).isZero();
	}

	@Test
	public void initCount_givenIncludeBallotCountForOtherContestFalse_returnsProtocolCountWithZeroSpecialCovers() {
		ProtocolCountBuilder protocolCountBuilder = buildProtocolCountBuilder(false);
		ProtocolCount protocolCount = protocolCountBuilder.build();
		assertThat(protocolCount.getSpecialCovers()).isZero();
		assertThat(protocolCount.getForeignSpecialCovers()).isNull();
		assertThat(protocolCount.getEmergencySpecialCovers()).isZero();
	}

	private ElectionDay electionDay1() {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setDate(ELECTION_DAY_1);
		return electionDay;
	}

	private ElectionDay electionDay2() {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setDate(ELECTION_DAY_2);
		return electionDay;
	}

	private Voting voting1() {
		Voting voting = new Voting();
		voting.setCastTimestamp(BEFORE_ELECTION_DAY_1.toDateTimeAtCurrentTime());
		return voting;
	}

	private Voting voting2() {
		Voting voting = new Voting();
		voting.setCastTimestamp(ELECTION_DAY_1);
		return voting;
	}

	private Voting voting3() {
		Voting voting = new Voting();
		voting.setCastTimestamp(ELECTION_DAY_1);
		return voting;
	}

	private Voting voting4() {
		Voting voting = new Voting();
		voting.setCastTimestamp(ELECTION_DAY_2);
		return voting;
	}

	private Voting voting5() {
		Voting voting = new Voting();
		voting.setCastTimestamp(ELECTION_DAY_2);
		return voting;
	}

	private Voting voting6() {
		Voting voting = new Voting();
		voting.setCastTimestamp(AFTER_ELECTION_DAY_2);
		return voting;
	}

	private DailyMarkOffCount dailyMarkOffCount1() {
		return new DailyMarkOffCount(ELECTION_DAY_1, 2);
	}

	private DailyMarkOffCount dailyMarkOffCount2() {
		return new DailyMarkOffCount(ELECTION_DAY_2, 2);
	}
}

