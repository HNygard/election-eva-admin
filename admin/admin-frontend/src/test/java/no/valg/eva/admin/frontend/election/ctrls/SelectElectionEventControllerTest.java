package no.valg.eva.admin.frontend.election.ctrls;

import org.testng.annotations.Test;

public class SelectElectionEventControllerTest {
	@Test(expectedExceptions = IllegalStateException.class)
	public void select_beforeSelectingElectionEvent_shallThrowIllegalStateException() {
		SelectElectionEventController controller = new SelectElectionEventController();
		controller.select();
	}
}
