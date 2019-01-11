package no.valg.eva.admin.frontend.reports.ctrls;

import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rapport.service.RapportService;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.service.JasperReportService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import org.primefaces.model.StreamedContent;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets;
import static no.valg.eva.admin.frontend.reports.ctrls.ParametersDialogBean.RAPPORT_AVKRYSNINGSMANNTALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ReportLinksControllerTest extends BaseFrontendTest {

	@Test
	public void init_withReports_buildsMenu() throws Exception {
		ReportLinksController ctrl = ctrl();

		assertThat(ctrl.getMenus()).hasSize(1);
		assertThat(ctrl.getCanonicalReportParameterParentIdMap()).isNotNull();
	}

	@Test
	public void getValghendelsesRapportById_withInvalidId_returnsNull() throws Exception {
		ReportLinksController ctrl = ctrl();

		assertThat(ctrl.getValghendelsesRapportById("INVALID")).isNull();
	}

	@Test
	public void getValghendelsesRapportById_withValidId_returnsRapport() throws Exception {
		ReportLinksController ctrl = ctrl();

		assertThat(ctrl.getValghendelsesRapportById("report_2")).isNotNull();
	}

	@Test
	public void selectReportAndOpenParameterDialog_withRapport_opensParamDialogWithBean() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById("report_2");

		ctrl.selectReportAndOpenParameterDialog(rapport);

		assertThat(ctrl.getParametersBean()).isNotNull();
		verify_setTitleAndOpen(ctrl.getReportParametersDialog(), "@rapport.meta.report_2.name");
	}

	@Test
	public void getReportContent_withNotReadyExecution_returnsNull() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById("report_2");
		ctrl.selectReportAndOpenParameterDialog(rapport);
		stub_executeReport(reportExecution(false));

		StreamedContent content = ctrl.getReportContent("pdf");

		assertThat(content).isNull();
	}

	@Test
	public void getReportContent_withReadyExecution_returnsContent() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById("report_2");
		ctrl.selectReportAndOpenParameterDialog(rapport);
		stub_executeReport(reportExecution(true));

		StreamedContent content = ctrl.getReportContent("pdf");

		assertThat(content).isNotNull();
	}

	@Test
	public void getReportContent_withAvkryssningsmanntallOgIkkeTest_markererRapportKjort() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById(RAPPORT_AVKRYSNINGSMANNTALL);
		ctrl.selectReportAndOpenParameterDialog(rapport);
		stub_executeReport(reportExecution(true));
        ctrl.getParametersBean().setAvkrysningsmanntallTest(false);

		ctrl.getReportContent("pdf");

		verify(getInjectMock(MunicipalityService.class)).markerAvkryssningsmanntallKjort(getUserDataMock(), getUserDataMock().getOperatorMvArea().areaPath(),
				true);
	}

	@Test
	public void pollReportContent_withNoAsyncReportExecution_callsExecuteReport() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById("report_2");
		ctrl.selectReportAndOpenParameterDialog(rapport);

		ctrl.pollReportContent("pdf");

        verify(getInjectMock(JasperReportService.class)).executeReport(eq(getUserDataMock()), any(), anyMap(), anyString());
	}

	@Test
	public void pollReportContent_withAsyncReportExecution_pollsServer() throws Exception {
		ReportLinksController ctrl = ctrl();
		ValghendelsesRapport rapport = ctrl.getValghendelsesRapportById("report_2");
		ctrl.selectReportAndOpenParameterDialog(rapport);
		ctrl.pollReportContent("pdf"); // Call 1 time to start polling

		ctrl.pollReportContent("pdf");

		verify(getInjectMock(JasperReportService.class)).pollReportExecution(eq(getUserDataMock()), any(ReportExecution.class));
	}

	@Test
	public void getReportDownloadUrl_withReportExecution_returnsUrlWithRequestId() throws Exception {
		ReportLinksController ctrl = ctrl();
		ReportExecution execution = mockField("asyncReportExecution", ReportExecution.class);
		when(execution.getRequestId()).thenReturn("12345");

		String url = ctrl.getReportDownloadUrl();

		assertThat(url).isEqualTo("/secure/reporting/reportContent?requestId=12345");
	}

	private ReportLinksController ctrl() throws Exception {
		return ctrl(false);
	}

	private ReportLinksController ctrl(boolean reportError) throws Exception {
		ReportLinksController ctrl = initializeMocks(new MyReportLinksController());
		if (reportError) {
			evoteExceptionWhen(RapportService.class).rapporterForBruker(eq(getUserDataMock()), any(ElectionPath.class));
		} else {
			stub_rapporterForBruker();
		}
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();
		AreaPath areaPath = mvArea.areaPath();
		when(getUserDataMock().getOperatorMvArea()).thenReturn(mvArea);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(areaPath);
		ctrl.init();
		return ctrl;
	}

	private List<ValghendelsesRapport> stub_rapporterForBruker() {
		List<ValghendelsesRapport> result = asList(
				valghendelsesRapport("report_1"),
				valghendelsesRapport("report_2"),
				valghendelsesRapport(RAPPORT_AVKRYSNINGSMANNTALL));
		when(getInjectMock(RapportService.class).rapporterForBruker(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(result);
		return result;
	}

	private ReportExecution stub_executeReport(ReportExecution reportExecution) {
        when(getInjectMock(JasperReportService.class).executeReport(eq(getUserDataMock()), any(), anyMap(), any())).thenReturn(reportExecution);
		return reportExecution;
	}

	private ReportExecution reportExecution(boolean isReady) {
		ReportExecution result = createMock(ReportExecution.class);
		when(result.isReady()).thenReturn(isReady);
		when(result.getContent()).thenReturn("content".getBytes());
		return result;
	}

	private ValghendelsesRapport valghendelsesRapport(String rapportId) {
		return new ValghendelsesRapport(rapportId, GRUNNLAGSDATA, Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets.getAccess());
	}

	private class MyReportLinksController extends ReportLinksController {
		private Dialog dialog = createMock(Dialog.class);

		@Override
		public Dialog getReportParametersDialog() {
			return dialog;
		}
	}
}

