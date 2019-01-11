package no.valg.eva.admin.frontend.area.ctrls;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.TECHNICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.evote.constants.AreaLevelEnum;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.Test;

public class IsAreaEditableResolverTest extends BaseFrontendTest {

	@Test
	public void isEditable_withNoArea_returnsFalse() throws Exception {
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(createMock(UserDataController.class));

		assertThat(resolver.isEditable(COUNTY, null)).isFalse();
	}

	@Test
	public void isEditable_withRegularPollingDistrictArea_returnsTrue() throws Exception {
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(createMock(UserDataController.class));
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_POLLING_DISTRICT).getValue();
		when(mvArea.getPollingDistrict()).thenReturn(fromType(REGULAR));

		assertThat(resolver.isEditable(POLLING_DISTRICT, mvArea)).isTrue();
	}

	@Test
	public void isEditable_withTechnicalPollingDistrictArea_returnsFalse() throws Exception {
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(createMock(UserDataController.class));
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_POLLING_DISTRICT).getValue();
		when(mvArea.getPollingDistrict()).thenReturn(fromType(TECHNICAL));

		assertThat(resolver.isEditable(POLLING_DISTRICT, mvArea)).isFalse();
	}

	@Test
	public void isEditable_withOverrideAccess_returnsTrue() throws Exception {
		UserDataController userDataController = createMock(UserDataController.class);
		when(userDataController.isOverrideAccess()).thenReturn(true);
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(userDataController);
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();

		assertThat(resolver.isEditable(AreaLevelEnum.MUNICIPALITY, mvArea)).isTrue();
	}

	@Test
	public void isEditable_withCentralStatus_returnsTrue() throws Exception {
		UserDataController userDataController = createMock(UserDataController.class);
		when(userDataController.isOverrideAccess()).thenReturn(false);
		when(userDataController.isCentralConfigurationStatus()).thenReturn(true);
		IsAreaEditableResolver resolver = new IsAreaEditableResolver(userDataController);
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();

		assertThat(resolver.isEditable(AreaLevelEnum.MUNICIPALITY, mvArea)).isTrue();
	}

	private PollingDistrict fromType(PollingDistrictType type) {
		PollingDistrict result = new PollingDistrict();
		if (type == MUNICIPALITY) {
			result.setMunicipality(true);
		} else if (type == PARENT) {
			result.setParentPollingDistrict(true);
		} else if (type == TECHNICAL) {
			result.setTechnicalPollingDistrict(true);
		} else if (type == CHILD) {
			result.setPollingDistrict(createMock(PollingDistrict.class));
		}
		return result;
	}

}
