package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistricts;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import org.primefaces.model.DefaultTreeNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ParentPollingDistrictConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_verifyState() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		stub_findParentPollingDistrictsByArea(parentPollingDistricts(2, 2, 2));

		ctrl.init();

		verifyInit(ctrl);
	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(ButtonType buttonType, boolean isEditable, boolean hasSelected,
			boolean hasSelectedNode, boolean isRendered, boolean isDisabled) throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl(isEditable);
		if (hasSelected) {
			ctrl.getSelected().add(regularPollingDistrict("1000", PollingDistrictType.REGULAR));
		}
		if (hasSelectedNode) {
			ctrl.setSelectedNode(createMock(DefaultTreeNode.class));
		}

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.CREATE, false, false, false, true, true },
				{ ButtonType.CREATE, true, false, false, true, true },
				{ ButtonType.CREATE, false, true, false, true, true },
				{ ButtonType.CREATE, true, true, false, true, false },
				{ ButtonType.DELETE, false, false, false, true, true },
				{ ButtonType.DELETE, true, false, false, true, true },
				{ ButtonType.DELETE, false, false, true, true, true },
				{ ButtonType.DELETE, true, false, true, true, false }
		};
	}

	@Test
	public void createParentPollingDistrict_withNoParent_returns() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();

		ctrl.createParentPollingDistrict();

		verify(getInjectMock(PollingDistrictService.class), never()).saveParentPollingDistrict(any(UserData.class), any(ParentPollingDistrict.class));
	}

	@Test
	public void createParentPollingDistrict_withSelected_verifyCreate() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		stub_findParentPollingDistrictsByArea(parentPollingDistricts(2, 2, 2));
		ctrl.getSelected().add(regularPollingDistrict("1000", PollingDistrictType.REGULAR));
		ParentPollingDistrict parent = parentPollingDistrict("2000");
		ctrl.setNewParentPollingDistrict(parent);
		when(ctrl.getMunicipalityConfigStatus().isPollingDistricts()).thenReturn(true);

		ctrl.createParentPollingDistrict();

		assertThat(parent.getChildren()).hasSize(1);
		verifySaveConfigStatus(true);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.parentpollingdistrict ParentPollingDistrict 2000]");
		verify(getInjectMock(PollingDistrictService.class)).saveParentPollingDistrict(getUserDataMock(), parent);
		verifySaveConfigStatus();
		verifyInit(ctrl);
	}

	@Test
	public void confirmDelete_withNoSelected_returns() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();

		ctrl.confirmDelete();

		verify(getInjectMock(PollingDistrictService.class), never()).deleteParentPollingDistrict(any(UserData.class), any(ParentPollingDistrict.class));
	}

	@Test
	public void confirmDelete_withSelected_verifyDelete() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		stub_findParentPollingDistrictsByArea(parentPollingDistricts(2, 2, 2));
		ctrl.init();
		DefaultTreeNode node = createMock(DefaultTreeNode.class);
		when(node.getData()).thenReturn(parentPollingDistrict("1000"));
		ctrl.setSelectedNode(node);

		ctrl.confirmDelete();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.deleted, @common.displayable.parentpollingdistrict ParentPollingDistrict 1000]");
		verify(getInjectMock(PollingDistrictService.class)).deleteParentPollingDistrict(any(UserData.class), any(ParentPollingDistrict.class));
		verifyInit(ctrl);
	}

	@Test
	public void saveDone_withParents_opensConfirmDeleteDialog() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		stub_findParentPollingDistrictsByArea(parentPollingDistricts(2, 2, 2));
		ctrl.init();
		ctrl.setUsingParentPollingDistrict(false);

		ctrl.saveDone();

		verify(ctrl.getConfirmDeleteAllParentPollingDistrictsDialog()).open();
	}

	@Test
	public void confirmSaveDone_withParents_verifyDelete() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		stub_findParentPollingDistrictsByArea(parentPollingDistricts(2, 2, 2));
		ctrl.init();

		ctrl.confirmSaveDone();

		verifySaveConfigStatus();
		verify(getInjectMock(PollingDistrictService.class), times(2)).deleteParentPollingDistrict(eq(getUserDataMock()), any(ParentPollingDistrict.class));
		verify(ctrl.getConfirmDeleteAllParentPollingDistrictsDialog()).closeAndUpdate("configurationPanel", "approve-form");
		verifyInit(ctrl);
	}

	@Test
	public void getView_returnsParentPollingDistrictView() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.PARENT_POLLING_DISTRICT);
	}

	@Test
	public void getName_returnsParentPollingDistrictView() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.parent_polling_district.name");
	}

	@Test(dataProvider = "canBeSetToDone")
	public void button_withDataProvider_verifyExpected(boolean usingParentPollingDistrict, boolean isValid, boolean expected) throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		mockFieldValue("usingParentPollingDistrict", usingParentPollingDistrict);
		ParentPollingDistricts districts = mockField("parentPollingDistricts", ParentPollingDistricts.class);
		when(districts.isValid()).thenReturn(isValid);

		assertThat(ctrl.canBeSetToDone()).isEqualTo(expected);
	}

	@DataProvider(name = "canBeSetToDone")
	public Object[][] canBeSetToDone() {
		return new Object[][] {
				{ false, false, true },
				{ true, false, false },
				{ true, true, true }
		};
	}

	@Test
	public void hasAccess_withoutAccess_returnsFalse() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.hasAccess()).isFalse();
	}

	@Test
	public void hasAccess_withAccessAndValgtingsstemmerSentraltSamlet_returnsFalse() throws Exception {
		ParentPollingDistrictConfigurationController ctrl = ctrl();
		hasAccess(Konfigurasjon_Geografi);
		ReportCountCategoriesConfigurationController countCtrl = addControllerToMainController(ctrl, ReportCountCategoriesConfigurationController.class);
		when(countCtrl.isValgtingsstemmerSentraltSamlet()).thenReturn(true);

		assertThat(ctrl.hasAccess()).isFalse();
	}

	private ParentPollingDistrictConfigurationController ctrl() throws Exception {
		return ctrl(true);
	}

	private ParentPollingDistrictConfigurationController ctrl(boolean isEditable) throws Exception {
		return ctrl(initializeMocks(new ParentPollingDistrictConfigurationController() {

			private Dialog dialog = createMock(Dialog.class);

			@Override
			public boolean isEditable() {
				return isEditable;
			}

			@Override
			public Dialog getConfirmDeleteAllParentPollingDistrictsDialog() {
				return dialog;
			}
		}), MUNICIPALITY);
	}

	private void stub_findParentPollingDistrictsByArea(ParentPollingDistricts districts) {
		when(getInjectMock(PollingDistrictService.class).findParentPollingDistrictsByArea(getUserDataMock(), MUNICIPALITY)).thenReturn(districts);
	}

	private ParentPollingDistricts parentPollingDistricts(int parents, int children, int regulars) {
		ParentPollingDistricts result = createMock(ParentPollingDistricts.class);
		List<ParentPollingDistrict> parentsList = new ArrayList<>();
		for (int i = 0; i < parents; i++) {
			ParentPollingDistrict parent = parentPollingDistrict(String.valueOf(1000 + i));
			parentsList.add(parent);
			for (int j = 0; j < children; j++) {
				parent.getChildren().add(regularPollingDistrict(String.valueOf(2000 + j), PollingDistrictType.REGULAR));
			}
		}
		List<RegularPollingDistrict> regularList = new ArrayList<>();
		for (int i = 0; i < regulars; i++) {
			regularList.add(regularPollingDistrict(String.valueOf(1000 + i), PollingDistrictType.REGULAR));
		}
		when(result.getParentPollingDistricts()).thenReturn(parentsList);
		when(result.getSelectableDistricts()).thenReturn(regularList);
		return result;
	}

	private void verifyInit(ParentPollingDistrictConfigurationController ctrl) {
		assertThat(ctrl.getSelected()).isEmpty();
		assertThat(ctrl.getNewParentPollingDistrict()).isNotNull();
		assertThat(ctrl.isUsingParentPollingDistrict()).isTrue();
		assertThat(ctrl.getPlaces()).hasSize(2);
		assertThat(ctrl.getPlaceConverter()).isNotNull();
		DefaultTreeNode tree = ctrl.getParentPollingDistrictsTree();
		assertThat(tree.getChildCount()).isEqualTo(2);
		assertThat(tree.getChildren().get(0).getChildCount()).isEqualTo(2);
		assertThat(tree.getChildren().get(1).getChildCount()).isEqualTo(2);
	}

}

