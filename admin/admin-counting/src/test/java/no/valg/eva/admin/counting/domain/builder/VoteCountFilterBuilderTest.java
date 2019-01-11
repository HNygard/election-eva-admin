package no.valg.eva.admin.counting.domain.builder;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.MUNICIPALITY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VoteCountFilterBuilderTest extends MockUtilsTestCase {
	private static final AreaPath AREA_PATH_1 = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final AreaPath AREA_PATH_2 = AreaPath.from("222222.22.22.2222.222222.2222");

	@Test(dataProvider = "testData")
	public void voteCountFilterFor_givenTestData_returnsFilterWithExceptedResult(
			VoteCount voteCount, CountCategory category, AreaPath areaPath, StatusType statusType, boolean expectedResult) throws Exception {
		Predicate<VoteCount> voteCountFilter = new VoteCountFilterBuilder().voteCountFilterFor(category, areaPath, statusType);
		assertThat(voteCountFilter.test(voteCount)).isEqualTo(expectedResult);
	}

	@DataProvider
	public Object[][] testData() {
		CountCategory category1 = anyOf(CountCategory.class);
		CountCategory category2 = anyBut(category1);
		StatusType notProtocolCountStatus = anyBut(PROTOCOL_COUNT_STATUS);
		return new Object[][] {
				new Object[] { voteCount(category1, AREA_PATH_1, PROTOCOL, null), category1, AREA_PATH_1, PROTOCOL_COUNT_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, PROTOCOL, null), category1, AREA_PATH_1, notProtocolCountStatus, false },
				new Object[] { voteCount(category1, AREA_PATH_1, PROTOCOL, null), category1, AREA_PATH_2, PROTOCOL_COUNT_STATUS, false },
				new Object[] { voteCount(category1, AREA_PATH_1, PROTOCOL, null), category2, AREA_PATH_1, PROTOCOL_COUNT_STATUS, false },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, MUNICIPALITY), category1, AREA_PATH_1, MUNICIPALITY_FINAL_COUNT_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, MUNICIPALITY), category1, AREA_PATH_1, FINAL_COUNT_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, null), category1, AREA_PATH_1, MUNICIPALITY_FINAL_COUNT_STATUS, false },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, COUNTY), category1, AREA_PATH_1, COUNTY_FINAL_COUNT_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, COUNTY), category1, AREA_PATH_1, FINAL_COUNT_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, null), category1, AREA_PATH_1, COUNTY_FINAL_COUNT_STATUS, false },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, MUNICIPALITY), category1, AREA_PATH_1, REJECTED_BALLOTS_STATUS, true },
				new Object[] { voteCount(category1, AREA_PATH_1, FINAL, COUNTY), category1, AREA_PATH_1, COUNTY_REJECTED_BALLOTS_STATUS, true },
		};
	}

	private VoteCount voteCount(CountCategory category, AreaPath areaPath, CountQualifier qualifier, AreaLevelEnum reportingUnitAreaLevel) {
		VoteCount voteCount = createMock(VoteCount.class);
		when(voteCount.getCountCategory()).thenReturn(category);
		when(voteCount.getMvArea().getAreaPath()).thenReturn(areaPath.path());
		when(voteCount.getCountQualifier().getId()).thenReturn(qualifier.getId());
		if (reportingUnitAreaLevel != null) {
			when(voteCount.getContestReport().getReportingUnit().getActualAreaLevel()).thenReturn(reportingUnitAreaLevel);
		}
		return voteCount;
	}
}
