package no.valg.eva.admin.rbac.domain;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.domain.model.RoleAreaLevel;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class RoleAreaServiceTest extends MockUtilsTestCase {

    private static final RoleItem ROLE_1 = new RoleItem("role_1", "role_1_name", false, null, new ArrayList<>());
    private static final RoleItem ROLE_2 = new RoleItem("role_2", "role_2_name", false, null, new ArrayList<>());
    private static final RoleItem ROLE_3 = new RoleItem("role_3", "role_3_name", false, null, new ArrayList<>());
	private static final AreaPath COUNTY_PATH = AreaPath.from("201301.47.03");
	private static final AreaPath MUNICIPALITY_PATH = AreaPath.from("201301.47.03.0301");
	private static final String BOROUGH_SUB_PATH = "030101";
	private static final String ADVANCE_BOROUGH_SUB_PATH = "030100";
	private static final String POLLING_DISTRICT_SUB_PATH = "0101";
	private static final String ADVANCE_POLLING_DISTRICT_SUB_PATH = RoleAreaService.MUNICIPALITY_POLLING_DISTRICT;
	private static final String POLLING_PLACE_SUB_PATH = "0101";
	private static final String ADVANCE_POLLING_PLACE_SUB_PATH = "0001";
	private static final String CENTRAL_REGISTRATION_SUB_PATH = "9999";
	private static final AreaPath POLLING_DISTRICT_PATH = MUNICIPALITY_PATH.add(BOROUGH_SUB_PATH).add(POLLING_DISTRICT_SUB_PATH);
	private static final AreaPath POLLING_PLACE_PATH = POLLING_DISTRICT_PATH.add(POLLING_PLACE_SUB_PATH);
	private static final AreaPath ADVANCE_POLLING_PLACE_PATH = MUNICIPALITY_PATH.add(ADVANCE_BOROUGH_SUB_PATH).add(ADVANCE_POLLING_DISTRICT_SUB_PATH)
			.add(ADVANCE_POLLING_PLACE_SUB_PATH);
	private static final Long AN_ELECTION_EVENT_PK = 1L;
	private static final ElectionEvent AN_ELECTION_EVENT = new ElectionEvent();
	static {
		AN_ELECTION_EVENT.setPk(AN_ELECTION_EVENT_PK);
	}
	private RoleAreaService roleAreaService;

	@BeforeMethod
	public void setup() throws Exception {
		roleAreaService = initializeMocks(RoleAreaService.class);
	}

	@Test
	public void areasForRole_whenNoAssignableRoles_returnsEmptyMap() {
		MvArea county = county();
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(COUNTY_PATH)).thenReturn(county);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(Collections.EMPTY_LIST);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(COUNTY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).isEmpty();
	}

	@Test
	public void areasForRole_areaPathForCounty_returnsMapWithRolesForCounty() {
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(COUNTY_PATH)).thenReturn(county());
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(twoAssignableRoles());
		Role role = mock(Role.class);
		when(role.canBeAssignedToArea(eq(AreaLevelEnum.COUNTY), any(PollingPlaceType.class))).thenReturn(true);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(role);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(COUNTY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(2);
		assertThat(roleAreaMap.get(ROLE_1)).isEqualTo(countyAreas());
		assertThat(roleAreaMap.get(ROLE_2)).isEqualTo(countyAreas());
	}

	@Test
	public void assignableRolesForArea_areaPathForCounty_returnsRolesForCounty() {
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(COUNTY_PATH)).thenReturn(county());
		List<RoleItem> roleItems = oneAssignableRole(ROLE_1);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(roleItems);

		Collection<RoleItem> result = roleAreaService.assignableRolesForArea(COUNTY_PATH, AN_ELECTION_EVENT_PK);

		assertThat(result).isEqualTo(roleItems);
	}

	@Test
	public void assignableRolesForArea_areaPathForMunicipality_returnsRolesForMunicipalityPollingDistrictAndPollingPlace() {
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(COUNTY_PATH)).thenReturn(county());
		List<RoleItem> roleItemsMunicipality = oneAssignableRole(ROLE_1);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(eq(AreaLevelEnum.MUNICIPALITY), anyLong())).thenReturn(roleItemsMunicipality);
		List<RoleItem> roleItemsPollingDistrict = oneAssignableRole(ROLE_2);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(eq(AreaLevelEnum.POLLING_DISTRICT), anyLong())).thenReturn(roleItemsPollingDistrict);
		List<RoleItem> roleItemsPollingPlace = oneAssignableRole(ROLE_3);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(eq(AreaLevelEnum.POLLING_PLACE), anyLong())).thenReturn(roleItemsPollingPlace);

		Collection<RoleItem> result = roleAreaService.assignableRolesForArea(MUNICIPALITY_PATH, AN_ELECTION_EVENT_PK);

		Collection<RoleItem> expectedResult = new HashSet<>(roleItemsMunicipality);
		expectedResult.addAll(roleItemsPollingDistrict);
		expectedResult.addAll(roleItemsPollingPlace);
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void findAssignableRolesForOperatorRole() {
		OperatorRole operatorRoleMock = createMock(OperatorRole.class);
		List<Role> roles = new ArrayList<>();
        roles.add(createRole("id2", "name2", true, new HashSet<>()));
        roles.add(createRole("id2", "name2", false, new HashSet<>()));
		when(getInjectMock(RoleRepository.class).findAssignableRolesForOperatorRole(operatorRoleMock)).thenReturn(roles);

		List<RoleItem> items = new ArrayList<>(roleAreaService.findAssignableRolesForOperatorRole(operatorRoleMock));

		assertThat(items).hasSize(2);
	}

	@Test
	public void areasForRole_areaPathForMunicipality_returnsMapWithRolesForMunicipalityAndPollingDistricts() {
		MvArea municipalityArea = municipalityArea(false, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(oneAssignableRole(ROLE_1));
		Role munAndPollingDistrictRole = mock(Role.class);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.MUNICIPALITY), any(PollingPlaceType.class))).thenReturn(true);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_DISTRICT), any(PollingPlaceType.class))).thenReturn(true);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_PLACE), any(PollingPlaceType.class))).thenReturn(false);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(munAndPollingDistrictRole);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(MUNICIPALITY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(1);
		assertThat(roleAreaMap.get(ROLE_1)).isEqualTo(expectedMunicipalityAndPollingDistrictAreas());
	}

	@Test
	public void areasForRole_areaPathForMunicipalityWithPollingDistrictWithId0000_returnsMapWithRolesForMunicipalityButNoPollingDistrict() {
		MvArea municipalityArea = municipalityArea(false, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(oneAssignableRole(ROLE_1));
		Role munAndPollingDistrictRole = mock(Role.class);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.MUNICIPALITY), any(PollingPlaceType.class))).thenReturn(true);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_DISTRICT), any(PollingPlaceType.class))).thenReturn(true);
		when(munAndPollingDistrictRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_PLACE), any(PollingPlaceType.class))).thenReturn(false);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(munAndPollingDistrictRole);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(MUNICIPALITY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(1);
		assertThat(roleAreaMap.get(ROLE_1)).isEqualTo(expectedMunicipalityAndPollingDistrictAreas());
	}

	@Test
	public void areasForRole_areaPathForMunicipality_returnsMapWithRolesForMunicipalityAndPollingPlaces() {
		MvArea municipalityArea = municipalityArea(false, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(oneAssignableRole(ROLE_1));
		Role munAndPollingPlaceRole = mock(Role.class);
		when(munAndPollingPlaceRole.canBeAssignedToArea(eq(AreaLevelEnum.MUNICIPALITY), any(PollingPlaceType.class))).thenReturn(true);
		when(munAndPollingPlaceRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_DISTRICT), any(PollingPlaceType.class))).thenReturn(false);
		when(munAndPollingPlaceRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_PLACE), any(PollingPlaceType.class))).thenReturn(true);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(munAndPollingPlaceRole);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(MUNICIPALITY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(1);
		assertThat(new TreeSet<>(roleAreaMap.get(ROLE_1))).isEqualTo(new TreeSet<>(expectedMunicipalityAndPollingPlaceAreas()));
	}

	@Test
	public void areasForRole_oneElectionDayPollingPlaceAsSubArea_returnsMapWithRoleForElectionDayPollingPlace() {
		MvArea municipalityArea = municipalityArea(false, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(oneAssignableRole(ROLE_1));
		Role pollingPlaceRole = mock(Role.class);
		when(pollingPlaceRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_PLACE), eq(PollingPlaceType.ELECTION_DAY_VOTING))).thenReturn(true);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(pollingPlaceRole);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(MUNICIPALITY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(1);
		assertThat(roleAreaMap.get(ROLE_1)).isEqualTo(expectedElectionDayPollingPlaceArea());
	}

	@Test
	public void areasForRole_twoAdvancePollingPlacesAsSubArea_returnsMapWithRoleForAdvancePollingPlaceWithCentralRegistrationPlaceLeftOut() {
		boolean addEnvelopePollingPlace = true;
		MvArea municipalityArea = municipalityArea(false, addEnvelopePollingPlace);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);
		when(getInjectMock(RoleRepository.class).assignableRolesForArea(any(AreaLevelEnum.class), anyLong())).thenReturn(oneAssignableRole(ROLE_1));
		Role pollingPlaceRole = mock(Role.class);
		when(pollingPlaceRole.canBeAssignedToArea(eq(AreaLevelEnum.POLLING_PLACE), eq(PollingPlaceType.ADVANCE_VOTING))).thenReturn(true);
		when(getInjectMock(RoleRepository.class).findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(pollingPlaceRole);

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = roleAreaService.areasForRole(MUNICIPALITY_PATH, AN_ELECTION_EVENT);

		assertThat(roleAreaMap).hasSize(1);
		assertThat(roleAreaMap.get(ROLE_1)).isEqualTo(expectedAdvancePollingPlaceArea());
	}

	private List<PollingPlaceArea> countyAreas() {
		List<PollingPlaceArea> areasForRole = new ArrayList<>();
		areasForRole.add(new PollingPlaceArea(COUNTY_PATH, "Fylke"));
		return areasForRole;
	}

	private List<PollingPlaceArea> expectedMunicipalityAndPollingPlaceAreas() {
		List<PollingPlaceArea> areasForRole = new ArrayList<>();
		areasForRole.add(new PollingPlaceArea(MUNICIPALITY_PATH, "Kommune"));
		areasForRole.add(new PollingPlaceArea(POLLING_PLACE_PATH, "Stemmested"));
		areasForRole.add(new PollingPlaceArea(ADVANCE_POLLING_PLACE_PATH, "Forhåndsstemmested"));
		return areasForRole;
	}

	private List<PollingPlaceArea> expectedElectionDayPollingPlaceArea() {
		List<PollingPlaceArea> areasForRole = new ArrayList<>();
		areasForRole.add(new PollingPlaceArea(POLLING_PLACE_PATH, "Stemmested", PollingPlaceType.ELECTION_DAY_VOTING));
		return areasForRole;
	}

	private List<PollingPlaceArea> expectedAdvancePollingPlaceArea() {
		List<PollingPlaceArea> areasForRole = new ArrayList<>();
		areasForRole.add(new PollingPlaceArea(ADVANCE_POLLING_PLACE_PATH, "Forhåndsstemmested", PollingPlaceType.ADVANCE_VOTING));
		return areasForRole;
	}

	private List<PollingPlaceArea> expectedMunicipalityAndPollingDistrictAreas() {
		List<PollingPlaceArea> areasForRole = new ArrayList<>();
		areasForRole.add(new PollingPlaceArea(MUNICIPALITY_PATH, "Kommune"));
		areasForRole.add(new PollingPlaceArea(POLLING_DISTRICT_PATH, "Stemmekrets"));
		return areasForRole;
	}

	private List<RoleItem> oneAssignableRole(RoleItem role1) {
		List<RoleItem> assignableRoles = new ArrayList<>();
		assignableRoles.add(role1);
		return assignableRoles;
	}

	private List<RoleItem> twoAssignableRoles() {
		List<RoleItem> assignableRoles = new ArrayList<>();
		assignableRoles.add(ROLE_1);
		assignableRoles.add(ROLE_2);
		return assignableRoles;
	}

	@Test
	public void listContainingCountyIsReturnedWhenAreaPathIsPathForCounty() {
		MvArea county = county();
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(COUNTY_PATH)).thenReturn(county);

		assertThat(roleAreaService.selectableSubAreasForArea(COUNTY_PATH).get(0)).isEqualTo(county.toViewObject());
	}

	@Test
	public void selectableSubAreasForArea_withTellekrets_shouldReturnTellekrets() {

		MvArea municipalityArea = municipalityArea(true, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);

		List<PollingPlaceArea> result = roleAreaService.selectableSubAreasForArea(MUNICIPALITY_PATH);

		assertThat(result).hasSize(4);

	}

	@Test
	public void listContainingMunicipalityPollingDistrictAndPollingPlacesIsReturnedWhenAreaPathIsPathForMunicipality() {
		MvArea municipalityArea = municipalityArea(false, false);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(MUNICIPALITY_PATH)).thenReturn(municipalityArea);

		List<PollingPlaceArea> result = roleAreaService.selectableSubAreasForArea(MUNICIPALITY_PATH);

		assertThat(result).hasSize(3);
	}

	private MvArea municipalityArea(boolean tellekrets, boolean addEnvelopePollingPlace) {
		MvArea municipalityArea = new MvArea();
		municipalityArea.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
		municipalityArea.setMunicipalityName("Kommune");
		municipalityArea.setAreaPath(MUNICIPALITY_PATH.path());
		County fakeCounty = mock(County.class);
		Municipality municipality = new Municipality();
		municipality.setCounty(fakeCounty);
		when(fakeCounty.areaPath()).thenReturn(AreaPath.from("201301.47.03"));
		municipality.setId(AreaPath.OSLO_MUNICIPALITY_ID);
		municipality.getBoroughs().add(borough(municipality, tellekrets));
		municipality.getBoroughs().add(boroughWithAdvance(municipality, addEnvelopePollingPlace));
		municipalityArea.setMunicipality(municipality);
		return municipalityArea;
	}

	private MvArea county() {
		MvArea county = new MvArea();
		county.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());
		county.setCountyName("Fylke");
		county.setAreaPath(COUNTY_PATH.path());
		return county;
	}

	private Borough borough(Municipality municipality, boolean tellekrets) {
		Borough borough = new Borough();
		borough.setPk(0L);
		borough.setId(BOROUGH_SUB_PATH);
		borough.setMunicipality(municipality);
		borough.getPollingDistricts().add(pollingDistrict(borough));
		if (tellekrets) {
			borough.getPollingDistricts().iterator().next().setPollingDistrict(pollingDistrict(borough));
		}
		return borough;
	}

	private Borough boroughWithAdvance(Municipality municipality, boolean addEnvelopePollingPlace) {
		Borough borough = new Borough();
		borough.setId(ADVANCE_BOROUGH_SUB_PATH);
		borough.setPk(1L);
		borough.setMunicipality(municipality);
		borough.getPollingDistricts().add(pollingDistrictWithId0000(borough, addEnvelopePollingPlace));
		return borough;
	}

	private PollingDistrict pollingDistrict(Borough borough) {
		PollingDistrict pd = new PollingDistrict();
		pd.setPk(0L);
		pd.setId(POLLING_DISTRICT_SUB_PATH);
		pd.setName("Stemmekrets");
		pd.setBorough(borough);
		pd.getPollingPlaces().add(electionDayPollingPlace(pd));
		return pd;
	}

	private PollingDistrict pollingDistrictWithId0000(Borough borough, boolean addEnvelopePollingPlace) {
		PollingDistrict pd = new PollingDistrict();
		pd.setPk(1L);
		pd.setId("0000");
		pd.setName("Krets 0000");
		pd.setBorough(borough);
		pd.getPollingPlaces().add(advancePollingPlace(pd));
		if (addEnvelopePollingPlace) {
			pd.getPollingPlaces().add(advancePollingPlaceForCentralEnvelopeRegistration(pd));
		}
		return pd;
	}

	private PollingPlace electionDayPollingPlace(PollingDistrict pollingDistrict) {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setPk(0L);
		pollingPlace.setId(POLLING_PLACE_SUB_PATH);
		pollingPlace.setName("Stemmested");
		pollingPlace.setPollingDistrict(pollingDistrict);
		pollingPlace.setElectionDayVoting(true);
		return pollingPlace;
	}

	private PollingPlace advancePollingPlace(PollingDistrict pollingDistrict) {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setPk(1L);
		pollingPlace.setId(ADVANCE_POLLING_PLACE_SUB_PATH);
		pollingPlace.setName("Forhåndsstemmested");
		pollingPlace.setPollingDistrict(pollingDistrict);
		return pollingPlace;
	}

	private PollingPlace advancePollingPlaceForCentralEnvelopeRegistration(PollingDistrict pollingDistrict) {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setPk(2L);
		pollingPlace.setId(CENTRAL_REGISTRATION_SUB_PATH);
		pollingPlace.setName("Forhåndsstemmested - konvolutt");
		pollingPlace.setPollingDistrict(pollingDistrict);
		return pollingPlace;
	}

	private Role createRole(String id, String name, boolean isSupport, Set<RoleAreaLevel> levels) {
		Role role = new Role();
		role.setId(id);
		role.setName(name);
		role.setUserSupport(isSupport);
		role.setRoleAreaLevels(levels);
		return role;
	}

}

