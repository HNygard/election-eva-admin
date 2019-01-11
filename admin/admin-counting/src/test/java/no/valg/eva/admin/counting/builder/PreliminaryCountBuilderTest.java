package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.domain.service.votecount.PreliminaryCountDataProvider;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


public class PreliminaryCountBuilderTest extends MockUtilsTestCase {
	public static final String COMMENT = "comment";
	public static final int BLANK_BALLOT_COUNT = 1;
	public static final int MARK_OFF_COUNT = 2;
	public static final int TOTAL_BALLOT_COUNT_FOR_OTHER_POLLING_DISTRICTS = 4;
	public static final String BALLOT_ID_1 = "B1";
	public static final String BALLOT_NAME_1 = "ballot1";
	public static final int UNMODIFIED_COUNT_1 = 11;
	public static final int MODIFIED_COUNT_1 = 21;
	public static final String BALLOT_ID_2 = "B2";
	public static final String BALLOT_NAME_2 = "ballot2";
	public static final int UNMODIFIED_COUNT_2 = 12;
	public static final int MODIFIED_COUNT_2 = 22;
	public static final String BALLOT_ID_3 = "B3";
	public static final String BALLOT_NAME_3 = "ballot3";
	public static final int UNMODIFIED_COUNT_3 = 13;
	public static final int MODIFIED_COUNT_3 = 23;
	public static final int QUESTIONABLE_BALLOT_COUNT = 5;
	public static final int LATE_VALIDATION_COVERS = 6;
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final String COUNT_ID = "FVO1";
	private static final String AREA_NAME = "areaName";
	private static final String REPORTING_UNIT_AREA_NAME = "reportingUnitAreaName";
	private static final boolean MANUAL_COUNT = true;
	private static final int EXPECTED_BALLOT_COUNT = 3;

	@Test
	public void constructor_always_setsBlankBallotCountCountToZero() {
		PreliminaryCount preliminaryCount = preliminaryCountBuilder(false, true).build();

		assertThat(preliminaryCount.getBlankBallotCount()).isEqualTo(0);
	}

	private PreliminaryCountBuilder preliminaryCountBuilder(boolean electronicMarkoff, boolean requiredProtocolCount) {
		return PreliminaryCountBuilder.create(VO, AREA_PATH, AREA_NAME, REPORTING_UNIT_AREA_NAME, MANUAL_COUNT,
				electronicMarkoff, requiredProtocolCount);
	}

	@Test
	public void applyDataProvider_givenDataProvider_setPreliminaryCountValues() {
		PreliminaryCount preliminaryCount = preliminaryCountBuilder(false, true).applyDataProvider(stubDataProvider()).build();
		assertThat(preliminaryCount).isEqualTo(expectedPreliminaryCount());
	}

	@Test
	public void applyElectionDays_withElectionsDays_createsDailyMarkoffs() {
		List<ElectionDay> days = Arrays.asList(
				electionDay(new LocalDate(2010, 1, 1)),
				electionDay(new LocalDate(2010, 1, 2)));
		PreliminaryCount count = preliminaryCountBuilder(false, true).applyDataProvider(stubDataProvider()).applyElectionDays(days).build();

		assertThat(count.getDailyMarkOffCounts()).isNotNull();
		assertThat(count.getDailyMarkOffCounts()).hasSize(2);
	}

	@Test
	public void applyManualContestVotings_withManualContestVotings_createsDailyMarkoffs() {
		List<ManualContestVoting> votings = Arrays.asList(
				manualContestVoting(new LocalDate(2010, 1, 1), 100),
				manualContestVoting(new LocalDate(2010, 1, 2), 300));
		PreliminaryCount count = preliminaryCountBuilder(false, true).applyDataProvider(stubDataProvider()).applyManualContestVotings(votings).build();

		assertThat(count.getDailyMarkOffCounts()).isNotNull();
		assertThat(count.getDailyMarkOffCounts()).hasSize(2);
		assertThat(count.getDailyMarkOffCounts().getMarkOffCount()).isEqualTo(400);
	}

