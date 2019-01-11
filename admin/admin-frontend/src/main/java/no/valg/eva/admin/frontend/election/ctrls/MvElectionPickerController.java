package no.valg.eva.admin.frontend.election.ctrls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.dto.MvElectionMinimal;
import no.evote.presentation.components.Action;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.common.configuration.status.ContestStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.ContestController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.ElectionController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.ElectionGroupController;
import no.valg.eva.admin.frontend.election.MvElectionPickerTable;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.commons.lang3.StringUtils;

/**
 * Controller class for the context picker and context editor. It represents the election hierarchy, MvAreaPickerController has almost identical structure and
 * represents the area hierarchy. Due to issues with JSF, rendering phases and the Primefaces datatable component, this functionality is modeled as a controller
 * with a set of MvElectionPickerTable instances, which represent columns in the picker/editor. The first column is loaded on initialization, the rest of the
 * columns are loaded on click in the parent column. Unless there is only one option in the parent column, in this case this option is selected and the next
 * column is populated.
 */
@Named
@ConversationScoped
public class MvElectionPickerController extends BaseController {
	private static final long serialVersionUID = 4300058725315103216L;
	private static final String PRESELECTED = "selectedMvElection";
	@Inject
	private MvElectionService mvElectionService;
	@Inject
	private ElectionService electionService;
	@Inject
	private ElectionGroupController electionGroupController;
	@Inject
	private ElectionController electionController;
	@Inject
	private ContestController contestController;
	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private MessageProvider mms;

	private MvElectionMinimal contextRoot;
	private MvElectionMinimal currentMvElection;
	private MvElection selectedMvElection;

	private ElectionType electionTypeFilter;

	private List<MvElectionPickerTable> mvElectionPickerTables = new ArrayList<>();

	private Integer selectionLevel;

	private boolean error;

	private Boolean isEditable;

	private boolean includeContestsAboveMyLevel = false;

	private String backPage;

	private Action actionToRunAfterCompletion;

	@PostConstruct
	public void init() {
		resetAndUpdate();
		actionToRunAfterCompletion = readActionToRunAfterCompletion();
	}

