package no.valg.eva.admin.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;


public class AreaTest {

	private static final String AREA_PATH_TECHNICAL_POLLING_DISTRICT = "201301.47.03.0301.030100.0001";

	@Test
	public void getAreaPath_withValidInput_returnsAreaPath() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT);

		assertThat(new PollingPlaceArea(path, "Navn").getAreaPath()).isEqualTo(path);
	}

	@Test
	public void getName_withValidInput_returnsName() throws Exception {
		AreaPath path = AreaPath.from(AREA_PATH_TECHNICAL_POLLING_DISTRICT);

		assertThat(new PollingPlaceArea(path, "Navn").getName()).isEqualTo("Navn");
	}
}
