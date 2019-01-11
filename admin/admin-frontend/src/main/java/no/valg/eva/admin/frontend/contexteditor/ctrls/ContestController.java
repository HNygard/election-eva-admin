package no.valg.eva.admin.frontend.contexteditor.ctrls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.exception.ErrorCode;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.configuration.LegacyContestService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.service.ContestService;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaMultipleController;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.configuration.ctrls.ContestListProposalDataSource;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.commons.lang3.StringUtils;

@Named
@ConversationScoped
public class ContestController extends ConversationScopedController implements ContestListProposalDataSource {
	static final int NOT_SELECTED_PENULTIMATE_RECOUNT = 0;
	static final int TRUE_SELECTED_PENULTIMATE_RECOUNT = 1;
	static final int FALSE_SELECTED_PENULTIMATE_RECOUNT = 2;

	@Inject
	private UserDataController userDataController;
	@Inject
	private ElectionService electionService;
	@Inject
	private ContestService contestService;
	@Inject
	private MvAreaController mvAreaController;
	@Inject
	private MvAreaMultipleController mvAreaMultipleController;
	@Inject
	private ContestAreaMultipleController contestAreaMultipleController;
	@Inject
	private MvElectionPickerController mvElectionPickerController;
	@Inject
	private LegacyContestService legacyContestService;
	@Inject
	private ContestAreaService contestAreaService;
	@Inject
	private MvElectionService mvElectionService;
	@Inject
	private MessageProvider messageProvider;

	private boolean isReadOnly;
	private Election parent;
	private Contest currentContest;
	private int penultimateRecount;

	@Override
	protected void doInit() {
	}

	public void prepareForCreate(MvElection parentMvElection) {
		parent = electionService.get(userDataController.getUserData(), parentMvElection.getElection().electionPath());

		resetNewContest();

		mvAreaController.init();
		mvAreaController.setSelectedAreaLevel(parent.getAreaLevel());
		mvAreaController.clearItemsLists();
		mvAreaController.setCountryId(mvAreaController.getCountryItems().get(0).getValue().toString());
		mvAreaController.changeCountry();
		mvAreaController.setCountyId(null);
	}

