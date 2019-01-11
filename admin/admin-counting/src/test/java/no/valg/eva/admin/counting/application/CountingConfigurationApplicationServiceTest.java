package no.valg.eva.admin.counting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;
import no.valg.eva.admin.configuration.domain.service.CountingConfigurationDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CountingConfigurationApplicationServiceTest extends MockUtilsTestCase {

	private static final AreaPath DEFAULT_AREA_PATH = AreaPath.from("730001.47.01.0101.010100.0001");
	private static final ElectionPath DEFAULT_CONTEST_PATH = ElectionPath.from("730001.01.01.000001");
	
	private CountingConfigurationApplicationService service;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(CountingConfigurationApplicationService.class);
	}

	@Test
	public void getCountingConfiguration_returnsExpectedConfiguration() {
		CountContext countContext = new CountContext(DEFAULT_CONTEST_PATH, CountCategory.VO);

		CountingConfiguration expectedConfiguration = new CountingConfiguration();
		expectedConfiguration.setContestAreaLevel(AreaLevelEnum.MUNICIPALITY);
		expectedConfiguration.setCountingMode(CountingMode.BY_POLLING_DISTRICT);
		expectedConfiguration.setRequiredProtocolCount(true);
		expectedConfiguration.setPenultimateRecount(true);

		when(getInjectMock(CountingConfigurationDomainService.class).getCountingConfiguration(countContext, DEFAULT_AREA_PATH)).thenReturn(expectedConfiguration);

		CountingConfiguration countingConfiguration = service.getCountingConfiguration(userData(), countContext, DEFAULT_AREA_PATH);

		assertThat(countingConfiguration).isEqualTo(expectedConfiguration);
	}
	
	private UserData userData() {
		return mock(UserData.class);
	}
}
