package no.valg.eva.admin.frontend.area.ctrls;

import static no.valg.eva.admin.util.StringUtil.isSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.commons.lang3.StringUtils;

/**
 * Used for selecting areas in the area hierarchy.
 */
public abstract class BaseMvAreaController extends ConversationScopedController {

	private static final String DASH = " - ";
	// Area level
	private final List<SelectItem> areaLevelItems = new ArrayList<>();
	// Area level 1: Country
	private final List<SelectItem> countryItems = new ArrayList<>();
	// Area level 2: County
	private final List<SelectItem> countyItems = new ArrayList<>();
	// Area level 3: Municipality
	private final List<SelectItem> municipalityItems = new ArrayList<>();
	// Area level 4: Borough
	private final List<SelectItem> boroughItems = new ArrayList<>();
	// Area level 5: Polling district
	private final List<SelectItem> pollingDistrictItems = new ArrayList<>();
	// Area level 6: Polling place
	private final List<SelectItem> pollingPlaceItems = new ArrayList<>();
	@Inject
	private MvAreaService mvAreaService;
	@Inject
	private MessageProvider messageProvider;
	// Election event
	private ElectionEvent electionEvent;
	private String areaLevelId;
	private Map<Integer, String> areaLevelMap;
	private List<AreaLevel> areaLevelList;
	private int selectedAreaLevel;
	// Area level 0: Root
	private MvArea mvAreaRoot;
	private String countryId;
	private Map<String, MvArea> countryMap;
	private List<MvArea> countryList;
	private String countyId;
	private List<MvArea> countyList;
	private Map<String, MvArea> countyMap;
	private String municipalityId;
	private Map<String, MvArea> municipalityMap;
	private List<MvArea> municipalityList;
	private String boroughId;
	private Map<String, MvArea> boroughMap;
	private List<MvArea> boroughList;
	private String pollingDistrictId;
	private Map<String, MvArea> pollingDistrictMap;
	private List<MvArea> pollingDistrictList;
	private String pollingPlaceId;
	private Map<String, MvArea> pollingPlaceMap;
	private List<MvArea> pollingPlaceList;

	// Common MvArea
	private MvArea selectedMvArea;

	private boolean areaSelectForAreaLevelCompleted = false;
	private boolean areaSelectForDownLevelCompleted = false;
	private boolean createAllForParentArea = false;

	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;

	@Override
	protected void doInit() {
		loadAndReset();
	}

	public void loadAndReset() {
		loadElectionEvent();
		loadMvAreaRoot();
		loadAreaLevels();
		reloadCountries();

		resetAreaChildListboxes(0);
		resetAreaFields();
		setSelectedAreaLevel(0);
	}

	public void changeAreaLevel() {
		int previousAreaLevel = selectedAreaLevel;
		if (areaLevelId != null) {
			changeAreaLevelToNull(areaLevelId);
		}

		if (selectedAreaLevel < previousAreaLevel) {
			changeToLowerAreaLevel();
			resetAreaChildListboxes(selectedAreaLevel);
		}
	}

