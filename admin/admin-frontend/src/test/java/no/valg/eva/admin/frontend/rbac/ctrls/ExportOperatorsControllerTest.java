package no.valg.eva.admin.frontend.rbac.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.Test;

public class ExportOperatorsControllerTest extends BaseFrontendTest {

	@Test
	public void export_withIOException_returnsErrorMessage() throws Exception {
		ExportOperatorsController ctrl = initializeMocks(ExportOperatorsController.class);
		doThrow(new IOException("error")).when(getServletContainer().getResponseMock()).getOutputStream();

		ctrl.export(false);

		assertFacesMessage(SEVERITY_ERROR, "[@rbac.import_export.export_operators.ioexception, cb5e100e]");
	}
}
