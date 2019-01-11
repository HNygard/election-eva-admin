package no.valg.eva.admin.frontend.rbac.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Operator;

import org.testng.annotations.Test;

public class OperatorCreatedControllerTest extends BaseFrontendTest {

	@Test
	public void init_withOperator_returnsOperator() throws Exception {
		OperatorCreatedController ctrl = initializeMocks(OperatorCreatedController.class);
		Operator operatorMocK = createMock(Operator.class);

		ctrl.init(operatorMocK);

		assertThat(ctrl.getOperator()).isSameAs(operatorMocK);
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.CREATED);
	}

	@Test
	public void openEditMode_withOperator_verifyResetStateAndEditMode() throws Exception {
		OperatorCreatedController ctrl = initializeMocks(OperatorCreatedController.class);
		Operator operatorMock = mockField("operator", Operator.class);

		ctrl.openEditMode();

		assertThat(ctrl.getOperator()).isNull();
		verify(getInjectMock(OperatorEditController.class)).init(operatorMock, RbacView.EDIT);
	}

	@Test
	public void createAnother_withOperator_verifyResetStateAndSearchMode() throws Exception {
		OperatorCreatedController ctrl = initializeMocks(OperatorCreatedController.class);
		mockField("operator", Operator.class);

		ctrl.createAnother();

		assertThat(ctrl.getOperator()).isNull();
		verify(getInjectMock(OperatorSearchController.class)).init();
	}

	@Test
	public void goToList_withOperator_verifyResetStateAndListMode() throws Exception {
		OperatorCreatedController ctrl = initializeMocks(OperatorCreatedController.class);
		mockField("operator", Operator.class);

		ctrl.goToList();

		assertThat(ctrl.getOperator()).isNull();
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.LIST);
	}
}