	@Test
	public void applyVotings() {
		List<ElectionDay> days = Arrays.asList(
				electionDay(new LocalDate(2010, 1, 1)),
				electionDay(new LocalDate(2010, 1, 2)));
		List<Voting> votings = Arrays.asList(
				voting(new LocalDate(2010, 1, 1)),
				voting(new LocalDate(2010, 1, 2)),
				voting(new LocalDate(2010, 1, 2)),
				voting(new LocalDate(2010, 1, 3)),
				voting(new LocalDate(2010, 1, 3)));

		PreliminaryCount count = preliminaryCountBuilder(false, true).applyDataProvider(stubDataProvider()).applyElectionDays(days).applyVotings(votings)
				.build();

		assertThat(count.getDailyMarkOffCounts()).isNotNull();
		assertThat(count.getDailyMarkOffCounts()).hasSize(2);
		assertThat(count.getDailyMarkOffCounts().getMarkOffCount()).isEqualTo(5);
		assertThat(count.getDailyMarkOffCounts().get(0).getMarkOffCount()).isEqualTo(1);
		assertThat(count.getDailyMarkOffCounts().get(1).getMarkOffCount()).isEqualTo(2);
	}

	private Voting voting(LocalDate localDate) {
		Voting result = createMock(Voting.class);
		when(result.getCastTimestamp()).thenReturn(localDate.toDateTimeAtStartOfDay());
		return result;
	}

	private ManualContestVoting manualContestVoting(LocalDate date, int votings) {
		ManualContestVoting result = createMock(ManualContestVoting.class);
		when(result.getElectionDay().getDate()).thenReturn(date);
		when(result.getVotings()).thenReturn(votings);
		return result;
	}

	private ElectionDay electionDay(LocalDate date) {
		ElectionDay result = createMock(ElectionDay.class);
		when(result.getDate()).thenReturn(date);
		return result;
	}

	private PreliminaryCount expectedPreliminaryCount() {
		PreliminaryCount preliminaryCount = new PreliminaryCount(COUNT_ID, AREA_PATH, VO, AREA_NAME, REPORTING_UNIT_AREA_NAME, MANUAL_COUNT,
				BLANK_BALLOT_COUNT);
		preliminaryCount.setStatus(SAVED);
		preliminaryCount.setComment(COMMENT);
		preliminaryCount.setMarkOffCount(MARK_OFF_COUNT);
		preliminaryCount.setExpectedBallotCount(EXPECTED_BALLOT_COUNT);
		preliminaryCount.setTotalBallotCountForOtherPollingDistricts(TOTAL_BALLOT_COUNT_FOR_OTHER_POLLING_DISTRICTS);
		preliminaryCount.setBallotCounts(ballotCounts());
		preliminaryCount.setQuestionableBallotCount(QUESTIONABLE_BALLOT_COUNT);
		preliminaryCount.setLateValidationCovers(LATE_VALIDATION_COVERS);
		return preliminaryCount;
	}

	private List<BallotCount> ballotCounts() {
		ArrayList<BallotCount> ballotCounts = new ArrayList<>();
		Collections.addAll(
				ballotCounts,
				ballotCount(BALLOT_ID_1, BALLOT_NAME_1, UNMODIFIED_COUNT_1, MODIFIED_COUNT_1),
				ballotCount(BALLOT_ID_2, BALLOT_NAME_2, UNMODIFIED_COUNT_2, MODIFIED_COUNT_2),
				ballotCount(BALLOT_ID_3, BALLOT_NAME_3, UNMODIFIED_COUNT_3, MODIFIED_COUNT_3));
		return ballotCounts;
	}

	private BallotCount ballotCount(String id, String name, int unmodifiedCount, int modifiedCount) {
		return new BallotCount(id, name, unmodifiedCount, modifiedCount);
	}

	private PreliminaryCountDataProvider stubDataProvider() {
		PreliminaryCountDataProvider dataProvider = stub(PreliminaryCountDataProvider.class);
		when(dataProvider.id()).thenReturn(COUNT_ID);
		when(dataProvider.status()).thenReturn(SAVED);
		when(dataProvider.comment()).thenReturn(COMMENT);
		when(dataProvider.blankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(dataProvider.markOffCount()).thenReturn(MARK_OFF_COUNT);
		when(dataProvider.expectedBallotCount()).thenReturn(EXPECTED_BALLOT_COUNT);
		when(dataProvider.totalBallotCountForOtherPollingDistricts()).thenReturn(TOTAL_BALLOT_COUNT_FOR_OTHER_POLLING_DISTRICTS);
		when(dataProvider.ballotCounts()).thenReturn(ballotCounts());
		when(dataProvider.questionableBallotCount()).thenReturn(QUESTIONABLE_BALLOT_COUNT);
		when(dataProvider.lateValidationCovers()).thenReturn(LATE_VALIDATION_COVERS);
		return dataProvider;
	}
}

