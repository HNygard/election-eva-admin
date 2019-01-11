package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.service.rbac.OperatorRoleService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.ImportOperatorMessage;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class ImportOperatorsControllerTest extends BaseFrontendTest {

	@Test
	public void fileUpload_withUserMessages_returnsInfoMessages() throws Exception {
		ImportOperatorsController ctrl = initializeMocks(ImportOperatorsController.class);
        stub_importOperatorRoles(Collections.singletonList(
                new ImportOperatorMessage(1, "@message", "@details")));
		FileUploadEvent event = fileUploadEvent();
        ArrayList<FacesMessage> expectedMessages = new ArrayList<>();
        expectedMessages.add(new FacesMessage(SEVERITY_INFO, "@rbac.import_export.users_imported_msgs", null));
        expectedMessages.add(new FacesMessage(SEVERITY_INFO, "[@rbac.import_operators.line_num, 1]: [@message, @details]", null));

		ctrl.fileUpload(event);

        assertFacesMessages(expectedMessages);
	}

	@Test
	public void fileUpload_withNoUserMessages_returnsInfoMessage() throws Exception {
		ImportOperatorsController ctrl = initializeMocks(ImportOperatorsController.class);
		stub_importOperatorRoles(new ArrayList<>());
		FileUploadEvent event = fileUploadEvent();

		ctrl.fileUpload(event);

		assertFacesMessage(SEVERITY_INFO, "@rbac.import_export.users_imported");
	}

	private FileUploadEvent fileUploadEvent() throws IOException {
		FileUploadEvent result = createMock(FileUploadEvent.class);
		when(result.getFile().getInputstream()).thenReturn(IOUtils.toInputStream("test"));
		return result;
	}

	private void stub_importOperatorRoles(List<ImportOperatorMessage> msgs) {
		when(getInjectMock(OperatorRoleService.class).importOperatorRoles(eq(getUserDataMock()), anyLong(), eq("test".getBytes()))).thenReturn(msgs);
	}
}

