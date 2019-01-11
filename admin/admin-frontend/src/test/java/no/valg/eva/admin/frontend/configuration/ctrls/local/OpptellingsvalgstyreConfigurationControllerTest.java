package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.configuration.service.ReportingUnitService;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.testng.annotations.Test;

public class OpptellingsvalgstyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_setsViewToFylkesvalgstyre() throws Exception {
		OpptellingsvalgstyreConfigurationController ctrl = ctrl();

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.OPPTELLINGSVALGSTYRE);

	}

	@Test
	public void getName_returnsFylkesvalgstyre() throws Exception {
		OpptellingsvalgstyreConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.opptellingsvalgstyre.name");
	}

	@Test
	public void hasAccess_withCorrectLevels_returnsTrue() throws Exception {
		OpptellingsvalgstyreConfigurationController ctrl = ctrl();
		when(getUserDataMock().isOpptellingsvalgstyret()).thenReturn(true);
		when(getInjectMock(ReportingUnitService.class).hasReportingUnitTypeConfigured(getUserDataMock(), OPPTELLINGSVALGSTYRET)).thenReturn(true);

		assertThat(ctrl.hasAccess()).isTrue();
	}

	@Test
	public void isDoneStatus_returnsFalse() throws Exception {
		OpptellingsvalgstyreConfigurationController ctrl = ctrl();

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	private OpptellingsvalgstyreConfigurationController ctrl() throws Exception {
		return ctrl(OpptellingsvalgstyreConfigurationController.class, ROOT);
	}
}
