package no.valg.eva.admin.frontend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.test.ObjectAssert;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SelectRoleControllerTest extends BaseFrontendTest {

	public static final String MUNICIPALITY = "municipality";
	private SelectRoleController selectRoleController;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		selectRoleController = new SelectRoleController();
	}

	@Test
	public void getBaseAreaName_withRoot_returnsSystem() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.ROOT);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo("System");
	}

	@Test
	public void getBaseAreaName_withCountry_returnsCountry() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.COUNTRY);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo("country");
	}

	@Test
	public void getBaseAreaName_withMunicipality_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.MUNICIPALITY);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getBaseAreaName_withBorough_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.BOROUGH);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getBaseAreaName_withPollingDistrict_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.POLLING_DISTRICT);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getBaseAreaName_withPollingPlace_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.POLLING_PLACE);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getBaseAreaName_withPollingStation_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.POLLING_STATION);
		ObjectAssert.assertThat(selectRoleController.getBaseAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getAreaName_withRoot_returnsSystem() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.ROOT);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("System");
	}

	@Test
	public void getAreaName_withCountry_returnsCountry() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.COUNTRY);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("country");
	}

	@Test
	public void getAreaName_withCounty_returnsCounty() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.COUNTY);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("county");
	}

	@Test
	public void getAreaName_withMunicipality_returnsMunicipality() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.MUNICIPALITY);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void getAreaName_withBorough_returnsBorough() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.BOROUGH);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("borough");
	}

	@Test
	public void getAreaName_withPollingDistrict_returnsIdAndPollingDistrict() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.POLLING_DISTRICT);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("AAAA pollingDistrict");
	}

	@Test
	public void getAreaName_withPollingPlace_returnsPollingPlace() throws Exception {
		OperatorRole role = mockOperatorRole(AreaLevelEnum.POLLING_PLACE);
		ObjectAssert.assertThat(selectRoleController.getAreaName(role)).isEqualTo("pollingPlace");
	}

	@Test
	public void getContestName_withContest_returnsContestName() throws Exception {
		SelectRoleController ctrl = initializeMocks(SelectRoleController.class);
		OperatorRole operatorRole = createMock(OperatorRole.class);
		when(operatorRole.getMvElection().getContest().getName()).thenReturn("Contest");

		ObjectAssert.assertThat(ctrl.getContestName(operatorRole)).isEqualTo("Contest");
	}

	private OperatorRole mockOperatorRole(AreaLevelEnum level) {
		OperatorRole result = mock(OperatorRole.class);
		MvArea area = new MvArea();
		area.setAreaLevel(level.getLevel());
		area.setCountryName("country");
		area.setCountyName("county");
		area.setMunicipalityName(MUNICIPALITY);
		area.setBoroughName("borough");
		area.setPollingDistrictName("pollingDistrict");
		area.setPollingDistrictId("AAAA");
		area.setPollingPlaceName("pollingPlace");
		area.setPollingPlaceId("BBBB");
		when(result.getMvArea()).thenReturn(area);
		return result;
	}
}
