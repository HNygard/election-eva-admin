package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.service.configuration.AreaImportService;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import org.primefaces.event.FileUploadEvent;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ImportAreaHierarchyControllerTest extends BaseFrontendTest {

	@Test
	public void fileUpload_withStream_verifyFileContentAndConfirmDialogShow() throws Exception {
		ImportAreaHierarchyController ctrl = initializeMocks(ImportAreaHierarchyController.class);
		FileUploadEvent event = createMock(FileUploadEvent.class);
		when(event.getFile().getInputstream()).thenReturn(getInputStream("test"));

		ctrl.fileUpload(event);

		assertThat(new String(getPrivateField("importFile", byte[].class))).isEqualTo("test");
		verify(getRequestContextMock()).execute("PF('confirmationWidget').show()");
	}

	@Test
	public void importUploadedFile_withNoImportFile_returnsErrorMessage() throws Exception {
		ImportAreaHierarchyController ctrl = initializeMocks(ImportAreaHierarchyController.class);

		ctrl.importUploadedFile();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.area.import_error");
	}

	@Test
	public void importUploadedFile_withImportFile_importBytes() throws Exception {
		ImportAreaHierarchyController ctrl = initializeMocks(ImportAreaHierarchyController.class);
		MockUtils.setPrivateField(ctrl, "importFile", "test".getBytes());

		ctrl.importUploadedFile();

		verify(getInjectMock(AreaImportService.class)).importAreaHierarchy(eq(getUserDataMock()), eq("test".getBytes()));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.area.import_success");
	}

	@Test(dataProvider = "oldConstraintViolationCheck")
	public void oldConstraintViolationCheck_withDataProvider_verifyExpected(String constaintName, String expected) throws Exception {
		ImportAreaHierarchyController ctrl = initializeMocks(ImportAreaHierarchyController.class);

		assertThat(ctrl.oldConstraintViolationCheck(constaintName)).isEqualTo(expected);
	}

	@DataProvider(name = "oldConstraintViolationCheck")
	public Object[][] oldConstraintViolationCheck() {
		return new Object[][] {
				{ "fk_voting_x_something", "@config.area.import_error.voting" },
				{ "fk_vote_count_x_something", "@config.area.import_error.vote_count" },
				{ "fk_voter_x_something", "@config.area.import_error.voter" },
				{ "something", null }
		};
	}

	private InputStream getInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}
}

