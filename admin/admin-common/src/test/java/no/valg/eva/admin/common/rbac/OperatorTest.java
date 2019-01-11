package no.valg.eva.admin.common.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;



public class OperatorTest extends MockUtilsTestCase {

	public static final String MATCHING_AREA_PATH = "201301";
	public static final String NOT_MATCHING_AREA_PATH = "201301.213421";
	public static final String POLLING_DISTRICT_PATH = "201301.47.03.0301.030101.0001";
	public static final String POLLING_PLACE_PATH = "201301.47.03.0301.030101.0001.0001";
	private static final String ROLE_ID_FOUND = "roleId";
	private static final String ROLE_ID_NOT_FOUND = "roleIdNotFound";
	private Operator operator;
	private Operator operatorWithRoleAtPollingDistrict;

	@BeforeMethod
	public void setUp() {
		operator = new Operator("", "", "", "", "", true);
		RoleItem role = new RoleItem(ROLE_ID_FOUND, ROLE_ID_FOUND, false, null, new ArrayList<AreaLevelEnum>());
		PollingPlaceArea area = new PollingPlaceArea(new AreaPath(MATCHING_AREA_PATH), "");
		RoleAssociation roleAssociation = new RoleAssociation(role, area);
		operator.addRoleAssociation(roleAssociation);

		operatorWithRoleAtPollingDistrict = new Operator("", "", "", "", "", true);
		RoleItem roleAtPollingDistrictLevel = new RoleItem(ROLE_ID_FOUND, ROLE_ID_FOUND, false, null, new ArrayList<AreaLevelEnum>());
		PollingPlaceArea pollingDistrict = new PollingPlaceArea(new AreaPath(POLLING_DISTRICT_PATH), "");
		RoleAssociation roleAssociationAtPollingDistrict = new RoleAssociation(roleAtPollingDistrictLevel, pollingDistrict);
		operatorWithRoleAtPollingDistrict.addRoleAssociation(roleAssociationAtPollingDistrict);
	}

	@Test
	public void nameIsLastNameCommaSpaceFirstName() {
		assertThat(new Operator("", "Ola", "Nordmann", "", "", true).name()).isEqualTo("Nordmann, Ola");
	}

	@Test
	public void hasRoleIsTrueWhenListOfRolesContainsRoleWithGivenId() {
		assertThat(operator.hasRole(ROLE_ID_FOUND)).isTrue();
	}

	@Test
	public void hasRoleIsFalseWhenListOfRolesDoesNotContainRoleWithGivenId() {
		assertThat(operator.hasRole(ROLE_ID_NOT_FOUND)).isFalse();
	}

	@Test
	public void hasRoleOnAreaIsTrueWhenListOfRolesContainsRoleOnGivenAreaPath() {
		assertThat(operator.hasRoleOnArea(MATCHING_AREA_PATH)).isTrue();
	}

	@Test
	public void hasRoleOnAreaIsFalseWhenListOfRolesDoesNotContainRoleOnGivenAreaPath() {
		assertThat(operator.hasRoleOnArea(NOT_MATCHING_AREA_PATH)).isFalse();
	}

	@Test
	public void whenAreaFilterIsPollingPlaceAndRoleAssocPathIsAtPollingDistrictLevelMathcingAreAndRoleIsReturnedWhenMatchingDownToLevel() {
		assertThat(operatorWithRoleAtPollingDistrict.getRoleAssociations(ROLE_ID_FOUND, AreaPath.from(POLLING_PLACE_PATH))).hasSize(1);
	}

	@Test
	public void getRoleAssociations_withPollingPlace_checkSize() throws Exception {
		operatorWithRoleAtPollingDistrict.clearRoleAssociations();
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001.0001"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001.0002"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0002.0001"));

		assertThat(operatorWithRoleAtPollingDistrict.getRoleAssociations(ROLE_ID_FOUND, AreaPath.from("201301.47.03.0301.030101.0001.0001"))).hasSize(2);
	}

	@Test
	public void getRoleAssociations_withPollingDistrict_checkSize() throws Exception {
		operatorWithRoleAtPollingDistrict.clearRoleAssociations();
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001.0001"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0001.0002"));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, "201301.47.03.0301.030101.0002.0001"));

		assertThat(operatorWithRoleAtPollingDistrict.getRoleAssociations(ROLE_ID_FOUND, AreaPath.from("201301.47.03.0301.030101.0001"))).hasSize(3);
	}

	@Test
	public void getRoleAssociations_withPollingDistrict_checkSortOrder() throws Exception {
		operatorWithRoleAtPollingDistrict.clearRoleAssociations();
		String path1 = "201301.47.03.0301.030101.0001.0001";
		String path2 = "201301.47.03.0301.030101.0001.0003";
		String path3 = "201301.47.03.0301.030101.0001.0002";
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, path1));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, path2));
		operatorWithRoleAtPollingDistrict.addRoleAssociation(getRoleAssociation(ROLE_ID_FOUND, path3));

		assertThat(Lists.transform(operatorWithRoleAtPollingDistrict.getRoleAssociations(), new Function<RoleAssociation, String>() {
			@Override
			public String apply(RoleAssociation input) {
				return input.getArea().getAreaPath().path();
			}
		})).containsExactly(path1, path2, path3);
	}

	private RoleAssociation getRoleAssociation(String roleId, String path) {
		RoleAssociation roleAssociation = createMock(RoleAssociation.class);
		when(roleAssociation.getRole().getRoleId()).thenReturn(roleId);
		when(roleAssociation.getArea().getAreaPath()).thenReturn(AreaPath.from(path));
		return roleAssociation;
	}
}

