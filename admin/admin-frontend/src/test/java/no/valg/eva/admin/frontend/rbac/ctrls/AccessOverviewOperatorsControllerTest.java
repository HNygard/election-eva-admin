package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.service.rbac.OperatorRoleService;
import no.evote.service.rbac.OperatorService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import org.primefaces.event.SelectEvent;
import org.testng.annotations.Test;

import javax.faces.model.ListDataModel;
import java.util.List;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AccessOverviewOperatorsControllerTest extends BaseFrontendTest {

	private static final int NO_OF_OPERATORS = 3;

	@Test
	public void init_withAccessAndOperators_returnsOperators() throws Exception {
		AccessOverviewOperatorsController ctrl = initCtrl();

		assertThat(ctrl.getOperators()).isNotNull();
		assertThat(ctrl.getOperators().getRowCount()).isEqualTo(NO_OF_OPERATORS);
	}

	@Test
	public void onOperatorSelect_withOperator_returnsSortedOperatorRoles() throws Exception {
		AccessOverviewOperatorsController ctrl = initCtrl();
		SelectEvent event = createMock(SelectEvent.class);
		when(event.getObject()).thenReturn(ctrl.getOperators().iterator().next());

		ctrl.onOperatorSelect(event);

		ListDataModel<OperatorRole> model = ctrl.getOperatorRoles();
		assertThat(model).isNotNull();
		assertThat(model).hasSize(NO_OF_OPERATORS);
		assertName(model, 0, "A Role");
		assertName(model, 1, "B Role");
		assertName(model, 2, "C Role");

	}

	private void assertName(ListDataModel<OperatorRole> model, int index, String roleName) {
		model.setRowIndex(index);
		assertThat(model.getRowData().getRole().getName()).isEqualTo(roleName);
	}

	private AccessOverviewOperatorsController initCtrl() throws Exception {
		AccessOverviewOperatorsController ctrl = initializeMocks(AccessOverviewOperatorsController.class);
		getServletContainer().setRequestParameter("access", getAccessToUse());
		stub_findOperatorsWithAccess();
		stub_findOperatorRolesGivingOperatorAccess();
		ctrl.init();
		return ctrl;
	}

	private void stub_findOperatorsWithAccess() {
		List<Operator> operators = asList(
				operator(),
				operator(),
				operator());
		when(getInjectMock(OperatorService.class)
				.findOperatorsWithAccess(eq(getUserDataMock()), any(MvArea.class), eq(new Access(getAccessToUse()))))
						.thenReturn(operators);
	}

	private void stub_findOperatorRolesGivingOperatorAccess() {
		List<OperatorRole> roles = asList(
				operatorRole("C Role"),
				operatorRole("A Role"),
				operatorRole("B Role"));

		when(getInjectMock(OperatorRoleService.class)
				.findOperatorRolesGivingOperatorAccess(eq(getUserDataMock()), any(MvArea.class), any(Operator.class), eq(new Access(getAccessToUse()))))
						.thenReturn(roles);
	}

	private Operator operator() {
		return createMock(Operator.class);
	}

	private OperatorRole operatorRole(String roleName) {
		OperatorRole result = createMock(OperatorRole.class);
		when(result.getRole().getName()).thenReturn(roleName);
		return result;
	}

	private String getAccessToUse() {
		return Konfigurasjon_Grunnlagsdata.paths()[0];
	}

}
