package no.valg.eva.admin.common.rbac;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class RoleTest {

	private Role role;

	@BeforeMethod
	public void setUp() {
		role = new Role();
	}

	@Test
	public void setPollingDistrictTrueMakesRoleAssignableOnPollingDistrictLevel() {
		role.setPollingDistrictAreaLevel(true);
		assertThat(role.canBeAssignedToAreaLevel(POLLING_DISTRICT)).isTrue();
	}

	@Test
	public void setPollingDistrictFalseMakesRoleNotAssignableOnPollingDistrictAndPollingPlaceLevel() {
		role.setPollingDistrictAreaLevel(false);
		assertThat(role.canBeAssignedToAreaLevel(POLLING_DISTRICT)).isFalse();
	}

	@Test
	public void setPollingPlaceTrueMakesRoleAssignableOnPollingPlaceLevel() {
		role.setPollingPlaceAreaLevel(true);
		assertThat(role.canBeAssignedToAreaLevel(POLLING_PLACE)).isTrue();
	}

	@Test
	public void setPollingPlaceFalseMakesRoleNotAssignableOnPollingPlaceLevel() {
		role.setPollingPlaceAreaLevel(false);
		assertThat(role.canBeAssignedToAreaLevel(POLLING_PLACE)).isFalse();
	}

	@Test
	public void setMunicipalityTrueMakesRoleAssignableOnMunicipalityLevel() {
		role.setMunicipalityAreaLevel(true);
		assertThat(role.canBeAssignedToAreaLevel(MUNICIPALITY)).isTrue();
	}

	@Test
	public void setMunicipalityFalseMakesRoleNotAssignableOnMunicipalityLevel() {
		role.setMunicipalityAreaLevel(false);
		assertThat(role.canBeAssignedToAreaLevel(MUNICIPALITY)).isFalse();
	}

	@Test
	public void setCountyTrueMakesRoleAssignableOnCountyLevel() {
		role.setCountyAreaLevel(true);
		assertThat(role.canBeAssignedToAreaLevel(COUNTY)).isTrue();
	}

	@Test
	public void setCountyFalseMakesRoleNotAssignableOnCountyLevel() {
		role.setCountyAreaLevel(false);
		assertThat(role.canBeAssignedToAreaLevel(COUNTY)).isFalse();
	}
}
