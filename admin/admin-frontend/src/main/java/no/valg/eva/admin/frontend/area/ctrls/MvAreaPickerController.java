package no.valg.eva.admin.frontend.area.ctrls;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.LOCAL_CONFIGURATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.presentation.components.Action;
import no.evote.presentation.filter.MvAreaFilter;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.MvAreaPickerTable;
import no.valg.eva.admin.frontend.contexteditor.ctrls.BoroughController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.CountryController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.CountyController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.MunicipalityController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.PollingDistrictController;
import no.valg.eva.admin.frontend.contexteditor.ctrls.PollingPlaceController;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.commons.lang3.StringUtils;

/**
 * Representation of the area hierarchy in the context picker/editor.
 */
@Named
@ConversationScoped
public class MvAreaPickerController extends BaseController {
	public static final String TRUE = "true";
	private static final long serialVersionUID = 248984123894172861L;
	private static final String PRESELECTED = "selectedMvArea";
	@Inject
	private transient MvAreaService mvAreaService;
	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private CountryController countryController;
	@Inject
	private CountyController countyController;
	@Inject
	private MunicipalityController municipalityController;
	@Inject
	private BoroughController boroughController;
	@Inject
	private PollingDistrictController pollingDistrictController;
	@Inject
	private PollingPlaceController pollingPlaceController;
	@Inject
	private MessageProvider mms;
	@Inject
	private MvElectionPickerController mvElectionPickerController;

	private MvArea contextPickerRoot;

	private MvArea selectedMvArea;

	private List<MvAreaPickerTable> mvAreaPickerTables = new ArrayList<>();

	private Integer selectionLevel;

	private Action actionToRunAfterCompletion;

	private boolean error;

	@PostConstruct
	public void init() {
		initializeOrRedirect(
				readIncludeAreasAboveMyLevel(),
				readMvAreaFilters(),
				readActionToRunAfterCompletion(),
				readSelectionLevel());
	}

	public boolean isElectionEventAdminUser() {
		return userData.isElectionEventAdminUser();
	}

	// This sounds like a method that does too much. Separation of concerns?
	// Reset and update what?
	// Theory: Reset is the "initialize data" part?

	/**
	 * @param includeAreasAboveMyLevel
	 */
	public void initializeOrRedirect(Integer includeAreasAboveMyLevel, List<MvAreaFilter> mvAreaFilters,
			Action actionToRunAfterCompletion, Integer selectionLevel) {
		// Initializing data
		setSelectedMvArea(null);
		setMvAreaPickerTables(new ArrayList<>());

		// Setting class variables given in parameters
		setContextPickerRoot(includeAreasAboveMyLevel);
		setActionToRunAfterCompletion(actionToRunAfterCompletion);
		setSelectionLevel(selectionLevel);

		if (selectionLevel == null) {
			return;
		}

		// If user does not have access high enough in the hierarchy, display an error
		if (contextPickerRoot.getAreaLevel() > selectionLevel) {
			showAreaLevelError();
			error = true;
			return;
		} else if (contextPickerRoot.getAreaLevel() == selectionLevel) {
			// User has access to this level only, no need to display a picker. If defined, redirect to page defined in action.
			setSelectedMvArea(contextPickerRoot);
			if (actionToRunAfterCompletion != null) {
				doRedirect(actionToRunAfterCompletion);
				return;
			}
		} else {
			// Initialize table structure
			for (int i = contextPickerRoot.getAreaLevel(); i < selectionLevel; i++) {
				MvAreaPickerTable mvAreaTable = new MvAreaPickerTable(mvAreaFilters);
				mvAreaTable.setLevel(i + 1);
				mvAreaTable.setMvAreaService(mvAreaService);
				if (i > contextPickerRoot.getAreaLevel()) {
					mvAreaPickerTables.get(mvAreaPickerTables.size() - 1).setChildTable(mvAreaTable);
				}
				mvAreaPickerTables.add(mvAreaTable);
			}
		}

		// Populate context root
		if (!mvAreaPickerTables.isEmpty()) {
			mvAreaPickerTables.get(0).setMvAreas(mvAreaService.findByPathAndChildLevel(contextPickerRoot));
		}

		boolean isEditor = TRUE.equals(FacesUtil.resolveExpression("#{cc.attrs.isEditor}"));
		// If there is only one available option, we select this so that the picker doesn't need to be shown
		if (!isEditor && !isAllSelectable() && !mvAreaPickerTables.isEmpty()) {
			MvAreaPickerTable lastTable = mvAreaPickerTables.get(mvAreaPickerTables.size() - 1);
			if (lastTable.getSelectedMvArea() != null) {
				setSelectedMvArea(lastTable.getSelectedMvArea());
				if (actionToRunAfterCompletion != null) {
					doRedirect(actionToRunAfterCompletion);
					return;
				}
			}
		}

		populateWithPreviouslySelectedArea();
	}

