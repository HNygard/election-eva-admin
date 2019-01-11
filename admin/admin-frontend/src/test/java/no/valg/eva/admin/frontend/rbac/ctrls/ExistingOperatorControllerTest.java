package no.valg.eva.admin.frontend.rbac.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Operator;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class ExistingOperatorControllerTest extends BaseFrontendTest {

	@Test
	public void init_withOperator_returnsOperator() throws Exception {
		ExistingOperatorController ctrl = initializeMocks(ExistingOperatorController.class);
		Operator operatorMock = createMock(Operator.class);

		ctrl.init(operatorMock);

		assertThat(ctrl.getOperator()).isSameAs(operatorMock);
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.EXISTING);
	}

	@Test
	public void goToEditView_withOperator_verifyEditMode() throws Exception {
		ExistingOperatorController ctrl = initializeMocks(ExistingOperatorController.class);
		Operator operatorMock = mockField("operator", Operator.class);

		ctrl.goToEditView();

		verify(getInjectMock(OperatorEditController.class)).init(eq(operatorMock), eq(RbacView.EDIT));

	}

	@Test
	public void backToSearch_withOperator_returnsNullOperator() throws Exception {
		ExistingOperatorController ctrl = initializeMocks(ExistingOperatorController.class);
		mockField("operator", Operator.class);

		ctrl.backToSearch();

		assertThat(ctrl.getOperator()).isNull();
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.SEARCH);
	}
}
