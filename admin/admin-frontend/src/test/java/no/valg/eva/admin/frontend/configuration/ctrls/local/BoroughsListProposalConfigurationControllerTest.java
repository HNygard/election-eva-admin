package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.common.configuration.service.ListProposalService;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.testng.annotations.Test;


public class BoroughsListProposalConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_withCountyAreaAndMessages_shouldGetConfigObjectAndReturnMessages() throws Exception {
		BoroughsListProposalConfigurationController ctrl = ctrl();
		ListProposalConfig config = listProposalConfig(true, true);
		when(config.isCountStarted()).thenReturn(true);
		when(getInjectMock(ListProposalService.class).findByArea(getUserDataMock(), ctrl.getAreaPath())).thenReturn(config);

		ctrl.init();

		assertThat(ctrl.getListProposal()).isNotNull();
		assertThat(ctrl.getRootTreeNode()).isNotNull();
		assertThat(ctrl.getSelectedTreeNode()).isNotNull();
		assertThat(ctrl.getRootTreeNode().getChildren()).hasSize(1);
		assertThat(ctrl.getRootTreeNode().getChildren().get(0).getChildren()).hasSize(1);
		assertFacesMessage(FacesMessage.SEVERITY_WARN, "@listProposal.lockedBeacuaseOfCountStarted");
	}

	@Test
	public void getView_returnsListProposalView() throws Exception {
		assertThat(ctrl().getView()).isSameAs(ConfigurationView.BOROUGHS_LIST_PROPOSAL);
	}

	@Test
	public void viewModel_withLeafNode_expandsParent() throws Exception {
		BoroughsListProposalConfigurationController ctrl = initializeMocks(BoroughsListProposalConfigurationController.class);
		NodeSelectEvent event = createMock(NodeSelectEvent.class);
		TreeNode node = event.getTreeNode();
		when(node.isLeaf()).thenReturn(true);
		ListProposalConfig config = createMock(ListProposalConfig.class);
		when(node.getData()).thenReturn(config);

		ctrl.viewModel(event);

		verify(node.getParent()).setExpanded(true);
		assertThat(ctrl.getListProposal()).isSameAs(config);

	}

	private BoroughsListProposalConfigurationController ctrl() throws Exception {
		return ctrl(true);
	}

	private BoroughsListProposalConfigurationController ctrl(boolean isEditable) throws Exception {
		return ctrl(MUNICIPALITY, isEditable);
	}

	private BoroughsListProposalConfigurationController ctrl(AreaPath areaPath, boolean isEditable) throws Exception {
		return ctrl(initializeMocks(new BoroughsListProposalConfigurationController() {
			@Override
			public boolean parentIsEditable() {
				return isEditable;
			}

			@Override
			boolean isHasBoroughs() {
				return true;
			}
		}), areaPath);
	}

	private ListProposalConfig listProposalConfig(boolean isValid, boolean isValidChildren) {
		ListProposalConfig config = createMock(ListProposalConfig.class);
		when(config.isValid()).thenReturn(isValid);
		List<ListProposalConfig> children = new ArrayList<>();
		children.add(createMock(ListProposalConfig.class));
		when(children.get(0).isValid()).thenReturn(isValidChildren);
		when(config.getChildren()).thenReturn(children);
		return config;
	}

}

