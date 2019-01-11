package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.evote.model.Batch;
import no.evote.model.BinaryData;
import no.evote.service.BatchService;
import no.evote.service.BinaryDataService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import org.primefaces.model.UploadedFile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class BatchControllerTest extends BaseFrontendTest {

	@Test
	public void init_withBatches_shouldHaveCategoriesAndBatch() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		when(getInjectMock(BatchService.class).listMyBatches(eq(getUserDataMock()), eq(COUNT_UPLOAD))).thenReturn(mockList(1, Batch.class));

		ctrl.init();

		assertThat(ctrl.getBatches()).hasSize(1);
	}

	@Test
	public void handleFileUpload_withNoUploadedFile_returnsCountNotReadError() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@listProposal.candidateList.couldNotRead");
	}

	@Test
	public void handleFileUpload_withInvalidFileSize_returnsFileSizeError() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		ctrl.setUploadedFile(uploadedFileMock);
		when(uploadedFileMock.getSize()).thenReturn(EvoteConstants.MAX_FILE_SIZE + 100);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.error.file_size");
	}

	@Test
	public void handleFileUpload_withIOException_returnsIOError() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		ioException().when(uploadedFileMock).getInputstream();
		ctrl.setUploadedFile(uploadedFileMock);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.error.upload.io: IOException");
	}

	@Test
	public void handleFileUpload_withEvoteNoRollbackExceptionWithErrorCode_returnsErrorCodeMessage() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		String fileContent = "content";
		byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
		when(uploadedFileMock.getInputstream()).thenReturn(new ByteArrayInputStream(fileBytes));
		evoteNoRollbackException(ErrorCode.ERROR_CODE_0320_SHOULD_BE_FINAL)
				.when(getInjectMock(BatchService.class))
				.importFile(eq(getUserDataMock()), anyInt(), anyLong(), any(Jobbkategori.class));
		ctrl.setUploadedFile(uploadedFileMock);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.error.upload.io: @count.error.import.should_be_final");
	}

	@Test
	public void handleFileUpload_withEvoteNoRollbackExceptionWithErrorMessage_returnsErrorMessage() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		String fileContent = "content";
		byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
		when(uploadedFileMock.getInputstream()).thenReturn(new ByteArrayInputStream(fileBytes));
		evoteNoRollbackException("evoteNoRollbackException")
				.when(getInjectMock(BatchService.class))
				.importFile(eq(getUserDataMock()), anyInt(), anyLong(), any(Jobbkategori.class));
		ctrl.setUploadedFile(uploadedFileMock);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.error.upload.io: evoteNoRollbackException");
	}

	@Test
	public void handleFileUpload_withFileSaveError_returnsIOError() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		ctrl.setUploadedFile(uploadedFileMock);
		String fileContent = "content";
		byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
		when(uploadedFileMock.getInputstream()).thenReturn(new ByteArrayInputStream(fileBytes));
        when(uploadedFileMock.getFileName()).thenReturn("filename");
		when(getInjectMock(BatchService.class).saveFile(eq(getUserDataMock()), eq(fileBytes), anyString(), any(Jobbkategori.class))).thenReturn(null);

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@count.error.upload.io");
	}

	@Test
	public void handleFileUpload_withValidFile_returnsImportOKMessage() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		UploadedFile uploadedFileMock = createMock(UploadedFile.class);
		ctrl.setUploadedFile(uploadedFileMock);
		String fileContent = "content";
		byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
		when(uploadedFileMock.getInputstream()).thenReturn(new ByteArrayInputStream(fileBytes));
		mockFieldValue("batches", new ArrayList<Batch>());

		ctrl.handleFileUpload();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@count.success.upload");
		verify(getInjectMock(BatchService.class)).importFile(eq(getUserDataMock()), anyInt(), anyLong(), eq(COUNT_UPLOAD));
		verify(getInjectMock(BatchService.class)).listMyBatches(eq(getUserDataMock()), eq(COUNT_UPLOAD));
		verify_closeAndUpdate(Dialogs.UPLOAD_BATCHES.getId(), "batchForm");
	}

	@Test
	public void getFile_withBatch_shouldWriteContentToOutputStream() throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		Batch batchMock = createMock(Batch.class);
		BinaryData binaryData = new BinaryData();
		binaryData.setFileName("file.txt");
		binaryData.setBinaryData("content".getBytes(StandardCharsets.UTF_8));
		binaryData.setMimeType("html/text");
		when(getInjectMock(BinaryDataService.class).findByPk(eq(getUserDataMock()), anyLong())).thenReturn(binaryData);

		ctrl.getFile(batchMock);

		verify(getServletContainer().getResponseMock().getOutputStream()).write("content".getBytes(StandardCharsets.UTF_8));

	}

	@Test(dataProvider = "readyForDownload")
	public void readyForDownload_withDataProvider_verifyExpected(int status, boolean expected) throws Exception {
		BatchController ctrl = initializeMocks(BatchController.class);
		Batch batchMock = createMock(Batch.class);
		when(batchMock.getBatchStatus().getId()).thenReturn(status);

		assertThat(ctrl.readyForDownload(batchMock)).isEqualTo(expected);
	}

	@DataProvider(name = "readyForDownload")
	public static Object[][] readyForDownload() {
		return new Object[][] {
				{ EvoteConstants.BATCH_STATUS_COMPLETED_ID, true },
				{ EvoteConstants.BATCH_STATUS_FAILED_ID, true },
				{ EvoteConstants.BATCH_STATUS_STARTED_ID, false },
				{ EvoteConstants.BATCH_STATUS_IN_QUEUE_ID, false }
		};
	}
}

