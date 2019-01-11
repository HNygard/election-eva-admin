package no.valg.eva.admin.frontend.rbac.ctrls;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.frontend.util.TreeUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.service.AccessService;
import no.valg.eva.admin.frontend.BaseController;

import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class AccessOverviewController extends BaseController {

	// Injected
	private UserData userData;
	private AccessService accessService;

	private TreeNode treeRoot;
	private TreeNode selectedNode;

	public AccessOverviewController() {
		// CDI
	}

	@Inject
	public AccessOverviewController(UserData userData, AccessService accessService) {
		this.userData = userData;
		this.accessService = accessService;
	}

	@PostConstruct
	public void init() {
		treeRoot = TreeUtil.pathToTree(accessService.findAll(userData));
	}

	public String viewOperators() {
		if (getSelectedNode() == null) {
			MessageUtil.buildDetailMessage("@rbac.inspect.noSelectionError", FacesMessage.SEVERITY_ERROR);
			return null;
		}

		Access selectedAccess = (Access) getSelectedNode().getData();
		return addRedirect("accessOverviewOperators.xhtml?access=" + selectedAccess.getPath());
	}

	public TreeNode getTreeRoot() {
		return treeRoot;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}
}
