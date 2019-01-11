package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityLocalConfigStatus;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class MunicipalityConfigStatusMapperTest extends MockUtilsTestCase {

	private static final int TEN = 10;

	@Test
	public void toMunicipalityConfigStatus_withoutLocalConfigStatus_returnsValueWithNoStatusValues() throws Exception {
		Municipality municipality = createMock(Municipality.class);
		when(municipality.getLocalConfigStatus()).thenReturn(null);

		MunicipalityConfigStatus result = MunicipalityConfigStatusMapper.toMunicipalityConfigStatus(municipality);

		verifyMunicipalityConfigStatus(result, 0, false, false, false);
	}

	@Test
	public void toMunicipalityConfigStatus_withLocalConfigStatus_returnsValueWithStatusValues() throws Exception {
		Municipality municipality = createMock(Municipality.class);
		when(municipality.getLocalConfigStatus().getAuditOplock()).thenReturn(TEN);
		when(municipality.getLocalConfigStatus().isLanguage()).thenReturn(true);

		MunicipalityConfigStatus result = MunicipalityConfigStatusMapper.toMunicipalityConfigStatus(municipality);

		verifyMunicipalityConfigStatus(result, TEN, true, false, false);
	}

	@Test
	public void toMunicipalityLocalConfigStatus_returnsEntityWithUpdatedValues() throws Exception {
		MunicipalityLocalConfigStatus dbStatus = new MunicipalityLocalConfigStatus();
		MunicipalityConfigStatus status = createMock(MunicipalityConfigStatus.class);
		when(status.isLanguage()).thenReturn(true);

		dbStatus = MunicipalityConfigStatusMapper.toMunicipalityLocalConfigStatus(dbStatus, status);

		assertThat(dbStatus.isLanguage()).isTrue();
	}

	@Test
	public void toMunicipalityLocalConfigStatus_with_should() throws Exception {

	}

	private void verifyMunicipalityConfigStatus(MunicipalityConfigStatus result, int version, boolean isLanguage, boolean isListProposals,
			boolean isAdvancePollingPlaces) {
		assertThat(result.getLocaleId()).isNotNull();
		assertThat(result.getVersion()).isEqualTo(version);
		assertThat(result.isLanguage()).isEqualTo(isLanguage);
		assertThat(result.isListProposals()).isEqualTo(isListProposals);
		assertThat(result.isAdvancePollingPlaces()).isEqualTo(isAdvancePollingPlaces);
	}
}
