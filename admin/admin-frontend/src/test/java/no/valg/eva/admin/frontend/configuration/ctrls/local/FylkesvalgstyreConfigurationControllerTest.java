package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.testng.annotations.Test;

public class FylkesvalgstyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_setsViewToFylkesvalgstyre() throws Exception {
		FylkesvalgstyreConfigurationController ctrl = ctrl();

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.FYLKESVALGSTYRE);

	}

	@Test
	public void getName_returnsFylkesvalgstyre() throws Exception {
		FylkesvalgstyreConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.fylkesvalgstyre.name");
	}

	@Test
	public void setDoneStatus_withStatus_setsFylkesstatus() throws Exception {
		FylkesvalgstyreConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getCountyConfigStatus()).setReportingUnitFylkesvalgstyre(true);
	}

	@Test
	public void isDoneStatus_withStatus_returnsFylkesStatus() throws Exception {
		FylkesvalgstyreConfigurationController ctrl = ctrl();
		when(ctrl.getCountyConfigStatus().isReportingUnitFylkesvalgstyre()).thenReturn(false);

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	private FylkesvalgstyreConfigurationController ctrl() throws Exception {
		return ctrl(FylkesvalgstyreConfigurationController.class, COUNTY);
	}
}
