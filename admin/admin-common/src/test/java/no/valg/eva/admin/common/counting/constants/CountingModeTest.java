package no.valg.eva.admin.common.counting.constants;

import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class CountingModeTest {

	@Test
	public void isCentralPreliminaryCount_givenByPollingDistrict_returnsFalse() throws Exception {
		assertThat(CountingMode.BY_POLLING_DISTRICT.isCentralPreliminaryCount()).isFalse();
	}

	@Test
	public void isCentralPreliminaryCount_givenByTechnicalPollingDistrict_returnsTrue() throws Exception {
		assertThat(CountingMode.BY_TECHNICAL_POLLING_DISTRICT.isCentralPreliminaryCount()).isTrue();
	}

	@Test
	public void isCentralPreliminaryCount_givenCentral_returnsTrue() throws Exception {
		assertThat(CountingMode.CENTRAL.isCentralPreliminaryCount()).isTrue();
	}

	@Test
	public void isCentralPreliminaryCount_givenCentralAndByPollingDistrict_returnsTrue() throws Exception {
		assertThat(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT.isCentralPreliminaryCount()).isTrue();
	}

	@Test
	public void isPollingDistrictCount_givenByPollingDistrict_returnsTrue() throws Exception {
		assertThat(CountingMode.BY_POLLING_DISTRICT.isPollingDistrictCount()).isTrue();
	}

	@Test
	public void isPollingDistrictCount_givenByTechnicalPollingDistrict_returnsFalse() throws Exception {
		assertThat(CountingMode.BY_TECHNICAL_POLLING_DISTRICT.isPollingDistrictCount()).isFalse();
	}

	@Test
	public void isPollingDistrictCount_givenCentral_returnsFalse() throws Exception {
		assertThat(CountingMode.CENTRAL.isPollingDistrictCount()).isFalse();
	}

	@Test
	public void isPollingDistrictCount_givenCentralAndByPollingDistrict_returnsTrue() throws Exception {
		assertThat(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT.isPollingDistrictCount()).isTrue();
	}

	@Test
	public void isTechnicalPollingDistrictCount_givenByPollingDistrict_returnsFalse() throws Exception {
		assertThat(CountingMode.BY_POLLING_DISTRICT.isTechnicalPollingDistrictCount()).isFalse();
	}

	@Test
	public void isTechnicalPollingDistrictCount_givenByTechnicalPollingDistrict_returnsTrue() throws Exception {
		assertThat(CountingMode.BY_TECHNICAL_POLLING_DISTRICT.isTechnicalPollingDistrictCount()).isTrue();
	}

	@Test
	public void isTechnicalPollingDistrictCount_givenCentral_returnsFalse() throws Exception {
		assertThat(CountingMode.CENTRAL.isTechnicalPollingDistrictCount()).isFalse();
	}

	@Test
	public void isTechnicalPollingDistrictCount_givenCentralAndByPollingDistrict_returnsFalse() throws Exception {
		assertThat(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT.isTechnicalPollingDistrictCount()).isFalse();
	}

	@Test
	public void getDescription_givenByPollingDistrict_returnsCorrectDescription() throws Exception {
		assertThat(CountingMode.BY_POLLING_DISTRICT.getDescription()).isEqualTo("@report_count_category.count_mode_select.by_polling_district");
	}

	@Test
	public void getDescription_givenByTechnicalPollingDistrict_returnsCorrectDescription() throws Exception {
		assertThat(CountingMode.BY_TECHNICAL_POLLING_DISTRICT.getDescription()).isEqualTo(
				"@report_count_category.count_mode_select.by_technical_polling_district");
	}

	@Test
	public void getDescription_givenCentral_returnsCorrectDescription() throws Exception {
		assertThat(CountingMode.CENTRAL.getDescription()).isEqualTo("@report_count_category.count_mode_select.central");
	}

	@Test
	public void getDescription_givenCentralAndByPollingDistrict_returnsCorrectDescription() throws Exception {
		assertThat(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT.getDescription()).isEqualTo(
				"@report_count_category.count_mode_select.central_and_by_polling_district");
	}

	@Test
	public void getCountingMode_givenByPollingDistrictBooleans_returnsByPollingDistrictCountingMode() throws Exception {
		boolean centralPreliminaryCount = false;
		boolean pollingDistrictCount = true;
		boolean technicalPollingDistrictCount = false;
		CountingMode countingMode = CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
		assertThat(countingMode).isSameAs(BY_POLLING_DISTRICT);
	}

	@Test
	public void getCountingMode_givenByTechnicalPollingDistrictBooleans_returnsByTechnicalPollingDistrictCountingMode() throws Exception {
		boolean centralPreliminaryCount = true;
		boolean pollingDistrictCount = false;
		boolean technicalPollingDistrictCount = true;
		CountingMode countingMode = CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
		assertThat(countingMode).isSameAs(BY_TECHNICAL_POLLING_DISTRICT);
	}

	@Test
	public void getCountingMode_givenCentralBooleans_returnsCentralCountingMode() throws Exception {
		boolean centralPreliminaryCount = true;
		boolean pollingDistrictCount = false;
		boolean technicalPollingDistrictCount = false;
		CountingMode countingMode = CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
		assertThat(countingMode).isSameAs(CENTRAL);
	}

	@Test
	public void getCountingMode_givenCentralAndByPollingDistrictBooleans_returnsCentralAndByPollingDistrictCountingMode() throws Exception {
		boolean centralPreliminaryCount = true;
		boolean pollingDistrictCount = true;
		boolean technicalPollingDistrictCount = false;
		CountingMode countingMode = CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
		assertThat(countingMode).isSameAs(CENTRAL_AND_BY_POLLING_DISTRICT);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "unknown counting mode: centralPreliminaryCount=<true>, pollingDistrictCount=<true>, technicalPollingDistrictCount=<true>")
	public void getCountingMode_givenUnknownBooleansTrueTrueTrue_throwsIllegalArgumentException() throws Exception {
		boolean centralPreliminaryCount = true;
		boolean pollingDistrictCount = true;
		boolean technicalPollingDistrictCount = true;
		CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp
			= "unknown counting mode: centralPreliminaryCount=<false>, pollingDistrictCount=<false>, technicalPollingDistrictCount=<false>")
	public void getCountingMode_givenUnknownBooleansFalseFalseFalse_throwsIllegalArgumentException() throws Exception {
		boolean centralPreliminaryCount = false;
		boolean pollingDistrictCount = false;
		boolean technicalPollingDistrictCount = false;
		CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "unknown counting mode: centralPreliminaryCount=<false>, pollingDistrictCount=<true>, technicalPollingDistrictCount=<true>")
	public void getCountingMode_givenUnknownBooleansFalseTrueTrue_throwsIllegalArgumentException() throws Exception {
		boolean centralPreliminaryCount = false;
		boolean pollingDistrictCount = true;
		boolean technicalPollingDistrictCount = true;
		CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp
			= "unknown counting mode: centralPreliminaryCount=<false>, pollingDistrictCount=<false>, technicalPollingDistrictCount=<true>")
	public void getCountingMode_givenUnknownBooleansFalseFalseTrue_throwsIllegalArgumentException() throws Exception {
		boolean centralPreliminaryCount = false;
		boolean pollingDistrictCount = false;
		boolean technicalPollingDistrictCount = true;
		CountingMode.getCountingMode(centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount);
	}
}
