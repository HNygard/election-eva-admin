package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.model.Batch;
import no.evote.service.BatchService;
import no.evote.service.configuration.ImportElectoralRollService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImportElectoralRollControllerTest extends BaseFrontendTest {

	@Test
	public void init_withBatches_verifyState() throws Exception {
		ImportElectoralRollController ctrl = initializeMocks(ImportElectoralRollController.class);
		stub_listBatchesByEventAndAccess();

		ctrl.init();

		assertThat(ctrl.getFilePath()).isEqualTo("/");
		assertThat(ctrl.getFinalImport()).isNull();
		assertThat(ctrl.getBatches()).hasSize(1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void importElectoralRoll_withMissingFinalImport_throwsError() throws Exception {
		ImportElectoralRollController ctrl = initializeMocks(ImportElectoralRollController.class);

		ctrl.importElectoralRoll();
	}

	@Test
	public void importElectoralRoll_withPrelimImport_verifyImport() throws Exception {
		ImportElectoralRollController ctrl = initializeMocks(ImportElectoralRollController.class);
		ctrl.setFinalImport(false);
		ctrl.setFilePath("/something");

		ctrl.importElectoralRoll();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@electoralRoll.importElectoralRoll.finished");
		assertThat(ctrl.getFilePath()).isEqualTo("/");
		assertThat(ctrl.getFinalImport()).isNull();
		verify_validateImportFile();
		verify_preliminaryFullImportElectoralRoll();
	}

	@Test
	public void importElectoralRoll_withFinalImport_verifyImport() throws Exception {
		ImportElectoralRollController ctrl = initializeMocks(ImportElectoralRollController.class);
		ctrl.setFinalImport(true);

		ctrl.importElectoralRoll();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@electoralRoll.importElectoralRoll.finished");
		verify_validateImportFile();
		verify_finalFullImportElectoralRoll();
	}

	private void stub_listBatchesByEventAndAccess() {
		when(getInjectMock(BatchService.class).listBatchesByEventAndCategory(eq(getUserDataMock()),
                any(Jobbkategori.class), any())).thenReturn(Collections.singletonList(createMock(Batch.class)));
	}

	private void verify_validateImportFile() {
		verify(getInjectMock(ImportElectoralRollService.class)).validateImportFile(eq(getUserDataMock()), any(ElectionEvent.class), anyString());
	}

	private void verify_preliminaryFullImportElectoralRoll() {
		verify(getInjectMock(ImportElectoralRollService.class))
				.preliminaryFullImportElectoralRoll(eq(getUserDataMock()), any(ElectionEvent.class), anyString());
	}

	private void verify_finalFullImportElectoralRoll() {
		verify(getInjectMock(ImportElectoralRollService.class)).finalFullImportElectoralRoll(eq(getUserDataMock()), any(ElectionEvent.class), anyString());
	}
}
