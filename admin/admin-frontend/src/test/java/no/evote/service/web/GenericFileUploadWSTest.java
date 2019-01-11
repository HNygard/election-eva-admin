package no.evote.service.web;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.evote.service.LegacyUserDataService;
import no.evote.service.web.exception.EvoteWsException;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ejb.EJBException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.net.InetAddress;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


public class GenericFileUploadWSTest extends BaseFrontendTest {
	private static final int BATCH_STATUS_COMPLETED_WITH_OFFSET = 1002;

	private GenericFileUploadWS genericFileUpload;

	@BeforeMethod
	public void setUp() throws Exception {
		this.genericFileUpload = initializeMocks(GenericFileUploadWS.class);
		when(getInjectMock(WebServiceContext.class).getMessageContext().get(MessageContext.SERVLET_REQUEST)).thenReturn(getServletContainer().getRequestMock());
		getServletContainer().setRemoteAddr("127.0.0.1");
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Unable to get user data.*")
	public void uploadFile_withFailedUserDataException_shouldThrowEvoteWSException() throws Exception {
		getUserDataThrows(new EJBException(new Exception("ERROR")));

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Unable to get user data, please check web service parameters.")
	public void uploadFile_withFailedUserDataEvoteException_shouldThrowEvoteWSException() throws Exception {
		getUserDataThrows(new EJBException(new EvoteException("ERROR")));

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Token is invalid")
	public void uploadFile_withFailedTokenValidationException_shouldThrowEvoteWSException() throws Exception {
		isFileUploadDownloadTokenValidThrows(new EJBException(new Exception("ERROR")));
		stub_getUserData(true);

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Token is invalid")
	public void uploadFile_withFailedTokenValidationEvoteException_shouldThrowEvoteWSException() throws Exception {
		isFileUploadDownloadTokenValidThrows(new EJBException(new EvoteException("ERROR")));
		stub_getUserData(true);

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Unexpected file upload error")
	public void uploadFile_withFailedSaveFileException_shouldThrowEvoteWSException() throws Exception {
		saveFileThrows(new EJBException(new Exception("ERROR")));
		stub_getUserData(true);

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Unexpected file upload error")
	public void uploadFile_withFailedSaveFileEvoteException_shouldThrowEvoteWSException() throws Exception {
		stub_getUserData(true);
		saveFileThrows(new EJBException(new EvoteException("ERROR")));

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test(expectedExceptions = EvoteWsException.class, expectedExceptionsMessageRegExp = "Unexpected file upload error")
	public void uploadFile_withFailedSaveFileEvoteNoRollbackException_shouldThrowEvoteWSException() throws Exception {
		saveFileThrows(new EJBException(new EvoteNoRollbackException("ERROR")));
		stub_getUserData(true);

		genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());
	}

	@Test
	public void uploadFile_withValidInput_returnsPk() throws Exception {
		Batch batch = createMock(Batch.class);
		when(getInjectMock(BatchService.class).saveFile(any(UserData.class), any(byte[].class), anyString(), any(Jobbkategori.class))).thenReturn(batch);
		when(batch.getNumber()).thenReturn(1);
		when(batch.getPk()).thenReturn(1L);
		stub_getUserData(true);

		long pk = genericFileUpload.uploadFile("token".getBytes(), "userId", "valgansvarlig_kommune", "areaPath", "electionPath", "file".getBytes(),
				COUNT_UPLOAD.toAccessPath());

		assertThat(pk).isEqualTo(1L);
	}

	@Test
	public void checkStatus_withValidInput_returnsBatchStatusId() throws Exception {
		when(getInjectMock(BatchService.class).checkStatus(anyLong())).thenReturn(2);

		int status = genericFileUpload.checkStatus(1L);

		assertThat(status).isEqualTo(BATCH_STATUS_COMPLETED_WITH_OFFSET);
	}

	@Test(
			expectedExceptions = EvoteWsException.class,
			expectedExceptionsMessageRegExp = "Only users with role valgansvarlig_kommune or valgansvarlig_fylke can upload ballot counts")
	public void uploadWithWrongRoleId_fails() throws Exception {
		genericFileUpload.uploadFile("token".getBytes(), "userId", "wrong_role_id", "areaPath", "electionPath", "file".getBytes(), COUNT_UPLOAD.toAccessPath());
	}

	private void getUserDataThrows(RuntimeException e) {
		doThrow(e).when(getInjectMock(LegacyUserDataService.class)).getUserData(anyString(), anyString(), anyString(), anyString(), any(InetAddress.class));
	}

	private void stub_getUserData(boolean hasAccess) {
		UserData userData = createMock(UserData.class);
		when(userData.hasAccess(Accesses.Opptelling_Importer)).thenReturn(hasAccess);
		when(getInjectMock(LegacyUserDataService.class).getUserData(anyString(), anyString(), anyString(), anyString(), any(InetAddress.class)))
				.thenReturn(userData);
	}

	private void isFileUploadDownloadTokenValidThrows(RuntimeException e) {
		doThrow(e).when(getInjectMock(LegacyUserDataService.class)).isFileUploadDownloadTokenValid(any(UserData.class), any(byte[].class), anyString());
	}

	private void saveFileThrows(RuntimeException e) {
		doThrow(e).when(getInjectMock(BatchService.class)).saveFile(any(UserData.class), any(byte[].class), anyString(), any(Jobbkategori.class));
	}
}
