package no.valg.eva.admin.frontend.counting.view.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewTabModel;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewTabModelMapperTest {
	private static final ElectionPath ELECTION_PATH = ElectionPath.from("111111.11.11");
	private static final ElectionPath CONTEST_PATH = ELECTION_PATH.toContestSubPath("111111");
	private static final String ELECTION_NAME = "electionName";
	private static final String CONTEST_NAME = "contestName";

	@Test(dataProvider = "testData")
	public void countingOverviewTabModel_givenTestData_returnsTab(ContestInfo contestInfo, CountingOverviewTabModel tab) throws Exception {
		assertThat(new CountingOverviewTabModelMapper().countingOverviewTabModel(contestInfo)).isEqualTo(tab);
	}

	@DataProvider
	public Object[][] testData() {
		return new Object[][] {
				new Object[] { new ContestInfo(ELECTION_PATH, ELECTION_NAME, null, null), new CountingOverviewTabModel(ELECTION_PATH, ELECTION_NAME) },
				new Object[] { new ContestInfo(CONTEST_PATH, ELECTION_NAME, CONTEST_NAME, null),
						new CountingOverviewTabModel(CONTEST_PATH, ELECTION_NAME + " " + CONTEST_NAME) }
		};
	}
}
