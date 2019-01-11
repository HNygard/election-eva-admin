package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.rbac.RoleItem;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RootLevelEditTest extends BaseRbacTest {

	private static final String ROLE1 = "role1";

	@Test
	public void newInstance_withCtrl_findByPathAndLevelShouldBeCalled() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);

		new RootLevelEdit(ctrl);

		verify(ctrl.getMvAreaService()).findByPathAndLevel(any(), anyInt());
	}

	@Test(dataProvider = "isReady")
	public void isReady_withDataProvider_verifyExpected(AreaLevelEnum areaLevelEnum, String county, String municipality,
			boolean expected) throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		ctrl.setSelectedRoleToAdd("role");
		setupLevels("role", areaLevelEnum);

		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(county);
		edit.setMunicipality(municipality);

		assertThat(edit.isReady()).isEqualTo(expected);
	}

	@Test(dataProvider = "isRenderCountyList")
	public void isRenderCountyList_withDataProvider_verifyExpected(String roleId, String selectedRole, String county, boolean expected) throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		ctrl.setSelectedRoleToAdd(selectedRole);
		setupLevels(selectedRole, AreaLevelEnum.MUNICIPALITY);

		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(county);

		assertThat(edit.isRenderCountyList(roleId)).isEqualTo(expected);
	}

	@Test(dataProvider = "isRenderMunicipalityList")
	public void isRenderMunicipalityList_withDataProvider_verifyExpected(String roleId, String selectedRole, String county, String municipality,
			boolean expected)
			throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		ctrl.setSelectedRoleToAdd(selectedRole);
		setupLevels(selectedRole, AreaLevelEnum.MUNICIPALITY);

		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(county);
		edit.setMunicipality(municipality);

		assertThat(edit.isRenderMunicipalityList(roleId)).isEqualTo(expected);
	}

	@Test
	public void selectCounty_withCountyAndCountyLevel_shouldInitAreaOptions() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		ctrl.setSelectedRoleToAdd("role");
		setupLevels("role", AreaLevelEnum.COUNTY);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());

		edit.selectCounty();

		verify(getInjectMock(AreaOptions.class)).init(AREA_OPPLAND.getAreaPath());
	}

	@Test
	public void selectCounty_withCountyAndMunicipalityLevel_shouldGetMunicipalityAreas() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());

		edit.selectCounty();

		verify(getInjectMock(MvAreaService.class)).findByPathAndLevel(AREA_OPPLAND.getAreaPath().path(), AreaLevelEnum.MUNICIPALITY.getLevel());
	}

	@Test
	public void selectMunicipality_withWithMunicipality0_verifyOpplandInitialization() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());
		edit.setMunicipality("0");

		edit.selectMunicipality();

		verify(getInjectMock(AreaOptions.class)).init(AREA_OPPLAND.getAreaPath());
	}

	@Test
	public void selectMunicipality_withWithKunner_verifyLunnerInitialization() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());
		edit.setMunicipality(AREA_LUNNER.getAreaPath().path());

		edit.selectMunicipality();

		verify(getInjectMock(AreaOptions.class)).init(AREA_LUNNER.getAreaPath());
	}

	@Test
	public void resetCounty_verifyResetOfBothCountyAndMunicipality() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());
		edit.setMunicipality(AREA_LUNNER.getAreaPath().path());

		edit.resetCounty();

		assertThat(edit.getCounty()).isNull();
		assertThat(edit.getMunicipality()).isNull();
	}

	@Test
	public void resetMunicipality_verifyResetOfMunicipalityOnly() throws Exception {
		OperatorEditController ctrl = initializeMocks(OperatorEditController.class);
		RootLevelEdit edit = new RootLevelEdit(ctrl);
		edit.setCounty(AREA_OPPLAND.getAreaPath().path());
		edit.setMunicipality(AREA_LUNNER.getAreaPath().path());

		edit.resetMunicipality();

		assertThat(edit.getCounty()).isEqualTo(AREA_OPPLAND.getAreaPath().path());
		assertThat(edit.getMunicipality()).isNull();
	}

	@DataProvider(name = "isReady")
	public Object[][] isReady() {
		return new Object[][] {
				{ AreaLevelEnum.ROOT, null, null, true },
				{ AreaLevelEnum.MUNICIPALITY, null, null, false },
				{ AreaLevelEnum.COUNTY, AREA_OPPLAND.getAreaPath().path(), null, true },
				{ AreaLevelEnum.MUNICIPALITY, AREA_OPPLAND.getAreaPath().path(), null, false },
				{ AreaLevelEnum.MUNICIPALITY, null, AREA_LUNNER.getAreaPath().path(), false },
				{ AreaLevelEnum.MUNICIPALITY, AREA_OPPLAND.getAreaPath().path(), AREA_LUNNER.getAreaPath().path(), true }
		};
	}

	@DataProvider(name = "isRenderCountyList")
	public Object[][] isRenderCountyList() {
		return new Object[][] {
				{ ROLE1, null, null, false },
				{ ROLE1, ROLE1, null, true },
				{ ROLE1, ROLE1, AREA_OPPLAND.getAreaPath().path(), false }
		};
	}

	@DataProvider(name = "isRenderMunicipalityList")
	public Object[][] isRenderMunicipalityList() {
		return new Object[][] {
				{ ROLE1, null, null, null, false },
				{ ROLE1, ROLE1, null, null, false },
				{ ROLE1, ROLE1, AREA_OPPLAND.getAreaPath().path(), null, true },
				{ ROLE1, ROLE1, AREA_OPPLAND.getAreaPath().path(), AREA_LUNNER.getAreaPath().path(), false },
				{ ROLE1, ROLE1, null, AREA_LUNNER.getAreaPath().path(), false }
		};
	}

	private void setupLevels(String selectedRole, AreaLevelEnum areaLevelEnum) {
		Map<String, RoleItem> roleMap = new HashMap<>();
		RoleItem roleItem = createMock(RoleItem.class);
		when(roleItem.getPermittedAreaLevels()).thenReturn(Collections.singletonList(areaLevelEnum));
		roleMap.put(selectedRole, roleItem);
		when(getInjectMock(RoleOptions.class).getRoleMap()).thenReturn(roleMap);
	}
}
