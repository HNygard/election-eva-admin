package no.valg.eva.admin.configuration.domain.model.filter;

import static no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum.DEFAULT;
import static no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum.FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum.FOR_ELECTRONIC_VOTING;
import static no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum.FOR_OPERATOR_NOT_ON_POLLING_DISTRICT;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PollingDistrictFilterEnumTest {
	@Test(dataProvider = "default")
	public void def(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(DEFAULT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "default")
	public Object[][] dataProviderDefault() {
		return new Object[][] {
			{ "whenPollingDistrictIsARegularOne", 	mvArea(false, false, false), true },
			{ "whenPollingDistrictIsMunicipality", 	mvArea(true,  false, false), false },
			{ "whenPollingDistrictIsTechnical", 	mvArea(false, true,  false), false },
			{ "whenPollingDistrictIsAParent", 	  	mvArea(false, false, true),  true }
		};
	}
	
	@Test(dataProvider = "forByTechnicalPollingDistrict")
	public void forByTechnicalPollingDistrict(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(FOR_BY_TECHNICAL_POLLING_DISTRICT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forByTechnicalPollingDistrict")
	public Object[][] dataProviderForByTechnicalPollingDistrict() {
		return new Object[][] {
			{ "whenPollingDistrictIsARegularOne", 	mvArea(false, false, false), false },
			{ "whenPollingDistrictIsMunicipality", 	mvArea(true,  false, false), false },
			{ "whenPollingDistrictIsTechnical", 	mvArea(false, true,  false), true  },
			{ "whenPollingDistrictIsAParent", 	  	mvArea(false, false, true),  false }
		};
	}
	
	@Test(dataProvider = "forCentralAndOperatorNotOnPollingDistrict")
	public void forCentralAndOperatorNotOnPollingDistrict(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forCentralAndOperatorNotOnPollingDistrict")
	public Object[][] dataProviderForCentralAndOperatorNotOnPollingDistrict() {
		return new Object[][] {
			{ "whenPollingDistrictIsARegularOne", 	mvArea(false, false, false), false },
			{ "whenPollingDistrictIsMunicipality", 	mvArea(true,  false, false), true  },
			{ "whenPollingDistrictIsTechnical", 	mvArea(false, true,  false), false },
			{ "whenPollingDistrictIsAParent", 	  	mvArea(false, false, true),  false }
		};
	}
	
	@Test(dataProvider = "forElectronicVoting")
	public void forElectronicVoting(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(FOR_ELECTRONIC_VOTING.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forElectronicVoting")
	public Object[][] dataProviderForElectronicVoting() {
		return new Object[][] {
			{ "whenPollingDistrictIsARegularOne", 	mvArea(false, false, false), true  },
			{ "whenPollingDistrictIsMunicipality", 	mvArea(true,  false, false), true  },
			{ "whenPollingDistrictIsTechnical", 	mvArea(false, true,  false), false },
			{ "whenPollingDistrictIsAParent", 	  	mvArea(false, false, true),  false }
		};
	}
	
	@Test(dataProvider = "forOperatorNotOnPollingDistrict")
	public void forOperatorNotOnPollingDistrict(String scenarioDescription, MvArea mvArea, boolean expectedFilteringResult) {
		assertThat(FOR_OPERATOR_NOT_ON_POLLING_DISTRICT.test(mvArea)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forOperatorNotOnPollingDistrict")
	public Object[][] dataProviderForOperatorNotOnPollingDistrict() {
		return new Object[][] {
			{ "whenPollingDistrictIsARegularOne", 	mvArea(false, false, false, false), true  },
			{ "whenPollingDistrictIsMunicipality", 	mvArea(true,  false, false, false), false  },
			{ "whenPollingDistrictIsTechnical", 	mvArea(false, true,  false, false), false },
			{ "whenPollingDistrictIsAParent", 	  	mvArea(false, false, true, false),  true },
			{ "whenPollingDistrictHasParent", 	  	mvArea(false, false, false, true),  false }
		};
	}
	
	private MvArea mvArea(boolean isMunicipality, boolean isTechnicalPollingDistrict, boolean isParent, boolean hasParentPollingDistrict) {
		MvArea mvArea = new MvArea();
		mvArea.setPollingDistrict(pollingDistrict(isMunicipality, isTechnicalPollingDistrict, isParent, hasParentPollingDistrict));
		return mvArea;
	}

	private MvArea mvArea(boolean isMunicipality, boolean isTechnicalPollingDistrict, boolean isParent) {
		MvArea mvArea = new MvArea();
		mvArea.setPollingDistrict(pollingDistrict(isMunicipality, isTechnicalPollingDistrict, isParent, false));
		return mvArea;
	}

	private PollingDistrict pollingDistrict(boolean isMunicipality, boolean isTechnicalPollingDistrict, boolean isParent, boolean hasParentPollingDistrict) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setMunicipality(isMunicipality);
		pollingDistrict.setTechnicalPollingDistrict(isTechnicalPollingDistrict);
		pollingDistrict.setParentPollingDistrict(isParent);
		if (hasParentPollingDistrict) {
			pollingDistrict.setPollingDistrict(new PollingDistrict());
		}
		return pollingDistrict;
	}
}
