package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ContestAreaTest {
	
	@Test
	public void isMunicipalityForSamiDistrict_isParent_isTrue() {
		ContestArea contestArea = new ContestArea();
		contestArea.setParentArea(true);
		assertThat(contestArea.isMunicipalityForSamiDistrict()).isTrue();
	}

	@Test
	public void isMunicipalityForSamiDistrict_isNotParent_isFalse() {
		ContestArea contestArea = new ContestArea();
		contestArea.setParentArea(false);
		assertThat(contestArea.isMunicipalityForSamiDistrict()).isFalse();
	}
}
