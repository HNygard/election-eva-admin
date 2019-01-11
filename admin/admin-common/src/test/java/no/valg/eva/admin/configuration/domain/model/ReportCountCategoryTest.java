package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.counting.constants.CountingMode;

import org.testng.annotations.Test;

public class ReportCountCategoryTest {

	@Test
	public void getCountingModeReturnsByPollingDistrictCountingMode() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(false);
		category.setPollingDistrictCount(true);
		category.setTechnicalPollingDistrictCount(false);

		CountingMode countingMode = category.getCountingMode();

		assertThat(countingMode).isEqualTo(CountingMode.BY_POLLING_DISTRICT);
	}

	@Test
	public void getCountingModeReturnsByTechnicalPollingDistrictCountingMode() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(true);
		category.setPollingDistrictCount(false);
		category.setTechnicalPollingDistrictCount(true);

		CountingMode countingMode = category.getCountingMode();

		assertThat(countingMode).isEqualTo(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
	}

	@Test
	public void getCountingModeReturnsCentralCountingMode() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(true);
		category.setPollingDistrictCount(false);
		category.setTechnicalPollingDistrictCount(false);

		CountingMode countingMode = category.getCountingMode();

		assertThat(countingMode).isEqualTo(CountingMode.CENTRAL);
	}

	@Test
	public void getCountingModeReturnsCentralAndByPollingDistrictCountingMode() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(true);
		category.setPollingDistrictCount(true);
		category.setTechnicalPollingDistrictCount(false);

		CountingMode countingMode = category.getCountingMode();

		assertThat(countingMode).isEqualTo(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected count mode for report count category to be.*")
	public void validateCountingModeThrowsExceptionForUnexpectedCountingModeWhenOneExpectedCountingMode() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(false);
		category.setPollingDistrictCount(true);
		category.setTechnicalPollingDistrictCount(false);

		category.validateCountingMode(CountingMode.CENTRAL);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected count mode for report count category to be one of.*")
	public void validateCountingModeThrowsExceptionForUnexpectedCountingModeWhenTwoExpectedCountingModes() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(false);
		category.setPollingDistrictCount(true);
		category.setTechnicalPollingDistrictCount(false);

		category.validateCountingMode(CountingMode.CENTRAL, CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected count mode for report count category to be one of.*")
	public void validateCountingModeThrowsExceptionForUnexpectedCountingModeWhenThreeExpectedCountingModes() {
		ReportCountCategory category = new ReportCountCategory();
		category.setCentralPreliminaryCount(false);
		category.setPollingDistrictCount(true);
		category.setTechnicalPollingDistrictCount(false);

		category.validateCountingMode(CountingMode.CENTRAL, CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT, CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
	}
}
