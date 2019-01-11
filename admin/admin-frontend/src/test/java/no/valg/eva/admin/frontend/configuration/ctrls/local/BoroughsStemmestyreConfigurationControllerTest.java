package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import org.primefaces.event.NodeSelectEvent;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class BoroughsStemmestyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_withTwoPollingDistricts_verifyState() throws Exception {
		BoroughsStemmestyreConfigurationController ctrl = ctrl();
		stub_findRegularPollingDistrictsByArea(2);

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.BOROUGHS_STEMMESTYRE);
		assertThat(ctrl.getRootTreeNode()).isNotNull();
	}

	@Test
	public void viewModel_withNewSelectedNode_cleansUpExpandedAndSetsPlace() throws Exception {
		BoroughsStemmestyreConfigurationController ctrl = ctrl();
		stub_findRegularPollingDistrictsByArea(2);
		NodeSelectEvent event = createMock(NodeSelectEvent.class);
		ctrl.init();
		when(event.getTreeNode()).thenReturn(ctrl.getRootTreeNode().getChildren().get(1).getChildren().get(0));

		ctrl.viewModel(event);

		assertThat(ctrl.getRootTreeNode().getChildren().get(0).isExpanded()).isFalse();
		assertThat(ctrl.getRootTreeNode().getChildren().get(1).isExpanded()).isTrue();
		assertThat(ctrl.getSelectId(ctrl.getPollingDistrict())).isEqualTo("1001-RegularPollingDistrict 1001");
	}

	private BoroughsStemmestyreConfigurationController ctrl() throws Exception {
		return ctrl(initializeMocks(new BoroughsStemmestyreConfigurationController() {
			@Override
			boolean isHasBoroughs() {
				return true;
			}
		}), MUNICIPALITY);
	}

	private PollingDistrictService getPollingDistrictService() throws Exception {
		return getPrivateField("pollingDistrictService", PollingDistrictService.class);
	}

	private List<PollingDistrict> stub_findRegularPollingDistrictsByArea(int size) throws Exception {
		return stub_findRegularPollingDistrictsByArea(size, true);
	}

	private List<PollingDistrict> stub_findRegularPollingDistrictsByArea(int size, boolean isHasResponsibleOffiers) throws Exception {
		List<PollingDistrict> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			PollingDistrict pollingDistrict = regularPollingDistrict("100" + i, PollingDistrictType.REGULAR);
			pollingDistrict.setHasResponsibleOffiers(isHasResponsibleOffiers);
			pollingDistrict.setBorough(borough("10000" + i, "Borough " + i));

			list.add(pollingDistrict);
		}
		when(getPollingDistrictService().findRegularPollingDistrictsByArea(eq(getUserDataMock()), any(AreaPath.class), anyBoolean())).thenReturn(list);
		return list;
	}

	@Override
	Borough borough(String id, String name) {
		Borough result = super.borough(id, name);
		when(result.getPath()).thenReturn(AreaPath.from(MUNICIPALITY + "." + id));
		return result;
	}
}

