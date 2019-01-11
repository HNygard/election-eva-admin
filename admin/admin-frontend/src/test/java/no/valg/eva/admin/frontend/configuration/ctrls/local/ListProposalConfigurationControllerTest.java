package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.common.configuration.service.ListProposalService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ListProposalConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_withCountyAreaAndMessages_shouldGetConfigObjectAndReturnMessages() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();
		ListProposalConfig config = stub_ListProposalConfig(true, true);
		when(getInjectMock(ListProposalService.class).findByArea(getUserDataMock(), ctrl.getAreaPath())).thenReturn(config);

		ctrl.init();

		assertThat(ctrl.getListProposal()).isNotNull();
		assertFacesMessage(FacesMessage.SEVERITY_WARN, "@listProposal.lockedBeacuaseOfCountStarted");
	}

	@Test(dataProvider = "isEditable")
	public void isEditable_withDataProvider_verifyExpected(boolean parentIsEditable, boolean isCountStarted, boolean expected)
			throws Exception {
		ListProposalConfigurationController ctrl = ctrl(parentIsEditable);
		stub_ListProposalConfig(isCountStarted, true);

		assertThat(ctrl.isEditable()).isEqualTo(expected);
	}

	@DataProvider(name = "isEditable")
	public Object[][] isEditable() {
		return new Object[][] {
				{ false, false, false },
				{ true, true, false },
				{ false, true, false },
				{ true, false, true }
		};
	}

	@Test
	public void button_withDoneAndEditable_returnsVisibleButton() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne()).thenReturn(true);
		stub_ListProposalConfig(false, true);

		Button saveButton = ctrl.button(ButtonType.DONE);

		assertThat(saveButton.isRendered()).isTrue();
		assertThat(saveButton.isDisabled()).isFalse();

	}

	@Test
	public void saveDone_withEditable_verifySave() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();
		ListProposalConfig config = stub_ListProposalConfig(false, true);
		when(ctrl.getCountyConfigStatus().isListProposals()).thenReturn(false);

		ctrl.saveDone();

		verifySaveConfigStatus();
		verify(getInjectMock(ListProposalService.class)).save(getUserDataMock(), config, false);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.listproposalconfig null]");
	}

	@Test
	public void getName_returnsCorrectName() throws Exception {
		ListProposalConfigurationController ctrl = initializeMocks(ListProposalConfigurationController.class);

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.list_proposal.name");
	}

	@Test
	public void hasAccess_withAccessAndCorrectLevel_returnsTrue() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();
		ListProposalConfig config = stub_ListProposalConfig(true, true);
		when(config.isSingleArea()).thenReturn(true);
		when(getInjectMock(ListProposalService.class).findByArea(getUserDataMock(), ctrl.getAreaPath())).thenReturn(config);

		assertThat(ctrl.hasAccess()).isTrue();
	}

	@Test
	public void isDoneStatus_withNoConfigStatus_returnsFalse() throws Exception {
		ListProposalConfigurationController ctrl = initializeMocks(ListProposalConfigurationController.class);

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	@Test
	public void isDoneStatus_withCountyListProposalTrue_returnsTrue() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();
		when(ctrl.getCountyConfigStatus().isListProposals()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void isDoneStatus_withMunicipalityListProposalTrue_returnsTrue() throws Exception {
		ListProposalConfigurationController ctrl = ctrl(MUNICIPALITY);
		when(ctrl.getMunicipalityConfigStatus().isListProposals()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withTrue_setsListProposalStatus() throws Exception {
		ListProposalConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getCountyConfigStatus()).setListProposals(true);
	}

	@Test
	public void setDoneStatus_withMunicipality_setsListProposalStatus() throws Exception {
		ListProposalConfigurationController ctrl = ctrl(MUNICIPALITY);

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setListProposals(true);
	}

	@Test
	public void getView_returnsListProposalView() throws Exception {
		assertThat(ctrl().getView()).isSameAs(ConfigurationView.LIST_PROPOSAL);
	}

	private ListProposalConfigurationController ctrl() throws Exception {
		return ctrl(true);
	}

	private ListProposalConfigurationController ctrl(AreaPath areaPath) throws Exception {
		return ctrl(areaPath, true);
	}

	private ListProposalConfigurationController ctrl(boolean parentIsEditable) throws Exception {
		return ctrl(COUNTY, parentIsEditable);
	}

	private ListProposalConfigurationController ctrl(AreaPath areaPath, boolean parentIsEditable) throws Exception {
		return ctrl(initializeMocks(new ListProposalConfigurationController() {
			@Override
			public boolean parentIsEditable() {
				return parentIsEditable;
			}

			@Override
			boolean isHasBoroughs() {
				return false;
			}
		}), areaPath);
	}

	private ListProposalConfig stub_ListProposalConfig(boolean isCountStarted, boolean isValid)
			throws Exception {
		ListProposalConfig config = mockField("listProposal", ListProposalConfig.class);
		when(config.isCountStarted()).thenReturn(isCountStarted);
		when(config.isValid()).thenReturn(isValid);
		when(config.getChildren()).thenReturn(new ArrayList<>());
		return config;
	}

}

