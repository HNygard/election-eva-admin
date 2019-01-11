package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.service.rbac.RoleService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.FindByIdRequest;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.CircularReferenceCheckRequest;
import no.valg.eva.admin.common.rbac.PersistRoleResponse;
import no.valg.eva.admin.common.rbac.Role;
import no.valg.eva.admin.common.rbac.service.AccessService;
import org.primefaces.model.TreeNode;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oppheve;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Styrer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RoleControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_verifyState() throws Exception {
		RoleController ctrl = ctrl();

		assertThat(ctrl.getRoleList()).hasSize(3);
		assertThat(ctrl.getRoleList().get(0).getName()).isEqualTo("1 name");
		assertThat(ctrl.getRoleList().get(1).getName()).isEqualTo("2 name");
		assertThat(ctrl.getRoleList().get(2).getName()).isEqualTo("3 name");
	}

	@Test
	public void openCreateDialog_verifyState() throws Exception {
		RoleController ctrl = ctrl();

		ctrl.openCreateDialog();

		assertThat(ctrl.getSelectedNodes()).isNull();
		assertThat(ctrl.getTreeRoot()).isNotNull();
		assertThat(ctrl.getTreeRoot().getChildren()).hasSize(3);
		assertRole(ctrl.getCurrentRole(), "", "", 3, true, false);
		assertThat(ctrl.getIncludedRolesList()).isNotNull();
		verify(getRequestContextMock()).execute("PF('createRoleDialog').show()");
	}

	@Test
	public void createRole_withNewRoleAndErrors_returnsErrorMessage() throws Exception {
		RoleController ctrl = ctrl();
		ctrl.openCreateDialog();
		TreeNode[] selectedAccesses = { ctrl.getTreeRoot().getChildren().get(0) };
		ctrl.setSelectedNodes(selectedAccesses);
		stub_saveRole(singletonList("@error"));

		ctrl.createRole();

		assertFacesMessage(SEVERITY_ERROR, "@error");
	}

	@Test
	public void createRole_withNewRoleAndNoErrors_createsNewRoleAndClosesDialog() throws Exception {
		RoleController ctrl = ctrl();
		ctrl.openCreateDialog();
		TreeNode[] selectedAccesses = { ctrl.getTreeRoot().getChildren().get(0) };
		ctrl.setSelectedNodes(selectedAccesses);
		stub_saveRole(new ArrayList<>());

		ctrl.createRole();

		verify(getRequestContextMock()).execute("PF('createRoleDialog').hide()");
	}

	@Test
	public void openEditDialog_withRole_verifyState() throws Exception {
		RoleController ctrl = ctrl();
		stub_findRoleWithAccessesForView();
		stub_findIncludedRolesForView();

		ctrl.openEditDialog(createMock(Role.class));

		assertThat(ctrl.getTreeRoot().getChildren().get(1).isSelected()).isTrue();
		verify(getRequestContextMock()).execute("PF('editRoleDialog').show()");
	}

	@Test
	public void editRole_withRoleAndErrors_returnsErrorMessage() throws Exception {
		RoleController ctrl = ctrl();
		stub_findRoleWithAccessesForView();
		stub_findIncludedRolesForView();
		ctrl.openEditDialog(createMock(Role.class));
		TreeNode[] selectedAccesses = { ctrl.getTreeRoot().getChildren().get(0) };
		ctrl.setSelectedNodes(selectedAccesses);
		stub_saveRole(singletonList("@error"));

		ctrl.editRole();

		assertFacesMessage(SEVERITY_ERROR, "@error");
	}

	@Test
	public void editRole_withRoleAndNoErrors_updatesRoleAndClosesDialog() throws Exception {
		RoleController ctrl = ctrl();
		stub_findRoleWithAccessesForView();
		stub_findIncludedRolesForView();
		ctrl.openEditDialog(createMock(Role.class));
		TreeNode[] selectedAccesses = { ctrl.getTreeRoot().getChildren().get(0) };
		ctrl.setSelectedNodes(selectedAccesses);
		stub_saveRole(new ArrayList<>());

		ctrl.editRole();

		verify(getRequestContextMock()).execute("PF('editRoleDialog').hide()");
	}

	@Test
	public void openConfirmRemoveIncludedDialog_opensDialog() throws Exception {
		RoleController ctrl = ctrl();

		ctrl.openConfirmRemoveIncludedDialog();

		verify(getRequestContextMock()).execute("PF('confirmRemoveIncludedRoleDialog').show()");
	}

	@Test
	public void openConfirmDeleteDialog_opensDialog() throws Exception {
		RoleController ctrl = ctrl();

		ctrl.openConfirmDeleteDialog(createMock(Role.class));

		verify(getRequestContextMock()).execute("PF('confirmDeleteRoleDialog').show()");
	}

	@Test
	public void addIncludedRole_withNotCreateAndCircularReference_verifyCircularReferenceErrorTitle() throws Exception {
		RoleController ctrl = initializeMocks(RoleController.class);
		Role role = new Role();
		role.setPk(0L);
		ctrl.setCurrentRole(role);
		ctrl.setNewIncludedRoleId("");
		when(getInjectMock(RoleService.class).isCircularReference(eq(getUserDataMock()), any(CircularReferenceCheckRequest.class))).thenReturn(true);

		ctrl.addIncludedRole();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@rbac.role.includedRoles.circularReferenceErrorTitle");
	}

	@Test
	public void addIncludedRole_withCreate_verifyRoleList() throws Exception {
		RoleController ctrl = initializeMocks(RoleController.class);
		Role role = new Role();
		role.setId("sameRoleId");
		ctrl.setCurrentRole(role);
		ctrl.setNewIncludedRoleId("sameRoleId");
		when(getInjectMock(RoleService.class).findForViewById(any(), any())).thenReturn(role);				
		
		ctrl.addIncludedRole();

		assertThat(ctrl.getIncludedRolesList().size()).isEqualTo(1);

		// Run again to trigger duplicate code
		ctrl.addIncludedRole();

		assertThat(ctrl.getIncludedRolesList().size()).isEqualTo(1);
	}

	@Test
	public void deleteRole_withRole_roleListLengthShouldBeOne() throws Exception {
		RoleController ctrl = initializeMocks(RoleController.class);
		Set<Access> accesses = new HashSet<>(singletonList(getAccess("/")));
		Role role = getRole(1L, accesses);
		ctrl.setCurrentRole(role);
		when(getInjectMock(RoleService.class).findAllRolesWithoutAccessesForView(getUserDataMock())).thenReturn(new ArrayList<>(singletonList(role)));

		ctrl.deleteRole();

		assertThat(ctrl.getRoleList().size()).isEqualTo(1);
	}

	private RoleController ctrl() throws Exception {
		RoleController ctrl = initializeMocks(RoleController.class);
		stub_findAllRolesWithoutAccessesForView();
		stub_accessService_findAll();
		ctrl.doInit();
		return ctrl;
	}

	private Access getAccess(String path) {
		return new Access(path);
	}

	private Role getRole(Long pk, Set<Access> accesses) {
		Role role = new Role();
		role.setPk(pk);
		role.setAccesses(accesses);
		role.setName(pk + " name");
		return role;
	}

	private List<Role> stub_findAllRolesWithoutAccessesForView() {
		List<Role> roles = asList(
				getRole(2L, new HashSet<>(singletonList(getAccess(Konfigurasjon_EML.paths()[0])))),
				getRole(3L, new HashSet<>(singletonList(getAccess(Konfigurasjon_Grunnlagsdata_Oppheve.paths()[0])))),
				getRole(1L, new HashSet<>(singletonList(getAccess(Konfigurasjon_Styrer.paths()[0])))));
		when(getInjectMock(RoleService.class).findAllRolesWithoutAccessesForView(getUserDataMock())).thenReturn(roles);
		return roles;
	}

	private List<Access> stub_accessService_findAll() {
		List<Access> result = asList(
				getAccess(Konfigurasjon_Styrer.paths()[0]),
				getAccess(Konfigurasjon_Grunnlagsdata_Oppheve.paths()[0]),
				getAccess(Konfigurasjon_EML.paths()[0]));
		when(getInjectMock(AccessService.class).findAll(getUserDataMock())).thenReturn(result);
		return result;
	}

	private void assertRole(Role role, String name, String id, int securityLevel, boolean active, boolean mutex) {
		assertThat(role.getName()).isEqualTo(name);
		assertThat(role.getId()).isEqualTo(id);
		assertThat(role.getSecurityLevel()).isEqualTo(securityLevel);
		assertThat(role.isActive()).isEqualTo(active);
		assertThat(role.isMutuallyExclusive()).isEqualTo(mutex);
	}

	private void stub_saveRole(List<String> validationFeedback) {
		PersistRoleResponse response = createMock(PersistRoleResponse.class);
		when(response.getValidationFeedback()).thenReturn(validationFeedback);
		when(getInjectMock(RoleService.class).persistRole(eq(getUserDataMock()), any(Role.class))).thenReturn(response);
		when(getInjectMock(RoleService.class).updateRole(eq(getUserDataMock()), any(Role.class))).thenReturn(validationFeedback);
	}

	private void stub_findRoleWithAccessesForView() {
		List<Access> accesses = singletonList(getAccess(Konfigurasjon_Grunnlagsdata_Oppheve.paths()[0]));
		Role role = createMock(Role.class);
		when(role.getAccesses()).thenReturn(new HashSet<>(accesses));
		when(getInjectMock(RoleService.class).findRoleWithAccessesForView(eq(getUserDataMock()), any(FindByIdRequest.class))).thenReturn(role);
	}

	private void stub_findIncludedRolesForView() {
		List<Access> accesses = singletonList(getAccess(Konfigurasjon_Styrer.paths()[0]));
		Role role = createMock(Role.class);
		when(role.getAccesses()).thenReturn(new HashSet<>(accesses));
		Set<Role> roles = new HashSet<>();
		roles.add(role);
		when(getInjectMock(RoleService.class).findIncludedRolesForView(eq(getUserDataMock()), any(FindByIdRequest.class))).thenReturn(roles);
	}

}

