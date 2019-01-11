package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.util.IOUtil;
import org.primefaces.event.FileUploadEvent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ImportBuypassSerialNumberControllerTest extends BaseFrontendTest {

	@Test
	public void fileUpload_withStream_verifyFileContentAndConfirmDialogShow() throws Exception {
		ImportBuypassSerialNumberController ctrl = initializeMocks(ImportBuypassSerialNumberController.class);
		FileUploadEvent event = createMock(FileUploadEvent.class);
		when(event.getFile().getInputstream()).thenReturn(getInputStream("test"));

		ctrl.fileUpload(event);

		assertThat(new String(getPrivateField("importFile", byte[].class))).isEqualTo("test");
		verify(getRequestContextMock()).execute("PF('confirmationWidget').show()");
	}

	@Test
	public void importUploadedFile_withNoImportFile_returnsErrorMessage() throws Exception {
		ImportBuypassSerialNumberController ctrl = initializeMocks(ImportBuypassSerialNumberController.class);

		ctrl.importUploadedFile();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.operator.import_buypass_number_missing_file");
	}

	@Test
	public void importUploadedFile_withImportFile_importBytes() throws Exception {
		ImportBuypassSerialNumberController ctrl = initializeMocks(ImportBuypassSerialNumberController.class);
		byte[] importFile = IOUtil.getBytes(getClass().getResourceAsStream("/EarlyVoteReceivers-oslo-2.xlsx"));
		MockUtils.setPrivateField(ctrl, "importFile", importFile);
		when(getInjectMock(AdminOperatorService.class).updateBuypassKeySerialNumbers(eq(getUserDataMock()), any(byte[].class)))
                .thenReturn(Collections.singletonList(createMock(BuypassOperator.class)));

		ctrl.importUploadedFile();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.operator.import_buypass_number_success");
		assertThat(ctrl.getOperators()).hasSize(1);
	}

	@Test
	public void importUploadedFile_withImportFailure_returnsErrorMessage() throws Exception {
		ImportBuypassSerialNumberController ctrl = initializeMocks(ImportBuypassSerialNumberController.class);
		byte[] importFile = IOUtil.getBytes(getClass().getResourceAsStream("/EarlyVoteReceivers-oslo-2.xlsx"));
		MockUtils.setPrivateField(ctrl, "importFile", importFile);
		doThrow(new RuntimeException("error")).when(getInjectMock(AdminOperatorService.class)).updateBuypassKeySerialNumbers(eq(getUserDataMock()),
				any(byte[].class));

		ctrl.importUploadedFile();

		assertFacesMessage(FacesMessage.SEVERITY_FATAL, "@config.operator.import_buypass_number_error");
	}

	private InputStream getInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}

}

