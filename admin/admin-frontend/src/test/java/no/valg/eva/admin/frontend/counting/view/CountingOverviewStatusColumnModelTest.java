package no.valg.eva.admin.frontend.counting.view;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PRELIMINARY_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.RejectedBallotsStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewStatusColumnModelTest extends MockUtilsTestCase {
	private static final String PRIMARY_ICON_STYLE_1 = "primaryIconStyle1";
	private static final String PRIMARY_ICON_STYLE_2 = "primaryIconStyle2";
	private static final String SECONDARY_ICON_STYLE_1 = "socondaryIconStyle1";
	private static final String SECONDARY_ICON_STYLE_2 = "socondaryIconStyle2";
	private static final Integer REJECTED_BALLOT_COUNT_1 = 1;
	private static final Integer REJECTED_BALLOT_COUNT_2 = 2;

	@DataProvider
	public static Object[][] itemsForTestData() {
		return new Object[][] {
				new Object[] { PROTOCOL_COUNT_STATUS,
						asList(new ColumnOverviewIconItemModel(PRIMARY_ICON_STYLE_1), new ColumnOverviewIconItemModel(SECONDARY_ICON_STYLE_1),
								new ColumnOverviewTextItemModel(REJECTED_BALLOT_COUNT_1)) },
				new Object[] { PRELIMINARY_COUNT_STATUS,
						asList(new ColumnOverviewIconItemModel(null), new ColumnOverviewIconItemModel(null), new ColumnOverviewTextItemModel((String) null)) },
				new Object[] { REJECTED_BALLOTS_STATUS,
						asList(new ColumnOverviewIconItemModel(PRIMARY_ICON_STYLE_2), new ColumnOverviewIconItemModel(SECONDARY_ICON_STYLE_2),
								new ColumnOverviewTextItemModel(REJECTED_BALLOT_COUNT_2)) },
				new Object[] { COUNTY_REJECTED_BALLOTS_STATUS, asList(new ColumnOverviewIconItemModel(null),
						new ColumnOverviewIconItemModel(null), new ColumnOverviewTextItemModel((Integer) null)) }
		};
	}

	@Test(dataProvider = "itemsForTestData")
	public void itemsFor_givenCountingOverviewAndStatusType_returnsItemList(StatusType statusType, List<ColumnOverviewItemModel> expected) throws Exception {
		assertThat(new CountingOverviewStatusColumnModel(statusType).itemsFor(countingOverview()))
				.containsExactly(expected.toArray(new ColumnOverviewItemModel[expected.size()]));
	}

	private CountingOverview countingOverview() {
		return new CountingOverview() {
			@Override
			public CountCategory getCategory() {
				return null;
			}

			@Override
			public ElectionPath getContestPath() {
				return null;
			}

			@Override
			public AreaPath getAreaPath() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public List<Status> getStatuses() {
				return asList(countingStatus(), rejectedBallotsStatus());
			}
		};
	}

	private CountingStatus countingStatus() {
		CountingStatus status = mock(CountingStatus.class);
		when(status.getStatusType()).thenReturn(PROTOCOL_COUNT_STATUS);
		when(status.getPrimaryIconStyle()).thenReturn(PRIMARY_ICON_STYLE_1);
		when(status.getSecondaryIconStyle()).thenReturn(SECONDARY_ICON_STYLE_1);
		when(status.getRejectedBallotCount()).thenReturn(REJECTED_BALLOT_COUNT_1);
		return status;
	}

	private RejectedBallotsStatus rejectedBallotsStatus() {
		RejectedBallotsStatus status = mock(RejectedBallotsStatus.class);
		when(status.getStatusType()).thenReturn(REJECTED_BALLOTS_STATUS);
		when(status.getPrimaryIconStyle()).thenReturn(PRIMARY_ICON_STYLE_2);
		when(status.getSecondaryIconStyle()).thenReturn(SECONDARY_ICON_STYLE_2);
		when(status.getRejectedBallotCount()).thenReturn(REJECTED_BALLOT_COUNT_2);
		return status;
	}

}
