package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.READ;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Denne controlleren er for grunnlangsdata for kommuner som har bydeler (typisk Oslo).
 */
@Named
@ViewScoped
public class BoroughsListProposalConfigurationController extends ListProposalBaseConfigurationController {

	private ListProposalConfig currentListProposal;
	private TreeNode rootTreeNode;
	private TreeNode selectedTreeNode;

	@Override
	public void init() {
		super.init();
		buildTree();
	}

	@Override
	public void prepareForSave() {
		if (isDoneStatus()) {
			unlock();
		}
		setMode(UPDATE);
	}

	@Override
	boolean canBeSetToDone() {
		return getListProposal() != null && getListProposal().isValid();
	}

	@Override
	public void saveListProposal() {
		if (isEditable()) {
			execute(() -> {
				getListProposalService().save(getUserData(), getListProposal(), false);
				MessageUtil.buildSavedMessage(getListProposal());
				loadData();
				buildTree();
				setMode(READ);
			});
		}
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.BOROUGHS_LIST_PROPOSAL;
	}

	@Override
	boolean hasAccess() {

		ListProposalConfig data = getParentListProposal();
		if (data == null) {
			try {
				data = loadData();
			} catch (RuntimeException e) {
				return false;
			}

		}
		return data != null && !data.getChildren().isEmpty() && data.isSingleArea();
	}

	@Override
	public Button button(ButtonType type) {
		switch (type) {
		case UPDATE:
			if (ConfigurationMode.READ.equals(getMode())) {
				return enabled(isEditable());
			}
			return notRendered();
		case EXECUTE_UPDATE:
			if (isWriteMode()) {
				return enabled(isEditable());
			}
			return notRendered();
		case CANCEL:
			if (isWriteMode()) {
				return enabled(isEditable());
			}
			return notRendered();
		default:
			return super.button(type);
		}
	}

	@Override
	public ListProposalConfig getListProposal() {
		return currentListProposal == null ? getParentListProposal() : currentListProposal;
	}

	private ListProposalConfig getParentListProposal() {
		return super.getListProposal();
	}

	public TreeNode getRootTreeNode() {
		return rootTreeNode;
	}

	public void setRootTreeNode(DefaultTreeNode municipalityTree) {
		this.rootTreeNode = municipalityTree;
	}

	public TreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public void setSelectedTreeNode(TreeNode selectedTreeNode) {
		this.selectedTreeNode = selectedTreeNode;
	}

	public void viewModel(NodeSelectEvent event) {
		TreeNode node = event.getTreeNode();
		if (node.isLeaf()) {
			node.getParent().setExpanded(true);
		}
		currentListProposal = (ListProposalConfig) node.getData();
	}

	private void buildTree() {
		setRootTreeNode(new DefaultTreeNode("Root", null));
		long currentPk = getListProposal().getContestPk();
		DefaultTreeNode topNode = new DefaultTreeNode(getParentListProposal(), getRootTreeNode());
		topNode.setSelected(currentPk == getParentListProposal().getContestPk());
		topNode.setExpanded(true);
		TreeNode selectedNode = topNode;
		currentListProposal = getParentListProposal();

		for (ListProposalConfig child : getParentListProposal().getChildren()) {
			DefaultTreeNode childNode = new DefaultTreeNode(child, topNode);
			childNode.setSelected(currentPk == child.getContestPk());
			if (childNode.isSelected()) {
				selectedNode = childNode;
				currentListProposal = child;
			}
		}
		setSelectedTreeNode(selectedNode);
	}
}
