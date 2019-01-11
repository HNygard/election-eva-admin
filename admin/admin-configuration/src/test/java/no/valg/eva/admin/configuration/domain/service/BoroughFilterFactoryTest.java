package no.valg.eva.admin.configuration.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.factory.BoroughFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



public class BoroughFilterFactoryTest extends MockUtilsTestCase {

	@Test(dataProvider = "build")
	public void build_choosesAppropriateFilter(
		boolean isElectionOnBoroughLevel,
		CountCategory countCategory,
		CountingMode countingMode,
		BoroughFilterEnum expected) throws Exception {

		Optional<BoroughFilterEnum> filter = setupFilter(isElectionOnBoroughLevel, countCategory, countingMode, AreaPath.from("111111"));
		if (expected == null) {
			assertThat(filter).isEmpty();
		} else {
			assertThat(filter).contains(expected);
		}
	}

	@DataProvider(name = "build")
	public Object[][] buildBoroughFilterProvider() {
		return new Object[][] {
			{ true,  null,             null,                                        BoroughFilterEnum.FOR_BOROUGH_ELECTION },
			{ false, null,             CountingMode.CENTRAL,                        BoroughFilterEnum.FOR_NOT_VO_AND_CENTRAL },
			{ false, null,             CountingMode.BY_TECHNICAL_POLLING_DISTRICT,  BoroughFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT },
			{ false, CountCategory.VO, CountingMode.BY_POLLING_DISTRICT,            BoroughFilterEnum.FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT },
			{ false, CountCategory.VO, CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT,
				BoroughFilterEnum.FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT },
			{ false, CountCategory.VO, CountingMode.CENTRAL,                        null }
		};
	}

	private Optional<BoroughFilterEnum> setupFilter(boolean isElectionOnBoroughLevel, CountCategory countCategory, CountingMode countingMode, AreaPath areaPath) {
		ValghierarkiDomainService valghierarkiDomainService = createMock(ValghierarkiDomainService.class);
		CountingModeDomainService countingModeDomainService = createMock(CountingModeDomainService.class);
		
		ElectionPath electionPath = createMock(ElectionPath.class);
		when(electionPath.toElectionGroupPath()).thenReturn(electionPath);
		
		when(valghierarkiDomainService.isElectionOnBoroughLevel(electionPath)).thenReturn(isElectionOnBoroughLevel);
		when(countingModeDomainService.findCountingMode(countCategory, electionPath, areaPath)).thenReturn(countingMode);

		BoroughFilterFactory boroughFilterFactory = new BoroughFilterFactory(valghierarkiDomainService, countingModeDomainService);

		return boroughFilterFactory.build(countCategory, electionPath, areaPath);
	}
}

