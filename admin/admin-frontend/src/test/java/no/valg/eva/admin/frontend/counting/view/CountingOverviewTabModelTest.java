package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewTabModelTest {
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final String TITLE = "title";

	@DataProvider
	public static Object[][] matchesElectionPathTestData() {
		return new Object[][] {
				new Object[] { CONTEST_PATH.toElectionPath(), true },
				new Object[] { ElectionPath.from("222222.22.22"), false }
		};
	}

	@Test
	public void constructor_givenParameters_createsObject() throws Exception {
		CountingOverviewTabModel countingOverviewTabModel = new CountingOverviewTabModel(CONTEST_PATH, TITLE);
		assertThat(countingOverviewTabModel.getElectionPath()).isEqualTo(CONTEST_PATH);
		assertThat(countingOverviewTabModel.getId()).isEqualTo(CONTEST_PATH.path());
		assertThat(countingOverviewTabModel.getTitle()).isEqualTo(TITLE);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void matchesElectionPath_givenElectionPathNotOnElectionLevel_throwsException() throws Exception {
		CountingOverviewTabModel countingOverviewTabModel = new CountingOverviewTabModel(CONTEST_PATH, TITLE);
		countingOverviewTabModel.matchesElectionPath(CONTEST_PATH);
	}

	@Test(dataProvider = "matchesElectionPathTestData")
	public void matchesElectionPath_givenTestData_returnsTrueOrFalse(ElectionPath electionPath, boolean expected) throws Exception {
		CountingOverviewTabModel countingOverviewTabModel = new CountingOverviewTabModel(CONTEST_PATH, TITLE);
		assertThat(countingOverviewTabModel.matchesElectionPath(electionPath)).isEqualTo(expected);
	}
}
