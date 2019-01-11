package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.testng.annotations.Test;

public class ValgstyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {
	@Test
	public void init_setsViewToFylkesvalgstyre() throws Exception {
		ValgstyreConfigurationController ctrl = ctrl();

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.VALGSTYRE);

	}

	@Test
	public void getName_returnsFylkesvalgstyre() throws Exception {
		ValgstyreConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.valgstyre.name");
	}

	@Test
	public void setDoneStatus_withStatus_setsFylkesstatus() throws Exception {
		ValgstyreConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMainController().getMunicipalityStatus()).setReportingUnitValgstyre(true);
	}

	@Test
	public void isDoneStatus_withStatus_returnsFylkesStatus() throws Exception {
		ValgstyreConfigurationController ctrl = ctrl();
		when(ctrl.getMainController().getMunicipalityStatus().isReportingUnitValgstyre()).thenReturn(false);

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	private ValgstyreConfigurationController ctrl() throws Exception {
		return ctrl(ValgstyreConfigurationController.class, MUNICIPALITY);
	}
}
