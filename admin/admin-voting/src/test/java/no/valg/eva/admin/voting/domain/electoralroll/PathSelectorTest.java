package no.valg.eva.admin.voting.domain.electoralroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.annotations.Test;

public class PathSelectorTest {

	@Test
	public void path_pollingDistrictIsNull_municipalityPathIsReturned() {
		AreaPath municipalityPath = AreaPath.from("200013.47.03.0301");
		assertThat(PathSelector.path(municipalityPath, null)).isEqualTo(municipalityPath);
	}

	@Test
	public void path_pollingDistrictIsNotNull_pollingDistrictPathIsReturned() {
		AreaPath municipalityPath = AreaPath.from("200013.47.03.0301");
		AreaPath pollingDistrictPath = AreaPath.from("200013.47.03.0301.030101.0101");
		PollingDistrict fakePollingDistrict = mock(PollingDistrict.class);
		when(fakePollingDistrict.areaPath()).thenReturn(pollingDistrictPath);
		assertThat(PathSelector.path(municipalityPath, fakePollingDistrict)).isEqualTo(pollingDistrictPath);
	}
}
