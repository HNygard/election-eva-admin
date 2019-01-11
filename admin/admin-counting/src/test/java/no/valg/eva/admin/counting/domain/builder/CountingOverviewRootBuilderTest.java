package no.valg.eva.admin.counting.domain.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CategoryCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CountingOverviewRootBuilderTest extends MockUtilsTestCase {
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111");
	private static final String AREA_NAME = "areaName";

	@Test
	public void countingOverviewRoot_givenInput_returnsCountingOverviewRoot() throws Exception {
		CountingOverviewRootBuilder builder = initializeMocks(CountingOverviewRootBuilder.class);
		Contest contest = createMock(Contest.class);
		MvArea mvArea = createMock(MvArea.class);
		List<StatusType> statusTypes = createListMock();
		List<CountCategory> countCategories = createListMock();
		List<VoteCountDigest> voteCountDigests = createListMock();
		Function<CountCategory, CountingMode> countingModeMapper = createFunctionMock();
		List<CategoryCountingOverview> categoryCountingOverviews = createListMock();
		CountingOverviewRoot coutingOverviewRoot = new CountingOverviewRoot(AREA_PATH, AREA_NAME, statusTypes, categoryCountingOverviews);

		when(mvArea.getAreaPath()).thenReturn(AREA_PATH.path());
		when(mvArea.getAreaName()).thenReturn(AREA_NAME);
		when(getInjectMock(CategoryCountingOverviewBuilder.class)
				.categoryCountingOverviews(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper))
						.thenReturn(categoryCountingOverviews);

		assertThat(builder.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper))
				.isEqualTo(coutingOverviewRoot);
	}
}
