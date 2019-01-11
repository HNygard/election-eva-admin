package no.valg.eva.admin.frontend.configuration.ctrls;

import no.evote.dto.BatchInfoDto;
import no.evote.service.ExportService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Map;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML_Behandle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class EMLExportControllerTest extends BaseFrontendTest {

	@Test
	public void init_witLocalConfigStatus_returnsDisabledMessage() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		stub_electionEventStatus(ElectionEventStatusEnum.LOCAL_CONFIGURATION);

		ctrl.init();

		assertFacesMessage(FacesMessage.SEVERITY_WARN, "@election.election_event.eml_disabled");
		verify(getInjectMock(ExportService.class)).getGeneratedEMLBatches(eq(getUserDataMock()), eq("100"));
	}

	@Test(dataProvider = "canGenerateEML")
	public void canGenerateEML_withDataProvider_verifyExpected(ElectionEventStatusEnum status, boolean expected) throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		stub_electionEventStatus(status);

		assertThat(ctrl.canGenerateEML()).isEqualTo(expected);
	}

	@DataProvider(name = "canGenerateEML")
	public Object[][] canGenerateEML() {
		return new Object[][] {
				{ ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false },
				{ ElectionEventStatusEnum.LOCAL_CONFIGURATION, false },
				{ ElectionEventStatusEnum.FINISHED_CONFIGURATION, false },
				{ ElectionEventStatusEnum.APPROVED_CONFIGURATION, true },
				{ ElectionEventStatusEnum.CLOSED, false }
		};
	}

	@Test
	public void isGenerateAccess_withAccess_returnsTrue() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		hasAccess(Konfigurasjon_EML_Behandle);

		assertThat(ctrl.isKonfigurasjonEmlBehandle()).isTrue();
	}

	@Test
	public void generateEML_withElection_verifyGenerate() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);

		ctrl.generateEML();

		verify(getInjectMock(ExportService.class)).generateEML(eq(getUserDataMock()), anyLong());
        verify(getInjectMock(ExportService.class)).getGeneratedEMLBatches(eq(getUserDataMock()), any());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void hasBeenValidated_withDifferentPks_verifyResult() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		Map<Long, Boolean> validEML = getPrivateField("validEML", Map.class);
		validEML.put(1L, true);

		assertThat(ctrl.hasBeenValidated(null)).isFalse();
		assertThat(ctrl.hasBeenValidated(batchInfoDto(1L))).isTrue();
		assertThat(ctrl.hasBeenValidated(batchInfoDto(2L))).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void isValid_withDifferentPks_verifyResult() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		Map<Long, Boolean> validEML = getPrivateField("validEML", Map.class);
		validEML.put(1L, true);
		validEML.put(2L, false);

		assertThat(ctrl.isValid(batchInfoDto(1L))).isTrue();
		assertThat(ctrl.isValid(batchInfoDto(2L))).isFalse();
	}

	@Test
	public void validateBatch_withBatch_verifyValidate() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		when(getInjectMock(ExportService.class).validateGeneratedEML(eq(getUserDataMock()), eq(1L))).thenReturn(true);
		BatchInfoDto batch = batchInfoDto(1L);

		ctrl.validateBatch(batch);

		assertThat(ctrl.hasBeenValidated(batch)).isTrue();
		assertThat(ctrl.isValid(batch)).isTrue();
	}

	@Test
	public void download_withBatch_verifyResponse() throws Exception {
		EMLExportController ctrl = initializeMocks(EMLExportController.class);
		BatchInfoDto batch = batchInfoDto(1L);
		when(getInjectMock(ExportService.class).getGeneratedEML(eq(getUserDataMock()), eq(1L))).thenReturn("test".getBytes());
		when(getInjectMock(UserDataController.class).getElectionEvent().getId()).thenReturn("101010");

		ctrl.download(batch);

		verify(getFacesContextMock().getExternalContext()).responseReset();
		verify(getServletContainer().getResponseMock()).setContentType("application/force-download");
		verify(getServletContainer().getResponseMock()).addHeader("Content-Disposition", "attachment; filename=\"EML_101010.zip\"");
		verify(getServletContainer().getResponseMock()).setContentLength(4);
		verify(getServletContainer().getResponseMock().getOutputStream()).write("test".getBytes());
		verify(getServletContainer().getResponseMock().getOutputStream()).close();
		verify(getFacesContextMock()).responseComplete();
	}

	private BatchInfoDto batchInfoDto(long pk) {
		BatchInfoDto result = createMock(BatchInfoDto.class);
		when(result.getPk()).thenReturn(pk);
		return result;
	}

	private void stub_electionEventStatus(ElectionEventStatusEnum status) {
		ElectionEventStatus electionEventStatus = createMock(ElectionEventStatus.class);
		when(electionEventStatus.getId()).thenReturn(status.id());
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus()).thenReturn(electionEventStatus);
		when(getInjectMock(UserDataController.class).getElectionEvent().getId()).thenReturn("100");
	}
}

