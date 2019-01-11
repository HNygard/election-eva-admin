package no.valg.eva.admin.frontend.user.ctrls;

import no.evote.presentation.cache.ElectionEventCache;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserDataControllerTest extends BaseFrontendTest {

	private UserDataController controller;

	@BeforeMethod
	public void init() throws Exception {
		controller = initializeMocks(UserDataController.class);
	}

	@Test
    public void getOperatorArea() {
		when(getMvAreaStub().getAreaLevel()).thenReturn(100);
		assertThat(controller.getOperatorArea().getAreaLevel()).isEqualTo(100);
	}

	@Test
    public void getLocale() {
		Locale localeStub = mock(Locale.class);
		when(localeStub.getId()).thenReturn("no");
		when(getUserDataStub().getLocale()).thenReturn(localeStub);
		assertThat(controller.getLocale()).isEqualTo("no");
	}

	@Test
    public void isRenderForceContactInfo_withOperatorRoleNull_returnsFalse() {
		when(getUserDataStub().getOperatorRole()).thenReturn(null);

		assertThat(controller.isRenderForceContactInfo()).isEqualTo(false);
	}

	@Test
    public void isRenderForceContactInfo_withConfirmed_returnsFalse() {
		when(getUserDataStub().getOperator().isContactInfoConfirmed()).thenReturn(true);

		assertThat(controller.isRenderForceContactInfo()).isEqualTo(false);
	}

	@Test
    public void isRenderForceContactInfo_withNoConfirmed_returnsTrue() {
		when(getUserDataStub().getOperator().isContactInfoConfirmed()).thenReturn(false);

		assertThat(controller.isRenderForceContactInfo()).isEqualTo(true);
	}

	@Test
    public void isRenderForceContactInfo_withSupportUser_returnsFalse() {
		when(getUserDataStub().getOperatorRole().getRole().isUserSupport()).thenReturn(true);
		when(getUserDataStub().getOperator().isContactInfoConfirmed()).thenReturn(false);

		assertThat(controller.isRenderForceContactInfo()).isEqualTo(false);
	}

	@SuppressWarnings("unchecked")
	@Test
    public void showContactInfoDialog_withNotValidContactInfo_verify() {
		controller.showContactInfoDialog();

		verify(getAdminOperatorServiceStub()).contactInfoForOperator(getUserDataStub());
		verify_open(Dialogs.EDIT_CURRENT_USER.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
    public void showContactInfoDialog_withValidContactInfo_verify() {
		ContactInfo info = new ContactInfo("phone", "email");
		when(getAdminOperatorServiceStub().contactInfoForOperator(getUserDataStub())).thenReturn(info);

		controller.showContactInfoDialog();

		verify(getUserDataStub().getOperator()).setContactInfoConfirmed(true);
		verify(getAdminOperatorServiceStub()).updateContactInfoForOperator(getUserDataStub(), info);
		verify_open(Dialogs.EDIT_CURRENT_USER.getId());
	}

	@Test
	public void saveOperator() throws Exception {
		ContactInfo info = new ContactInfo("phone", "email");
		mockFieldValue("contactInfo", info);

		controller.saveOperator();

		verify(getAdminOperatorServiceStub()).updateContactInfoForOperator(getUserDataStub(), info);
	}

	@Test
    public void isCurrentElectionEventDisabled_withClosedStatus_returnsTrue() {
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		when(electionEvent.getElectionEventStatus().getId()).thenReturn(ElectionEventStatusEnum.CLOSED.id());
		when(getInjectMock(ElectionEventCache.class).get(eq(getUserDataMock()), anyLong())).thenReturn(electionEvent);

		assertThat(controller.isCurrentElectionEventDisabled()).isEqualTo(true);
	}

	@Test
	public void getElectionNameLine_withContest_returnsElectionNameAndContestName() throws Exception {
		UserDataController ctrl = initializeMocks(UserDataController.class);
		when(getUserDataMock().getOperatorRole().getMvElection().getElectionEventName()).thenReturn("Election");
		when(getUserDataMock().getOperatorRole().getMvElection().getContest().getName()).thenReturn("Contest");

		assertThat(ctrl.getElectionNameLine()).isEqualTo("Election - Contest");
	}

	private UserData getUserDataStub() {
		return getUserDataMock();
	}

	private OperatorRole getOperatorRoleStub() {
		return getUserDataStub().getOperatorRole();
	}

	private MvArea getMvAreaStub() {
		return getOperatorRoleStub().getMvArea();
	}

	private AdminOperatorService getAdminOperatorServiceStub() {
		return getInjectMock(AdminOperatorService.class);
	}

}