	private void resetAndUpdate() {
		selectedMvElection = null;

		// This list is the representation of the columns
		mvElectionPickerTables = new ArrayList<>();

		// Read parameter from component (contextEditor.xhtml) parameter. Set to true when editing, false when picking.
		if (FacesUtil.resolveExpression("#{cc.attrs.includeContestsAboveMyLevel}") != null) {
			this.includeContestsAboveMyLevel = Boolean.parseBoolean((String) FacesUtil.resolveExpression("#{cc.attrs.includeContestsAboveMyLevel}"));
		}

		// At what level should we select
		Integer configLevel = getSelectionLevel();
		if (configLevel != null) {
			selectionLevel = configLevel;
		} else if (selectionLevel == null) {
			return;
		}

		// Filter for only displaying elections with a specific election type
		if (FacesUtil.resolveExpression("#{cc.attrs.mvElectionElectionTypeFilter}") != null) {
			String electionTypeId = (String) FacesUtil.resolveExpression("#{cc.attrs.mvElectionElectionTypeFilter}");
			electionTypeFilter = electionService.findElectionTypeById(electionTypeId);
		}

		// Set root to the user/operatorRole root
		this.contextRoot = mvElectionService.getMvElectionMinimal(userData, userData.getOperatorRole().getMvElection());

		// If user does not have access high enough in the hierarchy, display an error
		if (contextRoot.getElectionLevel() > selectionLevel) {
			StringBuilder msg = new StringBuilder().append(mms.get("@area.common.no_access"));
			msg.append(" ");
			msg.append(mms.get("@election_level[" + selectionLevel + "].name"));
			FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), "");
			getFacesContext().addMessage(null, fmsg);
			error = true;
			return;
		} else if (contextRoot.getElectionLevel() == selectionLevel) {
			// User has access to this level only, no need to display a picker
			this.selectedMvElection = mvElectionService.findByPk(contextRoot.getPk());
		} else {
			// Initialize table structure
			for (int i = contextRoot.getElectionLevel(); i < selectionLevel; i++) {
				MvElectionPickerTable mvElectionPickerTable = new MvElectionPickerTable(i + 1, mvElectionService, userData, includeContestsAboveMyLevel);
				if (i > contextRoot.getElectionLevel()) {
					mvElectionPickerTables.get(mvElectionPickerTables.size() - 1).setChildTable(mvElectionPickerTable);
				}

				if (mvElectionPickerTable.getLevel() < 2 && electionTypeFilter != null) {
					mvElectionPickerTable.setElectionTypeFilter(electionTypeFilter);
				}
				mvElectionPickerTables.add(mvElectionPickerTable);
			}
		}

		// Populate first column
		if (!mvElectionPickerTables.isEmpty()) {
			mvElectionPickerTables.get(0).setMvElections(
					mvElectionService.findByPathAndChildLevelMinimal(userData, contextRoot.getPk(), includeContestsAboveMyLevel));
		}

		isEditable = userDataController.getElectionEvent().getElectionEventStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA;
		boolean isEditor = "true".equals(FacesUtil.resolveExpression("#{cc.attrs.isEditor}"));

		// In some situations it should be possible to select on all levels, for instance when deleting voters, votings and vote counts

		// If there is only one available option, we select this so that the picker doesn't need to be shown
		if (!isEditor && !isAllSelectable() && !mvElectionPickerTables.isEmpty()) {
			MvElectionPickerTable lastTable = mvElectionPickerTables.get(mvElectionPickerTables.size() - 1);
			if (lastTable.getSelectedMvElection() != null) {
				setSelectedMvElection(mvElectionService.findByPk(lastTable.getSelectedMvElection().getPk()));
			}
		}

		populateWithPreviouslySelection();
	}

	private void populateWithPreviouslySelection() {
		MvElection previouslySelectedMvElection = MvElection.class.cast(FacesUtil.getSessionAttribute(PRESELECTED));

		// If there's a previously selected mvelection, and the filter is either not defined, or the filter *is* defined and the selected election matches the
		// filter
		if (previouslySelectedMvElection != null
				&& (electionTypeFilter == null || (previouslySelectedMvElection.getElection() != null && previouslySelectedMvElection.getElection()
						.getElectionType().getId().equals(electionTypeFilter.getId())))) {
			String electionPath = previouslySelectedMvElection.getElectionPath();
			String[] pathElements = electionPath.split("\\.");

			int previousElectionLevel = previouslySelectedMvElection.getElectionLevel();
			for (int level = contextRoot.getElectionLevel() + 1; level <= previousElectionLevel && level <= selectionLevel; level++) {
				String path = StringUtils.join(Arrays.copyOfRange(pathElements, 0, level + 1), '.');
				update(level, path);
			}
		}
	}

	/**
	 * Evaluates sends the mvElection for evaluation by the method wrapped in me
	 * @param me The method that evaluates the the mvElection
	 * @param mvElection The mvElection to be evaluated
	 * @return The result of the foreign evaluation
	 */
	public Boolean evaluateIndicator(MethodExpression me, MvElection mvElection) {
		FacesContext context = getFacesContext();

		return (Boolean) me.invoke(context.getELContext(), new Object[] { mvElection });
	}

	public Boolean evaluateIndicator(MethodExpression me, MvElectionMinimal mvElectionMinimal) {
		FacesContext context = getFacesContext();

		return (Boolean) me.invoke(context.getELContext(), new Object[] { mvElectionMinimal });
	}

	/**
	 * Reload the specified level and selects the correct element if electionPath is provided
	 */
	public void update(int level, String electionPath) {
		MvElectionMinimal parentMvElection = getParentMvElection(level);
		int currentIndex = level - contextRoot.getElectionLevel() - 1;
		mvElectionPickerTables.get(currentIndex).setMvElections(
				mvElectionService.findByPathAndChildLevelMinimal(userData, parentMvElection.getPk(), includeContestsAboveMyLevel));
		if (!StringUtils.isEmpty(electionPath)) {
			mvElectionPickerTables.get(currentIndex).setSelectedMvElection(mvElectionService.findSingleByPathMinimal(userData, electionPath));
		}
	}

	/**
	 * A click on the select button in the picker
	 */
	public String select(int level) {
		int currentIndex = level - contextRoot.getElectionLevel() - 1;
		Long mvMinimalPk = mvElectionPickerTables.get(currentIndex).getSelectedMvElection().getPk();
		setSelectedMvElection(mvElectionService.findByPk(mvMinimalPk));
		FacesUtil.setSessionAttribute(PRESELECTED, getSelectedMvElection());

		if (actionToRunAfterCompletion != null) {
			if (isRedirectWithoutCid()) {
				responseRedirect(actionToRunAfterCompletion.action());
			} else {
				return actionToRunAfterCompletion.action();
			}
		}
		return null;
	}

	private void responseRedirect(String url) {
		if (url == null) {
			return;
		}
		try {
			((HttpServletResponse) getFacesContext().getExternalContext().getResponse()).sendRedirect(url);
		} catch (IOException e) {
			return;
		}
	}

	public String back() {
		return backPage;
	}

	/**
	 * Get the parent element for a level
	 */
	public MvElectionMinimal getParentMvElection(int level) {
		// Need to subtract root election level (not always showing the entire tree) and 1 (because index starts at 0, level at 1)
		int currentIndex = level - contextRoot.getElectionLevel() - 1;
		int parentIndex = currentIndex - 1;

		if (currentIndex == 0) {
			// we're at the top level
			return contextRoot;
		}

		return mvElectionPickerTables.get(parentIndex).getSelectedMvElection();
	}

	public void injectParentMvElection(int level) {
		MvElectionMinimal parentMvElectionMini = getParentMvElection(level);
		MvElection parentMvElection = mvElectionService.findByPk(parentMvElectionMini.getPk());
		ElectionLevelEnum levelEnum = ElectionLevelEnum.getLevel(level);
		switch (levelEnum) {
		case ELECTION_GROUP:
			electionGroupController.prepareForCreate(parentMvElection);
			break;
		case ELECTION:
			electionController.prepareForCreate(ElectionPath.from(parentMvElection.getElectionPath()), parentMvElection.getElectionGroupName());
			break;
		case CONTEST:
			contestController.prepareForCreate(parentMvElection);
			break;

		default:
			break;
		}
	}

	public void editMvElection(MvElectionMinimal mvElectionMini) {
		setCurrentMvElection(mvElectionMini);
		int level = mvElectionMini.getElectionLevel();
		injectParentMvElection(level);
		ElectionLevelEnum levelEnum = ElectionLevelEnum.getLevel(level);
		switch (levelEnum) {
		case ELECTION_GROUP:
			electionGroupController.prepareForUpdate(mvElectionMini);
			break;
		case ELECTION:
			electionController.prepareForUpdate(mvElectionMini);
			break;
		case CONTEST:
			contestController.prepareForUpdate(mvElectionMini);
			break;

		default:
			break;
		}

	}

	public void readMvElection(MvElectionMinimal mvElectionMini) {
		this.editMvElection(mvElectionMini);
		int level = mvElectionMini.getElectionLevel();
		ElectionLevelEnum levelEnum = ElectionLevelEnum.getLevel(level);
		switch (levelEnum) {
		case ELECTION_GROUP:
			electionGroupController.setReadOnly(true);
			break;
		case ELECTION:
			electionController.setReadOnly(true);
			break;
		case CONTEST:
			contestController.setReadOnly(true);
			break;

		default:
			break;
		}
	}

	public String getBackPage() {
		return backPage;
	}

	public void setBackPage(String backPage) {
		this.backPage = backPage;
	}

	public MvElection getSelectedMvElection() {
		return selectedMvElection;
	}

	public void setSelectedMvElection(MvElection selectedMvElection) {
		this.selectedMvElection = selectedMvElection;
	}

	public List<MvElectionPickerTable> getMvElectionPickerTables() {
		return mvElectionPickerTables;
	}

	public boolean isError() {
		return error;
	}

	public Boolean getIsEditable() {
		return isEditable;
	}

	public void setIsEditable(Boolean isEditable) {
		this.isEditable = isEditable;
	}

	/**
	 * Checks status and access rights to find out whether this element can be edited
	 */
	public Boolean getIsEditable(int level, MvElectionMinimal mvElectionMinimal) {
		if (userDataController.isCurrentElectionEventDisabled()) {
			return false;
		}
		if (userDataController.isOverrideAccess()) {
			return true;
		}
		isEditable = false;

		ElectionLevelEnum levelEnum = ElectionLevelEnum.getLevel(level);
		switch (levelEnum) {
		case ELECTION_GROUP:
			isEditable = userDataController.getUserAccess().isKonfigurasjonValgValggruppe() && userDataController.isCentralConfigurationStatus();
			break;
		case ELECTION:
			isEditable = userDataController.getUserAccess().isKonfigurasjonValgValg()
					&& userDataController.isCentralConfigurationStatus();
			break;
		case CONTEST:
			if (mvElectionMinimal != null) {
				isEditable = userDataController.getUserAccess().isKonfigurasjonValgValgdistrikt()
						&& userDataController.getElectionEvent().getElectionEventStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA
						&& mvElectionMinimal.isContestOnMyLevelOrBelow()
						&& mvElectionMinimal.getContestStatusId() < ContestStatus.FINISHED_CONFIGURATION.id();
			} else {
				isEditable = false;
			}
			break;
		default:
			isEditable = false;
			break;
		}

		return isEditable;
	}

	/**
	 * Checks status and access rights to find out whether elements can be created
	 */
	public Boolean getIsCreatable(int level) {

		if (userDataController.isOverrideAccess()) {
			return true;
		}

		Boolean isCreatable = false;
		ElectionLevelEnum levelEnum = ElectionLevelEnum.getLevel(level);
		switch (levelEnum) {
		case ELECTION_GROUP:
			isCreatable = userDataController.getUserAccess().isKonfigurasjonValgValggruppe()
					&& userDataController.isCentralConfigurationStatus();
			break;
		case ELECTION:
			isCreatable = userDataController.getUserAccess().isKonfigurasjonValgValg()
					&& userDataController.isCentralConfigurationStatus();
			break;
		case CONTEST:
			isCreatable = userDataController.getUserAccess().isKonfigurasjonValgValgdistrikt()
					&& userDataController.isCentralConfigurationStatus();
			break;
		default:
			isCreatable = false;
			break;
		}

		// Immediately return if the user doesn't have access
		if (!isCreatable) {
			return isCreatable;
		}

		int idx = level - 1;
		// If we're at the first column, it should always be possible to create new
		if (idx > 0 && idx < mvElectionPickerTables.size()) {
			MvElectionPickerTable pickerTable = mvElectionPickerTables.get(idx);
			// The link should by default not be enabled if there are no MvAreas in the list
			isCreatable = (pickerTable.getMvElections() != null && pickerTable.getMvElections().size() > 0);

			// However, even if there are no MvElectionss in the current column, the link should be displayed if there are are MvAreas on the *preceeding*
			// level, and it has an item selected:
			if (!isCreatable && idx != 0) {
				MvElectionPickerTable prevPickerTable = mvElectionPickerTables.get(idx - 1);
				if (prevPickerTable.getSelectedMvElection() != null && prevPickerTable.getMvElections() != null
						&& prevPickerTable.getMvElections().size() > 0) {
					isCreatable = true;
				}
			}

		}
		return isCreatable;
	}

	public MvElectionMinimal getCurrentMvElection() {
		return currentMvElection;
	}

	public void setCurrentMvElection(MvElectionMinimal currentMvElection) {
		this.currentMvElection = currentMvElection;
	}

	public String getAjaxUpdateSelector(int hierarchyIndex, String hierachyDepthInt) {

		int hierachyDepth = Integer.parseInt(hierachyDepthInt);
		StringBuilder ajaxSelector = new StringBuilder();

		// If we already are at the max selection level, just return a blank string. There are no more levels to update
		if (hierarchyIndex == hierachyDepth) {
			return ajaxSelector.toString();
		}

		// We do not want to update the current level, only the lower levels
		int currentHierarchyIndex = hierarchyIndex + 1;

		while (currentHierarchyIndex <= hierachyDepth) {
			ajaxSelector
					.append("@composite:electionLevel")
					.append(currentHierarchyIndex)
					.append(" ");
			currentHierarchyIndex++;
		}

		return ajaxSelector.toString();
	}

	public Boolean shouldAddAjaxUpdate(int hierarchyIndex, String hierachyDepthInt) {
		return hierarchyIndex < Integer.parseInt(hierachyDepthInt);
	}

	public Integer getSelectionLevel() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.mvElectionSelectionLevel}");
		if (expr == null) {
			return null;
		}
		return FacesUtil.getIntFromStringOrInteger(expr);
	}

	private boolean isAllSelectable() {
		return "true".equals(FacesUtil.resolveExpression("#{cc.attrs.mvElectionAllSelectable}"));
	}

	private boolean isDisableSelect() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.disableSelect}");
		return o != null && (Boolean) o;
	}

	public boolean isRenderPicker() {
		return !isError() && getSelectionLevel() != null && getSelectedMvElection() == null;
	}

	public String getHeaderKey() {
		if (isAllSelectable()) {
			return "@election.common.election_level";
		}
		return "@election_level[" + getSelectionLevel() + "].name";
	}

	public boolean isRenderButton(MvElectionPickerTable table) {
		return isAllSelectable() || (getSelectionLevel() != null && table.getLevel() == getSelectionLevel());
	}

	public boolean isDisabledButton(MvElectionPickerTable table) {
		return table.getSelectedMvElection() == null || isDisableSelect();
	}

	public boolean isHideTable(MvElectionPickerTable table) {
		if (isHideSingles(table)) {
			return true;
		}
		boolean oneElectionInList = table.getMvElections() != null && table.getSize() == 1;
		boolean levelNotOnSelectionLevel = table.getLevel() != getSelectionLevel();
		boolean hideBelowWantedLevel = isHideBelowWantedElectionLevel() && table.getLevel() > getSelectionLevel();

		return (oneElectionInList && levelNotOnSelectionLevel && !isAllSelectable()) || hideBelowWantedLevel;
	}

	private boolean isHideSingles(MvElectionPickerTable table) {
		if (isHideLeadingSingles()) {
			if (table.getMvElections() != null && table.getMvElections().size() == 1) {
				// This one is one, what about parents?
				MvElectionPickerTable parent = parent(table);
				if (parent == null) {
					// Singles until parent = null means true!
					return true;
				}
				return isHideSingles(parent);
			}
			return false;
		} else {
			return false;
		}
	}

	private MvElectionPickerTable parent(MvElectionPickerTable mvElectionPickerTable) {
		for (int i = 0; i < mvElectionPickerTables.size(); i++) {
			MvElectionPickerTable table = mvElectionPickerTables.get(i);
			if (i > 0 && table.getLevel() == mvElectionPickerTable.getLevel()) {
				return mvElectionPickerTables.get(i - 1);
			}
		}
		return null;
	}

	private boolean isHideLeadingSingles() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.mvElectionHideLeadingSingles}");
		return o != null && (Boolean) o;
	}

	private boolean isHideBelowWantedElectionLevel() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.hideBelowWantedElectionLevel}");
		if (expr == null) {
			return false;
		}
		return (Boolean) expr;
	}

	private Action readActionToRunAfterCompletion() {
		Object resolvedExpression = FacesUtil.resolveExpression("#{cc.attrs.actionController}");
		if (resolvedExpression != null) {
			return (Action) resolvedExpression;
		} else {
			return null;
		}
	}

	private boolean isRedirectWithoutCid() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.redirectWithoutCid}");
		if (expr == null) {
			return false;
		}
		return (Boolean) expr;
	}
}
