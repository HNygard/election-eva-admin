package no.valg.eva.admin.settlement.domain.builder;

import static java.lang.String.format;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.baseline;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.test.SettlementBuilderTestData;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class SettlementBuilderFactoryTest {

	@DataProvider
	public static Object[][] testDataFileNames() {
		return new Object[][] {
				{ "test_data_simple_test_personal_and_writein.json" },
				{ "test_data_vestfold_fylkestingsvalget.json" },
				{ "test_data_horten_kommunestyrevalget.json" }
		};
	}

	@Test(dataProvider = "testDataFileNames")
	public void settlementBuilderFactory_givenTestData_buildsSettlementBuilderAndBuildsSettlement(String testDataFileName)
			throws Exception {
		SettlementBuilderTestData testData = new Gson().fromJson(new InputStreamReader(testDataInputStream(testDataFileName)), SettlementBuilderTestData.class);
		Settlement settlement = settlementBuilder(testData).build();
		assertThat(settlement).isNotNull();
		assertThat(settlement.getAffiliationVoteCounts()).containsExactlyInAnyOrder(testData.expectedAffiliationVoteCounts(settlement));
		assertThat(settlement.getCandidateVoteCounts()).containsExactlyInAnyOrder(testData.expectedCandidateVoteCounts(settlement));
		assertThat(settlement.getCandidateRanks()).containsExactlyInAnyOrder(testData.expectedCandidateRanks(settlement));
		assertThat(settlement.getCandidateSeats()).containsExactlyInAnyOrder(testData.expectedCandidateSeats(settlement));
	}

	private InputStream testDataInputStream(String testDataFileName) {
		String testDataFilePath = "/test_data/settlement_builder/" + testDataFileName;
		InputStream testDataStream = getClass().getResourceAsStream(testDataFilePath);
		if (testDataStream == null) {
			throw new NullPointerException(format("no such test data file: %s", testDataFilePath));
		}
		return testDataStream;
	}

	private SettlementBuilder settlementBuilder(SettlementBuilderTestData testData) {
		Contest contest = testData.contest();
		List<ContestReport> contestReports = testData.contestReports();
		if (contest.hasRenumbering()) {
			return new SettlementBuilderFactory().settlementBuilderForRenumberingAndStrikeOuts(contest, contestReports);
		} else {
			VoteCategory baselineVoteCategory = testData.voteCategoryMap().get(baseline);
			return new SettlementBuilderFactory().settlementBuilderForPersonalVotesAndWriteIns(contest, contestReports, baselineVoteCategory);
		}
	}
}
