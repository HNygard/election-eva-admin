package no.evote.service.configuration;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyLocalConfigStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CountyServiceEjbTest extends MockUtilsTestCase {

	@Test
	public void saveCountyConfigStatus_withExisting_updatesExisting() throws Exception {
		CountyServiceEjb service = initializeMocks(CountyServiceEjb.class);
		CountyConfigStatus status = countyConfigStatus(true, false, false);
		County county = stub_countyByElectionEventAndId(createMock(County.class));

		service.saveCountyConfigStatus(createMock(UserData.class), status);

		verify(county).updateStatus(status);
		verify(county).setLocale(any(Locale.class));
	}

	@Test
	public void findCountyStatusByArea_withExisting_verifyResult() throws Exception {
		CountyServiceEjb service = initializeMocks(CountyServiceEjb.class);
		County county = stub_countyByElectionEventAndId(createMock(County.class));
		CountyLocalConfigStatus dbStatus = createMock(CountyLocalConfigStatus.class);
		when(county.getLocalConfigStatus()).thenReturn(dbStatus);

		CountyConfigStatus result = service.findCountyStatusByArea(createMock(UserData.class), AreaPath.from("111111.22.33"));

		assertThat(result.isReportingUnitFylkesvalgstyre()).isEqualTo(dbStatus.isReportingUnitFylkesvalgstyre());
		assertThat(result.isLanguage()).isEqualTo(dbStatus.isLanguage());
		assertThat(result.isListProposals()).isEqualTo(dbStatus.isListProposals());
	}

	private County stub_countyByElectionEventAndId(County county) {
        when(getInjectMock(CountyRepository.class).countyByElectionEventAndId(anyLong(), any())).thenReturn(county);
		return county;
	}

	private CountyConfigStatus countyConfigStatus(boolean isReportingUnitFylkesvalgstyre, boolean isLanguage, boolean isListProposals) {
		CountyConfigStatus status = createMock(CountyConfigStatus.class);
		when(status.isReportingUnitFylkesvalgstyre()).thenReturn(isReportingUnitFylkesvalgstyre);
		when(status.isLanguage()).thenReturn(isLanguage);
		when(status.isListProposals()).thenReturn(isListProposals);
		return status;
	}

}