	public void createContest() {
		FacesUtil.addCallbackParam("op", "createContest");
		currentContest.getContestAreas().add(mvAreaController.getSelectedMvArea().areaPath());
		if (execute(() -> {
			currentContest = contestService.save(userDataController.getUserData(), currentContest);
			mvElectionPickerController.update(ElectionLevelEnum.CONTEST.getLevel(), currentContest.getElectionPath().path());
		}, getErrorCodeHandler())) {
			String[] summaryParams = { currentContest.getName(), parent.getName() };
			MessageUtil.buildDetailMessage(MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			if (isSingleArea()) {
				resetNewContest();
			} else {
				FacesUtil.addCallbackParam("edit", currentContest.getId());
			}
		} else {
			FacesUtil.addCallbackParam("error", true);
		}
	}

	public void prepareForUpdate(MvElectionMinimal mvElectionMini) {
		MvElection mvElection = mvElectionService.findByPk(mvElectionMini.getPk());
		parent = electionService.get(userDataController.getUserData(), mvElection.getElection().electionPath());
		currentContest = contestService.get(userDataController.getUserData(), mvElection.getContest().electionPath());

		selectedContestArea = null;
		selectedContestAreaType = null;
		contestAreaMultipleController.setCurrentContest(null);
		isReadOnly = false;
		populatePenultimateRecount();

		if (currentContest != null) {
			populateCurrentContestAreaList();
		} else {
			currentContestAreaList = null;
		}
	}

	public void updateContest() {
		FacesUtil.addCallbackParam("op", "updateContest");
		if (execute(() -> {
			currentContest = contestService.save(userDataController.getUserData(), currentContest);
			mvElectionPickerController.update(ElectionLevelEnum.CONTEST.getLevel(), null);
		}, getErrorCodeHandler())) {
			MessageUtil.buildDetailMessage(MessageUtil.UPDATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);
		} else {
			FacesUtil.addCallbackParam("error", true);
		}
	}

	public void deleteContest() {
		FacesUtil.addCallbackParam("op", "deleteContest");
		if (execute(() -> {
			contestService.delete(userDataController.getUserData(), currentContest.getElectionPath());
			mvElectionPickerController.update(ElectionLevelEnum.CONTEST.getLevel(), null);
		})) {
			String[] summaryParams = { currentContest.getName(), parent.getName() };
			MessageUtil.buildDetailMessage(MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			currentContest = null;
		} else {
			FacesUtil.addCallbackParam("error", true);
		}
	}

	public void doGetCreateContestArea() {
		String previousCountyId = mvAreaMultipleController.getCountyId();
		contestAreaMultipleController.doGetCreateContestArea(legacyContestService.findByPk(userDataController.getUserData(), currentContest.getPk()));
		// Set currenty county to previous if valid.
		if (!StringUtils.isEmpty(previousCountyId)) {
			List<SelectItem> countyItems = getCountyItems();
			for (SelectItem item : countyItems) {
				if (previousCountyId.equals(item.getValue())) {
					mvAreaMultipleController.setCountyId(previousCountyId);
					mvAreaMultipleController.changeCounty();
					break;
				}
			}
		}
	}

	public void doCreateContestArea() {
		if (!execute(() -> {
			contestAreaMultipleController.doCreateContestArea();
			populateCurrentContestAreaList();
		})) {
			populateCurrentContestAreaList();
		}
	}

	public Election getParent() {
		return parent;
	}

	public boolean isKonfigurasjonValgValgdistrikt() {
		return userDataController.getUserAccess().isKonfigurasjonValgValgdistrikt();
	}

	public boolean isSingleArea() {
		return parent != null && parent.isSingleArea();
	}

	private void resetNewContest() {
		currentContest = new Contest(parent);
		currentContest.setId("");
		currentContest.setName("");
		populatePenultimateRecount();
	}

	private void populatePenultimateRecount() {
		if (currentContest.getPenultimateRecount() == null) {
			penultimateRecount = NOT_SELECTED_PENULTIMATE_RECOUNT;
		} else if (currentContest.getPenultimateRecount()) {
			penultimateRecount = TRUE_SELECTED_PENULTIMATE_RECOUNT;
		} else {
			penultimateRecount = FALSE_SELECTED_PENULTIMATE_RECOUNT;
		}
	}

	public void changePenultimateRecount(ValueChangeEvent event) {
		int value = (Integer) event.getNewValue();
		switch (value) {
		case NOT_SELECTED_PENULTIMATE_RECOUNT:
			currentContest.setPenultimateRecount(null);
			break;
		case TRUE_SELECTED_PENULTIMATE_RECOUNT:
			currentContest.setPenultimateRecount(true);
			break;
		case FALSE_SELECTED_PENULTIMATE_RECOUNT:
			currentContest.setPenultimateRecount(false);
			break;
		default:
			break;
		}
	}

	public Boolean getIsCurrentRemovable() {
		return userDataController.isCentralConfigurationStatus();
	}

	public Contest getCurrentContest() {
		return currentContest;
	}

	public AreaLevelEnum getParentAreaLevel() {
		if (parent == null) {
			return null;
		}
		return AreaLevelEnum.getLevel(parent.getAreaLevel());
	}

	public String getContestStatus() {
		if (currentContest == null || currentContest.getContestStatus() == null) {
			return null;
		}
		return currentContest.getContestStatus().getName();
	}

	public boolean isDisabled() {
		return userDataController.isCurrentElectionEventDisabled()
				|| (isReadOnly && !userDataController.isOverrideAccess());
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public List<ContestAreaType> getContestAreaTypes() {
		return Arrays.asList(ContestAreaType.ORDINARY, ContestAreaType.PARENT, ContestAreaType.CHILD);
	}

	public int getPenultimateRecount() {
		return penultimateRecount;
	}

	public void setPenultimateRecount(int penultimateRecount) {
		this.penultimateRecount = penultimateRecount;
	}

	private ErrorCodeHandler getErrorCodeHandler() {
		return (errorCode, params) -> {
			if (errorCode == ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION) {
				return messageProvider.get(MessageUtil.CHOOSE_UNIQUE_ID, messageProvider.get("@election_level[3].name"),
						currentContest.getId(), parent.getName());
			}
			return null;
		};
	}

	public boolean isUpdateContestButtonDisabled() {
		return isEditContestArea() || isCurrentContestNewAndAreaNotReady() || !isKonfigurasjonValgValgdistrikt() || isDisabled();
	}

	public boolean isDeleteContestButtonDisabled() {
		boolean missingAccessOrIsLocked = !getIsCurrentRemovable() || !isKonfigurasjonValgValgdistrikt() || isDisabled();
		return isEditContestArea() || isCurrentContestNewAndAreaNotReady() || missingAccessOrIsLocked;
	}

	private boolean isCurrentContestNewAndAreaNotReady() {
		return currentContest != null && currentContest.getPk() == null && !mvAreaController.isAreaSelectForAreaLevelCompleted();
	}

	public enum ContestAreaType {
		ORDINARY, PARENT, CHILD;

		public static ContestAreaType from(ContestArea area) {
			if (area.isParentArea()) {
				return PARENT;
			}
			if (area.isChildArea()) {
				return CHILD;
			} else {
				return ORDINARY;
			}
		}

		public String getLabel() {
			return "@election.contest.contest_area[" + this + "].name";
		}
	}

	@Override
	public ContestListProposalData getContestListProposalData() {
		return currentContest == null ? null : currentContest.getListProposalData();
	}

	@Override
	public String getAjaxKeyUpUpdate() {
		return "";
	}

	@Override
	public boolean isAjaxKeyUpDisabled() {
		return true;
	}

	@Override
	public void ajaxKeyUpListener() {
		// Not implemented
	}

	@Override
	public boolean isListProposalWriteMode() {
		return !isSingleArea();
	}

	@Override
	public void saveListProposal() {
		// Not implemented
	}

	@Override
	public void prepareForSave() {
		// Not implemented
	}

	private List<ContestArea> existingContestAreas;
	private List<ContestArea> currentContestAreaList;
	private ContestArea selectedContestArea;
	private ContestAreaType selectedContestAreaType;

	public void updateContestArea() {
		selectedContestArea.setParentArea(selectedContestAreaType == ContestAreaType.PARENT);
		selectedContestArea.setChildArea(selectedContestAreaType == ContestAreaType.CHILD);
		if (execute(() -> contestAreaService.update(userDataController.getUserData(), selectedContestArea))) {
			selectedContestArea = null;
			selectedContestAreaType = null;
			populateCurrentContestAreaList();
			MessageUtil.buildDetailMessage(MessageUtil.UPDATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);
		} else {
			populateCurrentContestAreaList();
		}
	}

	public void deleteSelectedContestArea() {
		if (execute(() -> contestAreaService.delete(userDataController.getUserData(), selectedContestArea.getPk()))) {
			setSelectedContestArea(null);
			populateCurrentContestAreaList();
			MessageUtil.buildDetailMessage(messageProvider.get(messageProvider.get("@common.message.delete.successful")), FacesMessage.SEVERITY_INFO);
		}
	}

	public List<ContestArea> getCurrentContestAreaList() {
		if (currentContestAreaList == null && currentContest != null && !StringUtils.isEmpty(currentContest.getId())) {
			populateCurrentContestAreaList();
		}
		return currentContestAreaList;
	}

	public List<SelectItem> getCountyItems() {
		List<SelectItem> items = mvAreaMultipleController.getCountyItems();
		List<SelectItem> result = new ArrayList<>();
		// Filter based on existing ContestAreas
		for (SelectItem item : items) {
			boolean add = true;
			out: if (existingContestAreas != null) {
				// If existing contains ALL municipalities for county, remove county all together
				List<MvArea> municipalities = mvAreaMultipleController.getMunicipalitiesForCounty((String) item.getValue());
				int found = 0;
				for (ContestArea contestArea : existingContestAreas) {
					for (MvArea municipality : municipalities) {
						if (contestArea.getMvArea().getAreaId().equals(municipality.getAreaId())) {
							found++;
							if (found == municipalities.size()) {
								add = false;
								break out;
							}
						}
					}
				}
			}
			if (add) {
				result.add(item);
			}
		}

		return result;
	}

	public List<SelectItem> getMunicipalityItems() {
		List<SelectItem> items = mvAreaMultipleController.getMunicipalityItems();
		List<SelectItem> result = new ArrayList<>();
		// Filter based on existing ContestAreas
		for (SelectItem item : items) {
			boolean add = true;
			if (existingContestAreas != null) {
				for (ContestArea contestArea : existingContestAreas) {
					if (contestArea.getMvArea().getAreaId().equals(item.getValue())) {
						add = false;
						break;
					}
				}
			}
			if (add) {
				result.add(item);
			}
		}

		return result;
	}

	public ContestAreaType getContestAreaType(ContestArea area) {
		return ContestAreaType.from(area);
	}

	public ContestArea getSelectedContestArea() {
		return selectedContestArea;
	}

	public void setSelectedContestArea(ContestArea selectedContestArea) {
		this.selectedContestArea = selectedContestArea;
		if (selectedContestArea == null) {
			this.selectedContestAreaType = null;
		} else {
			this.selectedContestAreaType = ContestAreaType.from(selectedContestArea);
		}
	}

	public ContestAreaType getSelectedContestAreaType() {
		return selectedContestAreaType;
	}

	public void setSelectedContestAreaType(ContestAreaType selectedContestAreaType) {
		this.selectedContestAreaType = selectedContestAreaType;
	}

	public boolean isEdited(ContestArea area) {
		return selectedContestArea != null && selectedContestArea.equals(area);
	}

	private void populateCurrentContestAreaList() {
		existingContestAreas = contestAreaService.findContestAreasForElectionPath(userDataController.getUserData(), currentContest.getParentElectionPath());
		currentContestAreaList = contestAreaService.findContestAreasForContestPath(userDataController.getUserData(), currentContest.getElectionPath());
		if (!currentContestAreaList.isEmpty()) {
			Collections.sort(currentContestAreaList, (area1, area2) -> area1.getPk().compareTo(area2.getPk()));
		}
	}

	private boolean isEditContestArea() {
		return selectedContestArea != null || contestAreaMultipleController.getCurrentContest() != null;
	}

}
