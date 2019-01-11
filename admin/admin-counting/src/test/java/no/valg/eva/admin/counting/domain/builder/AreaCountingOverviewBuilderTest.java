package no.valg.eva.admin.counting.domain.builder;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.TECHNICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AreaCountingOverviewBuilderTest extends MockUtilsTestCase {
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath PD_AREA_PATH = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final String PD_AREA_NAME = "pollingDistrict";
	private static final AreaPath CHILD_PD_AREA_PATH = AreaPath.from("111111.11.11.1111.111111.2222");
	private static final String CHILD_PD_AREA_NAME = "childPollingDistrict";

	@DataProvider
	public static Object[][] testData() {
		return new Object[][] {
				new Object[] { VO, CENTRAL, null, false, false },
				new Object[] { VO, CENTRAL_AND_BY_POLLING_DISTRICT, mock(Borough.class), false, true },
				new Object[] { VO, BY_POLLING_DISTRICT, null, true, true },
				new Object[] { FO, BY_TECHNICAL_POLLING_DISTRICT, null, false, true }
		};
	}

	@Test(dataProvider = "testData")
	public void areaCountingOverviews_givenTestData_returnAreaCountingOverviews(
			CountCategory category, CountingMode countingMode, Borough borough, boolean parentPollingDistrict, boolean hasCount) throws Exception {
		AreaCountingOverviewBuilder builder = initializeMocks(AreaCountingOverviewBuilder.class);
		Contest contest = createMock(Contest.class);
		MvArea mvArea = createMock(MvArea.class);
		List<StatusType> statusTypes = createListMock();
		List<VoteCountDigest> voteCountDigests = createListMock();
		PollingDistrict pollingDistrict = createMock(PollingDistrict.class);
		List<Status> statuses = createListMock();
		List<Status> childStatuses = createListMock();
		AreaCountingOverview areaCountingOverview;
		if (parentPollingDistrict) {
			AreaCountingOverview childAreaCountingOverview = new AreaCountingOverview(CHILD_PD_AREA_NAME, category, CONTEST_PATH, CHILD_PD_AREA_PATH,
					childStatuses);
			List<AreaCountingOverview> childAreaCountingOverviews = new ArrayList<>();
			childAreaCountingOverviews.add(childAreaCountingOverview);
			areaCountingOverview = new AreaCountingOverview(PD_AREA_NAME, category, CONTEST_PATH, PD_AREA_PATH, hasCount, statuses, childAreaCountingOverviews);
		} else {
			areaCountingOverview = new AreaCountingOverview(PD_AREA_NAME, category, CONTEST_PATH, PD_AREA_PATH, hasCount, statuses);
		}

		PollingDistrictType pollingDistrictType;
		if (category.isEarlyVoting()) {
			pollingDistrictType = TECHNICAL;
			when(mvArea.getMunicipality().technicalPollingDistricts()).thenReturn(singletonList(pollingDistrict));
		} else if (borough != null) {
			pollingDistrictType = REGULAR;
			Set<PollingDistrict> pollingDistricts = new HashSet<>();
			pollingDistricts.add(pollingDistrict);
			when(borough.getPollingDistricts()).thenReturn(pollingDistricts);
		} else if (parentPollingDistrict) {
			pollingDistrictType = PARENT;
			Set<PollingDistrict> childPollingDistricts = new HashSet<>();
			PollingDistrict childPollingDistrict = createMock(PollingDistrict.class);
			childPollingDistricts.add(childPollingDistrict);

			when(mvArea.getMunicipality().regularPollingDistricts(true, false)).thenReturn(singletonList(pollingDistrict));
			when(pollingDistrict.getChildPollingDistricts()).thenReturn(childPollingDistricts);
			when(pollingDistrict.isParentPollingDistrict()).thenReturn(true);
			when(childPollingDistrict.areaPath()).thenReturn(CHILD_PD_AREA_PATH);
			when(childPollingDistrict.getName()).thenReturn(CHILD_PD_AREA_NAME);
			when(childPollingDistrict.type()).thenReturn(CHILD);
			when(statusTypes.contains(PROTOCOL_COUNT_STATUS)).thenReturn(true);
			when(getInjectMock(CountingOverviewStatusBuilder.class)
					.countingOverviewStatuses(category, CHILD_PD_AREA_PATH, CHILD, statusTypes, voteCountDigests, countingMode))
							.thenReturn(childStatuses);
		} else {
			pollingDistrictType = REGULAR;
			when(mvArea.getMunicipality().regularPollingDistricts(true, false)).thenReturn(singletonList(pollingDistrict));
		}
		when(contest.electionPath()).thenReturn(CONTEST_PATH);
		when(mvArea.getBorough()).thenReturn(borough);
		when(pollingDistrict.areaPath()).thenReturn(PD_AREA_PATH);
		when(pollingDistrict.getName()).thenReturn(PD_AREA_NAME);
		when(pollingDistrict.type()).thenReturn(pollingDistrictType);
		when(getInjectMock(CountingOverviewStatusBuilder.class)
				.countingOverviewStatuses(category, PD_AREA_PATH, pollingDistrictType, statusTypes, voteCountDigests, countingMode))
						.thenReturn(statuses);

		assertThat(builder.areaCountingOverviews(category, contest, mvArea, statusTypes, voteCountDigests, countingMode)).containsExactly(areaCountingOverview);
	}

}
