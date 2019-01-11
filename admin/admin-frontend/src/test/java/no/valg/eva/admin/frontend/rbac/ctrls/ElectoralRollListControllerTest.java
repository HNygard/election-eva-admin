package no.valg.eva.admin.frontend.rbac.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectoralRollListControllerTest extends BaseFrontendTest {

	@Test
	public void init_withPersons_checkList() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		List<Person> personList = Arrays.asList(createMock(Person.class), createMock(Person.class));

		ctrl.init(personList);

		assertThat(ctrl.getPersonList()).isSameAs(personList);
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.ELECTORAL_ROLL_LIST);
	}

	@Test
	public void goToEditView_withExistingPerson_verifyEditMode() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		Person personMock = createMock(Person.class);
		Operator operatorMock = createMock(Operator.class);
		when(getInjectMock(OperatorListController.class).exists(personMock)).thenReturn(true);
		when(getInjectMock(OperatorListController.class).getOperator(personMock)).thenReturn(operatorMock);

		ctrl.goToEditView(personMock);

		verify(getInjectMock(OperatorEditController.class)).init(operatorMock, RbacView.EDIT);
	}

	@Test
	public void goToEditView_withNewFromElectoralRoll_verifyNewElectoralRoleMode() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		Person personMock = createMock(Person.class);
		Operator operatorMock = createMock(Operator.class);
		when(getInjectMock(OperatorListController.class).exists(personMock)).thenReturn(false);
		when(getInjectMock(AdminOperatorService.class).operatorOrVoterById(eq(getUserDataMock()), any(PersonId.class))).thenReturn(operatorMock);
        when(operatorMock.getRoleAssociations()).thenReturn(new ArrayList<>());
		ctrl.goToEditView(personMock);

		verify(getInjectMock(OperatorEditController.class)).init(operatorMock, RbacView.NEW_FROM_ELECTORAL_ROLL);
	}

	@Test
	public void goToEditView_withNonExistingEdit_verifyEditode() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		Person personMock = createMock(Person.class);
		Operator operatorMock = createMock(Operator.class);
		when(getInjectMock(OperatorListController.class).exists(personMock)).thenReturn(false);
		when(getInjectMock(AdminOperatorService.class).operatorOrVoterById(eq(getUserDataMock()), any(PersonId.class))).thenReturn(operatorMock);
        when(operatorMock.getRoleAssociations()).thenReturn(Collections.singletonList(createMock(RoleAssociation.class)));
		ctrl.goToEditView(personMock);

		verify(getInjectMock(OperatorEditController.class)).init(operatorMock, RbacView.EDIT);
	}

	@Test
	public void backToSearch_withPersonList_personListIsNull() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		mockFieldValue("personList", new ArrayList<Person>());

		ctrl.backToSearch();

		assertThat(ctrl.getPersonList()).isNull();
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.SEARCH);
	}

	@Test
	public void getOpenEditLabel_withExistingPerson_returnsNameAndExisting() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		Person personMock = createMock(Person.class);
		when(personMock.nameLine()).thenReturn("Per Persen");
		when(getInjectMock(OperatorListController.class).exists(personMock)).thenReturn(true);

		assertThat(ctrl.getOpenEditLabel(personMock)).isEqualTo("Per Persen (@rbac.operator.already.registered)");

	}

	@Test
	public void getOpenEditLabel_withNonExistingPerson_returnsNameOnly() throws Exception {
		ElectoralRollListController ctrl = initializeMocks(ElectoralRollListController.class);
		Person personMock = createMock(Person.class);
		when(personMock.nameLine()).thenReturn("Per Persen");
		when(getInjectMock(OperatorListController.class).exists(personMock)).thenReturn(false);

		assertThat(ctrl.getOpenEditLabel(personMock)).isEqualTo("Per Persen");
	}
}
