package no.valg.eva.admin.common.counting.model.countingoverview;

import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RejectedBallotsStatusTest {
	@DataProvider
	public static Object[][] getStatusTypeTestData() {
		return new Object[][] {
				new Object[] { true, COUNTY_REJECTED_BALLOTS_STATUS },
				new Object[] { false, REJECTED_BALLOTS_STATUS }
		};
	}

	@DataProvider
	public static Object[][] isRejectedBallotsPendingTestData() {
		return new Object[][] {
				new Object[] { null, null, null, false },
				new Object[] { true, true, 1, false },
				new Object[] { true, true, 0, false },
				new Object[] { true, false, 1, true },
				new Object[] { true, false, 0, false },
				new Object[] { false, true, 1, false },
				new Object[] { false, true, 0, false },
				new Object[] { false, false, 1, false },
				new Object[] { false, false, 0, false }
		};
	}

	@DataProvider
	public static Object[][] isManualCountTestData() {
		return new Object[][] {
				new Object[] { null, false },
				new Object[] { false, false },
				new Object[] { true, true }
		};
	}

	@DataProvider
	public static Object[][] getRejectedBallotCountTestData() {
		return new Object[][] {
				new Object[] { null, null },
				new Object[] { 1, 1 }
		};
	}

	@DataProvider
	public static Object[][] getPrimaryIconStyleTestData() {
		return new Object[][] {
				new Object[] { null, null, null },
				new Object[] { false, 0, null },
				new Object[] { false, 1, "eva-icon-warning" },
				new Object[] { true, 0, "eva-icon-checkmark" },
				new Object[] { true, 1, "eva-icon-checkmark" }
		};
	}

	@DataProvider
	public static Object[][] getSecondayrIconStyleTestData() {
		return new Object[][] {
				new Object[] { null, null },
				new Object[] { false, "eva-icon-print" },
				new Object[] { true, "eva-icon-user" }
		};
	}

	@DataProvider
	public static Object[][] getPanelStyleTestData() {
		return new Object[][] {
				new Object[] { null, null, null },
				new Object[] { false, 0, null },
				new Object[] { false, 1, "eva-icon-warning warning" },
				new Object[] { true, 0, "eva-icon-checkmark completed" },
				new Object[] { true, 1, "eva-icon-checkmark completed" }
		};
	}

	@DataProvider
	public static Object[][] mergeTestData() {
		return new Object[][] {
				new Object[] { false, false, false, 0, null, null, null, null },
				new Object[] { false, false, false, 0, false, false, false, 0 },
				new Object[] { false, false, false, 0, false, false, false, 1 },
				new Object[] { false, false, false, 0, false, false, true, 0 },
				new Object[] { false, false, false, 0, false, false, true, 1 },
				new Object[] { false, false, false, 0, true, false, false, 0 },
				new Object[] { false, false, false, 0, true, false, false, 1 },
				new Object[] { false, false, false, 0, true, false, true, 0 },
				new Object[] { false, false, false, 0, true, false, true, 1 },
				new Object[] { false, false, false, 0, true, true, false, 0 },
				new Object[] { false, false, false, 0, true, true, false, 1 },
				new Object[] { false, false, false, 0, true, true, true, 0 },
				new Object[] { false, false, false, 0, true, true, true, 1 }
		};
	}

	@DataProvider
	public static Object[][] equalsAndHashcodeTestData() {
		return new Object[][] {
				new Object[] { new RejectedBallotsStatus(true), new RejectedBallotsStatus(true), true },
				new Object[] { new RejectedBallotsStatus(true), new RejectedBallotsStatus(false), false },
				new Object[] { new RejectedBallotsStatus(false), new RejectedBallotsStatus(false), true },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(true, true, true, true, 0), true },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(true, true, true, true, 1), false },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(true, true, true, false, 0), false },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(true, true, false, true, 0), false },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(true, false, true, true, 0), false },
				new Object[] { rejectedBallotsStatus(true, true, true, true, 0), rejectedBallotsStatus(false, true, true, true, 0), false },
		};
	}

	private static RejectedBallotsStatus rejectedBallotsStatus(
			boolean county, boolean approved, boolean rejectedBallotsProcessed, boolean manualCount, int rejectedBallotCount) {
		return new RejectedBallotsStatus(county, approved, rejectedBallotsProcessed, manualCount, rejectedBallotCount);
	}

	@Test(dataProvider = "getStatusTypeTestData")
	public void getStatusType_givenCountyTestData_returnsCorrectStatusType(boolean county, StatusType statusType) throws Exception {
		assertThat(new RejectedBallotsStatus(county).getStatusType()).isEqualTo(statusType);
		assertThat(rejectedBallotsStatus(county, false, false, false, 0).getStatusType()).isEqualTo(statusType);
	}

	@Test(dataProvider = "isRejectedBallotsPendingTestData")
	public void isRejectedBallotsPending_givenTestData_returnsTrueOrFalse(
			Boolean approved, Boolean rejectedBallotsProcessed, Integer rejectedBallotCount, boolean expected) throws Exception {
		assertThat(rejectedBallotsStatus(approved, rejectedBallotsProcessed, rejectedBallotCount).isRejectedBallotsPending()).isEqualTo(expected);
	}

	@Test(dataProvider = "isManualCountTestData")
	public void getManualCount_givenTestData_returnsCorrectValue(Boolean manualCount, boolean expected) throws Exception {
		assertThat(rejectedBallotsStatus(manualCount).isManualCount()).isEqualTo(expected);
	}

	@Test(dataProvider = "getRejectedBallotCountTestData")
	public void getRejectedBallotCount_givenTestData_returnsCorrectValue(Integer rejectedBallotCount, Integer expected) throws Exception {
		assertThat(rejectedBallotsStatus(rejectedBallotCount).getRejectedBallotCount()).isEqualTo(expected);
	}

	@Test(dataProvider = "getPrimaryIconStyleTestData")
	public void getPrimaryIconStyle_givenTestData_returnsCorrectStyle(
			Boolean rejectedBallotsProcessed, Integer rejectedBallotCount, String style) throws Exception {
		assertThat(rejectedBallotsStatus(rejectedBallotsProcessed, rejectedBallotCount).getPrimaryIconStyle()).isEqualTo(style);
	}

	@Test(dataProvider = "getSecondayrIconStyleTestData")
	public void getSecondaryIconStyle_givenTestData_returnsCorrectStyle(
			Boolean manualCount, String style) throws Exception {
		assertThat(rejectedBallotsStatus(manualCount).getSecondaryIconStyle()).isEqualTo(style);
	}

	@Test(dataProvider = "getPanelStyleTestData")
	public void getPanelStyle_givenTestData_returnCorrectStyle(Boolean rejectedBallotsProcessed, Integer rejectedBallotCount, String style) throws Exception {
		assertThat(rejectedBallotsStatus(rejectedBallotsProcessed, rejectedBallotCount).getPanelStyle()).isEqualTo(style);
	}

	@Test(dataProvider = "mergeTestData")
	public void merge_givenTestData_mergesStatuses(boolean approved1, boolean rejectedBallotsProcessed1, boolean manualCount1, int rejectedBallotCount1,
			Boolean approved2, Boolean rejectedBallotsProcessed2, Boolean manualCount2, Integer rejectedBallotCount2)
			throws Exception {
		RejectedBallotsStatus rejectedBallotsStatus1 = rejectedBallotsStatus(approved1, rejectedBallotsProcessed1, manualCount1, rejectedBallotCount1);
		RejectedBallotsStatus rejectedBallotsStatus2 = rejectedBallotsStatus(approved2, rejectedBallotsProcessed2, manualCount2, rejectedBallotCount2);
		RejectedBallotsStatus mergedRejectedBallotsStatus = (RejectedBallotsStatus) rejectedBallotsStatus1.merge(rejectedBallotsStatus2);
		assertThat(mergedRejectedBallotsStatus.getStatusType()).isEqualTo(rejectedBallotsStatus1.getStatusType());
		assertThat(mergedRejectedBallotsStatus.isRejectedBallotsPending())
				.isEqualTo(rejectedBallotsStatus1.isRejectedBallotsPending() && rejectedBallotsStatus2.isRejectedBallotsPending());
		assertThat(mergedRejectedBallotsStatus.isManualCount()).isFalse();
		if (rejectedBallotCount2 != null) {
			assertThat(mergedRejectedBallotsStatus.getRejectedBallotCount())
					.isEqualTo(rejectedBallotsStatus1.getRejectedBallotCount() + rejectedBallotsStatus2.getRejectedBallotCount());
		} else {
			assertThat(mergedRejectedBallotsStatus.getRejectedBallotCount()).isEqualTo(rejectedBallotsStatus1.getRejectedBallotCount());
		}
	}

	@Test
	public void merge_givenCountingStatus_returnThis() throws Exception {
		RejectedBallotsStatus rejectedBallotsStatus = rejectedBallotsStatus();
		assertThat(rejectedBallotsStatus.merge(new CountingStatus())).isSameAs(rejectedBallotsStatus);
	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
	public void equals_givenTestData_returnsTrueOrFalse(
			RejectedBallotsStatus rejectedBallotsStatus1, RejectedBallotsStatus rejectedBallotsStatus2, boolean expected) throws Exception {
		assertThat(rejectedBallotsStatus1.equals(rejectedBallotsStatus2)).isEqualTo(expected);
	}

	@Test
	public void equals_givenSimpleTestData_returnTrueOrFalse() throws Exception {
		RejectedBallotsStatus rejectedBallotsStatus = rejectedBallotsStatus();
		Object anObject = new Object();
		Object aNull = null;

		assertThat(rejectedBallotsStatus.equals(anObject)).isFalse();
		assertThat(rejectedBallotsStatus.equals(aNull)).isFalse();
		assertThat(rejectedBallotsStatus.equals(rejectedBallotsStatus)).isTrue();

	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
	public void hashcode_givenTestData_isEqualOrNot(
			RejectedBallotsStatus rejectedBallotsStatus1, RejectedBallotsStatus rejectedBallotsStatus2, boolean expected) throws Exception {
		assertThat(rejectedBallotsStatus1.hashCode() == rejectedBallotsStatus2.hashCode()).isEqualTo(expected);
	}

	private RejectedBallotsStatus rejectedBallotsStatus() {
		return new RejectedBallotsStatus(false);
	}

	private RejectedBallotsStatus rejectedBallotsStatus(Boolean rejectedBallotsProcessed, Integer rejectedBallotCount) {
		Boolean approved = rejectedBallotsProcessed != null ? true : null;
		return rejectedBallotsStatus(approved, rejectedBallotsProcessed, rejectedBallotCount);
	}

	private RejectedBallotsStatus rejectedBallotsStatus(Integer rejectedBallotCount) {
		Boolean approved = rejectedBallotCount != null ? true : null;
		Boolean rejectedBallotsProcessed = rejectedBallotCount != null ? true : null;
		return rejectedBallotsStatus(approved, rejectedBallotsProcessed, rejectedBallotCount);
	}

	private RejectedBallotsStatus rejectedBallotsStatus(Boolean approved, Boolean rejectedBallotsProcessed, Integer rejectedBallotCount) {
		if (approved == null) {
			return new RejectedBallotsStatus(false);
		}
		return rejectedBallotsStatus(false, approved, rejectedBallotsProcessed, false, rejectedBallotCount);
	}

	private RejectedBallotsStatus rejectedBallotsStatus(Boolean approved, Boolean rejectedBallotsProcessed, Boolean manualCount, Integer rejectedBallotCount) {
		if (approved == null) {
			return new RejectedBallotsStatus(false);
		}
		return rejectedBallotsStatus(false, approved, rejectedBallotsProcessed, manualCount, rejectedBallotCount);
	}

	private RejectedBallotsStatus rejectedBallotsStatus(Boolean manualCount) {
		if (manualCount == null) {
			return new RejectedBallotsStatus(false);
		}
		return rejectedBallotsStatus(false, false, false, manualCount, 0);
	}
}
