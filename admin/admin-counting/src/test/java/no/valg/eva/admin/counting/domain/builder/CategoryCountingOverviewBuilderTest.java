package no.valg.eva.admin.counting.domain.builder;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CategoryCountingOverviewBuilderTest extends MockUtilsTestCase {
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111");

	@DataProvider
	public static Object[][] categoryCountingOverviewsTestData() {
		return new Object[][] {
				new Object[] { false },
				new Object[] { true }
		};
	}

	@Test(dataProvider = "categoryCountingOverviewsTestData")
	public void categoryCountingOverviews_givenInput_returnsCategoryCountingOverviews(boolean contestOnBoroughLevel) throws Exception {
		CategoryCountingOverviewBuilder builder = initializeMocks(CategoryCountingOverviewBuilder.class);
		CountingOverviewStatusBuilder countingOverviewStatusBuilder = getInjectMock(CountingOverviewStatusBuilder.class);
		Contest contest = createMock(Contest.class);
		MvArea mvArea = createMock(MvArea.class);
		List<StatusType> statusTypes = createListMock();
		CountCategory category1 = FO;
		CountCategory category2 = VO;
		List<CountCategory> categories = asList(category1, category2);
		List<VoteCountDigest> voteCountDigests = createListMock();
		Function<CountCategory, CountingMode> countingModeMapper = createFunctionMock();
		CountingMode countingMode1 = CENTRAL;
		CountingMode countingMode2 = CENTRAL_AND_BY_POLLING_DISTRICT;
		List<Status> statuses1 = createListMock();
		List<Status> statuses2 = createListMock();
		List<AreaCountingOverview> areaCountingOverviews = createListMock();
		AreaPath countingAreaPath1 = contestOnBoroughLevel ? AREA_PATH.toBoroughSubPath("111111") : AREA_PATH.toMunicipalityPollingDistrictPath();
		AreaPath countingAreaPath2 = AREA_PATH.toMunicipalityPollingDistrictPath();
		CategoryCountingOverview categoryCountingOverview1 = new CategoryCountingOverview(category1, CONTEST_PATH, countingAreaPath1, statuses1);
		CategoryCountingOverview categoryCountingOverview2 = new CategoryCountingOverview(category2, CONTEST_PATH, countingAreaPath2, false, statuses2,
				areaCountingOverviews);

		when(contest.isOnBoroughLevel()).thenReturn(contestOnBoroughLevel);
		when(contest.electionPath()).thenReturn(CONTEST_PATH);
		if (contestOnBoroughLevel) {
			when(mvArea.getAreaPath()).thenReturn(AREA_PATH.toBoroughSubPath("111111").path());
		} else {
			when(mvArea.getAreaPath()).thenReturn(AREA_PATH.path());
		}
		when(countingModeMapper.apply(category1)).thenReturn(countingMode1);
		when(countingModeMapper.apply(category2)).thenReturn(countingMode2);
		when(countingOverviewStatusBuilder.countingOverviewStatuses(category1, countingAreaPath1, MUNICIPALITY, statusTypes, voteCountDigests, countingMode1))
				.thenReturn(statuses1);
		when(countingOverviewStatusBuilder.countingOverviewStatuses(category2, countingAreaPath2, MUNICIPALITY, statusTypes, voteCountDigests, countingMode2))
				.thenReturn(statuses2);
		when(getInjectMock(AreaCountingOverviewBuilder.class).areaCountingOverviews(category2, contest, mvArea, statusTypes, voteCountDigests, countingMode2))
				.thenReturn(areaCountingOverviews);

		assertThat(builder.categoryCountingOverviews(contest, mvArea, statusTypes, categories, voteCountDigests, countingModeMapper))
				.containsExactly(categoryCountingOverview1, categoryCountingOverview2);
	}
}
