package no.valg.eva.admin.configuration.domain.model.valgnatt;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ReportConfigurationTest {

	private static final String EMPTY = "";

	@Test
	public void isMunicipalityPollingDistrict_pollingDistrictIdIs0000_returnsTrue() {
		assertThat(new ReportConfiguration(1, "0000", null, null, null, false, false, null, null, EMPTY, EMPTY, EMPTY, EMPTY, 1, null)
				.isMunicipalityPollingDistrict())
				.isTrue();
	}

	@Test
	public void isMunicipalityPollingDistrict_pollingDistrictIdIsNot0000_returnsFalse() {
		assertThat(new ReportConfiguration(1, "0101", null, null, null, false, false, null, null, EMPTY, EMPTY, EMPTY, EMPTY, 1, null)
				.isMunicipalityPollingDistrict())
				.isFalse();
	}
}
