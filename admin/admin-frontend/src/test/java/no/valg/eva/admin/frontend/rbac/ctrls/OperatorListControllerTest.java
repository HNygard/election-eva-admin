package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OperatorListControllerTest extends BaseRbacTest {

	@Test
	public void extractAreaFromRoleAssociation_withMixedAskerAreas_returns4Areas() throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		List<RoleAssociation> roleAssociations = new ArrayList<>();

		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220"), "Asker"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220"), "Asker"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0000"), "Hele kommunen"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0000"), "Hele kommunen"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0000.0001"), "URNE - Servicetorget"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0000.0001"), "URNE - Servicetorget"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0001"), "Nesøya"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0002"), "Nesbru"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0001"), "Nesøya"));
		roleAssociations.add(getRoleAssociation(AreaPath.from("150001.47.02.0220.022000.0002"), "Nesbru"));

		Set<PollingPlaceArea> areaSet = new HashSet<>();
		for (RoleAssociation association : roleAssociations) {
			ctrl.extractAreaFromRoleAssociation(association, areaSet);
		}

		Set<String> expected = new HashSet<>(asList("0220", "0220.022000.0000.0001", "0220.022000.0001", "0220.022000.0002"));
		for (PollingPlaceArea area : areaSet) {
			expected.remove(area.getAreaPath().path().substring("150001.47.02.".length()));
		}
		assertThat(expected).isEmpty();
	}

	private RoleAssociation getRoleAssociation(AreaPath areaPath, String name) {
		RoleAssociation result = createMock(RoleAssociation.class);
		PollingPlaceArea area = new PollingPlaceArea(areaPath, name);
		when(result.getArea()).thenReturn(area);
		return result;
	}

	@Test
	public void initOperatorLists_withOperators_verifyInitialState() throws Exception {
		OperatorListController ctrl = defaultSetup();

		assertThat(ctrl.getOperatorList()).hasSize(3);
		assertThat(ctrl.getFilteredOperatorList()).hasSize(3);
		assertThat(ctrl.getRoleFilters()).hasSize(3);
		assertThat(ctrl.getRoleFilters().get(0).getValue()).isEqualTo(ROLE_ANSVARLIG_URNETELLING.getRoleId());
		assertThat(ctrl.getRoleFilters().get(1).getValue()).isEqualTo(ROLE_FORHANDSTEMME_MOTTAKER.getRoleId());
		assertThat(ctrl.getRoleFilters().get(2).getValue()).isEqualTo(ROLE_VALGADMIN_KOMMUNE.getRoleId());
		assertThat(ctrl.getAreaFilters()).hasSize(3);
		assertThat(ctrl.getAreaFilters().get(0).getLabel()).isEqualTo("<span class=\"pollingplace pollingplace-advance\">Ambulerende</span>");
		assertThat(ctrl.getAreaFilters().get(1).getLabel()).isEqualTo("<span class=\"pollingplace pollingplace-electionday\">Lunner</span>");
		assertThat(ctrl.getAreaFilters().get(2).getLabel()).isEqualTo("<span>Lunner omsorgssenter</span>");
	}

	@Test
	public void initOperatorLists_withOperatorsAndOperatorRootLevel_verifyInitialState() throws Exception {
		OperatorListController ctrl = rootLevelSetup();

		assertThat(ctrl.getOperatorList()).hasSize(1);
		assertThat(ctrl.getRoleFilters().get(0).getValue()).isEqualTo(ROLE_ANSVARLIG_URNETELLING.getRoleId());
		assertThat(ctrl.getRoleFilters().get(1).getValue()).isEqualTo(ROLE_FORHANDSTEMME_MOTTAKER.getRoleId());
		assertThat(ctrl.getRoleFilters().get(2).getValue()).isEqualTo(ROLE_VALGADMIN_KOMMUNE.getRoleId());
		// For root level we should only get county and municipality level filters (in this case only Oppland county and Lunner municipality)
		assertThat(ctrl.getAreaFilters()).hasSize(2);
		assertThat(ctrl.getAreaFilters().get(0).getLabel()).isEqualTo("<span class=\"pollingplace pollingplace-electionday\">Lunner</span>");
		assertThat(ctrl.getAreaFilters().get(1).getLabel()).isEqualTo("<span>Oppland</span>");
	}

	@Test
	public void openEditMode_withCandidate_verifyEditMode() throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		Operator operator = createMock(Operator.class);

		ctrl.openEditMode(operator);

		verify(getInjectMock(OperatorEditController.class)).init(operator, RbacView.EDIT);
	}

	@Test
	public void filterRoleAssociations_withInvalidPattern_returnsTrue() throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);

		assertThat(ctrl.filterRoleAssociations(null, "bad-filter", null)).isTrue();
	}

	@Test(dataProvider = "filterRoleAssociations")
	public void filterRoleAssociations_withDataProvider_verifyExpected(List<RoleAssociation> associations, String filter, boolean expected) throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);

		assertThat(AreaPath.from("752900.47.05.0533.053300.0000").isPollingDistrictLevel()).isTrue();
		assertThat(AreaPath.from("752900.47.05.0533.053300.0000.0002").isPollingPlaceLevel()).isTrue();

		assertThat(ctrl.filterRoleAssociations(associations, filter, null)).isEqualTo(expected);
	}

	@Test(dataProvider = "getRoleAssociations")
	public void getRoleAssociations_withDataSource_verifyExpected(String roleFilter, String areaFilter, List<RoleAssociation> associations, int expected)
			throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		ctrl.setRoleFilter(roleFilter);
		ctrl.setAreaFilter(areaFilter);
		Operator operator = createMock(Operator.class);
		when(operator.getRoleAssociations()).thenReturn(associations);

		assertThat(ctrl.getRoleAssociations(operator).size()).isEqualTo(expected);
	}

	@Test
	public void getRoleAssociations_withRootLevelOperatorAndCountyFilter_returnsOppland() throws Exception {
		OperatorListController ctrl = rootLevelSetup();
		Operator operator = ctrl.getOperatorList().get(0).getValue();
		ctrl.setAreaFilter(AREA_OPPLAND.getAreaPath().path());

		List<RoleAssociation> result = ctrl.getRoleAssociations(operator);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getArea().getName()).isEqualTo("Oppland");
	}

	@Test
	public void getRoleAssociations_withRootLevelOperatorAndMunicipalityFilter_returnsLunnerAndChildren() throws Exception {
		OperatorListController ctrl = rootLevelSetup();
		Operator operator = ctrl.getOperatorList().get(0).getValue();
		ctrl.setAreaFilter(AREA_LUNNER.getAreaPath().path());

		List<RoleAssociation> result = ctrl.getRoleAssociations(operator);

		assertThat(result).hasSize(3);
		assertThat(result.get(0).getArea().getName()).isEqualTo("Lunner");
		assertThat(result.get(1).getArea().getName()).isEqualTo("Lunner district");
		assertThat(result.get(2).getArea().getName()).isEqualTo("Lunner omsorgssenter");
	}

	@Test(dataProvider = "getAreaNameStyle")
	public void getAreaNameStyle_withDataProvider_verifyExpected(PollingPlaceType type, String expected) throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		PollingPlaceArea area = createMock(PollingPlaceArea.class);
		when(area.getPollingPlaceType()).thenReturn(type);

		assertThat(ctrl.getAreaNameStyle(area)).isEqualTo(expected);
	}

	@Test
	public void getOperator_withNonExisting_returnsNull() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Person person = createMock(Person.class);
		when(person.getPersonId().getId()).thenReturn("NOT_EXISTING");

		assertThat(ctrl.getOperator(person)).isNull();
	}

	@Test
	public void getOperator_withExisting_returnsOperator() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Person person = createMock(Person.class);
		when(person.getPersonId().getId()).thenReturn(BRUKER_ID_2);

		assertThat(ctrl.getOperator(person)).isNotNull();
	}

	@Test
	public void exists_withNonExisting_returnsFalse() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Person person = createMock(Person.class);
		when(person.getPersonId().getId()).thenReturn("NOT_EXISTING");

		assertThat(ctrl.exists(person)).isFalse();
	}

	@Test
	public void exists_withExisting_returnsTrue() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Person person = createMock(Person.class);
		when(person.getPersonId().getId()).thenReturn(BRUKER_ID_3);

		assertThat(ctrl.exists(person)).isTrue();
	}

	@Test
	public void updated_withNonExisting_verifyAddedAndRoleAndAreaFiltersUpdated() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Operator operator = createMock(Operator.class);
		when(operator.getPersonId().getId()).thenReturn("NOT_EXISTING");
        List<RoleAssociation> associations = singletonList(roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_LUNNER_DISTRICT));
		when(operator.getRoleAssociations()).thenReturn(associations);
		assertThat(ctrl.getOperatorList()).hasSize(3);
		assertThat(ctrl.getRoleFilters()).hasSize(3);
		assertThat(ctrl.getAreaFilters()).hasSize(3);

		ctrl.updated(operator);

		assertThat(ctrl.exists(operator)).isTrue();
		assertThat(ctrl.getOperator(operator)).isNotNull();
		assertThat(ctrl.getOperatorList()).hasSize(4);
		assertThat(ctrl.getRoleFilters()).hasSize(3);
		assertThat(ctrl.getAreaFilters()).hasSize(3);
	}

	@Test
	public void updated_withExisting_verifyUpdated() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Operator operator = createMock(Operator.class);
		when(operator.getPersonId().getId()).thenReturn(BRUKER_ID_2);
        List<RoleAssociation> associations = singletonList(roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_LUNNER_DISTRICT));
		when(operator.getRoleAssociations()).thenReturn(associations);
		assertThat(ctrl.getOperatorList()).hasSize(3);
		assertThat(ctrl.getOperator(operator).getRoleAssociations()).hasSize(2);

		ctrl.updated(operator);

		assertThat(ctrl.getOperatorList()).hasSize(3);
		assertThat(ctrl.getOperator(operator).getRoleAssociations()).hasSize(1);
	}

	@Test
	public void removed_withOperator_verifyRemoved() throws Exception {
		OperatorListController ctrl = defaultSetup();
		Operator operator = createMock(Operator.class);
		when(operator.getPersonId().getId()).thenReturn(BRUKER_ID_2);
		assertThat(ctrl.getOperatorList()).hasSize(3);
		assertThat(ctrl.getFilteredOperatorList()).hasSize(3);

		ctrl.removed(operator);

		assertThat(ctrl.getOperatorList()).hasSize(2);
		assertThat(ctrl.getFilteredOperatorList()).hasSize(2);

	}

	private OperatorListController defaultSetup() throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		when(getUserDataMock().getOperatorAreaPath().isRootLevel()).thenReturn(false);
		stub_adminOperatorService_operatorsInArea(asList(
				operator(BRUKER_ID_1, asList(
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER),
						roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_LUNNER_OMSORGSSENTER_PLACE),
						roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_LUNNER_OMSORGSSENTER_PLACE))),
				operator(BRUKER_ID_2, asList(
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER),
						roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_LUNNER))),
                operator(BRUKER_ID_3, singletonList(
                        roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE)))));

		ctrl.initOperatorListsInArea(getUserDataMock().getOperatorAreaPath());
		return ctrl;
	}

	private OperatorListController rootLevelSetup() throws Exception {
		OperatorListController ctrl = initializeMocks(OperatorListController.class);
		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
		/*when(getUserDataMock().getOperatorRole().getMvArea().getAreaLevel()).thenReturn(AreaLevelEnum.ROOT.getLevel());
		when(getUserDataMock().getOperatorRole().getMvElection().getElectionLevel()).thenReturn(ElectionLevelEnum.ELECTION_EVENT.getLevel());*/
        stub_adminOperatorService_operatorsInArea(singletonList(operator(BRUKER_ID_1, asList(
                roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_OPPLAND),
                roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER),
                roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_LUNNER_DISTRICT),
                roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_LUNNER_OMSORGSSENTER_PLACE)))));

		ctrl.initOperatorListsInArea(getUserDataMock().getOperatorAreaPath());
		return ctrl;
	}

	private void stub_adminOperatorService_operatorsInArea(List<Operator> operatorList) {
		when(getInjectMock(AdminOperatorService.class).operatorsInArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(operatorList);
	}

	@DataProvider(name = "filterRoleAssociations")
	public Object[][] filterRoleAssociations() {
		return new Object[][] {
				withNoAssociationsReturnsFalse(),
				withOneAssociationAndNoFiltersReturnsTrue(),
				withRoleFilterAndOneMatchReturnsTrue(),
				withRoleFilterAndNoMatchReturnsFalse(),
				withAreaFilterAndOneMatchReturnsTrue(),
				withAreaFilterAndNoMatchReturnsFalse(),
				withRoleAndAreaFilterAndOneMatchReturnsTrue(),
				withRoleAndAreaFilterAndNoMatchReturnsFalse(),
				withPollingPlaceAreaFilterAndPollingDistrictMatchReturnsTrue(),
				withPollingDistrictAreaFilterAndPollingPlaceMatchReturnsTrue()
		};
	}

	private Object[] withNoAssociationsReturnsFalse() {
		return new Object[] { new ArrayList<RoleAssociation>(), "0@0", false };
	}

	private Object[] withOneAssociationAndNoFiltersReturnsTrue() {
		return new Object[] { mockList(1, RoleAssociation.class), "0@0", true };
	}

	private Object[] withRoleFilterAndOneMatchReturnsTrue() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				ROLE_VALGADMIN_KOMMUNE.getRoleId() + "@0",
				true };
	}

	private Object[] withRoleFilterAndNoMatchReturnsFalse() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				ROLE_FORHANDSTEMME_MOTTAKER.getRoleId() + "@0",
				false };
	}

	private Object[] withAreaFilterAndOneMatchReturnsTrue() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				"0@" + AREA_AMBULERENDE_PLACE.getAreaPath().path(),
				true };
	}

	private Object[] withAreaFilterAndNoMatchReturnsFalse() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				"0@" + AREA_LUNNER.getAreaPath().path(),
				false };
	}

	private Object[] withRoleAndAreaFilterAndOneMatchReturnsTrue() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				ROLE_VALGADMIN_KOMMUNE.getRoleId() + "@" + AREA_AMBULERENDE_PLACE.getAreaPath().path(),
				true };
	}

	private Object[] withRoleAndAreaFilterAndNoMatchReturnsFalse() {
		return new Object[] { asList(
				roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE),
				roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				ROLE_ANSVARLIG_URNETELLING.getRoleId() + "@" + AREA_LUNNER.getAreaPath().path(),
				false };
	}

	private Object[] withPollingPlaceAreaFilterAndPollingDistrictMatchReturnsTrue() {
        return new Object[]{singletonList(
                roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_LUNNER_DISTRICT)),
				"0@" + AREA_AMBULERENDE_PLACE.getAreaPath().path(),
				true };
	}

	private Object[] withPollingDistrictAreaFilterAndPollingPlaceMatchReturnsTrue() {
        return new Object[]{singletonList(
                roleAssociation(ROLE_ANSVARLIG_URNETELLING, AREA_AMBULERENDE_PLACE)),
				"0@" + AREA_LUNNER_DISTRICT.getAreaPath().path(),
				true };
	}

	@DataProvider(name = "getRoleAssociations")
	public Object[][] getRoleAssociations() {
		return new Object[][] {
				withNoFilterAndNoAssociationsReturns0(),
				withRoleFilterAndOneMatchOfTwoReturns1(),
				withAreaFilterAndOneMatchOfTwoReturns1(),
				withPollingPlaceAreaFilterAndBothPollingPlaceAndDistrictMatchReturns2(),
				withPollingDistrictAreaFilterAndBothPollingPlaceAndDistrictMatchReturns2()
		};
	}

	private Object[] withNoFilterAndNoAssociationsReturns0() {
		return new Object[] { "0", "0", new ArrayList<RoleAssociation>(), 0 };
	}

	private Object[] withRoleFilterAndOneMatchOfTwoReturns1() {
		return new Object[] { ROLE_FORHANDSTEMME_MOTTAKER.getRoleId(), "0",
				asList(
						roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_AMBULERENDE_PLACE),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE)),
				1 };
	}

	private Object[] withAreaFilterAndOneMatchOfTwoReturns1() {
		return new Object[] { "0", AREA_AMBULERENDE_PLACE.getAreaPath().path(),
				asList(
						roleAssociation(ROLE_FORHANDSTEMME_MOTTAKER, AREA_AMBULERENDE_PLACE),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER)),
				1 };
	}

	private Object[] withPollingPlaceAreaFilterAndBothPollingPlaceAndDistrictMatchReturns2() {
		return new Object[] { "0", AREA_AMBULERENDE_PLACE.getAreaPath().path(),
				asList(
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER_DISTRICT),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER)),
				2 };
	}

	private Object[] withPollingDistrictAreaFilterAndBothPollingPlaceAndDistrictMatchReturns2() {
		return new Object[] { "0", AREA_LUNNER_DISTRICT.getAreaPath().path(),
				asList(
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_AMBULERENDE_PLACE),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER_DISTRICT),
						roleAssociation(ROLE_VALGADMIN_KOMMUNE, AREA_LUNNER)),
				2 };
	}

	@DataProvider(name = "getAreaNameStyle")
	public Object[][] getAreaNameStyle() {
		return new Object[][] {
				{ PollingPlaceType.NOT_APPLICABLE, "" },
				{ PollingPlaceType.ADVANCE_VOTING, "pollingplace pollingplace-advance" },
				{ PollingPlaceType.ELECTION_DAY_VOTING, "pollingplace pollingplace-electionday" }
		};
	}
}

