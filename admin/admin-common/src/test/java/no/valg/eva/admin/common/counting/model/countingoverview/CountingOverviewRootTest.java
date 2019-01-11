package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewRootTest extends MockUtilsTestCase {
	private static final AreaPath AREA_PATH_1 = AreaPath.from("111111.11.11.1111");
	private static final AreaPath AREA_PATH_2 = AreaPath.from("111111.11.11.2222");
	private static final String AREA_NAME_1 = "areaName1";
	private static final String AREA_NAME_2 = "areaName2";

	@SuppressWarnings("unchecked")
	@DataProvider
	public static Object[][] equalsAndHashCodeTestData() {
		List<StatusType> statusTypes1 = mock(List.class);
		List<StatusType> statusTypes2 = mock(List.class);
		List<CategoryCountingOverview> ccOverviews1 = mock(List.class);
		List<CategoryCountingOverview> ccOverviews2 = mock(List.class);
		return new Object[][] {
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, true },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews2, false },

				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_1, statusTypes2, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_1, statusTypes2, ccOverviews2, false },

				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_2, statusTypes1, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_2, statusTypes1, ccOverviews2, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_2, statusTypes2, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_1, AREA_NAME_2, statusTypes2, ccOverviews2, false },

				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_1, statusTypes1, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_1, statusTypes1, ccOverviews2, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_1, statusTypes2, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_1, statusTypes2, ccOverviews2, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_2, statusTypes1, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_2, statusTypes1, ccOverviews2, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_2, statusTypes2, ccOverviews1, false },
				new Object[] { AREA_PATH_1, AREA_NAME_1, statusTypes1, ccOverviews1, AREA_PATH_2, AREA_NAME_2, statusTypes2, ccOverviews2, false }
		};
	}

	@Test
	public void getAreaPath_givenObject_returnsAreaPath() throws Exception {
		assertThat(countingOverviewRoot().getAreaPath()).isEqualTo(AREA_PATH_1);
	}

	@Test
	public void getName_givenObject_returnsAreaName() throws Exception {
		assertThat(countingOverviewRoot().getName()).isEqualTo(AREA_NAME_1);
	}

	@Test
	public void getStatusTypes_givenObject_returnsStatusTypes() throws Exception {
		List<StatusType> statusTypes = createListMock();
		assertThat(countingOverviewRoot(statusTypes, createListMock()).getStatusTypes()).isEqualTo(statusTypes);
	}

	@Test
	public void getCategoryCountingOverviews_givenObject_returnsCategoryCountingOverviews() throws Exception {
		List<CategoryCountingOverview> categoryCountingOverviews = createListMock();
		assertThat(countingOverviewRoot(createListMock(), categoryCountingOverviews).getCategoryCountingOverviews()).isEqualTo(categoryCountingOverviews);
	}

	@Test
	public void getStatuses_givenObject_returnsStatuses() throws Exception {
		CategoryCountingOverview categoryCountingOverview1 = createMock(CategoryCountingOverview.class);
		CategoryCountingOverview categoryCountingOverview2 = createMock(CategoryCountingOverview.class);
		List<CategoryCountingOverview> categoryCountingOverviews = asList(categoryCountingOverview1, categoryCountingOverview2);
		Status status1 = createMock(Status.class);
		Status status2 = createMock(Status.class);
		Status status3 = createMock(Status.class);
		when(categoryCountingOverview1.getStatuses()).thenReturn(singletonList(status1));
		when(categoryCountingOverview2.getStatuses()).thenReturn(singletonList(status2));
		when(status1.merge(status2)).thenReturn(status3);
		assertThat(countingOverviewRoot(createListMock(), categoryCountingOverviews).getStatuses()).containsExactly(status3);
	}

	@Test(dataProvider = "equalsAndHashCodeTestData")
	public void equals_givenTestData_returnsTrueOrFalse(
			AreaPath areaPath1, String areaName1, List<StatusType> statusTypes1, List<CategoryCountingOverview> categoryCountingOverviews1,
			AreaPath areaPath2, String areaName2, List<StatusType> statusTypes2, List<CategoryCountingOverview> categoryCountingOverviews2,
			Boolean expected) throws Exception {
		CountingOverviewRoot countingOverviewRoot1 = countingOverviewRoot(areaPath1, areaName1, statusTypes1, categoryCountingOverviews1);
		assertThat(countingOverviewRoot1
				.equals(countingOverviewRoot(areaPath2, areaName2, statusTypes2, categoryCountingOverviews2)))
						.isEqualTo(expected);
	}

	@Test
	public void equals_givenSimpleTestData_returnsTrueOrFalse() {
		CountingOverviewRoot countingOverviewRoot1 = countingOverviewRoot(AREA_PATH_1, AREA_NAME_1, createListMock(), createListMock());
		Object anObject = new Object();
		Object aNull = null;

		assertThat(countingOverviewRoot1.equals(anObject)).isFalse();
		assertThat(countingOverviewRoot1.equals(aNull)).isFalse();
		assertThat(countingOverviewRoot1.equals(countingOverviewRoot1)).isTrue();
	}

	@Test(dataProvider = "equalsAndHashCodeTestData")
	public void hashCode_givenTestData_testTrueOrFalse(
			AreaPath areaPath1, String areaName1, List<StatusType> statusTypes1, List<CategoryCountingOverview> categoryCountingOverviews1,
			AreaPath areaPath2, String areaName2, List<StatusType> statusTypes2, List<CategoryCountingOverview> categoryCountingOverviews2,
			Boolean expected) throws Exception {
		CountingOverviewRoot countingOverviewRoot1 = countingOverviewRoot(areaPath1, areaName1, statusTypes1, categoryCountingOverviews1);
		CountingOverviewRoot countingOverviewRoot2 = countingOverviewRoot(areaPath2, areaName2, statusTypes2, categoryCountingOverviews2);
		assertThat(countingOverviewRoot1.hashCode() == countingOverviewRoot2.hashCode()).isEqualTo(expected);
	}

	private CountingOverviewRoot countingOverviewRoot() {
		return countingOverviewRoot(createListMock(), createListMock());
	}

	private CountingOverviewRoot countingOverviewRoot(List<StatusType> statusTypes, List<CategoryCountingOverview> categoryCountingOverviews) {
		return countingOverviewRoot(AREA_PATH_1, AREA_NAME_1, statusTypes, categoryCountingOverviews);
	}

	private CountingOverviewRoot countingOverviewRoot(AreaPath areaPath, String areaName, List<StatusType> statusTypes,
			List<CategoryCountingOverview> categoryCountingOverviews) {
		return new CountingOverviewRoot(areaPath, areaName, statusTypes, categoryCountingOverviews);
	}
}
