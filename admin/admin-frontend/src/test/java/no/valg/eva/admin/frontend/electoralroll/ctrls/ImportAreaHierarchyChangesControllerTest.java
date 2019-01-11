package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.service.AreaImportChangesService;
import no.valg.eva.admin.util.IOUtil;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ImportAreaHierarchyChangesControllerTest extends BaseFrontendTest {

	@Test
	public void fileUpload_withStream_verifyFileContentAndConfirmDialogShow() throws Exception {
		ImportAreaHierarchyChangesController ctrl = initializeMocks(ImportAreaHierarchyChangesController.class);
		FileUploadEvent event = createMock(FileUploadEvent.class);
		when(event.getFile().getInputstream()).thenReturn(getInputStream("test"));

		ctrl.fileUpload(event);

		assertThat(new String(getPrivateField("importFile", byte[].class))).isEqualTo("test");
		verify(getRequestContextMock()).execute("PF('confirmationWidget').show()");
	}

	@Test
	public void importUploadedFile_withNoImportFile_returnsErrorMessage() throws Exception {
		ImportAreaHierarchyChangesController ctrl = initializeMocks(ImportAreaHierarchyChangesController.class);

		ctrl.importUploadedFile();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.area.import_error");
	}

	@Test
	public void importUploadedFile_withImportFile_importBytes() throws Exception {
		ImportAreaHierarchyChangesController ctrl = initializeMocks(ImportAreaHierarchyChangesController.class);
		MockUtils.setPrivateField(ctrl, "importFile", "test".getBytes());

		ctrl.importUploadedFile();

		verify(getInjectMock(AreaImportChangesService.class)).importAreaHierarchyChanges(eq(getUserDataMock()), eq("test".getBytes()));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.area.import_success");
	}

	@Test
	public void getUploadResponse_withNoResponse_returnsNull() throws Exception {
		ImportAreaHierarchyChangesController ctrl = initializeMocks(ImportAreaHierarchyChangesController.class);

		assertThat(ctrl.getUploadResponse()).isNull();
	}

	@Test
	public void getUploadResponse_withResponse_returnsXX() throws Exception {
		ImportAreaHierarchyChangesController ctrl = initializeMocks(ImportAreaHierarchyChangesController.class);
		MockUtils.setPrivateField(ctrl, "response", "test".getBytes());

		DefaultStreamedContent content = ctrl.getUploadResponse();

		assertThat(content).isNotNull();
		assertThat(content.getContentType()).isEqualTo("text/plain");
		assertThat(new String(IOUtil.getBytes(content.getStream()))).isEqualTo("test");
	}

	private InputStream getInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}
}

