package no.valg.eva.admin.frontend.rbac.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;

import org.testng.annotations.Test;


public class AreaOptionsTest extends BaseRbacTest {

	public static final String SOME_ROLE = "some_role";
	public static final String ANOTHER_ROLE = "another_role";
	public static final String AVAILABLE_AREAS_FOR_ROLE = "availableAreasForRole";
	public static final String ROLE = "role";

	@Test
	public void init_withRoleItems_verifyState() throws Exception {
		AreaOptions options = initializeMocks(AreaOptions.class);
		AreaPath areaPath = AREA_LUNNER.getAreaPath();
		Map<RoleItem, List<PollingPlaceArea>> map = new HashMap<>();
		addAreasForRole(map, SOME_ROLE, AREA_AMBULERENDE_PLACE.getAreaPath().path(), AREA_LUNNER.getAreaPath().path());
		addAreasForRole(map, ANOTHER_ROLE, AREA_OPPLAND.getAreaPath().path());
		when(getInjectMock(AdminOperatorService.class).areasForRole(getUserDataMock(), areaPath)).thenReturn(map);

		options.init(areaPath);

		Map<String, PollingPlaceArea> areaMap = options.getAreaMap();
		assertThat(areaMap.containsKey(AREA_OPPLAND.getAreaPath().path())).isTrue();
		assertThat(areaMap.containsKey(AREA_LUNNER.getAreaPath().path())).isTrue();
		assertThat(areaMap.containsKey(AREA_AMBULERENDE_PLACE.getAreaPath().path())).isTrue();
		Map<String, List<PollingPlaceArea>> availableAreasForRole = options.getAvailableAreasForRole();
		assertThat(availableAreasForRole.containsKey(ANOTHER_ROLE)).isTrue();
		assertThat(availableAreasForRole.get(ANOTHER_ROLE)).hasSize(1);
		assertThat(availableAreasForRole.containsKey(SOME_ROLE)).isTrue();
		assertThat(availableAreasForRole.get(SOME_ROLE)).hasSize(2);
		assertThat(availableAreasForRole.get(SOME_ROLE).get(0).getAreaPath()).isEqualTo(AREA_LUNNER.getAreaPath());
		assertThat(availableAreasForRole.get(SOME_ROLE).get(1).getAreaPath()).isEqualTo(AREA_AMBULERENDE_PLACE.getAreaPath());
	}

	@Test
	public void hasAreasForRole_withNoAreas_returnsFalse() throws Exception {
		AreaOptions options = initializeMocks(AreaOptions.class);
		Map<String, List<PollingPlaceArea>> areaMap = new HashMap<>();
		mockFieldValue(AVAILABLE_AREAS_FOR_ROLE, areaMap);

		assertThat(options.hasAreasForRole(ROLE)).isFalse();
	}

	@Test
	public void hasAreasForRole_withNoAreasForRole_returnsFalse() throws Exception {
		AreaOptions options = initializeMocks(AreaOptions.class);
		Map<String, List<PollingPlaceArea>> areaMap = new HashMap<>();
		areaMap.put("someotherrole", new ArrayList<PollingPlaceArea>());
		mockFieldValue(AVAILABLE_AREAS_FOR_ROLE, areaMap);

		assertThat(options.hasAreasForRole(ROLE)).isFalse();
	}

	@Test
	public void hasAreasForRole_withEmptyAreasForRole_returnsFalse() throws Exception {
		AreaOptions options = initializeMocks(AreaOptions.class);
		Map<String, List<PollingPlaceArea>> areaMap = new HashMap<>();
		areaMap.put(ROLE, new ArrayList<PollingPlaceArea>());
		mockFieldValue(AVAILABLE_AREAS_FOR_ROLE, areaMap);

		assertThat(options.hasAreasForRole(ROLE)).isFalse();
	}

	@Test
	public void hasAreasForRole_withAreasForRole_returnsFalse() throws Exception {
		AreaOptions options = initializeMocks(AreaOptions.class);
		Map<String, List<PollingPlaceArea>> areaMap = new HashMap<>();
		List<PollingPlaceArea> areaList = Arrays.asList(area("Oppland", AREA_LUNNER.getAreaPath().path()));
		areaMap.put(ROLE, areaList);
		mockFieldValue(AVAILABLE_AREAS_FOR_ROLE, areaMap);

		assertThat(options.hasAreasForRole(ROLE)).isTrue();
	}

	private void addAreasForRole(Map<RoleItem, List<PollingPlaceArea>> map, String roleId, String... areaPaths) {
		RoleItem roleItem = roleItem(roleId);
		List<PollingPlaceArea> areaList = new ArrayList<>();
		int count = 100;
		for (String areaPath : areaPaths) {
			areaList.add(area("area" + (--count), areaPath));
		}
		map.put(roleItem, areaList);

	}
}