	private void changeToLowerAreaLevel() {
		AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
		switch (level) {
		case COUNTRY:
			if (isSet(countryId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = countryMap.get(countryId);
			}
			break;
		case COUNTY:
			if (isSet(countyId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = countyMap.get(countyId);
			}

			areaSelectForDownLevelCompleted = true;
			break;
		case MUNICIPALITY:
			if (isSet(municipalityId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = municipalityMap.get(municipalityId);
			}

			areaSelectForDownLevelCompleted = true;
			break;
		case BOROUGH:
			if (isSet(boroughId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = boroughMap.get(boroughId);
			}

			areaSelectForDownLevelCompleted = true;
			break;
		case POLLING_DISTRICT:
			if (isSet(pollingDistrictId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = pollingDistrictMap.get(pollingDistrictId);
			}
			areaSelectForDownLevelCompleted = true;
			break;
		case POLLING_PLACE:
			if (isSet(pollingPlaceId)) {
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = pollingPlaceMap.get(pollingPlaceId);
			}
			areaSelectForDownLevelCompleted = true;
			break;
		default:
			break;
		}
	}

	private void changeAreaLevelToNull(final String areaLevelId) {
		selectedAreaLevel = Integer.parseInt(areaLevelId);
		resetAreaFields();
		AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
		switch (level) {
		case ROOT:
			areaSelectForAreaLevelCompleted = true;
			areaSelectForDownLevelCompleted = true;
			selectedMvArea = mvAreaRoot;
			break;
		case COUNTRY:
			areaSelectForDownLevelCompleted = true;
			break;
		default:
			break;
		}
	}

	public void changeCountry() {
		resetAreaFields();
		if (isSet(countryId) && getSelectedAreaLevel() > 0) {
			reloadCounties(countryId);
			AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
			switch (level) {
			case COUNTRY:
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = countryMap.get(countryId);
				break;
			case COUNTY:
				areaSelectForDownLevelCompleted = true;
				break;
			default:
				break;
			}
		}

		resetAreaChildListboxes(1);
	}

	public void changeCounty() {
		resetAreaFields();
		if (isSet(countyId) && getSelectedAreaLevel() > AreaLevelEnum.COUNTRY.getLevel()) {
			reloadMunicipalities(countyId);
			AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
			switch (level) {
			case COUNTY:
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = countyMap.get(countyId);
				break;
			case MUNICIPALITY:
				areaSelectForDownLevelCompleted = true;
				break;
			default:
				break;
			}
		}

		resetAreaChildListboxes(2);
	}

	public void changeMunicipality() {
		resetAreaFields();
		// NOTE EVA-811: It must be possible to select municipality also on county level
		if (isSet(municipalityId) && getSelectedAreaLevel() >= AreaLevelEnum.COUNTY.getLevel()) {
			reloadBoroughs(municipalityId);
			AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
			switch (level) {
			case COUNTY:
			case MUNICIPALITY:
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = municipalityMap.get(municipalityId);
				break;
			case BOROUGH:
				areaSelectForDownLevelCompleted = true;
				break;
			default:
				break;
			}
		}

		resetAreaChildListboxes(AreaLevelEnum.MUNICIPALITY.getLevel());
	}

	public void changeBorough() {
		resetAreaFields();
		if (isSet(boroughId) && getSelectedAreaLevel() > AreaLevelEnum.MUNICIPALITY.getLevel()) {
			reloadPollingDistricts(boroughId);
			AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
			switch (level) {
			case BOROUGH:
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = boroughMap.get(boroughId);
				break;
			case POLLING_DISTRICT:
				areaSelectForDownLevelCompleted = true;
				break;
			default:
				break;
			}
		}

		resetAreaChildListboxes(AreaLevelEnum.BOROUGH.getLevel());
	}

	public void changePollingDistrict() {
		resetAreaFields();
		if (isSet(pollingDistrictId) && getSelectedAreaLevel() > AreaLevelEnum.BOROUGH.getLevel()) {
			reloadPollingPlaces(pollingDistrictId);
			AreaLevelEnum level = AreaLevelEnum.getLevel(getSelectedAreaLevel());
			switch (level) {
			case POLLING_DISTRICT:
				areaSelectForAreaLevelCompleted = true;
				selectedMvArea = pollingDistrictMap.get(pollingDistrictId);
				break;
			case POLLING_PLACE:
				areaSelectForDownLevelCompleted = true;
				break;
			default:
				break;
			}
		}

		resetAreaChildListboxes(AreaLevelEnum.POLLING_DISTRICT.getLevel());
	}

	/**
	 * Used for clearing drop downs when creating contests. Don't reset country
	 */
	public void clearItemsLists() {
		countyItems.clear();
		municipalityItems.clear();
		boroughItems.clear();
		pollingDistrictItems.clear();
		pollingPlaceItems.clear();
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public Map<Integer, String> getAreaLevelMap() {
		return areaLevelMap;
	}

	public List<SelectItem> getAreaLevelItems() {
		return areaLevelItems;
	}

	public List<SelectItem> getCountryItems() {
		return countryItems;
	}

	public List<SelectItem> getCountyItems() {
		return countyItems;
	}

	public List<SelectItem> getMunicipalityItems() {
		return municipalityItems;
	}

	public List<SelectItem> getBoroughItems() {
		return boroughItems;
	}

	public List<SelectItem> getPollingDistrictItems() {
		return pollingDistrictItems;
	}

	public List<SelectItem> getPollingPlaceItems() {
		return pollingPlaceItems;
	}

	public String getAreaLevelId() {
		return areaLevelId;
	}

	public void setAreaLevelId(final String areaLevelId) {
		this.areaLevelId = areaLevelId;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	public String getCountyId() {
		return countyId;
	}

	public void setCountyId(final String countyId) {
		this.countyId = countyId;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public String getBoroughId() {
		return boroughId;
	}

	public void setBoroughId(final String boroughId) {
		this.boroughId = boroughId;
	}

	public String getPollingDistrictId() {
		return pollingDistrictId;
	}

	public void setPollingDistrictId(final String pollingDistrictId) {
		this.pollingDistrictId = pollingDistrictId;
	}

	public String getPollingPlaceId() {
		return pollingPlaceId;
	}

	public void setPollingPlaceId(final String pollingPlaceId) {
		this.pollingPlaceId = pollingPlaceId;
	}

	public int getSelectedAreaLevel() {
		return selectedAreaLevel;
	}

	public void setSelectedAreaLevel(final int selectedAreaLevel) {
		this.selectedAreaLevel = selectedAreaLevel;
	}

	public int getSelectedAreaLevelUpLevel() {
		if (selectedAreaLevel > 0) {
			return selectedAreaLevel - 1;
		} else {
			return 0;
		}
	}

	public MvArea getSelectedMvArea() {
		return selectedMvArea;
	}

	public List<MvArea> getMunicipalityList() {
		return municipalityList;
	}

	public boolean isAreaSelectForAreaLevelCompleted() {
		return areaSelectForAreaLevelCompleted;
	}

	public boolean isAreaSelectForDownLevelCompleted() {
		return areaSelectForDownLevelCompleted;
	}

	public String getSelectedAreaId() {
		if (selectedMvArea == null) {
			return "";
		}

		AreaLevelEnum level = selectedMvArea.getActualAreaLevel();
		switch (level) {
		case COUNTRY:
			return selectedMvArea.getCountryId();
		case COUNTY:
			return selectedMvArea.getCountyId();
		case MUNICIPALITY:
			return selectedMvArea.getMunicipalityId();
		case BOROUGH:
			return selectedMvArea.getBoroughId();
		case POLLING_DISTRICT:
			return selectedMvArea.getPollingDistrictId();
		case POLLING_PLACE:
			return selectedMvArea.getPollingPlaceId();
		default:
			return "-";
		}
	}

	public String getSelectedAreaName() {
		if (selectedMvArea == null) {
			return "";
		}

		AreaLevelEnum level = selectedMvArea.getActualAreaLevel();
		switch (level) {
		case COUNTRY:
			return selectedMvArea.getCountryName();
		case COUNTY:
			return selectedMvArea.getCountyName();
		case MUNICIPALITY:
			return selectedMvArea.getMunicipalityName();
		case BOROUGH:
			return selectedMvArea.getBoroughName();
		case POLLING_DISTRICT:
			return selectedMvArea.getPollingDistrictName();
		case POLLING_PLACE:
			return selectedMvArea.getPollingPlaceName();
		default:
			return "-";
		}
	}

	/**
	 * When election is not single area the user can type in the name of the contest when creating and editing of a contest
	 */
	public void setSelectedAreaName(final String selectedAreaName) {
		if (selectedMvArea == null) {
			return;
		}

		AreaLevelEnum level = selectedMvArea.getActualAreaLevel();
		switch (level) {
		case COUNTRY:
			selectedMvArea.setCountryName(selectedAreaName);
			break;
		case COUNTY:
			selectedMvArea.setCountyName(selectedAreaName);
			break;
		case MUNICIPALITY:
			selectedMvArea.setMunicipalityName(selectedAreaName);
			break;
		case BOROUGH:
			selectedMvArea.setBoroughName(selectedAreaName);
			break;
		case POLLING_DISTRICT:
			selectedMvArea.setPollingDistrictName(selectedAreaName);
			break;
		case POLLING_PLACE:
			selectedMvArea.setPollingPlaceName(selectedAreaName);
			break;
		default:
			return;
		}
	}

	private void loadElectionEvent() {
		electionEvent = userDataController.getElectionEvent();
	}

	private void loadAreaLevels() {
		if (areaLevelList == null) {
			areaLevelList = mvAreaService.findAllAreaLevels(userData);
			areaLevelMap = new HashMap<>();
		}
		areaLevelItems.clear();
		String areaLevelName = "";
		for (AreaLevel areaLevel : areaLevelList) {
			areaLevelName = messageProvider.get(areaLevel.getName());
			areaLevelItems.add(new SelectItem(areaLevel.getId(), areaLevel.getId() + DASH + areaLevelName));
			areaLevelMap.put(areaLevel.getId(), areaLevelName);
		}
	}

	private void loadMvAreaRoot() {
		mvAreaRoot = mvAreaService.findRoot(getElectionEvent().getPk());
	}

	private void reloadCountries() {
		countryList = mvAreaService.findByPathAndChildLevel(mvAreaRoot);
		countryMap = new HashMap<>();
		countryItems.clear();
		for (MvArea mvArea : countryList) {
			countryMap.put(mvArea.getCountryId(), mvArea);
			countryItems.add(new SelectItem(mvArea.getCountryId(), mvArea.getCountryId() + DASH + mvArea.getCountryName()));
		}
	}

	private void reloadCounties(final String countryId) {
		if (!StringUtils.isEmpty(countryId)) {
			MvArea selectedCountry = countryMap.get(countryId);
			countyList = mvAreaService.findByPathAndChildLevel(selectedCountry);
			countyMap = new HashMap<>();
			countyItems.clear();
			for (MvArea mvArea : countyList) {
				countyMap.put(mvArea.getCountyId(), mvArea);
				countyItems.add(new SelectItem(mvArea.getCountyId(), mvArea.getCountyId() + DASH + mvArea.getCountyName()));
			}
		} else {
			countyList = null;
			countyMap = null;
		}
	}

	private void reloadMunicipalities(final String countyId) {
		if (!StringUtils.isEmpty(countyId)) {
			municipalityList = getMunicipalitiesForCounty(countyId);
			municipalityMap = new HashMap<>();
			municipalityItems.clear();
			for (MvArea mvArea : municipalityList) {
				municipalityMap.put(mvArea.getMunicipalityId(), mvArea);
				municipalityItems.add(new SelectItem(mvArea.getMunicipalityId(), mvArea.getMunicipalityId() + DASH + mvArea.getMunicipalityName()));
			}
		} else {
			municipalityList = null;
			municipalityMap = null;
		}
	}
	
	public List<MvArea> getMunicipalitiesForCounty(String countyId) {
		if (countyId == null)  {
			return new ArrayList<>();
		}
		MvArea selectedCounty = countyMap.get(countyId);
		return mvAreaService.findByPathAndChildLevel(selectedCounty);
	}

	private void reloadBoroughs(final String municipalityId) {
		if (!StringUtils.isEmpty(municipalityId)) {
			MvArea selectedMunicipality = municipalityMap.get(municipalityId);
			boroughList = mvAreaService.findByPathAndChildLevel(selectedMunicipality);
			boroughMap = new HashMap<>();
			boroughItems.clear();
			for (MvArea mvArea : boroughList) {
				boroughMap.put(mvArea.getBoroughId(), mvArea);
				boroughItems.add(new SelectItem(mvArea.getBoroughId(), mvArea.getBoroughId() + DASH + mvArea.getBoroughName()));
			}
		} else {
			boroughList = null;
			boroughMap = null;
		}
	}

	private void reloadPollingDistricts(final String boroughId) {
		if (!StringUtils.isEmpty(boroughId)) {
			MvArea selectedBorough = boroughMap.get(boroughId);
			pollingDistrictList = mvAreaService.findByPathAndChildLevel(selectedBorough);
			pollingDistrictMap = new HashMap<>();
			pollingDistrictItems.clear();
			for (MvArea mvArea : pollingDistrictList) {
				pollingDistrictMap.put(mvArea.getPollingDistrictId(), mvArea);
				pollingDistrictItems.add(new SelectItem(mvArea.getPollingDistrictId(), mvArea.getPollingDistrictId() + DASH + mvArea.getPollingDistrictName()));
			}
		} else {
			pollingDistrictList = null;
			pollingDistrictMap = null;
		}
	}

	private void reloadPollingPlaces(final String pollingDistrictId) {
		if (!StringUtils.isEmpty(pollingDistrictId)) {
			MvArea selectedPollingDistrict = pollingDistrictMap.get(pollingDistrictId);
			pollingPlaceList = mvAreaService.findByPathAndChildLevel(selectedPollingDistrict);
			pollingPlaceMap = new HashMap<>();
			pollingPlaceItems.clear();
			for (MvArea mvArea : pollingPlaceList) {
				pollingPlaceMap.put(mvArea.getPollingPlaceId(), mvArea);
				pollingPlaceItems.add(new SelectItem(mvArea.getPollingPlaceId(), mvArea.getPollingPlaceId() + DASH + mvArea.getPollingPlaceName()));
			}
		} else {
			pollingPlaceList = null;
			pollingPlaceMap = null;
		}
	}

	private void resetAreaFields() {
		areaSelectForAreaLevelCompleted = false;
		areaSelectForDownLevelCompleted = false;
		selectedMvArea = null;
	}

	private void resetAreaChildListboxes(final int parentAreaLevel) {
		if (parentAreaLevel < AreaLevelEnum.COUNTRY.getLevel() && isSet(countryId)) {
			countryId = null;
		}

		if (parentAreaLevel < AreaLevelEnum.COUNTY.getLevel() && isSet(countyId)) {
			countyId = null;
		}

		if (parentAreaLevel < AreaLevelEnum.MUNICIPALITY.getLevel() && isSet(municipalityId)) {
			municipalityId = null;
		}

		if (parentAreaLevel < AreaLevelEnum.BOROUGH.getLevel() && isSet(boroughId)) {
			boroughId = null;
		}

		if (parentAreaLevel < AreaLevelEnum.POLLING_DISTRICT.getLevel() && isSet(pollingDistrictId)) {
			pollingDistrictId = null;
		}

		if (parentAreaLevel < AreaLevelEnum.POLLING_PLACE.getLevel() && isSet(pollingPlaceId)) {
			pollingPlaceId = null;
		}

	}

	public List<MvArea> returnListBasedOnLevel(final int areaLevel) {
		switch (AreaLevelEnum.getLevel(areaLevel)) {
		case COUNTRY:
			return countryList;
		case COUNTY:
			return countyList;
		case MUNICIPALITY:
			return municipalityList;
		case BOROUGH:
			return boroughList;
		case POLLING_DISTRICT:
			return pollingDistrictList;
		case POLLING_PLACE:
			return pollingPlaceList;
		default:
			return null;
		}
	}

	public boolean isCreateAllForParentArea() {
		return createAllForParentArea;
	}

	public void setCreateAllForParentArea(final boolean createAllForParentArea) {
		this.createAllForParentArea = createAllForParentArea;
	}
}
