package no.valg.eva.admin.frontend.rbac.ctrls;

import static com.google.common.collect.Lists.transform;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ViewScoped
public class OperatorListController extends BaseController {

	private static final Pattern ROLE_PLACE_PATTERN = Pattern.compile("([^@]*)@([^@]*)");
	private static final String FILTER_NOT_SET = "0";

	@Inject
	private AdminOperatorService adminOperatorService;
	@Inject
	private OperatorEditController editController;
	@Inject
	private UserData userData;
	@Inject
	private MessageProvider messageProvider;

	private List<OperatorWrapper> operatorList;
	private List<OperatorWrapper> filteredOperatorList;
	private Map<String, Operator> operatorIds = new HashMap<>();
	private List<RoleItem> extractedRoles;
	private List<PollingPlaceArea> extractAreas;
	private List<SelectItem> roleFilters;
	private List<SelectItem> areaFilters;
	private String roleFilter = FILTER_NOT_SET;
	private String areaFilter = FILTER_NOT_SET;

	public void initOperatorListsInArea(AreaPath areaPath) {
		setOperatorList(new ArrayList<>(transform(
				adminOperatorService.operatorsInArea(userData, areaPath), 
				OperatorWrapper::new)
		));
		operatorIds.clear();
		for (OperatorWrapper wrapper : operatorList) {
			operatorIds.put(wrapper.getValue().getPersonId().getId(), wrapper.getValue());
		}
		extractRolesAndAreasFromOperatorList();
		setFilteredOperatorList(operatorList);
	}

	public void openEditMode(Operator operator) {
		editController.init(operator, RbacView.EDIT);
	}

	public boolean filterRoleAssociations(Object value, Object filter, Locale locale) {
		Matcher matcher = ROLE_PLACE_PATTERN.matcher(filter.toString());
		if (!matcher.matches()) {
			return true;
		}
		String aRoleFilter = matcher.group(1);
		String placeFilter = matcher.group(2);
		List<RoleAssociation> roleAssociations = (List<RoleAssociation>) value;
		for (RoleAssociation roleAssociation : roleAssociations) {
			if (filterRoleAssociation(aRoleFilter, placeFilter, roleAssociation)) {
				return true;
			}
		}
		return false;
	}

	public List<RoleAssociation> getRoleAssociations(Operator operator) {
		List<RoleAssociation> result = new ArrayList<>();
		for (RoleAssociation association : operator.getRoleAssociations()) {
			if (filterRoleAssociation(getRoleFilter(), getAreaFilter(), association)) {
				result.add(association);
			}
		}
		return result;
	}

	public String getAreaNameStyle(PollingPlaceArea area) {
		if (area.getPollingPlaceType() == PollingPlaceType.ELECTION_DAY_VOTING) {
			return "pollingplace pollingplace-electionday";
		} else if (area.getPollingPlaceType() == PollingPlaceType.ADVANCE_VOTING) {
			return "pollingplace pollingplace-advance";
		}
		return "";
	}

	public Operator getOperator(Person person) {
		return operatorIds.get(person.getPersonId().getId());
	}

	public boolean exists(Person person) {
		return operatorIds.containsKey(person.getPersonId().getId());
	}

	public void updated(Operator operator) {
		OperatorWrapper wrapper = new OperatorWrapper(operator);
		int index = operatorList.indexOf(wrapper);
		if (index == -1) {
			operatorList.add(0, wrapper);
		} else {
			operatorList.get(index).setValue(operator);
		}
		operatorIds.put(operator.getPersonId().getId(), operator);
		extractRolesAndAreasFromOperatorList();
	}

	public void removed(Operator operator) {
		OperatorWrapper wrapper = new OperatorWrapper(operator);
		operatorList.remove(wrapper);
		filteredOperatorList.remove(wrapper);
		operatorIds.remove(operator.getPersonId().getId());
	}

	public List<OperatorWrapper> getOperatorList() {
		return operatorList;
	}

	public void setOperatorList(List<OperatorWrapper> operatorList) {
		this.operatorList = operatorList;
	}

	public List<OperatorWrapper> getFilteredOperatorList() {
		return filteredOperatorList;
	}

	public void setFilteredOperatorList(List<OperatorWrapper> filteredOperatorList) {
		this.filteredOperatorList = filteredOperatorList;
	}

	public List<SelectItem> getRoleFilters() {
		return roleFilters;
	}

	public List<SelectItem> getAreaFilters() {
		return areaFilters;
	}

	public String getRoleFilter() {
		return roleFilter;
	}

	public void setRoleFilter(String roleFilter) {
		this.roleFilter = roleFilter;
	}

	public String getAreaFilter() {
		return areaFilter;
	}

	public void setAreaFilter(String areaFilter) {
		this.areaFilter = areaFilter;
	}

	private void extractRolesAndAreasFromOperatorList() {
		Set<RoleItem> roleSet = new HashSet<>();
		Set<PollingPlaceArea> areaSet = new HashSet<>();
		for (OperatorWrapper wrapper : operatorList) {
			for (RoleAssociation association : wrapper.getValue().getRoleAssociations()) {
				roleSet.add(association.getRole());
				extractAreaFromRoleAssociation(association, areaSet);
			}
		}
		extractedRoles = new ArrayList<>(roleSet);
		extractAreas = new ArrayList<>(areaSet);
		createRoleFilters();
		createAreaFilters();
	}

