package no.valg.eva.admin.common.counting.model.countingoverview;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNTING;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNT_NOT_REQUIRED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.MUNICIPALITY_REJECTED_BALLOTS;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.NOT_STARTED;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PRELIMINARY_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CountingStatusTest {
	private static final StatusType STATUS_TYPE_1 = PROTOCOL_COUNT_STATUS;
	private static final StatusType STATUS_TYPE_2 = PRELIMINARY_COUNT_STATUS;
	private static final CountingStatus.Value VALUE_1 = COUNT_NOT_REQUIRED;
	private static final CountingStatus.Value VALUE_2 = COUNTING;

	@DataProvider
	public static Object[][] getPrimaryIconStyleTestData() {
		return new Object[][] {
				new Object[] { COUNT_NOT_REQUIRED, "eva-icon-ellipsis" },
				new Object[] { NOT_STARTED, null },
				new Object[] { COUNTING, "eva-icon-file" },
				new Object[] { APPROVED, "eva-icon-checkmark" },
				new Object[] { MUNICIPALITY_REJECTED_BALLOTS, "eva-icon-warning" }
		};
	}

	@DataProvider
	public static Object[][] getSecondaryIconStyleTestData() {
		return new Object[][] {
				new Object[] { COUNT_NOT_REQUIRED, null, null },
				new Object[] { COUNT_NOT_REQUIRED, true, null },
				new Object[] { COUNT_NOT_REQUIRED, false, null },
				new Object[] { NOT_STARTED, null, null },
				new Object[] { NOT_STARTED, true, null },
				new Object[] { NOT_STARTED, false, null },
				new Object[] { COUNTING, null, null },
				new Object[] { COUNTING, true, "eva-icon-user" },
				new Object[] { COUNTING, false, "eva-icon-print" },
				new Object[] { APPROVED, null, null },
				new Object[] { APPROVED, true, "eva-icon-user" },
				new Object[] { APPROVED, false, "eva-icon-print" }
		};
	}

	@DataProvider
	public static Object[][] getPanelStyleTestData() {
		return new Object[][] {
				new Object[] { COUNT_NOT_REQUIRED, null },
				new Object[] { NOT_STARTED, null },
				new Object[] { COUNTING, "eva-icon-file" },
				new Object[] { APPROVED, "eva-icon-checkmark completed" },
				new Object[] { MUNICIPALITY_REJECTED_BALLOTS, null }
		};
	}

	@DataProvider
	public static Object[][] equalsAndHashcodeTestData() {
		return new Object[][] {
				new Object[] { countingStatus(), countingStatus(), true },

				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus(STATUS_TYPE_1), true },
				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus(STATUS_TYPE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus((StatusType) null), false },
				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus(), false },

				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(STATUS_TYPE_1, NOT_STARTED), true },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(STATUS_TYPE_2, NOT_STARTED), false },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(null, NOT_STARTED), false },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(STATUS_TYPE_1), true },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(STATUS_TYPE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus((StatusType) null), false },
				new Object[] { countingStatus(STATUS_TYPE_1, NOT_STARTED), countingStatus(), false },

				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(STATUS_TYPE_1, VALUE_1), true },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(STATUS_TYPE_1, VALUE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(STATUS_TYPE_2, VALUE_1), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(STATUS_TYPE_1), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(STATUS_TYPE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1), countingStatus(), false },

				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1, VALUE_1, true), true },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1, VALUE_1, false), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1, VALUE_2, true), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_2, VALUE_1, true), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1, VALUE_1), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1, VALUE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_2, VALUE_1), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_1), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(STATUS_TYPE_2), false },
				new Object[] { countingStatus(STATUS_TYPE_1, VALUE_1, true), countingStatus(), false },
		};
	}

	@DataProvider
	public static Object[][] mergeTestData() {
		RejectedBallotsStatus rejectedBallotsStatusPending = rejectedBallotsStatus(true);
		RejectedBallotsStatus rejectedBallotsStatusNotPending = rejectedBallotsStatus(false);
		return new Object[][] {
				new Object[] { countingStatus(), null, countingStatus() },
				new Object[] { countingStatus(), countingStatus(), countingStatus() },
				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus(STATUS_TYPE_1), countingStatus(STATUS_TYPE_1) },
				new Object[] { countingStatus(STATUS_TYPE_1), countingStatus(STATUS_TYPE_2), countingStatus() },

				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNT_NOT_REQUIRED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(NOT_STARTED), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNTING), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(APPROVED), countingStatus(APPROVED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(STATUS_TYPE_1), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), rejectedBallotsStatusPending, rejectedBallotsStatusPending },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), rejectedBallotsStatusNotPending, rejectedBallotsStatusNotPending },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(NOT_STARTED), countingStatus(NOT_STARTED), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(COUNT_NOT_REQUIRED), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(COUNTING), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(APPROVED), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(STATUS_TYPE_1), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), rejectedBallotsStatusPending, rejectedBallotsStatusPending },
				new Object[] { countingStatus(NOT_STARTED), rejectedBallotsStatusNotPending, countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(COUNTING), countingStatus(NOT_STARTED), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), countingStatus(COUNTING), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), countingStatus(APPROVED), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), countingStatus(STATUS_TYPE_1), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), rejectedBallotsStatusPending, rejectedBallotsStatusPending },
				new Object[] { countingStatus(COUNTING), rejectedBallotsStatusNotPending, countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING), countingStatus(MUNICIPALITY_REJECTED_BALLOTS), countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(APPROVED), countingStatus(NOT_STARTED), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED), countingStatus(COUNT_NOT_REQUIRED), countingStatus(APPROVED) },
				new Object[] { countingStatus(APPROVED), countingStatus(COUNTING), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED), countingStatus(APPROVED), countingStatus(APPROVED) },
				new Object[] { countingStatus(APPROVED), countingStatus(STATUS_TYPE_1), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED), rejectedBallotsStatusPending, rejectedBallotsStatusPending },
				new Object[] { countingStatus(APPROVED), rejectedBallotsStatusNotPending, countingStatus(APPROVED) },
				new Object[] { countingStatus(APPROVED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS), countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNTING, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(APPROVED, true), countingStatus(APPROVED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(STATUS_TYPE_1, true), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNTING, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(APPROVED, false), countingStatus(APPROVED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(STATUS_TYPE_1, false), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(COUNT_NOT_REQUIRED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },
				
				new Object[] { countingStatus(NOT_STARTED), countingStatus(COUNTING, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(APPROVED, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(STATUS_TYPE_1, true), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(NOT_STARTED), countingStatus(COUNTING, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(APPROVED, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(STATUS_TYPE_1, false), countingStatus(NOT_STARTED) },
				new Object[] { countingStatus(NOT_STARTED), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(COUNTING, true), countingStatus(NOT_STARTED, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(COUNT_NOT_REQUIRED), countingStatus(COUNTING, true) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(COUNTING, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(APPROVED, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(STATUS_TYPE_1, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(COUNTING, true), countingStatus(NOT_STARTED, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(COUNTING, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(APPROVED, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(STATUS_TYPE_1, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(COUNTING, true), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(APPROVED, true), countingStatus(NOT_STARTED, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(COUNT_NOT_REQUIRED), countingStatus(APPROVED, true) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(COUNTING, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(APPROVED, true), countingStatus(APPROVED) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(STATUS_TYPE_1, true), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(APPROVED, true), countingStatus(NOT_STARTED, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(COUNTING, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(APPROVED, false), countingStatus(APPROVED) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(STATUS_TYPE_1, false), countingStatus(COUNTING) },
				new Object[] { countingStatus(APPROVED, true), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 0) },

				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(NOT_STARTED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNT_NOT_REQUIRED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNTING, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(APPROVED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(STATUS_TYPE_1, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 2),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 3) },

				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(NOT_STARTED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNT_NOT_REQUIRED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNTING, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(APPROVED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(STATUS_TYPE_1, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false, 2),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 3) },

				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(NOT_STARTED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNT_NOT_REQUIRED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNTING, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(APPROVED, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(STATUS_TYPE_1, true),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 2),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 3) },

				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(NOT_STARTED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNT_NOT_REQUIRED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(COUNTING, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(APPROVED, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(STATUS_TYPE_1, false),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 1) },
				new Object[] { countingStatus(MUNICIPALITY_REJECTED_BALLOTS, true, 1), countingStatus(MUNICIPALITY_REJECTED_BALLOTS, false, 2),
						countingStatusMock(MUNICIPALITY_REJECTED_BALLOTS, 3) }
		};
	}

	private static RejectedBallotsStatus rejectedBallotsStatus(boolean rejectedBallotsPending) {
		RejectedBallotsStatus rejectedBallotsStatus = mock(RejectedBallotsStatus.class, RETURNS_DEEP_STUBS);
		when(rejectedBallotsStatus.isRejectedBallotsPending()).thenReturn(rejectedBallotsPending);
		return rejectedBallotsStatus;
	}

	private static CountingStatus countingStatusMock(CountingStatus.Value value, Integer rejectedBallotCount) {
        return new CountingStatus(STATUS_TYPE_1, value, null, rejectedBallotCount);
	}

	private static CountingStatus countingStatus(CountingStatus.Value value, boolean manualCount) {
		return new CountingStatus(STATUS_TYPE_1, value, manualCount);
	}

	private static CountingStatus countingStatus(CountingStatus.Value value) {
		return new CountingStatus(STATUS_TYPE_1, value);
	}

	private static CountingStatus countingStatus(CountingStatus.Value value, boolean manualCount, Integer rejectedBallotCount) {
		return new CountingStatus(STATUS_TYPE_1, value, manualCount, rejectedBallotCount);
	}

	private static CountingStatus countingStatus(StatusType statusType, CountingStatus.Value value, boolean manualCount) {
		return new CountingStatus(statusType, value, manualCount);
	}

	private static CountingStatus countingStatus(StatusType statusType, CountingStatus.Value value) {
		return new CountingStatus(statusType, value);
	}

	private static CountingStatus countingStatus(StatusType statusType) {
		return new CountingStatus(statusType);
	}

	private static CountingStatus countingStatus(StatusType statusType, boolean manualCount) {
		return new CountingStatus(statusType, null, manualCount);
	}

	private static CountingStatus countingStatus() {
		return new CountingStatus();
	}

	@Test
    public void getStatusType_givenStatusType_returnsStatusType() {
		assertThat(countingStatus(PROTOCOL_COUNT_STATUS).getStatusType()).isEqualTo(PROTOCOL_COUNT_STATUS);
		assertThat(countingStatus(PROTOCOL_COUNT_STATUS, null).getStatusType()).isEqualTo(PROTOCOL_COUNT_STATUS);
		assertThat(countingStatus(PROTOCOL_COUNT_STATUS, null, false).getStatusType()).isEqualTo(PROTOCOL_COUNT_STATUS);
	}

	@Test
    public void getStatusType_givenNullStatusType_returnsNull() {
		assertThat(countingStatus().getStatusType()).isNull();
		assertThat(countingStatus((StatusType) null).getStatusType()).isNull();
		assertThat(countingStatus(null, null).getStatusType()).isNull();
		assertThat(countingStatus(null, null, false).getStatusType()).isNull();
	}

	@Test
    public void isManualCount_givenManualCount_returnsManualCount() {
		assertThat(countingStatus(PROTOCOL_COUNT_STATUS, null, true).isManualCount()).isTrue();
		assertThat(countingStatus(PROTOCOL_COUNT_STATUS, null, false).isManualCount()).isFalse();
	}

	@Test
    public void isManualCount_givenNullManualCount_returnsFalse() {
		assertThat(countingStatus().isManualCount()).isFalse();
		assertThat(countingStatus((StatusType) null).isManualCount()).isFalse();
		assertThat(countingStatus(null, null).isManualCount()).isFalse();
	}

	@Test(dataProvider = "getPrimaryIconStyleTestData")
    public void getPrimaryIconStyle_givenTestData_returnsStyle(CountingStatus.Value value, String style) {
		assertThat(countingStatus(null, value).getPrimaryIconStyle()).isEqualTo(style);
		assertThat(countingStatus(null, value, false).getPrimaryIconStyle()).isEqualTo(style);
	}

	@Test(dataProvider = "getSecondaryIconStyleTestData")
    public void getSecondaryIconStyle_givenTestData_returnsStyle(CountingStatus.Value value, Boolean manualCount, String style) {
		if (manualCount == null) {
			assertThat(countingStatus(null, value).getSecondaryIconStyle()).isEqualTo(style);
		} else {
			assertThat(countingStatus(null, value, manualCount).getSecondaryIconStyle()).isEqualTo(style);
		}
	}

	@Test
    public void getRejectedBallotCount_givenCountingStatus_returnsNull() {
		assertThat(countingStatus().getRejectedBallotCount()).isNull();
	}

	@Test(dataProvider = "getPanelStyleTestData")
    public void getPanelStyle_givenTestData_returnStyle(CountingStatus.Value value, String style) {
		assertThat(countingStatus(null, value).getPanelStyle()).isEqualTo(style);
		assertThat(countingStatus(null, value, false).getPanelStyle()).isEqualTo(style);
	}

	@Test
    public void getValue_givenValue_returnsValue() {
		assertThat(countingStatus().getValue()).isEqualTo(NOT_STARTED);
		assertThat(countingStatus((StatusType) null).getValue()).isEqualTo(NOT_STARTED);
		assertThat(countingStatus(null, null).getValue()).isEqualTo(NOT_STARTED);
		assertThat(countingStatus(null, null, false).getValue()).isEqualTo(NOT_STARTED);
		assertThat(countingStatus(null, COUNT_NOT_REQUIRED).getValue()).isEqualTo(COUNT_NOT_REQUIRED);
		assertThat(countingStatus(null, COUNT_NOT_REQUIRED, false).getValue()).isEqualTo(COUNT_NOT_REQUIRED);
	}

	@Test(dataProvider = "mergeTestData")
    public void merge_givenStatuses_returnsMergedStatus(Status status1, Status status2, Status mergedStatus) {
		assertThat(status1.merge(status2)).isEqualTo(mergedStatus);
	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
    public void equals_givenTestData_returnsTrueOrFalse(Status status1, Status status2, boolean expected) {
		assertThat(status1.equals(status2)).isEqualTo(expected);
	}

	@Test
    public void equals_givenSimpleTestData_returnsTrueOrFalse() {
		CountingStatus countingStatus = countingStatus();
		Object anObject = new Object();
		Object aNull = null;

		assertThat(countingStatus.equals(anObject)).isFalse();
		assertThat(countingStatus.equals(aNull)).isFalse();
		assertThat(countingStatus.equals(countingStatus)).isTrue();

	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
    public void hashcode_givenTestData_isEqualOrNot(Status status1, Status status2, boolean expected) {
		assertThat(status1.hashCode() == status2.hashCode()).isEqualTo(expected);
	}
}

