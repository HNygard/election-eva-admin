package no.valg.eva.admin.common.counting.model.countingoverview;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AreaCountingOverviewTest extends MockUtilsTestCase {
	private static final CountCategory CATEGORY_1 = VO;
	private static final ElectionPath CONTEST_PATH_1 = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath AREA_PATH_1 = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final String AREA_NAME_1 = "areaName1";
	private static final String AREA_NAME_2 = "areaName2";

	@Test
	public void getName_givenAreaCountingOverview_returnsName() throws Exception {
		assertThat(areaCountingOverview().getName()).isEqualTo(AREA_PATH_1.getPollingDistrictId() + " " + AREA_NAME_1);
	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
	public void equals_givenTestData_returnsTrueOrFalse(
			AreaCountingOverview areaCountingOverview1, AreaCountingOverview areaCountingOverview2, boolean expected) throws Exception {
		assertThat(areaCountingOverview1.equals(areaCountingOverview2)).isEqualTo(expected);
	}

	@Test
	public void equals_givenSimpleTestData_returnsTrueOrFalse() {
		AreaCountingOverview areaCountingOverview = areaCountingOverview();
		Object anObject = new Object();
		Object aNull = null;

		assertThat(areaCountingOverview.equals(anObject)).isFalse();
		assertThat(areaCountingOverview.equals(aNull)).isFalse();
		assertThat(areaCountingOverview.equals(areaCountingOverview)).isTrue();
	}

	@Test(dataProvider = "equalsAndHashcodeTestData")
	public void hashcode_givenTestData_isEqualOrNot(
			AreaCountingOverview areaCountingOverview1, AreaCountingOverview areaCountingOverview2, boolean expected) throws Exception {
		assertThat(areaCountingOverview1.hashCode() == areaCountingOverview2.hashCode()).isEqualTo(expected);
	}

	@SuppressWarnings("unchecked")
	@DataProvider
	public Object[][] equalsAndHashcodeTestData() {
		List<Status> statuses = createListMock();
		List<AreaCountingOverview> areaCountingOverviews = createListMock();
		return new Object[][] {
				new Object[] { areaCountingOverview(AREA_NAME_1, statuses, areaCountingOverviews),
						areaCountingOverview(AREA_NAME_1, statuses, areaCountingOverviews), true },
				new Object[] { areaCountingOverview(AREA_NAME_1, statuses, areaCountingOverviews),
						areaCountingOverview(AREA_NAME_2, statuses, areaCountingOverviews), false }
		};
	}

	private AreaCountingOverview areaCountingOverview(String areaName, List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		return areaCountingOverview(areaName, CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, statuses, areaCountingOverviews);
	}

	private AreaCountingOverview areaCountingOverview() {
		return areaCountingOverview(AREA_NAME_1, CATEGORY_1, CONTEST_PATH_1, AREA_PATH_1, true, createListMock(), createListMock());
	}

	private AreaCountingOverview areaCountingOverview(String areaName, CountCategory category, ElectionPath contestPath, AreaPath areaPath,
			boolean hasCount, List<Status> statuses, List<AreaCountingOverview> areaCountingOverviews) {
		return new AreaCountingOverview(areaName, category, contestPath, areaPath, hasCount, statuses, areaCountingOverviews);
	}
}