	protected void doRedirect(Action actionToRunAfterCompletion) {
		FacesUtil.redirect(actionToRunAfterCompletion.action(), true);
	}

	private Integer readIncludeAreasAboveMyLevel() {
		Integer includeAreasAboveMyLevel = null;
		if (FacesUtil.resolveExpression("#{cc.attrs.includeAreasAboveMyLevelUpTo}") != null) {
			includeAreasAboveMyLevel = Integer.parseInt((String) FacesUtil.resolveExpression("#{cc.attrs.includeAreasAboveMyLevelUpTo}"));
		}
		return includeAreasAboveMyLevel;
	}

	private Integer readSelectionLevel() {
		Object resolvedExpression = FacesUtil.resolveExpression("#{cc.attrs.mvAreaSelectionLevel}");
		if (resolvedExpression != null) {
			return FacesUtil.getIntFromStringOrInteger(resolvedExpression);
		} else {
			return null;
		}
	}

	private Action readActionToRunAfterCompletion() {
		Object resolvedExpression = FacesUtil.resolveExpression("#{cc.attrs.actionController}");
		if (resolvedExpression != null) {
			return (Action) resolvedExpression;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<MvAreaFilter> readMvAreaFilters() {
		List<MvAreaFilter> mvAreaFilters = Collections.EMPTY_LIST;
		Object resolvedExpression = FacesUtil.resolveExpression("#{cc.attrs.mvAreaFilters}");
		if (resolvedExpression != null) {
			return (List<MvAreaFilter>) resolvedExpression;
		}
		return mvAreaFilters;
	}

	private void showAreaLevelError() {
		FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, mms.get("@area.common.no_access") + " " + mms.get("@area_level[" + selectionLevel
				+ "].name"), "");
		getFacesContext().addMessage(null, fmsg);
	}

	private void populateWithPreviouslySelectedArea() {
		boolean notPopulateWithPreviouslySelected = TRUE.equals(FacesUtil.resolveExpression("#{cc.attrs.notPopulateWithPreviouslySelected}"));
		if (!notPopulateWithPreviouslySelected) {
			MvArea previouslySelectedMvArea = MvArea.class.cast(FacesUtil.getSessionAttribute(PRESELECTED));
			if (previouslySelectedMvArea != null) {
				String areaPath = previouslySelectedMvArea.getAreaPath();
				String[] areaElements = areaPath.split("\\.");

				for (int level = contextPickerRoot.getAreaLevel() + 1; level <= previouslySelectedMvArea.getAreaLevel() && level <= selectionLevel; level++) {
					String path = StringUtils.join(Arrays.copyOfRange(areaElements, 0, level + 1), '.');
					update(level, path);
				}
			}
		}
	}

	/**
	 * Reload the specified level and selects the correct element if areaPath is provided
	 */
	public void update(int level, String areaPath) {
		MvArea parentMvArea = getParentMvArea(level);
		int currentIndex = level - contextPickerRoot.getAreaLevel() - 1;
		mvAreaPickerTables.get(currentIndex).setMvAreas(mvAreaService.findByPathAndChildLevel(parentMvArea));
		if (!StringUtils.isEmpty(areaPath)) {
			MvArea mvArea = mvAreaService.findSingleByPath(areaPath);
			if (mvArea != null && mvAreaPickerTables.get(currentIndex) != null && mvAreaPickerTables.get(currentIndex).getMvAreas() != null
					&& mvAreaPickerTables.get(currentIndex).getMvAreas().contains(mvArea)) {
				mvAreaPickerTables.get(currentIndex).setSelectedMvArea(mvArea);
			}
		}
	}

	/**
	 * A click on the select button in the picker
	 */
	public String select(int level) {
		int currentIndex = level - contextPickerRoot.getAreaLevel() - 1;
		setSelectedMvArea(mvAreaPickerTables.get(currentIndex).getSelectedMvArea());
		FacesUtil.setSessionAttribute(PRESELECTED, getSelectedMvArea());

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

	/**
	 * Evaluates sends the mvArea for evaluation by the method wrapped in me
	 * @param me The method that evaluates the the mvArea
	 * @param mvArea The mvArea to be evaluated
	 * @return The result of the foreign evaluation
	 */
	public Boolean evaluateIndicator(MethodExpression me, MvArea mvArea) {
		FacesContext context = getFacesContext();

		return (Boolean) me.invoke(context.getELContext(), new Object[] { mvArea });
	}

	/**
	 * Get the parent element for a level
	 */
	public MvArea getParentMvArea(int level) {
		// Need to subtract root area level (not always showing the entire tree) and 1 (because index starts at 0, level at 1)
		int currentIndex = level - contextPickerRoot.getAreaLevel() - 1;
		int parentIndex = currentIndex - 1;

		if (currentIndex == 0) {
			// we're at the top level
			return contextPickerRoot;
		}

		return mvAreaPickerTables.get(parentIndex).getSelectedMvArea();
	}

	public void injectParentMvArea(int level) {
		MvArea parentMvArea = getParentMvArea(level);
		AreaLevelEnum areaLevel = AreaLevelEnum.getLevel(level);
		switch (areaLevel) {
		case COUNTRY:
			countryController.setParentMvArea(parentMvArea);
			break;
		case COUNTY:
			countyController.setParentMvArea(parentMvArea);
			break;
		case MUNICIPALITY:
			municipalityController.setParentMvArea(parentMvArea);
			break;
		case BOROUGH:
			boroughController.setParentMvArea(parentMvArea);
			break;
		case POLLING_DISTRICT:
			pollingDistrictController.setParentMvArea(parentMvArea);
			break;
		case POLLING_PLACE:
			pollingPlaceController.setParentMvArea(parentMvArea);
			break;
		default:
			break;
		}
	}

	public void editMvArea(MvArea mvArea) {
		AreaLevelEnum level = AreaLevelEnum.getLevel(mvArea.getAreaLevel());
		injectParentMvArea(mvArea.getAreaLevel());
		switch (level) {
		case COUNTRY:
			countryController.setMvArea(mvArea);
			break;
		case COUNTY:
			countyController.setMvArea(mvArea);
			break;
		case MUNICIPALITY:
			municipalityController.setMvArea(mvArea);
			break;
		case BOROUGH:
			boroughController.setMvArea(mvArea);
			break;
		case POLLING_DISTRICT:
			pollingDistrictController.setMvArea(mvArea);
			break;
		case POLLING_PLACE:
			pollingPlaceController.setMvArea(mvArea);
			break;
		default:
			break;
		}
	}

	public void readMvArea(MvArea mvArea) {
		this.editMvArea(mvArea);
		AreaLevelEnum level = AreaLevelEnum.getLevel(mvArea.getAreaLevel());
		switch (level) {
		case COUNTRY:
			countryController.setReadOnly(true);
			break;
		case COUNTY:
			countyController.setReadOnly(true);
			break;
		case MUNICIPALITY:
			municipalityController.setReadOnly(true);
			break;
		case BOROUGH:
			boroughController.setReadOnly(true);
			break;
		case POLLING_DISTRICT:
			pollingDistrictController.setReadOnly(true);
			break;
		case POLLING_PLACE:
			pollingPlaceController.setReadOnly(true);
			break;
		default:
			break;
		}
	}

	public MvArea getSelectedMvArea() {
		return selectedMvArea;
	}

	public void setSelectedMvArea(MvArea selectedMvArea) {
		this.selectedMvArea = selectedMvArea;
	}

	public List<MvAreaPickerTable> getMvAreaPickerTables() {
		return mvAreaPickerTables;
	}

	public void setMvAreaPickerTables(List<MvAreaPickerTable> mvAreaPickerTables) {
		this.mvAreaPickerTables = mvAreaPickerTables;
	}

	public boolean isError() {
		return error;
	}

	/**
	 * Checks status and access rights to find out whether this element can be edited
	 * 
	 * @param level area context level for checking access rights
	 * @param mvArea area which may be editable or not
	 * @return true if editable or if the user has override privileges, else false
	 */
	public Boolean getIsEditable(int level, MvArea mvArea) {
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(userDataController);
		return resolver.isEditable(AreaLevelEnum.getLevel(level), mvArea);
	}

	/**
	 * Checks status and access rights to find out whether elements can be created
	 */
	public Boolean getIsCreatable(MvAreaPickerTable areaType) {

		if (userDataController.isOverrideAccess()) {
			return true;
		}

		int level = areaType.getLevel();

		boolean isCreatable = false;
		MvArea parentMvArea = getParentMvArea(level);

		switch (AreaLevelEnum.getLevel(level)) {
		case COUNTRY:
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi() && userDataController.isCentralConfigurationStatus();
			}
			break;
		case COUNTY:
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi() && parentMvArea.getElectionEvent().getElectionEventStatus().getId() == CENTRAL_CONFIGURATION.id();
			}
			break;
		case MUNICIPALITY:
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi() && parentMvArea.getElectionEvent().getElectionEventStatus().getId() == CENTRAL_CONFIGURATION.id();
			}
			break;
		case BOROUGH:
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi() && parentMvArea.getElectionEvent().getElectionEventStatus().getId() == CENTRAL_CONFIGURATION.id();
			}
			break;
		case POLLING_DISTRICT:
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi()
						&& parentMvArea.getMunicipality().getMunicipalityStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA
						&& parentMvArea.getElectionEvent().getElectionEventStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA;
			}
			break;
		case POLLING_PLACE:
			
			if (parentMvArea != null) {
				isCreatable = isKonfigurasjonGeografi()
						&& parentMvArea.getMunicipality().getMunicipalityStatus().getId() == MunicipalityStatusEnum.LOCAL_CONFIGURATION.id()
						&& parentMvArea.getElectionEvent().getElectionEventStatus().getId() == LOCAL_CONFIGURATION.id()
						&& !parentMvArea.getPollingDistrict().isParentPollingDistrict() && !parentMvArea.getPollingDistrict().isTechnicalPollingDistrict()
						&& !isPollingDistrictVotingDayAndAlreadyHasOnePollingPlace(areaType);
			}
			break;
		default:
			isCreatable = false;
			break;
		}

		// Immediately return if the user doesn't have access
		if (!isCreatable) {
			return false;
		}

		int idx = level - 1;
		// If we're at the first column, it should always be possible to create new
		if (idx > 0 && idx < mvAreaPickerTables.size()) {
			MvAreaPickerTable pickerTable = mvAreaPickerTables.get(idx);
			// The link should by default not be enabled if there are no MvAreas in the list
			isCreatable = pickerTable.getMvAreas() != null && !pickerTable.getMvAreas().isEmpty();

			// However, even if there are no MvAreas in the current column, the link should be displayed if there are are MvAreas on the *preceeding* level, and
			// it has an item selected:
			if (!isCreatable && idx != 0) {
				MvAreaPickerTable prevPickerTable = mvAreaPickerTables.get(idx - 1);
				if (prevPickerTable.getSelectedMvArea() != null && prevPickerTable.getMvAreas() != null && !prevPickerTable.getMvAreas().isEmpty()) {
					isCreatable = true;
				}
			}

		}
		return isCreatable;
	}

	private boolean isKonfigurasjonGeografi() {
		return userDataController.getUserAccess().isKonfigurasjonGeografi();
	}

	public boolean isPollingDistrictVotingDayAndAlreadyHasOnePollingPlace(MvAreaPickerTable areaType) {
		
		return areaType.getLevel() == AreaLevelEnum.POLLING_PLACE.getLevel() && areaType.getSize() > 0 && areaType.getMvAreas() != null
				&& areaType.getMvAreas().get(0).getPollingPlace() != null
				&& areaType.getMvAreas().get(0).getPollingPlace().isElectionDayVoting();
	}

	public void selectTopLevel() {
		setSelectedMvArea(mvAreaService.findRoot(userData.getElectionEventPk()));
	}

	/**
	 * Sets context root for picker. Default scope for testability.
	 * @param includeAreasAboveMyLevel true if areas above users access level should be shown
	 */
	void setContextPickerRoot(Integer includeAreasAboveMyLevel) {
		if (includeAreasAboveMyLevel != null) {
			contextPickerRoot = userData.getOperatorRole().getMvArea();
			if (contextPickerRoot.getAreaLevel() > includeAreasAboveMyLevel) {
				AreaPath areaPath = AreaPath.from(contextPickerRoot.getAreaPath()).toAreaLevelPath(AreaLevelEnum.getLevel(includeAreasAboveMyLevel));
				contextPickerRoot = mvAreaService.findSingleByPath(areaPath);
			}
		} else {
			contextPickerRoot = userData.getOperatorRole().getMvArea();
		}
	}

	public void setActionToRunAfterCompletion(Action actionToRunAfterCompletion) {
		this.actionToRunAfterCompletion = actionToRunAfterCompletion;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public String getAjaxUpdateSelector(int hierarchyIndex, String hierachyDepthInt) {

		int hierachyDepth = Integer.parseInt(hierachyDepthInt);
		StringBuilder ajaxSelector = new StringBuilder();

		// If we already are at the max selection level, just return a blank string. There are no more levels to update
		if (hierarchyIndex == hierachyDepth) {
			ajaxSelector
					.append("@composite:selectArea")
					.append(hierarchyIndex);
		} else {
			// We do not want to update the current level, only the lower levels
			int currentHierarchyIndex = hierarchyIndex + 1;

			while (currentHierarchyIndex <= hierachyDepth) {
				ajaxSelector
						.append("@composite:areaLevel")
						.append(currentHierarchyIndex)
						.append(" ");
				currentHierarchyIndex++;
			}
		}

		return ajaxSelector.toString();
	}

	public Boolean shouldAddAjaxUpdate(int hierarchyIndex, String hierachyDepthInt) {
		return hierarchyIndex < Integer.parseInt(hierachyDepthInt);
	}

	public boolean isPollingDistrictLevel(int areaLevel) {
		return areaLevel == AreaLevelEnum.POLLING_DISTRICT.getLevel();
	}

	public Integer getSelectionLevel() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.mvAreaSelectionLevel}");
		if (expr == null) {
			return null;
		}
		return FacesUtil.getIntFromStringOrInteger(expr);
	}

	public void setSelectionLevel(Integer selectionLevel) {
		this.selectionLevel = selectionLevel;
	}

	private boolean isAllSelectable() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.mvAreaAllSelectable}");
		return TRUE.equals(o) || Boolean.TRUE.equals(o);
	}

	private boolean isKeepLastPickerAfterSelection() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.keepLastPickerAfterSelection}");
		if (expr == null) {
			return false;
		}
		return (Boolean) expr;
	}

	private boolean isHideBelowWantedAreaLevel() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.hideBelowWantedAreaLevel}");
		if (expr == null) {
			return false;
		}
		return (Boolean) expr;
	}

	private boolean isRedirectWithoutCid() {
		Object expr = FacesUtil.resolveExpression("#{cc.attrs.redirectWithoutCid}");
		if (expr == null) {
			return false;
		}
		return (Boolean) expr;
	}

	private boolean isDisableSelect() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.disableSelect}");
		return o != null && (Boolean) o;
	}

	private boolean isHideLeadingSingles() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.mvAreaHideLeadingSingles}");
		return o != null && (Boolean) o;
	}

	private boolean isDisableSelectWhenNotOk() {
		Object o = FacesUtil.resolveExpression("#{cc.attrs.disableSelectWhenNotOk}");
		return o != null && (Boolean) o;
	}

	private MethodExpression getIndicatorEvaluator() {
		return (MethodExpression) FacesUtil.resolveExpression("#{cc.attrs.mvAreaIndicatorEvaluator}");
	}

	public boolean isRenderPicker() {
		boolean turnedOn = getSelectionLevel() != null;
		boolean mvElectionOK = mvElectionPickerController.getSelectionLevel() == null || mvElectionPickerController.getSelectedMvElection() != null;
		boolean needsPicking = getSelectedMvArea() == null || isKeepLastPickerAfterSelection();
		return !isError() && turnedOn && mvElectionOK && needsPicking;
	}

	public String getHeaderKey() {
		if (isAllSelectable()) {
			return "@area.common.area_level";
		}
		return "@area_level[" + getSelectionLevel() + "].name";
	}

	public boolean isHideTable(MvAreaPickerTable table) {
		if (isHideSingles(table)) {
			return true;
		}
		boolean oneAreaInList = table.getMvAreas() != null && table.getMvAreas().size() == 1;
		boolean levelNotOnSelectionLevel = table.getLevel() != getSelectionLevel();
		boolean hideBelowWantedLevel = isHideBelowWantedAreaLevel() && table.getLevel() > getSelectionLevel();
		return (oneAreaInList && levelNotOnSelectionLevel && !isAllSelectable()) || hideBelowWantedLevel;
	}

	public boolean isRenderButton(MvAreaPickerTable table) {
		if (isHideSingles(table)) {
			return false;
		}
		return isAllSelectable() || (getSelectionLevel() != null && table.getLevel() == getSelectionLevel());
	}

	public boolean isDisabledButton(MvAreaPickerTable table) {
		if (table.getSelectedMvArea() == null || isDisableSelect()) {
			return true;
		}
		return isDisableSelectWhenNotOk() && getIndicatorEvaluator() != null
				&& !evaluateIndicator(getIndicatorEvaluator(), table.getSelectedMvArea());
	}

	private boolean isHideSingles(MvAreaPickerTable table) {
		if (isHideLeadingSingles()) {
			if (table.getMvAreas() != null && table.getMvAreas().size() == 1) {
				// This one is one, what about parents?
				MvAreaPickerTable parent = parent(table);
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

	private MvAreaPickerTable parent(MvAreaPickerTable mvAreaPickerTable) {
		for (int i = 0; i < mvAreaPickerTables.size(); i++) {
			MvAreaPickerTable table = mvAreaPickerTables.get(i);
			if (i > 0 && table.getLevel() == mvAreaPickerTable.getLevel()) {
				return mvAreaPickerTables.get(i - 1);
			}
		}
		return null;
	}
}
