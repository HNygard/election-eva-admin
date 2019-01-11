package no.valg.eva.admin.configuration.domain.model.filter;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ElectionFilterEnumTest {

	@Test(dataProvider = "forBf")
	public void forBf(String scenarioDescription, MvElection mvElection, boolean expectedFilteringResult) {
		assertThat(ElectionFilterEnum.FOR_BF.test(mvElection)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forBf")
	public Object[][] dataProviderForBoroughElection() {
		return new Object[][] {
			{ "whenElectionIsOnBoroughLevel", 	 mvElection(BOROUGH), 		true },
			{ "whenElectionIsNotOnBoroughLevel", mvElection(MUNICIPALITY), 	false }
		};
	}

	@Test(dataProvider = "forMunicipalityListProposals")
	public void forMunicipalityListProposals(String scenarioDescription, MvElection mvElection, boolean expectedFilteringResult) {
		assertThat(ElectionFilterEnum.FOR_MUNICIPALITY_LIST_PROPOSALS.test(mvElection)).isEqualTo(expectedFilteringResult);
	}

	@DataProvider(name = "forMunicipalityListProposals")
	public Object[][] dataProviderForMunicipalityListProposals() {
		return new Object[][] {
			{ "whenElectionIsOnBoroughLevel", 	 		mvElection(BOROUGH), 			true },
			{ "whenElectionIsOnMunicipalityLevel", 		mvElection(MUNICIPALITY), 		true },
			{ "whenElectionIsOnPollingDistrictLevel", 	mvElection(POLLING_DISTRICT), 	false },
			{ "whenElectionIsOnCountyLevel", 			mvElection(COUNTY), 			false }
		};
	}
	
	private MvElection mvElection(AreaLevelEnum areaLevelEnum) {
		MvElection mvElection = new MvElection();
		mvElection.setAreaLevel(areaLevelEnum.getLevel());
		return mvElection;
	}
}