	void extractAreaFromRoleAssociation(RoleAssociation association, Set<PollingPlaceArea> areaSet) {
		boolean addArea = true;
		PollingPlaceArea area = association.getArea();
		if (userData.isElectionEventAdminUser()) {
			// If root level operator, only add areas with level municipality and higher
			addArea = area.getAreaPath().getLevel().getLevel() <= AreaLevelEnum.MUNICIPALITY.getLevel();
		} else {
			// This is done because they often have the same same, and it will confuse the end users seeing both.
			if (area.getAreaPath().isPollingPlaceLevel()) {
				// Remove any matching polling districts
				Set<PollingPlaceArea> pollingDistricts = getMatchingPollingDistrictsForPollingPlace(areaSet, area);
				for (PollingPlaceArea pollingDistrict : pollingDistricts) {
					areaSet.remove(pollingDistrict);
				}
			} else if (area.getAreaPath().isPollingDistrictLevel()) {
				// Add only if we dont find matching polling places
				addArea = !doesSetContainMatchingPollingPlaceForPollingDistrict(areaSet, area);
			}
		}
		if (addArea) {
			areaSet.add(area);
		}
	}

	private boolean doesSetContainMatchingPollingPlaceForPollingDistrict(Set<PollingPlaceArea> areaSet, PollingPlaceArea pollingDistrictArea) {
		for (PollingPlaceArea areaFromSet : areaSet) {
			if (areaFromSet.getAreaPath().isPollingPlaceLevel() && areaFromSet.getAreaPath().isSubpathOf(pollingDistrictArea.getAreaPath())) {
				return true;
			}
		}
		return false;
	}

	private Set<PollingPlaceArea> getMatchingPollingDistrictsForPollingPlace(Set<PollingPlaceArea> areaSet, PollingPlaceArea pollingPlaceArea) {
		Set<PollingPlaceArea> result = new HashSet<>();
		for (PollingPlaceArea pollingDistrictArea : areaSet) {
			if (pollingDistrictArea.getAreaPath().isPollingDistrictLevel() && pollingDistrictArea.contains(pollingPlaceArea)) {
				result.add(pollingDistrictArea);
			}
		}
		return result;
	}

	private void createRoleFilters() {
		roleFilters = new ArrayList<>();
		for (RoleItem roleItem : extractedRoles) {
			if (userData.isElectionEventAdminUser() || !roleItem.isUserSupport()) {
				roleFilters.add(new SelectItem(roleItem.getRoleId(), messageProvider.getByElectionEvent(roleItem.getRoleName(),
						userData.getElectionEventPk())));
			}
		}
		// Sort by label
		Collections.sort(roleFilters, new Comparator<SelectItem>() {
			@Override
			public int compare(SelectItem item1, SelectItem item2) {
				return item1.getLabel().compareTo(item2.getLabel());
			}
		});
	}

	private void createAreaFilters() {
		// Order by name
		Collections.sort(extractAreas, new Comparator<PollingPlaceArea>() {
			@Override
			public int compare(PollingPlaceArea area1, PollingPlaceArea area2) {
				return area1.getName().compareTo(area2.getName());
			}
		});
		areaFilters = new ArrayList<>();
		for (PollingPlaceArea area : extractAreas) {
			StringBuilder label = new StringBuilder();
			String cls = getAreaNameStyle(area);
			label.append("<span");
			if (!cls.isEmpty()) {
				label.append(" class=\"").append(cls).append("\"");
			}
			label.append(">");
			label.append(area.getName());
			label.append("</span>");
			SelectItem item = new SelectItem(area.getAreaPath().path(), label.toString());
			item.setEscape(false);
			areaFilters.add(item);
		}
	}

	private boolean filterRoleAssociation(String roleFilter, String placeFilter, RoleAssociation roleAssociation) {
		boolean includeByRole = isBlankFilter(roleFilter) || roleFilter.equals(roleAssociation.getRole().getRoleId());
		boolean includeByArea = isBlankFilter(placeFilter) || placeFilter.equals(roleAssociation.getArea().getAreaPath().path());
		if (userData.isElectionEventAdminUser()) {
			// Root level operator can only filter on municipality level or higher. If filter is on municipality level, include sub paths
			includeByArea = includeByArea || AreaPath.from(placeFilter).isMunicipalityLevel()
					&& roleAssociation.getArea().getAreaPath().isSubpathOf(AreaPath.from(placeFilter));
		} else {
			includeByArea = includeByArea || isFilterOnPollingPlaceLevelAndMatchOnPollingDistrict(AreaPath.from(placeFilter), roleAssociation);
			includeByArea = includeByArea || isFilterOnPollingDistrictLevelAndMatchOnPollingPlace(AreaPath.from(placeFilter), roleAssociation);
		}
		return includeByRole && includeByArea;
	}

	private boolean isBlankFilter(String filter) {
		return isBlank(filter) || FILTER_NOT_SET.equals(filter);
	}

	private boolean isFilterOnPollingPlaceLevelAndMatchOnPollingDistrict(AreaPath areaPathFilter, RoleAssociation association) {
		if (areaPathFilter != null && areaPathFilter.isPollingPlaceLevel()) {
			return areaPathFilter.toPollingDistrictPath().equals(association.getArea().getAreaPath());
		}
		return false;

	}

	private boolean isFilterOnPollingDistrictLevelAndMatchOnPollingPlace(AreaPath areaPathFilter, RoleAssociation association) {
		if (areaPathFilter != null && areaPathFilter.isPollingDistrictLevel()) {
			AreaPath associationPath = association.getArea().getAreaPath();
			boolean isAssociationOnPollingPlaceLevel = associationPath.isPollingPlaceLevel();
			return isAssociationOnPollingPlaceLevel && associationPath.toPollingDistrictPath().equals(areaPathFilter);
		}
		return false;
	}
}
