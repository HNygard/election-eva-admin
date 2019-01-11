package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.PlaceTreeData;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Denne controlleren er for grunnlangsdata for kommuner som har bydeler (typisk Oslo).
 */
@Named
@ViewScoped
public class BoroughsStemmestyreConfigurationController extends StemmestyreBaseConfigurationController {

	private TreeNode rootTreeNode;
	private TreeNode selectedTreeNode;

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.BOROUGHS_STEMMESTYRE;
	}

	@Override
	boolean hasAccess() {
		return super.hasAccess() && isHasBoroughs();
	}

    @Override
    String getBoardMemberFormIdPath() {
        return "configurationPanel:" + getMainController().getActiveControllerIndex() + ":stemmestyreBorough:boardMemberForm";
    }

	@Override
	public void init() {
		super.init();
		buildTree();
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
			for (TreeNode parent : getRootTreeNode().getChildren()) {
				parent.setExpanded(parent.getRowKey().equals(node.getParent().getRowKey()));
			}
			districtSelected(((PlaceTreeData<PollingDistrict>) node.getData()).getPlace());
		}
	}

    private void buildTree() {
		setRootTreeNode(new DefaultTreeNode("Root", null));
		Map<Borough, List<PollingDistrict>> grouped = groupByBorough();
		PollingDistrict selectedPlace = null;
		if (getSelectedTreeNode() == null && !grouped.isEmpty()) {
			selectedPlace = grouped.entrySet().iterator().next().getValue().get(0);
		} else if (getSelectedTreeNode() != null && getSelectedTreeNode().isLeaf()) {
			selectedPlace = ((PlaceTreeData<PollingDistrict>) getSelectedTreeNode().getData()).getPlace();
		}
		if (selectedPlace != null) {
			setPollingDistrict(selectedPlace);
		}
		addChildren(grouped, selectedPlace);
	}

	private void addChildren(Map<Borough, List<PollingDistrict>> grouped, PollingDistrict selectedPlace) {
		for (Map.Entry<Borough, List<PollingDistrict>> entry : grouped.entrySet()) {
			DefaultTreeNode topNode = new DefaultTreeNode(new PlaceTreeData<>(this, entry.getKey(), entry.getValue()), getRootTreeNode());
			for (PollingDistrict child : entry.getValue()) {
				DefaultTreeNode childNode = new DefaultTreeNode(new PlaceTreeData<>(this, child), topNode);
				boolean selected = selectedPlace != null && selectedPlace.getId().equals(child.getId());
				childNode.setSelected(selected);
				if (selected) {
					childNode.getParent().setExpanded(true);
					setSelectedTreeNode(childNode);
				}
			}
		}
	}

	private SortedMap<Borough, List<PollingDistrict>> groupByBorough() {
        SortedMap<Borough, List<PollingDistrict>> result = new TreeMap<>(Comparator.comparing(borough -> borough.getPath().path()));
		for (PollingDistrict place : getPlaces()) {
            List<PollingDistrict> grouped = result.computeIfAbsent(place.getBorough(), k -> new ArrayList<>());
            grouped.add(place);
		}
		return result;
	}

}
