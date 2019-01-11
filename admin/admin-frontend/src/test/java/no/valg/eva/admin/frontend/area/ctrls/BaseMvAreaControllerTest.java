package no.valg.eva.admin.frontend.area.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BaseMvAreaControllerTest extends BaseFrontendTest {

	private BaseMvAreaController ctrl;

	@BeforeMethod
	public void setUp() throws Exception {
		this.ctrl = initializeMocks(new MyBaseAreaController());

		when(getInjectMock(MvAreaService.class).findAllAreaLevels(any(UserData.class))).thenReturn(Arrays.asList(
				getAreaLevel(AreaLevelEnum.COUNTRY),
				getAreaLevel(AreaLevelEnum.COUNTY),
				getAreaLevel(AreaLevelEnum.MUNICIPALITY),
				getAreaLevel(AreaLevelEnum.BOROUGH),
				getAreaLevel(AreaLevelEnum.POLLING_DISTRICT)));
        when(getInjectMock(MvAreaService.class).findByPathAndChildLevel(any(MvArea.class))).thenReturn(Collections.singletonList(getCountry().values().iterator().next()));

		defaultSetup();

		this.ctrl.doInit();
		this.ctrl.setSelectedAreaLevel(AreaLevelEnum.POLLING_STATION.getLevel());
	}

	@Test(dataProvider = "changeAreaLevel")
	public void changeAreaLevel_withDataProvider_verifyState(AreaLevelEnum areaLevelEnum) throws Exception {
		defaultSetup();
		ctrl.setAreaLevelId(String.valueOf(areaLevelEnum.getLevel()));

		ctrl.changeAreaLevel();

		assertThat(ctrl.getSelectedAreaLevel()).isEqualTo(areaLevelEnum.getLevel());
		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changeCountry_withCountryLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.COUNTRY.getLevel());

		ctrl.changeCountry();

		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changeCountry_withCountyLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.COUNTY.getLevel());

		ctrl.changeCountry();

		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNull();
	}

	@Test
	public void changeCounty_withCountyLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.COUNTY.getLevel());

		ctrl.changeCounty();

		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changeCounty_withMunicipalityLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());

		ctrl.changeCounty();

		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNull();
	}

	@Test
	public void changeMunicipality_withMunicipalityLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());

		ctrl.changeMunicipality();

		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changeMunicipality_withBoroughLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.BOROUGH.getLevel());

		ctrl.changeMunicipality();

		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNull();
	}

	@Test
	public void changeBorough_withBoroughLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.BOROUGH.getLevel());

		ctrl.changeBorough();

		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changeBorough_withPollingDistrictLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.POLLING_DISTRICT.getLevel());

		ctrl.changeBorough();

		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNull();
	}

	@Test
	public void changePollingDistrict_withPollingDistrictLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.POLLING_DISTRICT.getLevel());

		ctrl.changePollingDistrict();

		assertThat(ctrl.isAreaSelectForAreaLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNotNull();
	}

	@Test
	public void changePollingDistrict_withPollingPlaceLevel_verifyState() throws Exception {
		defaultSetup();
		ctrl.setSelectedAreaLevel(AreaLevelEnum.POLLING_PLACE.getLevel());

		ctrl.changePollingDistrict();

		assertThat(ctrl.isAreaSelectForDownLevelCompleted()).isTrue();
		assertThat(ctrl.getSelectedMvArea()).isNull();
	}

	@DataProvider(name = "changeAreaLevel")
	public static Object[][] changeAreaLevel() {
		return new Object[][] {
				{ AreaLevelEnum.ROOT },
				{ AreaLevelEnum.COUNTRY },
				{ AreaLevelEnum.COUNTY },
				{ AreaLevelEnum.MUNICIPALITY },
				{ AreaLevelEnum.BOROUGH },
				{ AreaLevelEnum.POLLING_DISTRICT },
				{ AreaLevelEnum.POLLING_PLACE }
		};
	}

	private void defaultSetup() throws Exception {
		getCountry();
		getCounty();
		getMunicipality();
		getBorough();
		getPollingDistrict();
		getPollingPlace();
	}

	private AreaLevel getAreaLevel(AreaLevelEnum areaLevelEnum) {
		return new AreaLevel(areaLevelEnum);

	}

	private Map<String, MvArea> getCountry() throws Exception {
		ctrl.setCountryId("47");
		MvArea mvArea = new MvArea();
		mvArea.setCountryId(ctrl.getCountryId());
		mvArea.setCountryName("Norge");
		mvArea.setAreaLevel(AreaLevelEnum.COUNTRY.getLevel());
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getCountryId(), mvArea);
		mockFieldValue("countryMap", result);
		return result;
	}

	private Map<String, MvArea> getCounty() throws Exception {
		ctrl.setCountyId("48");
		MvArea mvArea = getCountry().values().iterator().next();
		mvArea.setCountyId(ctrl.getCountyId());
		mvArea.setCountyName("Oslo");
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getCountyId(), mvArea);
		mockFieldValue("countyMap", result);
		return result;
	}

	private Map<String, MvArea> getMunicipality() throws Exception {
		ctrl.setMunicipalityId("49");
		MvArea mvArea = getCounty().values().iterator().next();
		mvArea.setMunicipalityId(ctrl.getMunicipalityId());
		mvArea.setMunicipalityName("Oslo");
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getMunicipalityId(), mvArea);
		mockFieldValue("municipalityMap", result);
		return result;
	}

	private Map<String, MvArea> getBorough() throws Exception {
		ctrl.setBoroughId("50");
		MvArea mvArea = getMunicipality().values().iterator().next();
		mvArea.setBoroughId(ctrl.getBoroughId());
		mvArea.setBoroughName("St Hanshaugen");
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getBoroughId(), mvArea);
		mockFieldValue("boroughMap", result);
		return result;
	}

	private Map<String, MvArea> getPollingDistrict() throws Exception {
		ctrl.setPollingDistrictId("52");
		MvArea mvArea = getBorough().values().iterator().next();
		mvArea.setPollingDistrictId(ctrl.getPollingDistrictId());
		mvArea.setPollingDistrictName("St Hanshaugen");
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getPollingDistrictId(), mvArea);
		mockFieldValue("pollingDistrictMap", result);
		return result;
	}

	private Map<String, MvArea> getPollingPlace() throws Exception {
		ctrl.setPollingPlaceId("53");
		MvArea mvArea = getPollingDistrict().values().iterator().next();
		mvArea.setPollingPlaceId(ctrl.getPollingPlaceId());
		mvArea.setPollingPlaceName("Ila skole");
		Map<String, MvArea> result = new HashMap<>();
		result.put(ctrl.getPollingPlaceId(), mvArea);
		mockFieldValue("pollingPlaceMap", result);
		return result;
	}

	private class MyBaseAreaController extends BaseMvAreaController {

	}
}
