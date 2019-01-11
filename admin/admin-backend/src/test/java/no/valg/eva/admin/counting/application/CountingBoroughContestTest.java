package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.counting.application.BoroughContestTestDataProvider.GAMLE_OSLO_AREA_PATH;
import static no.valg.eva.admin.counting.application.BoroughContestTestDataProvider.KAMPEN_SKOLE_AREA_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)

public class CountingBoroughContestTest extends AbstractJpaTestBase {

	private CountingService countingService;
	private BoroughContestTestDataProvider.BoroughContestObjects fixture;

	@DataProvider(name = "successfulCountCategories")
	public static Object[][] successfulCountCategories() {
		return new Object[][]
		{ { CountCategory.FO }, { CountCategory.FS }, { CountCategory.BF }, { CountCategory.VS }, { CountCategory.VB } };
	}

	@BeforeMethod(alwaysRun = true)
	public void initBackend() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();
		setupTransactionSynchronizationRegistry();

		countingService = backend.getCountingService();

		BoroughContestTestDataProvider testDataProvider = new BoroughContestTestDataProvider(backend, getEntityManager());
		fixture = testDataProvider.createBoroughContestInOslo();
	}

	@Test
	public void approveCount_withVO_shallSucceed() {
		ElectionPath electionPathForGamleOsloContest = BoroughContestTestDataProvider
				.getPathForBoroughContest(BoroughContestTestDataProvider.GAMLE_OSLO_BOROUGH_ID);
		CountContext countContext = new CountContext(electionPathForGamleOsloContest, CountCategory.VO);

		UserData opptellingsansvarligUserData = fixture.getOpptellingsansvarligUserData();
		Counts counts = countingService.getCounts(opptellingsansvarligUserData, countContext, KAMPEN_SKOLE_AREA_PATH);

		createAndApproveProtocolCount(opptellingsansvarligUserData, countContext, counts.getFirstProtocolCount());

		counts = countingService.getCounts(opptellingsansvarligUserData, countContext, KAMPEN_SKOLE_AREA_PATH);

		BallotCount ballotCount = createAndApprovePreliminaryCount(opptellingsansvarligUserData, countContext, counts);
		PreliminaryCount preliminaryCount;

		counts = countingService.getCounts(opptellingsansvarligUserData, countContext, KAMPEN_SKOLE_AREA_PATH);

		preliminaryCount = counts.getPreliminaryCount();
		assertThat(preliminaryCount.getBallotCounts()).containsExactly(ballotCount);

		createAndApproveFinalCount(opptellingsansvarligUserData, countContext, counts);
	}
	
	@Test(dataProvider = "successfulCountCategories")
	public void approveCount_withGivenCountCategory_succeeds(CountCategory countCategory) {
		ElectionPath electionPathForGamleOsloContest = BoroughContestTestDataProvider.getPathForBoroughContest(BoroughContestTestDataProvider.GAMLE_OSLO_BOROUGH_ID);
		CountContext countContext = new CountContext(electionPathForGamleOsloContest, countCategory);

		UserData opptellingsansvarligUserData = fixture.getOpptellingsansvarligUserData();

		Counts counts = countingService.getCounts(opptellingsansvarligUserData, countContext, GAMLE_OSLO_AREA_PATH);

		BallotCount ballotCount = createAndApprovePreliminaryCount(opptellingsansvarligUserData, countContext, counts);
		PreliminaryCount preliminaryCount;

		counts = countingService.getCounts(opptellingsansvarligUserData, countContext, GAMLE_OSLO_AREA_PATH);

		preliminaryCount = counts.getPreliminaryCount();
		assertThat(preliminaryCount.getBallotCounts()).containsExactly(ballotCount);

		createAndApproveFinalCount(opptellingsansvarligUserData, countContext, counts);
	}

	private ProtocolCount createAndApproveProtocolCount(UserData userData, CountContext countContext, ProtocolCount protocolCount) {
		protocolCount.setOrdinaryBallotCount(50);
		protocolCount.setQuestionableBallotCount(2);
		protocolCount.setBallotCountForOtherContests(3);
		protocolCount.setComment("Avviker fra antall manntallskryss, fordi det ikke er manntallskryss i testdataene");

		protocolCount.setDailyMarkOffCounts(createDailyMarkOffCounts(20, 35));
		protocolCount.setDailyMarkOffCountsForOtherContests(createDailyMarkOffCounts(1, 2));

		return countingService.approveCount(userData, countContext, protocolCount);

		// The reason we're not having an assert here is that an exception will be thrown when approveCount fails
	}

	private BallotCount createAndApprovePreliminaryCount(UserData userData, CountContext countContext, Counts counts) {
		PreliminaryCount preliminaryCount = counts.getPreliminaryCount();
		BallotCount ballotCount = new BallotCount("A", "@party[A].name", 51, 1);
		preliminaryCount.setBallotCounts(Arrays.asList(ballotCount));
		preliminaryCount.setComment("comment");
		countingService.approveCount(userData, countContext, preliminaryCount);
		return ballotCount;
	}

	private void createAndApproveFinalCount(UserData userData, CountContext countContext, Counts counts) {
		List<FinalCount> finalCounts = counts.getFinalCounts();
		assertThat(finalCounts).hasSize(1);
		
		FinalCount finalCount = finalCounts.get(0);
		BallotCount ballotCount = new BallotCount("A", "@party[A].name", 51, 1);
		finalCount.setBallotCounts(Arrays.asList(ballotCount));

		countingService.approveCount(userData, countContext, finalCount);
	}

	private DailyMarkOffCounts createDailyMarkOffCounts(int countDay1, int countDay2) {
		DailyMarkOffCounts dailyMarkOffCounts = new DailyMarkOffCounts();
		dailyMarkOffCounts.add(new DailyMarkOffCount(BoroughContestTestDataProvider.ELECTION_DAY_1, countDay1));
		dailyMarkOffCounts.add(new DailyMarkOffCount(BoroughContestTestDataProvider.ELECTION_DAY_2, countDay2));
		return dailyMarkOffCounts;
	}
}

