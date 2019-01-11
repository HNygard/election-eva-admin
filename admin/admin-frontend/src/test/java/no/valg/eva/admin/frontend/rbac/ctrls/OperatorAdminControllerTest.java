package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.rbac.OperatorImportHelper;
import no.valg.eva.admin.frontend.rbac.SpreadSheetValidationException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Collections;

import static no.valg.eva.admin.frontend.rbac.ctrls.OperatorAdminController.EARLY_VOTE_RECEIVER_TEMPLATE_PATH;
import static no.valg.eva.admin.frontend.rbac.ctrls.OperatorAdminController.VOTE_RECEIVER_AND_PP_RESPONSIBLE_TEMPLATE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OperatorAdminControllerTest extends BaseFrontendTest {

    private static final String USER_AGENT_CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
			+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.104 Safari/537.36";

	private OperatorAdminController controller;
	private UserData userData;
	
	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(OperatorAdminController.class);
		userData = getUserDataMock();
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_MUNICIPALITY);
	}
	
	@Test
    public void init_withUserAgent_verifyState() {
		getServletContainer().setUserAgent(USER_AGENT_CHROME);

		controller.init();
		
		verify(getInjectMock(OperatorListController.class)).initOperatorListsInArea(userData.getOperatorAreaPath());
		assertThat(controller.isMsieBelow10()).isFalse();
		assertThat(controller.getView()).isSameAs(RbacView.LIST);
	}

	@Test
	public void uploadAdvanceVoteReceivers_withEvoteException_returnsErrorMessage() throws Exception {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);
		evoteExceptionWhen(OperatorImportHelper.class).importAdvanceVoteReceivers(
				any(OperatorImportHelper.InputStreamWrapper.class));
		
		controller.uploadAdvanceVoteReceivers(fileMock);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.error.unexpected, cb0e38f0]");
	}

	@Test
	public void uploadAdvanceVoteReceivers_withSpreadSheetValidationException_returnsErrorMessage() throws Exception {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);
        SpreadSheetValidationException e = new SpreadSheetValidationException(Collections.singletonList("SPREADSHEET_ERROR"));
		doThrow(e).when(getInjectMock(OperatorImportHelper.class)).importAdvanceVoteReceivers(
				any(OperatorImportHelper.InputStreamWrapper.class));
		
		controller.uploadAdvanceVoteReceivers(fileMock);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "SPREADSHEET_ERROR");
	}

	@Test
    public void uploadAdvanceVoteReceivers_withFile_verifyInitOperatorLists() {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);
		
		controller.uploadAdvanceVoteReceivers(fileMock);

		verify(getInjectMock(OperatorListController.class)).initOperatorListsInArea(null); // Controller not inited, hence no AreaPath set
	}

	@Test
	public void uploadVoteReceiverAndPollingPlaceResponsibles_withEvoteException_returnsErrorMessage() throws Exception {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);
		evoteExceptionWhen(OperatorImportHelper.class).importVoteReceiverAndPollingPlaceResponsibles(
				any(OperatorImportHelper.InputStreamWrapper.class));

		controller.uploadVoteReceiverAndPollingPlaceResponsibles(fileMock);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.error.unexpected, cb0e38f0]");
	}

	@Test
	public void uploadVoteReceiverAndPollingPlaceResponsibles_withSpreadSheetValidationException_returnsErrorMessage() throws Exception {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);
        SpreadSheetValidationException e = new SpreadSheetValidationException(Collections.singletonList("SPREADSHEET_ERROR"));
		doThrow(e).when(getInjectMock(OperatorImportHelper.class)).importVoteReceiverAndPollingPlaceResponsibles(
				any(OperatorImportHelper.InputStreamWrapper.class));

		controller.uploadVoteReceiverAndPollingPlaceResponsibles(fileMock);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "SPREADSHEET_ERROR");
	}

	@Test
    public void uploadVoteReceiverAndPollingPlaceResponsibles_withFile_verifyInitOperatorLists() {
		FileUploadEvent fileMock = createMock(FileUploadEvent.class);

		controller.uploadVoteReceiverAndPollingPlaceResponsibles(fileMock);

		verify(getInjectMock(OperatorListController.class)).initOperatorListsInArea(null); // Controller not inited, hence no AreaPath set
	}

	@Test
    public void getPageTitleMeta_verifyExecutions() {
		controller.getPageTitleMeta();

		verify(getUserDataMock()).getOperatorMvArea();
		verify(getInjectMock(PageTitleMetaBuilder.class)).area(any(MvArea.class));
	}

	@Test
    public void getAdvanceVoteReceiversTemplate() {
		String path = EARLY_VOTE_RECEIVER_TEMPLATE_PATH;
		when(getFacesContextMock().getExternalContext().getResourceAsStream(path)).thenReturn(getClass().getResourceAsStream(path));

		StreamedContent content = controller.getAdvanceVoteReceiversTemplate();

		assertThat(content).isNotNull();
		assertThat(content.getName()).isEqualTo("@rbac.uploadAdvanceVoteReceivers.templateFileName.xlsx");
		assertThat(content.getContentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@Test
    public void getVoteReceiverAndPollingPlaceResponsibleTemplate() {
		String path = VOTE_RECEIVER_AND_PP_RESPONSIBLE_TEMPLATE_PATH;
		when(getFacesContextMock().getExternalContext().getResourceAsStream(path)).thenReturn(getClass().getResourceAsStream(path));

		StreamedContent content = controller.getVoteReceiverAndPollingPlaceResponsibleTemplate();

		assertThat(content).isNotNull();
		assertThat(content.getName()).isEqualTo("@rbac.uploadVoteReceiversAndPollingPlaceResponsible.templateName.xlsx");
		assertThat(content.getContentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@Test
	public void init_givenElectionEventAdmin_redirectToPicker() throws Exception {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_ROOT);
		
		controller.init();

		verify(getFacesContextMock().getExternalContext()).redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|1,2,3][side|uri|null]");
	}
}
