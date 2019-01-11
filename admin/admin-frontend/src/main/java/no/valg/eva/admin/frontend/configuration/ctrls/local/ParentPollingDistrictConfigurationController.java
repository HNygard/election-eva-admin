package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistricts;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.DeleteAction;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverter;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverterSource;
import org.primefaces.model.DefaultTreeNode;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.frontend.common.Button.enabled;

@Named
@ViewScoped
public class ParentPollingDistrictConfigurationController extends ConfigurationController
		implements PlaceConverterSource<RegularPollingDistrict>, DeleteAction {

	// Injected
	private PollingDistrictService pollingDistrictService;

	private PlaceConverter placeConverter = new PlaceConverter(this);
	private ParentPollingDistricts parentPollingDistricts = new ParentPollingDistricts();
	private List<RegularPollingDistrict> selected = new ArrayList<>();
	private ParentPollingDistrict newParentPollingDistrict;
	private DefaultTreeNode parentPollingDistrictsTree;
	private DefaultTreeNode selectedNode;
	private boolean usingParentPollingDistrict = false;

	public ParentPollingDistrictConfigurationController() {
		// CDI
	}

	@Inject
	public ParentPollingDistrictConfigurationController(PollingDistrictService pollingDistrictService) {
		this.pollingDistrictService = pollingDistrictService;
	}

	@Override
	public void init() {
		init(true);
	}

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.PARENT_POLLING_DISTRICT;
	}

	@Override
	public String getName() {
		return "@config.local.accordion.parent_polling_district.name";
	}

	@Override
	boolean hasAccess() {
		if (!isMunicipalityLevel()) {
			return false;
		}
		try {
			ReportCountCategoriesConfigurationController countCtrl = getController(ReportCountCategoriesConfigurationController.class);
			return countCtrl != null && !countCtrl.isValgtingsstemmerSentraltSamlet();
		} catch (RuntimeException e) {
			return true;
		}
	}

	@Override
	Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
		return new Class[] { ReportCountCategoriesConfigurationController.class };
	}

	@Override
	Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
		return new Class[] { ElectionDayPollingPlacesConfigurationController.class };
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setPollingDistricts(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		return isMunicipalityLevel() && getMunicipalityConfigStatus().isPollingDistricts();
	}

	@Override
	boolean canBeSetToDone() {
		return !usingParentPollingDistrict || parentPollingDistricts.isValid();
	}

	@Override
	public void saveDone() {
		if (!isUsingParentPollingDistrict() && !parentPollingDistricts.getParentPollingDistricts().isEmpty()) {
			// Confirm delete of existing parents
			getConfirmDeleteAllParentPollingDistrictsDialog().open();
		} else {
			super.saveDone();
		}
	}

	@Override
	public void confirmDelete() {
		if (getSelectedNode() == null) {
			return;
		}
		execute(() -> {
			saveDone(false);
			pollingDistrictService.deleteParentPollingDistrict(getUserData(), getSelectedParentPollingDistrict());
			MessageUtil.buildDeletedMessage(getSelectedParentPollingDistrict());
			init(false);
		});
	}

	@Override
	public Button button(ButtonType type) {
		switch (type) {
		case CREATE:
			return enabled(isEditable() && !selected.isEmpty());
		case DELETE:
		case EXECUTE_DELETE:
			return enabled(isEditable() && getSelectedNode() != null);
		default:
			return super.button(type);
		}
	}

	@Override
	public PlaceConverter getPlaceConverter() {
		return placeConverter;
	}

	@Override
	public List<RegularPollingDistrict> getPlaces() {
		return parentPollingDistricts.getSelectableDistricts();
	}

	private void init(boolean resetUsing) {
		setSelected(new ArrayList<>());
		newParentPollingDistrict = new ParentPollingDistrict(getAreaPath());
		parentPollingDistricts = pollingDistrictService.findParentPollingDistrictsByArea(getUserData(), getAreaPath());
		if (resetUsing) {
			setUsingParentPollingDistrict(!parentPollingDistricts.getParentPollingDistricts().isEmpty());
		}
		buildTree();
	}

	public void createParentPollingDistrict() {
		if (newParentPollingDistrict == null || selected.isEmpty()) {
			return;
		}
		execute(() -> {
			// Add selected as children to newParentPollingDistrict
			saveDone(false);
			newParentPollingDistrict.getChildren().clear();
			selected.forEach(newParentPollingDistrict::addChild);
			pollingDistrictService.saveParentPollingDistrict(getUserData(), newParentPollingDistrict);
			MessageUtil.buildSavedMessage(newParentPollingDistrict);
			init();
		});
	}

	public void confirmSaveDone() {
		if (super.saveDone(true) && execute(() -> {
			for (ParentPollingDistrict district : parentPollingDistricts.getParentPollingDistricts()) {
				pollingDistrictService.deleteParentPollingDistrict(getUserData(), district);
			}

		})) {
			getConfirmDeleteAllParentPollingDistrictsDialog().closeAndUpdate("configurationPanel", "approve-form");
		}
		init();
	}

	public Dialog getConfirmDeleteAllParentPollingDistrictsDialog() {
		return Dialogs.CONFIRM_DELETE_ALL_PARENT_POLLING_DISTRICTS;
	}

	public ParentPollingDistrict getSelectedParentPollingDistrict() {
		if (getSelectedNode() == null) {
			return null;
		}
		return (ParentPollingDistrict) getSelectedNode().getData();
	}

	private void buildTree() {
		parentPollingDistrictsTree = new DefaultTreeNode("Root", null);
		for (ParentPollingDistrict ppd : parentPollingDistricts.getParentPollingDistricts()) {
			DefaultTreeNode parentPollingDistrictNode = new DefaultTreeNode(ppd.getId() + " " + ppd.getName(), parentPollingDistrictsTree);
			parentPollingDistrictNode.setData(ppd);
			for (RegularPollingDistrict child : ppd.getChildren()) {
				DefaultTreeNode childNode = new DefaultTreeNode(child.getId() + " " + child.getName(), parentPollingDistrictNode);
				childNode.setData(child);
			}
		}
	}

	public DefaultTreeNode getParentPollingDistrictsTree() {
		return parentPollingDistrictsTree;
	}

	public DefaultTreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(DefaultTreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<RegularPollingDistrict> getSelected() {
		return selected;
	}

	public void setSelected(List<RegularPollingDistrict> selected) {
		this.selected = selected;
	}

	public ParentPollingDistrict getNewParentPollingDistrict() {
		return newParentPollingDistrict;
	}

	public void setNewParentPollingDistrict(ParentPollingDistrict newParentPollingDistrict) {
		this.newParentPollingDistrict = newParentPollingDistrict;
	}

	public boolean isUsingParentPollingDistrict() {
		return usingParentPollingDistrict;
	}

	public void setUsingParentPollingDistrict(boolean usingParentPollingDistrict) {
		this.usingParentPollingDistrict = usingParentPollingDistrict;
	}
}
