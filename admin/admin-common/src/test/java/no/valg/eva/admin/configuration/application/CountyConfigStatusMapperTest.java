package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyLocalConfigStatus;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CountyConfigStatusMapperTest extends MockUtilsTestCase {

	private static final int TEN = 10;

	@Test
	public void toCountyConfigStatus_withoutLocalConfigStatus_returnsValueWithNoStatusValues() {
		County county = createMock(County.class);
		when(county.getLocalConfigStatus()).thenReturn(null);

		CountyConfigStatus result = CountyConfigStatusMapper.toCountyConfigStatus(county);

		verifyCountyConfigStatus(result, 0, false, false, false, false);
	}

	@Test
	public void toCountyConfigStatus_withLocalConfigStatus_returnsValueWithStatusValues() {
		County county = createMock(County.class);
		when(county.getLocalConfigStatus().getAuditOplock()).thenReturn(TEN);
		when(county.getLocalConfigStatus().isLanguage()).thenReturn(true);
		when(county.getLocalConfigStatus().isScanning()).thenReturn(true);

		CountyConfigStatus result = CountyConfigStatusMapper.toCountyConfigStatus(county);

		verifyCountyConfigStatus(result, TEN, true, false, false, true);
	}

	@Test
	public void toCountyLocalConfigStatus_returnsEntityWithUpdatedValues() {
		CountyLocalConfigStatus dbStatus = new CountyLocalConfigStatus();
		CountyConfigStatus status = createMock(CountyConfigStatus.class);
		when(status.isLanguage()).thenReturn(true);

		dbStatus = CountyConfigStatusMapper.toCountyLocalConfigStatus(dbStatus, status);

		assertThat(dbStatus.isLanguage()).isTrue();
	}

	private void verifyCountyConfigStatus(CountyConfigStatus result, int version, boolean isLanguage, boolean isListProposals,
										  boolean isReportingUnitFylkesvalgstyre, boolean isScanning) {
		assertThat(result.getLocaleId()).isNotNull();
		assertThat(result.getVersion()).isEqualTo(version);
		assertThat(result.isLanguage()).isEqualTo(isLanguage);
		assertThat(result.isListProposals()).isEqualTo(isListProposals);
		assertThat(result.isReportingUnitFylkesvalgstyre()).isEqualTo(isReportingUnitFylkesvalgstyre);
		assertThat(result.isScanning()).isEqualTo(isScanning);
	}
}
