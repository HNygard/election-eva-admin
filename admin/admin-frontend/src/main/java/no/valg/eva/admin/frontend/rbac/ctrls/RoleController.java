package no.valg.eva.admin.frontend.rbac.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_DELETE_ROLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_REMOVE_INCLUDED_ROLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CREATE_ROLE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.EDIT_ROLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.frontend.util.TreeUtil;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleService;
import no.evote.service.rbac.RoleService;
import no.valg.eva.admin.common.FindByIdRequest;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.CircularReferenceCheckRequest;
import no.valg.eva.admin.common.rbac.PersistRoleResponse;
import no.valg.eva.admin.common.rbac.Role;
import no.valg.eva.admin.common.rbac.service.AccessService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class RoleController extends BaseController {

	private static final int DEFAULT_SECURITY_LEVEL = 3;

	// Injected
	private AccessService accessService;
	private RoleService roleService;
	private OperatorRoleService operatorRoleService;
	private UserData userData;
	private MessageProvider messageProvider;

	private List<Role> includedRolesList;
	private List<Role> tempIncludedRolesList = new ArrayList<>();
	private Role selectedIncludedRole;
	private List<Role> roleList;
	private String newIncludedRoleId = null;
	private Role currentRole;
	private TreeNode treeRoot;
	private TreeNode[] selectedNodes;
	private Dialog currentEditDialog;
	private boolean changedTree = false;

	@SuppressWarnings("unused")
	public RoleController() {
		// CDI
	}

	@Inject
	public RoleController(UserData userData, MessageProvider messageProvider,
			AccessService accessService, RoleService roleService, OperatorRoleService operatorRoleService) {
		this.userData = userData;
		this.messageProvider = messageProvider;
		this.accessService = accessService;
		this.roleService = roleService;
		this.operatorRoleService = operatorRoleService;
	}

	@PostConstruct
	public void doInit() {
		reloadRoleList();
	}

	public void openCreateDialog() {
		selectedNodes = null;
		generateTree();
		currentRole = initCreateRole();
		tempIncludedRolesList = new ArrayList<>();
		reloadIncludedRolesList();
		open(getCreateRoleDialog());
	}

	public void createRole() {

		Set<Access> accesses = new HashSet<>();
		for (TreeNode node : selectedNodes) {
			accesses.add((Access) node.getData());
		}
		currentRole.setAccesses(accesses);
		currentRole.setMapAccesses(true);
		currentRole.setElectionEventPk(userData.getElectionEventPk());
		currentRole.setIncludedRoles(new HashSet<>(tempIncludedRolesList));

		execute(() -> {
			PersistRoleResponse persistRoleResponse = roleService.persistRole(userData, currentRole);
			if (handleValidationFeedback(persistRoleResponse.getValidationFeedback())) {
				currentRole = null;
				reloadRoleList();
				getCreateRoleDialog().closeAndUpdate("form:roleListTable");
			} else {
				reloadRoleList();
			}
		});
	}

	public void openEditDialog(Role role) {
		changedTree = false;
		currentRole = role;
		generateTree();
		TreeNode tempNode;

		FindByIdRequest findByIdRequest = new FindByIdRequest(currentRole.getId());
		for (Access a : roleService.findRoleWithAccessesForView(userData, findByIdRequest).getAccesses()) {
			tempNode = TreeUtil.getNodeFromData(getTreeRoot(), a);
			if (tempNode != null) {
				tempNode.setSelected(true);
			}
		}
		tempIncludedRolesList = new ArrayList<>(roleService.findIncludedRolesForView(userData, findByIdRequest));
		reloadIncludedRolesList();
		open(getEditRoleDialog());
	}

	public void editRole() {

		if (selectedNodes.length != 0 || changedTree) {
			Set<Access> newAccessSet = new HashSet<>();
			for (TreeNode node : selectedNodes) {
				newAccessSet.add((Access) node.getData());
			}
			currentRole.setAccesses(newAccessSet);
			currentRole.setMapAccesses(true);
		}
		currentRole.setIncludedRoles(new HashSet<>(tempIncludedRolesList));

		execute(() -> {
			if (handleValidationFeedback(roleService.updateRole(userData, currentRole))) {
				currentRole = null;
				reloadRoleList();
				getEditRoleDialog().closeAndUpdate("form:roleListTable");
			} else {
				reloadRoleList();
			}
		});
	}

	public void addIncludedRole() {
		boolean duplicate = false;
		CircularReferenceCheckRequest circularReferenceCheckRequest = new CircularReferenceCheckRequest(currentRole, newIncludedRoleId);
		if (!isCreate() && roleService.isCircularReference(userData, circularReferenceCheckRequest)) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageProvider.get("@rbac.role.includedRoles.circularReferenceErrorTitle"),
					"@rbac.role.includedRoles.circularReferenceErrorMessage");
			FacesContext context = getFacesContext();
			context.addMessage(null, message);
			return;
		}
		for (Role r : tempIncludedRolesList) {
			if (r != null && r.getId() != null && r.getId().equals(newIncludedRoleId)) {
				duplicate = true;
			}
		}
		if (!duplicate) {
			tempIncludedRolesList.add(roleService.findForViewById(userData, new FindByIdRequest(newIncludedRoleId)));
		}
		reloadIncludedRolesList();
	}

	public void openConfirmRemoveIncludedDialog() {
		open(getConfirmRemoveIncludedDialog());
	}

	public void deleteIncludedRole() {
		tempIncludedRolesList.remove(selectedIncludedRole);
		reloadIncludedRolesList();
		String dialogForm = getCurrentEditDialog().getId() + ":dialogForm";
		selectedIncludedRole = null;
		getConfirmRemoveIncludedDialog().closeAndUpdate(dialogForm + ":includedRolesTableContainer",
				dialogForm + ":deleteIncludedRoleButton");
	}

	public void openConfirmDeleteDialog(Role role) {
		currentRole = role;
		open(getConfirmDeleteRoleDialog());
	}

	public void deleteRole() {
		if (execute(() -> {
			roleService.delete(userData, currentRole.getPk());
			reloadRoleList();
		})) {
			getConfirmDeleteRoleDialog().closeAndUpdate("form:roleListTable");
		} else {
			FacesUtil.updateDom("confirmDeleteRoleDialog:dialogForm:msgDialog");
		}
	}

	public Dialog getCreateRoleDialog() {
		return CREATE_ROLE;
	}

	public Dialog getEditRoleDialog() {
		return EDIT_ROLE;
	}

	public Dialog getCurrentEditDialog() {
		return currentEditDialog;
	}

	public Dialog getConfirmRemoveIncludedDialog() {
		return CONFIRM_REMOVE_INCLUDED_ROLE;
	}

	public Dialog getConfirmDeleteRoleDialog() {
		return CONFIRM_DELETE_ROLE;
	}

	private void reloadIncludedRolesList() {
		includedRolesList = tempIncludedRolesList;
	}

	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(final List<Role> roleList) {
		this.roleList = roleList;
	}

	public Role getCurrentRole() {
		return currentRole;
	}

	public void setCurrentRole(final Role currentRole) {
		this.currentRole = currentRole;
	}

	public TreeNode getTreeRoot() {
		return treeRoot;
	}

	public void setTreeRoot(final TreeNode treeRoot) {
		this.treeRoot = treeRoot;
	}

	public TreeNode[] getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(final TreeNode[] selectedNodes) {
		if (selectedNodes != null) {
			this.selectedNodes = new TreeNode[selectedNodes.length];
			System.arraycopy(selectedNodes, 0, this.selectedNodes, 0, selectedNodes.length);
		}
	}

	public List<Role> getIncludedRolesList() {
		return includedRolesList;
	}

	public void setIncludedRolesList(final List<Role> includedRolesList) {
		this.includedRolesList = includedRolesList;
	}

	public Role getCurrentIncludedRole() {
		return selectedIncludedRole;
	}

	public void setCurrentIncludedRole(final Role currentIncludedRole) {
		selectedIncludedRole = currentIncludedRole;
	}

	public String getNewIncludedRoleId() {
		return newIncludedRoleId;
	}

	public void setNewIncludedRoleId(String newIncludedRoleId) {
		this.newIncludedRoleId = newIncludedRoleId;
	}

	public Integer getAccumulatedSecLevel(final Role r) {
		return roleService.getAccumulatedSecLevelFor(userData, new FindByIdRequest(r.getId()));
	}

	public Long getNumberOfUsers(final Role r) {
		return operatorRoleService.findUserCountForRole(r.getPk());
	}

	public boolean isCreate() {
		return getCurrentEditDialog() == getCreateRoleDialog();
	}

	public boolean isSelf() {
		if (currentRole == null || currentRole.getPk() == null) {
			return false;
		}
		return currentRole.getPk().equals(userData.getOperatorRole().getRole().getPk());
	}

	public void onNodeSelect(@SuppressWarnings("unused") final NodeSelectEvent event) {
		changedTree = true;
	}

	private void open(Dialog dialog) {
		dialog.open();
		if (dialog == getCreateRoleDialog() || dialog == getEditRoleDialog()) {
			currentEditDialog = dialog;
		}
	}

	private boolean handleValidationFeedback(List<String> validationFeedback) {
		if (validationFeedback.isEmpty()) {
			return true;
		}
		for (String msgId : validationFeedback) {
			MessageUtil.buildDetailMessage(msgId, SEVERITY_ERROR);
		}
		return false;
	}

	private Role initCreateRole() {
		Role result = new Role();
		result.setName("");
		result.setId("");
		result.setSecurityLevel(DEFAULT_SECURITY_LEVEL);
		result.setActive(true);
		result.setMutuallyExclusive(false);
		return result;
	}

	private void reloadRoleList() {
		roleList = new ArrayList<>(roleService.findAllRolesWithoutAccessesForView(userData));
		RoleSorter.sortTranslated(messageProvider, roleList, Role::getName);
	}

	private void generateTree() {
		List<Access> accesses = accessService.findAll(userData);
		RoleSorter.sortTranslated(messageProvider, accesses, new RoleSorter.SortKey<Access>() {
			@Override
			public String getSortKey(final Access access) {
				return access.getName();
			}

			@Override
			public void setTranslated(Access o, String translated) {
				o.setName(translated);
			}
		});
		treeRoot = TreeUtil.pathToTree(accesses);
	}

}
