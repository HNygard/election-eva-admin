package no.valg.eva.admin.common.counting.model;

import static org.testng.Assert.assertEquals;

import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ModifiedBallotsStatusTest {

	@DataProvider(name = "hasModifiedBallotsAndRegistrationIsNotDone")
	public Object[][] testDataSet1() {
		return new Object[][] {
				{ buildModifiedBallotCountWithModifiedAndUnProcessedBallots(), true },
				{ buildModifiedBallotCountWithAllBallotsProcessed(), false },
				{ buildModifiedBallotCountWithNoModifiedBallots(), false }
		};
	}

	@DataProvider(name = "hasModifiedBallotsAndRegistrationIsDone")
	public Object[][] testDataSet2() {
		return new Object[][] {
				{ buildModifiedBallotCountWithModifiedAndUnProcessedBallots(), false },
				{ buildModifiedBallotCountWithAllBallotsProcessed(), true },
				{ buildModifiedBallotCountWithNoModifiedBallots(), false }
		};
	}

	@DataProvider(name = "hasModifiedBallots")
	public Object[][] testDataSet3() {
		return new Object[][] {
				{ buildModifiedBallotCountWithModifiedAndUnProcessedBallots(), true },
				{ buildModifiedBallotCountWithNoModifiedBallots(), false },
		};
	}

	private ModifiedBallotsStatus buildModifiedBallotCountWithNoModifiedBallots() {
		return buildModifiedBallotCount(0, 0, 0);
	}

	private ModifiedBallotsStatus buildModifiedBallotCountWithAllBallotsProcessed() {
		return buildModifiedBallotCount(10, 0, 10);
	}

	private ModifiedBallotsStatus buildModifiedBallotCountWithModifiedAndUnProcessedBallots() {
		return buildModifiedBallotCount(10, 0, 0);
	}

	private ModifiedBallotsStatus buildModifiedBallotCount(int total, int inProgress, int completed) {
		return new ModifiedBallotsStatus(null, total, inProgress, completed, null);
	}

	@Test(dataProvider = "hasModifiedBallotsAndRegistrationIsNotDone")
	public void hasModifiedBallotsAndRegistrationIsNotDone(ModifiedBallotsStatus modifiedBallotsStatus, boolean expectedResult) {
		boolean actualResult = modifiedBallotsStatus.isCanCreateNewBatch();

		assertEquals(expectedResult, actualResult);
	}

	@Test(dataProvider = "hasModifiedBallotsAndRegistrationIsDone")
	public void hasModifiedBallotsAndRegistrationIsDone(ModifiedBallotsStatus modifiedBallotsStatus, boolean expectedResult) {
		boolean actualResult = modifiedBallotsStatus.hasModifiedBallotsAndRegistrationIsDone();

		assertEquals(expectedResult, actualResult);
	}

	@Test(dataProvider = "hasModifiedBallots")
	public void hasModifiedBallots(ModifiedBallotsStatus modifiedBallotsStatus, boolean expectedResult) {
		boolean actualResult = modifiedBallotsStatus.hasModifiedBallots();

		assertEquals(expectedResult, actualResult);
	}

}

