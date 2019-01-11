package no.valg.eva.admin.frontend.counting.view;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel.COUNTING_BASE_URL;
import static no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel.COUNTING_URL_PARAMETERS;
import static no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel.MANUAL_REJECTED_BASE_URL;
import static no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel.REJECTED_URL_PARAMETERS;
import static no.valg.eva.admin.frontend.counting.view.CountingOverviewActionsColumnModel.SCANNED_REJECTED_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CountingOverviewActionsColumnModelTest extends MockUtilsTestCase {
	private static final CountCategory CATEGORY = VO;
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111.111111.1111");

	@DataProvider
	public static Object[][] testData() {
		return new Object[][] {
				new Object[] { null, null, MUNICIPALITY },
				new Object[] { null, null, COUNTY },
				new Object[] { null, null, MUNICIPALITY },
				new Object[] { null, FYLKESVALGSTYRET, COUNTY },
				new Object[] { false, null, MUNICIPALITY },
				new Object[] { false, null, COUNTY },
				new Object[] { false, null, MUNICIPALITY },
				new Object[] { false, FYLKESVALGSTYRET, COUNTY },
				new Object[] { true, null, MUNICIPALITY },
				new Object[] { true, null, COUNTY },
				new Object[] { true, null, MUNICIPALITY },
				new Object[] { true, FYLKESVALGSTYRET, COUNTY }
		};
	}

	@Test(dataProvider = "testData")
	public void itemsFor_givenTestData_returnsItems(Boolean manualCount, ReportingUnitTypeId reportingUnitTypeId, AreaLevelEnum pickerAreaLevel)
			throws Exception {
		List<ColumnOverviewItemModel> items = actionsColumn(reportingUnitTypeId, pickerAreaLevel).itemsFor(countingOverview(true, manualCount));
		assertThat(items).containsExactly(expectedItems(manualCount, reportingUnitTypeId, pickerAreaLevel));
	}

	private ColumnOverviewItemModel[] expectedItems(Boolean manualCount, ReportingUnitTypeId reportingUnitTypeId, AreaLevelEnum pickerAreaLevel) {
		if (manualCount == null) {
			return new ColumnOverviewItemModel[] {
					new ColumnOverviewLinkItemModel(url(COUNTING_BASE_URL + "?" + COUNTING_URL_PARAMETERS, reportingUnitTypeId, pickerAreaLevel),
                            "@common.view")
			};
		}
		String manualRejectedBaseUrl = manualCount ? MANUAL_REJECTED_BASE_URL : SCANNED_REJECTED_BASE_URL;
		String description = manualCount ? "@count.overview.rejected" : "@count.overview.rejected.scanned";
		return new ColumnOverviewItemModel[] {
				new ColumnOverviewLinkItemModel(url(COUNTING_BASE_URL + "?" + COUNTING_URL_PARAMETERS, reportingUnitTypeId, pickerAreaLevel),
                        "@common.view"),
				new ColumnOverviewLinkItemModel(url(manualRejectedBaseUrl + "?" + REJECTED_URL_PARAMETERS, reportingUnitTypeId, pickerAreaLevel), description)
		};
	}

	@Test
	public void itemsFor_givenGoToEnabledFalse_returnsEmptyItems() throws Exception {
		assertThat(actionsColumn(null, MUNICIPALITY).itemsFor(countingOverview(false, null))).isEmpty();
	}

	private String url(String url, ReportingUnitTypeId reportingUnitTypeId, AreaLevelEnum pickerAreaLevel) {
		ElectionPath pickerElectionPath = CONTEST_PATH.toElectionPath();
		AreaPath pickerAreaPath = pickerAreaLevel == COUNTY ? AREA_PATH.toCountyPath() : AREA_PATH.toMunicipalityPath();
		if (reportingUnitTypeId != null) {
			return format(url + "&reportingUnitType=%s", CATEGORY, CONTEST_PATH, AREA_PATH, pickerElectionPath, pickerAreaPath, FYLKESVALGSTYRET);
		}
		return format(url, CATEGORY, CONTEST_PATH, AREA_PATH, pickerElectionPath, pickerAreaPath);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructor_givenNullPickerAreaLevel_throwsException() throws Exception {
		new CountingOverviewActionsColumnModel(null, null);
	}

	private CountingOverviewActionsColumnModel actionsColumn(ReportingUnitTypeId reportingUnitTypeId, AreaLevelEnum pickerAreaLevel) {
		return new CountingOverviewActionsColumnModel(reportingUnitTypeId, pickerAreaLevel);
	}

	private CountingOverview countingOverview(boolean hasCount, Boolean manualCount) {
		CountingOverview countingOverview = createMock(CountingOverview.class);
		when(countingOverview.hasCount()).thenReturn(hasCount);
		when(countingOverview.getCategory()).thenReturn(CATEGORY);
		when(countingOverview.getContestPath()).thenReturn(CONTEST_PATH);
		when(countingOverview.getAreaPath()).thenReturn(AREA_PATH);
		if (manualCount == null) {
			return countingOverview;
		}
		when(countingOverview.isRejectedBallotsPending()).thenReturn(true);
		if (manualCount) {
			when(countingOverview.isManualRejectedBallotsPending()).thenReturn(true);
		}
		return countingOverview;
	}
}
