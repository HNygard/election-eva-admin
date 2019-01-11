package no.valg.eva.admin.frontend.rbac.ctrls;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;

import org.testng.annotations.Test;

public class RoleOptionsTest extends BaseRbacTest {

	@Test
	public void init_withRoleItems_verifyState() throws Exception {
		RoleOptions options = initializeMocks(RoleOptions.class);

		AreaPath areaPath = AREA_LUNNER.getAreaPath();
		List<RoleItem> roleItemList = asList(
				roleItem("some_role", false),
				roleItem("my_role", true),
				roleItem("another_role", false));
		when(getInjectMock(AdminOperatorService.class).assignableRolesForArea(getUserDataMock(), areaPath)).thenReturn(roleItemList);

		options.init(AREA_LUNNER.getAreaPath());

		List<SelectItem> items = options.getRoleNameOptions();
		List<SelectItem> expected = new ArrayList<>();
		expected.add(new SelectItem("another_role", "[@role[another_role].name, 0]"));
		expected.add(new SelectItem("some_role", "[@role[some_role].name, 0]"));
		assertThat(items).hasSize(2);
		assertThat(items).isSortedAccordingTo(new RoleOptionsComparator());
		Map<String, RoleItem> roleMap = options.getRoleMap();
		assertThat(roleMap).hasSize(2);
	}
}
