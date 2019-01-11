package no.valg.eva.admin.frontend.configuration.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.Test;



public class CentralConfigControllerTest extends BaseFrontendTest {

	@Test
	public void init_withStubbedService_summaryShouldBeSet() throws Exception {
		CentralConfigController ctrl = initializeMocks(CentralConfigController.class);

		ctrl.init();

		assertThat(ctrl.getSummary()).isNotNull();
	}

	@Test
	public void getConfigurationStatus_withState1_returnsState1() throws Exception {
		CentralConfigController ctrl = initializeMocks(CentralConfigController.class);
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getName()).thenReturn("STATE1");

		assertThat(ctrl.getConfigurationStatus()).isEqualTo("STATE1");
	}
}

