package no.valg.eva.admin.common.configuration.status;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class ContestStatusTest {

	@Test
	public void canBeSentToApproval_localConfiguration_true() {
		assertTrue(ContestStatus.LOCAL_CONFIGURATION.canBeSentToApproval());
	}

	@Test
	public void canBeSentToApproval_finisheConfiguration_false() {
		assertFalse(ContestStatus.FINISHED_CONFIGURATION.canBeSentToApproval());
	}

	@Test
	public void canBeApproved_localConfiguration_true() {
		assertTrue(ContestStatus.LOCAL_CONFIGURATION.canBeApproved());
	}

	@Test
	public void canBeApproved_finishedConfiguration_true() {
		assertTrue(ContestStatus.FINISHED_CONFIGURATION.canBeApproved());
	}

	@Test
	public void canBeApproved_centralConfiguration_false() {
		assertFalse(ContestStatus.CENTRAL_CONFIGURATION.canBeApproved());
	}
}
