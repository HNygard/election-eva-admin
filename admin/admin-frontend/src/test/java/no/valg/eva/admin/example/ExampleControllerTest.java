package no.valg.eva.admin.example;

import no.evote.exception.ErrorCode;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ExampleControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_withUserData_operatorListIsPopulated() throws Exception {
		// FEATURE: Create new instance of controller and setup mocks for all injected dependencies
		// initializeMocks also supports instance: initializeMocks(new ExampleController())
		ExampleController ctrl = initializeMocks(ExampleController.class);
		// FEATURE: Some code uses FacesUtil.resolveExpression(String) to resolve expressions in java code.
		// stubResolveExpression can be used to stub those values.
		stubResolveExpression("#{cc.attrs.addToOperatorList}", "true");
		// FEATURE: See comment in mock_findDescOperators
		mock_findDescOperators();

		ctrl.doInit();

		assertThat(ctrl.getOperatorList()).isNotNull();
		assertThat(ctrl.isAddToOperatorList()).isTrue();
		// FEATURE: Also use getUserDataMock() which is always available.
		verify(getUserDataMock(), atLeastOnce()).getOperatorMvArea();
		// FEATURE: Mock for FacesContext is always available.
		verify(getFacesContextMock()).isPostback();
		// FEATURE: Mock for RequestContext is always available.
		verify(getRequestContextMock()).isAjaxRequest();
	}

	@Test
	public void saveOperator_withOptimisticLockException_throwsEvoteException() throws Exception {
		ExampleController ctrl = initializeMocks(ExampleController.class);
		Operator operator = createMock(Operator.class);
		// FEATURE: Use optimisticLockExceptionWhen to make injected mock for throw EvoteException with optimistic lock cause.
		// This is a shorthand for optimisticLockException().when(getInjectMock(OperatorService.class)) which is also available.
		// FEATURE: You also have similiar helper methods for EvoteException, IOException and ValidateException.
		optimisticLockExceptionWhen(AdminOperatorService.class).updateOperator(
				eq(getUserDataMock()), eq(operator), any(AreaPath.class), anyList(), anyList());
		// Setup a spy to verify method calls
		ctrl = spy(ctrl);

		ctrl.saveOperator(operator);

		verify(ctrl).onError(ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, null);
		verify_loadData();
		// FEATURE: Use assertFacesMessage to assert that a faces message is added.
		// FEATURE: MessageProvider is always mocked to resolve output to the same as input.
		// You could also have performed the following verification:
		// verify(getMessageProviderMock()).get("@error.optimisticLockingException");
		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@error.optimisticLockingException");
	}

	@Test
	public void saveOperator_withOperator_verifySave() throws Exception {
		ExampleController ctrl = initializeMocks(ExampleController.class);
		Operator operator = createMock(Operator.class);
		// FEATURE: Use mockFieldValue to explicit set av value of a (typically) private field.
		mockFieldValue("addToOperatorList", true);
		// FEATURE: Use mockField to mock a (typically) private field.
		List<Operator> operatorList = mockField("operatorList", List.class);

		ctrl.saveOperator(operator);

		verify(getInjectMock(AdminOperatorService.class)).updateOperator(
				eq(getUserDataMock()), eq(operator), any(AreaPath.class), anyList(), anyList());
		verify(operatorList).add(any(Operator.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@operator.saved");
		// FEATURE: getPrivateField can be used to get private fields if needed.
		List<Operator> list = getPrivateField("operatorList", List.class);
		assertThat(list).isSameAs(operatorList);
	}

	@Test
	public void back_verifyRedirect() throws Exception {
		ExampleController ctrl = initializeMocks(ExampleController.class);

		ctrl.back();

		// FEATURE: If code uses NavigationHandler for navigate, you will always have a mock for it available.
		verify(getNavigationHandler()).performNavigation("/secure/index.xhtml");
	}

	private void verify_loadData() {
		verify(getInjectMock(AdminOperatorService.class)).operatorsInArea(getUserDataMock(), getUserDataMock().getOperatorMvArea().areaPath());
	}

	private void mock_findDescOperators() {
		// FEATURE: Using getInjectMock for getting mock for injected service
		// FEATURE: Use mockList for get a list of 5 mocks of type Operator.class.
		when(getInjectMock(AdminOperatorService.class).operatorsInArea(any(UserData.class), any(AreaPath.class))).thenReturn(mockList(5, Operator.class));
	}
}

