package no.valg.eva.admin.common.counting.model;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.model.modifiedballots.RegisterModifiedBallotCountStatus;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class RegisterModifiedBallotsStatusStatusTest {

	@DataProvider
	public Object[][] registerModifiedBallotCountStatusScenarios() {
		return new Object[][] {
				{ buildRegisterModifiedBallotCountStatus(withAllBallotsCompleted()), true },
				{ buildRegisterModifiedBallotCountStatus(withBallotsInProgress()), false },
				{ buildRegisterModifiedBallotCountStatus(withRemainingBallots()), false },
		};
	}

	@Test(dataProvider = "registerModifiedBallotCountStatusScenarios")
	public void isRegistrationOfAllModifiedBallotsCompleted(RegisterModifiedBallotCountStatus registerModifiedBallotCountStatus, boolean expectedResult) {
		boolean result = registerModifiedBallotCountStatus.isRegistrationOfAllModifiedBallotsCompleted();

		assertEquals(expectedResult, result);
	}

	private RegisterModifiedBallotCountStatus buildRegisterModifiedBallotCountStatus(ModifiedBallotsStatus modifiedBallotsStatus) {
		List<ModifiedBallotsStatus> modifiedBallotsStatuses = new ArrayList<>();
		modifiedBallotsStatuses.add(withAllBallotsCompleted());
		modifiedBallotsStatuses.add(modifiedBallotsStatus);
		return new RegisterModifiedBallotCountStatus(modifiedBallotsStatuses);
	}

	private ModifiedBallotsStatus withAllBallotsCompleted() {
		int total = 20;
		int completed = 20;
		int inProgress = 0;
		return new ModifiedBallotsStatus(null, total, inProgress, completed, null);
	}

	private ModifiedBallotsStatus withRemainingBallots() {
		int total = 20;
		int completed = 10;
		int inProgress = 0;
		return new ModifiedBallotsStatus(null, total, inProgress, completed, null);
	}

	private ModifiedBallotsStatus withBallotsInProgress() {
		int total = 20;
		int completed = 10;
		int inProgress = 10;
		return new ModifiedBallotsStatus(null, total, inProgress, completed, null);
	}
}

