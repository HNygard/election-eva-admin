package no.valg.eva.admin.configuration.domain.model.filter;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BoroughFilterEnumTest {
	
	@Test(dataProvider = "forBoroughElection")
	public void forBoroughElection(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(BoroughFilterEnum.FOR_BOROUGH_ELECTION.test(mvArea)).isEqualTo(expectedFilteringResult);
	}
	
	@DataProvider(name = "forBoroughElection")
	public Object[][] dataProviderForBoroughElection() {
		return new Object[][] {
			{ "whenBoroughIsOnMunicipalityLevel", 	 mvArea(borough(true, false)),  false },
			{ "whenBoroughIsNotOnMunicipalityLevel", mvArea(borough(false, false)), true }
		};
	}

	@Test(dataProvider = "forNotVoAndCentral")
	public void forNotVoAndCentral_filtersOnBoroughOnMunicipalityLevel(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(BoroughFilterEnum.FOR_NOT_VO_AND_CENTRAL.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forNotVoAndCentral")
	public Object[][] dataProviderForNotVoAndCentral() {
		return new Object[][] {
			{ "whenBoroughIsOnMunicipalityLevel", 	 mvArea(borough(true, false)),  true },
			{ "whenBoroughIsNotOnMunicipalityLevel", mvArea(borough(false, false)), false }
		};
	}

	@Test(dataProvider = "forVoAndByPollingDistrictOrCentralAndByPollingDistrict")
	public void forVoAndByPollingDistrictOrCentralAndByPollingDistrict(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(BoroughFilterEnum.FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forVoAndByPollingDistrictOrCentralAndByPollingDistrict")
	public Object[][] dataProviderForVoAndByPollingDistrictOrCentralAndByPollingDistrict() {
		return new Object[][] {
			{ "whenBoroughHasRegularPollingDistricts", 								  mvArea(borough(false, true)),  true },
			{ "whenBoroughHasRegularPollingDistrictsAndIsOnMunicipalityLevel", 		  mvArea(borough(true, true)),   true },
				{"whenBoroughNeitherHasRegularPollingDistrictsNorIsOnMunicipalityLevel", mvArea(borough(false, false)), true},
				{"whenBoroughHasOnlyParentPollingDistrictsAndIsOnMunicipalityLevel", mvArea(borough(true, false, true)), true},
			{ "whenBoroughIsOnMunicipalityLevel", 	   								  mvArea(borough(true, false)),  false }
		};
	}

	@Test(dataProvider = "forByTechnicalPollingDistrict")
	public void forByTechnicalPollingDistrict(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(BoroughFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forByTechnicalPollingDistrict")
	public Object[][] dataProviderForByTechnicalPollingDistrict() {
		return new Object[][] {
			{ "whenBoroughIsOnMunicipalityLevel", 	 mvArea(borough(true, false)),  true },
			{ "whenBoroughIsNotOnMunicipalityLevel", mvArea(borough(false, false)), false }
		};
	}

	private MvArea mvArea(Borough borough) {
		MvArea mvArea = new MvArea();
		mvArea.setBorough(borough);
		return mvArea;
	}

	private Borough borough(boolean isOnMunicipalityLevel, boolean hasRegularPollingDistricts) {
		return borough(isOnMunicipalityLevel, hasRegularPollingDistricts, false);
	}

	private Borough borough(boolean isOnMunicipalityLevel, boolean hasRegularPollingDistricts, boolean hasParentPollingDistrict) {
		Borough borough = new Borough();
		borough.setMunicipality1(isOnMunicipalityLevel);
		if (hasRegularPollingDistricts) {
			borough.getPollingDistricts().add(pollingDistrict());
		}
		if (hasParentPollingDistrict) {
			borough.getPollingDistricts().add(parentPollingDistrict());
		}
		return borough;
	}

	private PollingDistrict pollingDistrict() {
		return new PollingDistrict();
	}

	private PollingDistrict parentPollingDistrict() {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setParentPollingDistrict(true);
		return pollingDistrict;
	}

}
