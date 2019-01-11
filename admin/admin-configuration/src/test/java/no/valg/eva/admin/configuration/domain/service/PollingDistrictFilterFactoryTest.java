package no.valg.eva.admin.configuration.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.factory.PollingDistrictFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PollingDistrictFilterFactoryTest extends MockUtilsTestCase {

	@Test(dataProvider = "pollingDistrictFilterTestData")
	public void build_choosesAppropriateFilter(
		AreaLevelEnum areaLevel,
		CountCategory countCategory,
		CountingMode countingMode,
		PollingDistrictFilterEnum expected) throws Exception {

		PollingDistrictFilterEnum filter = setupPollingDistrictFilter(countCategory, countingMode, areaLevel, AreaPath.from("111111.11.11.1111"));
		assertThat((Predicate<MvArea>) filter).isSameAs(expected);
	}

	@DataProvider(name = "pollingDistrictFilterTestData")
	public Object[][] pollingDistrictFilterTestData() {
		return new Object[][] {
			{ AreaLevelEnum.COUNTY, CountCategory.VO, CountingMode.BY_TECHNICAL_POLLING_DISTRICT,
				PollingDistrictFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT },
			{ AreaLevelEnum.POLLING_DISTRICT, CountCategory.VO, CountingMode.CENTRAL, PollingDistrictFilterEnum.DEFAULT },
			{ AreaLevelEnum.COUNTY, CountCategory.VO, CountingMode.CENTRAL, PollingDistrictFilterEnum.FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT },
			{ AreaLevelEnum.MUNICIPALITY, CountCategory.VO, CountingMode.BY_POLLING_DISTRICT,
				PollingDistrictFilterEnum.FOR_OPERATOR_NOT_ON_POLLING_DISTRICT },
			{ AreaLevelEnum.POLLING_DISTRICT, CountCategory.VO, CountingMode.BY_POLLING_DISTRICT, PollingDistrictFilterEnum.DEFAULT }
		};
	}

	private PollingDistrictFilterEnum setupPollingDistrictFilter(
		CountCategory countCategory, CountingMode countingMode, AreaLevelEnum areaLevel, AreaPath areaPath) throws Exception {

		PollingDistrictFilterFactory pollingDistrictFilterFactory = initializeMocks(PollingDistrictFilterFactory.class);

		ElectionPath selectedElectionPath = createMock(ElectionPath.class);
		when(selectedElectionPath.toElectionGroupPath()).thenReturn(selectedElectionPath);
		
		CountingModeDomainService countingModeDomainService = getInjectMock(CountingModeDomainService.class);
		when(countingModeDomainService.findCountingMode(countCategory, selectedElectionPath, areaPath)).thenReturn(countingMode);
		
		UserData userData = createMock(UserData.class);
		when(userData.getOperatorAreaLevel()).thenReturn(areaLevel);

		return pollingDistrictFilterFactory.build(userData, countCategory, selectedElectionPath, areaPath);
	}

}
