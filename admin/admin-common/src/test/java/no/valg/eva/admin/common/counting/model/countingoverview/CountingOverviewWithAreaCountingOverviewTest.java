package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewWithAreaCountingOverviewTest extends MockUtilsTestCase {
	private static final CountCategory CATEGORY_1 = VO;
	private static final CountCategory CATEGORY_2 = FO;
	private static final ElectionPath CONTEST_PATH_1 = ElectionPath.from("111111.11.11.111111");
	private static final ElectionPath CONTEST_PATH_2 = ElectionPath.from("111111.11.11.222222");
	private static final AreaPath AREA_PATH_1 = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final AreaPath AREA_PATH_2 = AreaPath.from("111111.11.11.1111.111111.2222");

	@Test
	public void getCategory_givenCategoryCountingOverview_returnsCategory() throws Exception {
		assertThat(countingOverview().getCategory()).isEqualTo(CATEGORY_1);
	}

	@Test
	public void getContestPath_givenCategoryCountingOverview_returnsContestPath() throws Exception {
		assertThat(countingOverview().getContestPath()).isEqualTo(CONTEST_PATH_1);
	}

	@Test
	public void getAreaPath_givenCategoryCountingOverview_returnsAreaPath() throws Exception {
		assertThat(countingOverview().getAreaPath()).isEqualTo(AREA_PATH_1);
	}

	@Test
	public void getStatuses_givenAreaCountingOverviews_returnsCombinedStatuses() throws Exception {
		Status status1 = createMock(Status.class);
		Status status2 = createMock(Status.class);
		Status status3 = createMock(Status.class);
		Status status4 = createMock(Status.class);
		Status status5 = createMock(Status.class);
		AreaCountingOverview childAreaCountingOverview1 = areaCountingOverview("childArea1", singletonList(status2));
		AreaCountingOverview childAreaCountingOverview2 = areaCountingOverview("childArea2", singletonList(status3));
		when(status2.merge(status3)).thenReturn(status4);
		when(status1.merge(status4)).thenReturn(status5);
		CountingOverviewWithAreaCountingOverview countingOverview = countingOverview(singletonList(status1),
				asList(childAreaCountingOverview1, childAreaCountingOverview2));
		assertThat(countingOverview.getStatuses()).containsExactly(status5);
	}

	@Test
	public void getAreaCountingOverviews_givenAreaCountingOverviews_returnsAreaCountingOverviews() throws Exception {
		List<AreaCountingOverview> areaCountingOverviews = createListMock();
		assertThat(countingOverview(createListMock(), areaCountingOverviews).getAreaCountingOverviews()).isEqualTo(areaCountingOverviews);
	}

	@Test(dataProvider = "equalsAndHashCodeTestData")
	public void equals_givenTestData_returnsTrueOrFalse(CountingOverviewWithAreaCountingOverview countingOverview1,
			CountingOverviewWithAreaCountingOverview countingOverview2, boolean expected) throws Exception {
		assertThat(countingOverview1.equals(countingOverview2)).isEqualTo(expected);
	}

	@Test
	public void equals_givenSimpleTestData_returnsTrueOrFalse() {
		CountingOverviewWithAreaCountingOverview countingOverview = countingOverview();
		Object anObject = new Object();
		Object aNull = null;

		assertThat(countingOverview.equals(anObject)).isFalse();
		assertThat(countingOverview.equals(aNull)).isFalse();
		assertThat(countingOverview.equals(countingOverview)).isTrue();
	}

	@Test(dataProvider = "equalsAndHashCodeTestData")
	public void hashCode_givenTestData_returnsTrueOrFalse(CountingOverviewWithAreaCountingOverview countingOverview1,
			CountingOverviewWithAreaCountingOverview countingOverview2, boolean expected) throws Exception {
		assertThat(countingOverview1.hashCode() == countingOverview2.hashCode()).isEqualTo(expected);
	}

	@Test
	public void hasCount_givenHasCount_returnsHasCount() throws Exception {
		assertThat(countingOverview(true).hasCount()).isTrue();
		assertThat(countingOverview(false).hasCount()).isFalse();

	}

	@DataProvider
	public Object[][] equalsAndHashCodeTestData() {
		List<Status> statuses1 = createListMock();
		List<Status> statuses2 = createListMock();
		List<AreaCountingOverview> areaCountingOverviews1 = createListMock();
		List<AreaCountingOverview> areaCountingOverviews2 = createListMock();
		return new Object[][] {
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1), true },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews2), false },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses2, areaCountingOverviews1), false },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, false, statuses1, areaCountingOverviews1), false },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_2, true, statuses1, areaCountingOverviews1), false },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_1, CONTEST_PATH_2, AREA_PATH_1, true, statuses1, areaCountingOverviews1), false },
				new Object[] { countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1),
						countingOverview(CATEGORY_2, CONTEST_PATH_1, AREA_PATH_1, true, statuses1, areaCountingOverviews1), false }
		};
	}

	private AreaCountingOverview areaCountingOverview(String areaName, List<Status> statuses) {
		return areaCountingOverview(areaName, CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses, emptyList());
	}

	private AreaCountingOverview areaCountingOverview(String areaName, CountCategory category, ElectionPath contestPath, AreaPath areaPath,
			boolean hasCount, List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		return new AreaCountingOverview(areaName, category, contestPath, areaPath, hasCount, statuses, areaCountingOverviews);
	}

	private CountingOverviewWithAreaCountingOverview countingOverview() {
		return countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, singletonList(new CountingStatus()), emptyList());
	}

	private CountingOverviewWithAreaCountingOverview countingOverview(boolean hasCount) {
		return countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, hasCount, createListMock(), emptyList());
	}

	private CountingOverviewWithAreaCountingOverview countingOverview(List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		return countingOverview(CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses, areaCountingOverviews);
	}

	private CountingOverviewWithAreaCountingOverview countingOverview(CountCategory category, ElectionPath contestPath, AreaPath areaPath, boolean hasCount,
			List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		return new CountingOverviewWithAreaCountingOverview(category, contestPath, areaPath, hasCount, statuses, areaCountingOverviews) {
			@Override
			public String getName() {
				return "name";
			}
		};
	}
}
