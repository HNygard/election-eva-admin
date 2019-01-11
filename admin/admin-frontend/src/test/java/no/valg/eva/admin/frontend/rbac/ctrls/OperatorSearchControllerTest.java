package no.valg.eva.admin.frontend.rbac.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OperatorSearchControllerTest extends BaseFrontendTest {

	@Test
	public void init_verifyInitialState() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);

		ctrl.init();

		assertThat(ctrl.getOperatorSearchCriteria()).isEqualTo("fnr");
		assertThat(ctrl.getSearchOperatorId()).isNull();
		assertThat(ctrl.getSearchOperatorName()).isNull();
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.SEARCH);
	}

	@Test
	public void searchOperator_withFnrAndNotFoundOperator_verifyEditModeWithNewState() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);
		ctrl.setOperatorSearchCriteria("fnr");
		ctrl.setSearchOperatorId("12345678900");
		when(getInjectMock(AdminOperatorService.class).operatorOrVoterById(eq(getUserDataMock()), eq(new PersonId("12345678900")))).thenReturn(null);

		ctrl.searchOperator();

		ArgumentCaptor<Operator> operatorArgumentCaptor = ArgumentCaptor.forClass(Operator.class);
		verify(getInjectMock(OperatorEditController.class)).init(operatorArgumentCaptor.capture(), eq(RbacView.NEW));
		Operator operator = operatorArgumentCaptor.getValue();
		assertThat(operator.getPersonId().getId()).isEqualTo("12345678900");
		assertThat(operator.getRoleAssociations()).isEmpty();
		assertThat(operator.getDateOfBirth()).isNull();
		assertThat(operator.getEmail()).isEmpty();
		assertThat(operator.getFirstName()).isEmpty();
		assertThat(operator.getLastName()).isEmpty();
		assertThat(operator.getTelephoneNumber()).isEmpty();
		assertThat(operator.getAddress()).isNotNull();
		assertThat(operator.getAddress().getAddressLine1()).isEmpty();
		assertThat(operator.getAddress().getAddressLine2()).isEmpty();
		assertThat(operator.getAddress().getAddressLine3()).isEmpty();
		assertThat(operator.getAddress().getMunicipality()).isEmpty();
		assertThat(operator.getAddress().getPostalCode()).isEmpty();
		assertThat(operator.getAddress().getPostTown()).isEmpty();
		assertThat(operator.getAddress().getShortDisplay()).isEmpty();
	}

	@Test
	public void searchOperator_withFnrAndExistingOperator_verifyExistingMode() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);
		ctrl.setOperatorSearchCriteria("fnr");
		ctrl.setSearchOperatorId("12345678900");
		Operator operatorMock = createMock(Operator.class);
		when(getInjectMock(AdminOperatorService.class).operatorOrVoterById(eq(getUserDataMock()), eq(new PersonId("12345678900")))).thenReturn(operatorMock);
		when(getInjectMock(OperatorListController.class).exists(operatorMock)).thenReturn(true);

		ctrl.searchOperator();

		verify(getInjectMock(ExistingOperatorController.class)).init(operatorMock);
	}

	@Test
	public void searchOperator_withFnrAndNonExistingOperator_verifyNewFromExistingMode() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);
		ctrl.setOperatorSearchCriteria("fnr");
		ctrl.setSearchOperatorId("12345678900");
		Operator operatorMock = createMock(Operator.class);
		when(getInjectMock(AdminOperatorService.class).operatorOrVoterById(eq(getUserDataMock()), eq(new PersonId("12345678900")))).thenReturn(operatorMock);
		when(getInjectMock(OperatorListController.class).exists(operatorMock)).thenReturn(false);

		ctrl.searchOperator();

		verify(getInjectMock(OperatorEditController.class)).init(operatorMock, RbacView.NEW_FROM_EXISTING_VOTER);
	}

	@Test
	public void searchOperator_withName_verifyElectoralRollListMode() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);
		ctrl.setOperatorSearchCriteria("name");
		ctrl.setSearchOperatorName("Pettersen");
		Collection<Person> personList = mockList(1, Person.class);
		when(getInjectMock(AdminOperatorService.class).operatorsByName(getUserDataMock(), "Pettersen")).thenReturn(personList);

		ctrl.searchOperator();

		verify(getInjectMock(ElectoralRollListController.class)).init(personList);
	}

	@Test
	public void backToList_withParams_verifyResetStateAndAndListMode() throws Exception {
		OperatorSearchController ctrl = initializeMocks(OperatorSearchController.class);
		ctrl.setOperatorSearchCriteria("AAA");
		ctrl.setSearchOperatorId("BBB");
		ctrl.setSearchOperatorName("CCC");

		ctrl.backToList();

		assertThat(ctrl.getOperatorSearchCriteria()).isNull();
		assertThat(ctrl.getSearchOperatorId()).isNull();
		assertThat(ctrl.getSearchOperatorName()).isNull();
		verify(getInjectMock(OperatorAdminController.class)).setView(RbacView.LIST);
	}

	@Test
    public void getSearchOperatorId_with_should() {

	}

	@Test
    public void setSearchOperatorId_with_should() {

	}

	@Test
    public void getSearchOperatorName_with_should() {

	}

	@Test
    public void setSearchOperatorName_with_should() {

	}

	@Test
    public void getOperatorSearchCriteria_with_should() {

	}

	@Test
    public void setOperatorSearchCriteria_with_should() {

	}
}
