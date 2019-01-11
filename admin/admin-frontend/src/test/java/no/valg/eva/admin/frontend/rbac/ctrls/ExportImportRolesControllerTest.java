package no.valg.eva.admin.frontend.rbac.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;

import no.evote.service.rbac.RoleService;
import no.valg.eva.admin.BaseFrontendTest;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.testng.annotations.Test;

public class ExportImportRolesControllerTest extends BaseFrontendTest {

	@Test
	public void exportRoles_withIOException_returnsErrorMessage() throws Exception {
		ExportImportRolesController ctrl = initializeMocks(ExportImportRolesController.class);
		stub_exportRoles();
		doThrow(new IOException("error")).when(getServletContainer().getResponseMock()).getOutputStream();

		ctrl.exportRoles();

		assertFacesMessage(SEVERITY_ERROR, "[@rbac.import_export.export_operators.ioexception, cb5e100e]");
	}

	@Test
	public void fileUpload_withEvent_uploadsFileAndReturnsMessage() throws Exception {
		ExportImportRolesController ctrl = initializeMocks(ExportImportRolesController.class);
		ctrl.setDeleteExistingRoles(true);
		FileUploadEvent event = fileUploadEvent();
		stub_importRoles("test", true, 1);

		ctrl.fileUpload(event);

		assertFacesMessage(SEVERITY_INFO, "[@rbac.roles.imported, 1]");
	}

	private void stub_exportRoles() {
		when(getInjectMock(RoleService.class).exportRoles(getUserDataMock(), true)).thenReturn("result");
	}

	private FileUploadEvent fileUploadEvent() throws IOException {
		FileUploadEvent result = createMock(FileUploadEvent.class);
		when(result.getFile().getInputstream()).thenReturn(IOUtils.toInputStream("test"));
		return result;
	}

	private void stub_importRoles(String s, boolean deleteExisting, int result) {
		when(getInjectMock(RoleService.class).importRoles(getUserDataMock(), s, deleteExisting)).thenReturn(result);
	}

}
