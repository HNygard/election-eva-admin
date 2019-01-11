package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OperatorEditControllerTest extends BaseRbacTest {

	private static final String ROLE1 = "role1";
	private static final String COUNTY = "county";
	private static final String MUNICIP = "municip";

	@Test
	public void init_withOperator_verifyInitialState() throws Exception {
		OperatorEditController ctrl = defaultSetup();

		assertThat(ctrl.getOperator()).isNotNull();
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.EDIT);
		verify(getInjectMock(AreaOptions.class)).init(getUserDataMock().getOperatorAreaPath());
		verify(getInjectMock(RoleOptions.class)).init(getUserDataMock().getOperatorAreaPath());
	}

	private OperatorEditController defaultSetup() throws Exception {
		return defaultSetup(false, RbacView.EDIT, BRUKER_ID_1);
	}

	private OperatorEditController defaultSetup(boolean rootLevel, RbacView view, String brukerId) throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		Operator operator = operator(brukerId, Collections.singletonList(roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER)));
		when(getInjectMock(OperatorAdminController.class).getView()).thenReturn(RbacView.LIST);
		if (rootLevel) {
			when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_VALG.getAreaPath());
		} else {
			when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_LUNNER.getAreaPath());
		}
		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(rootLevel);
		when(getInjectMock(OperatorAdminController.class).getView()).thenReturn(view);

		ctrl.init(operator, view);

		return ctrl;
	}
	@Test
	public void init_withOperatorAndRootLevel_verifyInitialState() throws Exception {
		OperatorEditController ctrl = defaultSetup(true);

		assertThat(ctrl.getOperator()).isNotNull();
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.EDIT);
		assertThat(ctrl.isRootLevel()).isTrue();
		assertThat(ctrl.getRootLevelEdit()).isNotNull();
		verify(getInjectMock(MvAreaService.class)).findByPathAndLevel(AREA_VALG.getAreaPath().path(), AreaLevelEnum.COUNTY.getLevel());
	}

	private OperatorEditController defaultSetup(boolean rootLevel) throws Exception {
		return defaultSetup(rootLevel, RbacView.EDIT);
	}

	private OperatorEditController defaultSetup(boolean rootLevel, RbacView rbacView) throws Exception {
		return defaultSetup(rootLevel, rbacView, BRUKER_ID_1);
	}
	
	@Test
	public void getPageHeader_withNewMode_returnsNewHeader() throws Exception {
		OperatorEditController ctrl = defaultSetup(RbacView.NEW);

		assertThat(ctrl.getPageHeader()).isEqualTo("@rbac.add.operator");
	}

	private OperatorEditController defaultSetup(RbacView view) throws Exception {
		return defaultSetup(false, view);
	}

	@Test
	public void getPageHeader_withEditMode_returnsEditHeader() throws Exception {
		OperatorEditController ctrl = defaultSetup(RbacView.EDIT);

		assertThat(ctrl.getPageHeader()).isEqualTo("@rbac.edit.operator");
	}

	@Test
	public void isNewCandidate_withNew_returnsTrue() throws Exception {
		OperatorEditController ctrl = defaultSetup(RbacView.NEW);

		assertThat(ctrl.isNewCandidate()).isTrue();
	}

	@Test
	public void deleteSelectedRoleAssociation_withAssociation_verifyDeleted() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);

		ctrl.deleteSelectedRoleAssociation(roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER));

		assertThat(ctrl.getEditRoleAssociations()).isEmpty();
	}

	@Test
	public void createOperator_withOperator_verifyCreate() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		Operator operator = createMock(Operator.class);
		AreaPath areaPath = AREA_LUNNER.getAreaPath();
		when(getInjectMock(AdminOperatorService.class).updateOperator(eq(getUserDataMock()), eq(ctrl.getOperator()),
				eq(areaPath), anyCollection(), anyCollection())).thenReturn(operator);

		ctrl.createOperator();

		verify(getInjectMock(OperatorListController.class)).updated(operator);
		verify(getInjectMock(OperatorCreatedController.class)).init(operator);
		assertThat(ctrl.getOperator()).isNull();
	}

	@Test
	public void cancelEdit_withCandidate_verifyFromView() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		mockFieldValue("fromView", RbacView.CREATED);

		ctrl.cancelEdit();

		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.LIST);
		assertThat(ctrl.getOperator()).isNull();
	}

	@Test
	public void roleChanged_withRootLevel_verifyReset() throws Exception {
		OperatorEditController ctrl = defaultSetup(true);
		ctrl.getRootLevelEdit().setCounty("test");
		ctrl.getRootLevelEdit().setMunicipality("test");
		when(getInjectMock(RoleOptions.class).getRoleMap().get(anyString())).thenReturn(createMock(RoleItem.class));

		ctrl.roleChanged();

		assertThat(ctrl.getRootLevelEdit().getCounty()).isNull();
		assertThat(ctrl.getRootLevelEdit().getMunicipality()).isNull();
	}

	@Test
	public void isEditMode_withNewMode_returnsFalse() throws Exception {
		OperatorEditController ctrl = defaultSetup(RbacView.NEW);

		assertThat(ctrl.isEditMode()).isFalse();
	}

	@Test
	public void saveOperator_withReplacedRoleAssociation_verifyReplacement() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		List<RoleAssociation> editRoleAssociations = Collections.singletonList(roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE));
		mockFieldValue("editRoleAssociations", editRoleAssociations);

		ctrl.saveOperator();

		ArgumentCaptor<Collection> addedCaptor = ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<Collection> deletedCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(getInjectMock(AdminOperatorService.class)).updateOperator(any(UserData.class), any(Operator.class), any(AreaPath.class), addedCaptor.capture(),
				deletedCaptor.capture());
		assertThat(addedCaptor.getValue()).hasSize(1);
		assertThat(deletedCaptor.getValue()).hasSize(1);
		verify(getInjectMock(OperatorListController.class)).updated(any(Operator.class));
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.LIST);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "last, first @operation.updated");
	}

	@Test
	public void saveOperator_withException_returnsErrorMessage() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		evoteExceptionWhen(AdminOperatorService.class, "@validiation.error").updateOperator(eq(getUserDataMock()), any(Operator.class),
				any(AreaPath.class), anyCollection(), anyCollection());

		ctrl.saveOperator();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@validiation.error");
	}

	@Test(dataProvider = "isRenderSelectArea")
	public void isRenderSelectArea_withDataProvider_verifyExpected(boolean rootLevel, String roleId, String selectedRole, String county, String municipality,
			boolean expected) throws Exception {
		OperatorEditController ctrl = defaultSetup(rootLevel);
		ctrl.setSelectedRoleToAdd(selectedRole);
		if (rootLevel) {
			ctrl.getRootLevelEdit().setCounty(county);
			ctrl.getRootLevelEdit().setMunicipality(municipality);
		}
		Map<String, RoleItem> roleMap = new HashMap<>();
		RoleItem roleItem = createMock(RoleItem.class);
		when(roleItem.getPermittedAreaLevels()).thenReturn(Collections.singletonList(AreaLevelEnum.MUNICIPALITY));
		roleMap.put(selectedRole, roleItem);
		when(getInjectMock(RoleOptions.class).getRoleMap()).thenReturn(roleMap);

		assertThat(ctrl.isRenderSelectArea(roleId)).isEqualTo(expected);
	}

	@DataProvider(name = "isRenderSelectArea")
	public Object[][] isRenderSelectArea() {
		return new Object[][] {
			{ false, ROLE1, null, null, null, false },
			{ false, ROLE1, ROLE1, null, null, true },
			{ true, ROLE1, ROLE1, null, null, false },
			{ true, ROLE1, ROLE1, COUNTY, null, false },
			{ true, ROLE1, ROLE1, COUNTY, MUNICIP, true }
		};
	}

	@Test(dataProvider = "isAddSelectedRoleButtonDisabled")
	public void isAddSelectedRoleButtonDisabled_withDataProvider_verifyExpected(boolean rootLevel, boolean hasAreasForRole, String county,
			String municipality, boolean expected) throws Exception {
		OperatorEditController ctrl = defaultSetup(rootLevel);
		when(getInjectMock(AreaOptions.class).hasAreasForRole(anyString())).thenReturn(hasAreasForRole);
		if (rootLevel) {
			ctrl.getRootLevelEdit().setCounty(county);
			ctrl.getRootLevelEdit().setMunicipality(municipality);
		}

		assertThat(ctrl.isAddSelectedRoleButtonDisabled()).isEqualTo(expected);
	}

	@DataProvider(name = "isAddSelectedRoleButtonDisabled")
	public Object[][] isAddSelectedRoleButtonDisabled() {
		return new Object[][] {
			{ false, false, null, null, true },
			{ true, false, null, null, true },
			{ true, true, null, null, true },
			{ true, true, COUNTY, null, true },
			{ true, true, COUNTY, MUNICIP, false }
		};
	}
	
	@Test
	public void isRenderNoAreasAvailableText_withAreaOptionsHasAreasForRole_returnsFalse() throws Exception {
		OperatorEditController ctrl = defaultSetup();
        when(getInjectMock(AreaOptions.class).hasAreasForRole(any())).thenReturn(true);

		assertThat(ctrl.isRenderNoAreasAvailableText()).isEqualTo(false);
	}

	@Test
	public void addSelectedRole_withNewRole_verifyAdded() throws Exception {
		OperatorEditController ctrl = setupAddSelectedRole(ROLE_ANSVARLIG_URNETELLING, AREA_LUNNER_DISTRICT);
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);

		ctrl.addSelectedRole();

		assertThat(ctrl.getEditRoleAssociations()).hasSize(2);
	}

	private OperatorEditController setupAddSelectedRole(RoleItem roleToAdd, PollingPlaceArea areaToAdd) throws Exception {
		boolean rootLevel = roleToAdd.getPermittedAreaLevels().get(0) == AreaLevelEnum.ROOT;
		OperatorEditController ctrl = defaultSetup(rootLevel, RbacView.EDIT);
		Map<String, String> selectedRoleAreaToAdd = new HashMap<>();
		selectedRoleAreaToAdd.put(roleToAdd.getRoleId(), areaToAdd.getAreaPath().path());
		ctrl.setSelectedRoleToAdd(roleToAdd.getRoleId());
		ctrl.setSelectedRoleAreaToAdd(selectedRoleAreaToAdd);
		when(getInjectMock(RoleOptions.class).getRoleMap().get(anyString())).thenReturn(roleToAdd);
		when(getInjectMock(AreaOptions.class).getAreaMap().get(areaToAdd.getAreaPath().path())).thenReturn(areaToAdd);
		return ctrl;
	}

	@Test
	public void addSelectedRole_withNewRoleAndRootLevel_verifyAdded() throws Exception {
		OperatorEditController ctrl = setupAddSelectedRole(ROLE_VALGADMIN, AREA_VALG);
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);

		ctrl.addSelectedRole();

		assertThat(ctrl.getEditRoleAssociations()).hasSize(2);
		verify(getUserDataMock()).getOperatorMvArea();
	}

	@Test
	public void addSelectedRole_withExistingRole_returnsMessage() throws Exception {
		OperatorEditController ctrl = setupAddSelectedRole(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER);
		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);

		ctrl.addSelectedRole();

		assertThat(ctrl.getEditRoleAssociations()).hasSize(1);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@rbac.import_operators.has_role");
	}

	@Test
	public void deleteOperator_withOperator_verifyState() throws Exception {
		OperatorEditController ctrl = defaultSetup();

		ctrl.deleteOperator();

		verify(getInjectMock(AdminOperatorService.class)).deleteOperator(eq(getUserDataMock()), any(Operator.class));
		verify(getInjectMock(OperatorListController.class)).removed(any(Operator.class));
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.LIST);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "last, first @operation.deleted");
	}

	@Test
	public void deleteOperator_withException_returnsErrorMessage() throws Exception {
		OperatorEditController ctrl = defaultSetup();
		evoteExceptionWhen(AdminOperatorService.class).deleteOperator(eq(getUserDataMock()), any(Operator.class));

		ctrl.deleteOperator();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.delete.unsuccessful");
	}

	@Test
	public void erRedigeringAvEgenBruker_hvisRedigererPaaEgenBruker_returnererTrue() throws Exception {
		OperatorEditController ctrl = defaultSetup(false, RbacView.EDIT, BRUKER_ID_1);
		when(getUserDataMock().getOperator().getId()).thenReturn(BRUKER_ID_1);
		
		assertThat(ctrl.erRedigeringAvEgenBruker()).isTrue();
	}
	
	@Test
	public void erRedigeringAvEgenBruker_hvisRedigererPaaAnnenBruker_returnererFalse() throws Exception {
		OperatorEditController ctrl = defaultSetup(false, RbacView.EDIT, BRUKER_ID_1);
		when(getUserDataMock().getOperator().getId()).thenReturn(BRUKER_ID_2);

		assertThat(ctrl.erRedigeringAvEgenBruker()).isFalse();
	}
}
