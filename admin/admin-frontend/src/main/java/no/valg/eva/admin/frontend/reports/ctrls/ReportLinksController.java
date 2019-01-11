package no.valg.eva.admin.frontend.reports.ctrls;

import com.google.common.collect.Maps;
import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rapport.service.RapportService;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.service.JasperReportService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.reports.ReportMenuBuilder;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.REPORT_PARAMETER;
import static no.valg.eva.admin.frontend.reporting.ReportContentDownloadServlet.SECURE_REPORTING_REPORT_CONTENT_URL;

@Named
@ViewScoped
public class ReportLinksController extends BaseController {

	private static final Maps.EntryTransformer<String, Object, String> OBJECT_STRING_ENTRY_TRANSFORMER = (key, value) -> value != null ? value.toString()
			: null;

	// Injected
	private UserDataController userDataController;
	private MessageProvider messageProvider;
	private RapportService rapportService;
	private JasperReportService jasperReportService;
	private Event<ReportExecution> reportContentEvent;
	private MunicipalityService municipalityService;

	@Inject
	@EjbProxy
	private VotingService votingService;

	private List<ValghendelsesRapport> reports = new ArrayList<>();
	private ReportMenuBuilder builder;
	private ParametersDialogBean parametersBean;
	private Map<String, String> canonicalReportParameterParentIdMap;
	private ReportExecution asyncReportExecution;

	public ReportLinksController() {
		// CDI
	}

	@Inject
	public ReportLinksController(UserDataController userDataController, MessageProvider messageProvider, RapportService rapportService,
			JasperReportService jasperReportService,
			Event<ReportExecution> reportContentEvent, MunicipalityService municipalityService) {
		this.userDataController = userDataController;
		this.messageProvider = messageProvider;
		this.rapportService = rapportService;
		this.jasperReportService = jasperReportService;
		this.reportContentEvent = reportContentEvent;
		this.municipalityService = municipalityService;
	}

	@PostConstruct
	public void init() {
		execute(() -> {
			reports = rapportService.rapporterForBruker(getUserData(), getUserData().getOperatorMvElection().getElectionEvent().electionPath());
			builder = new ReportMenuBuilder(userDataController, reports);
		});
	}

	public ValghendelsesRapport getValghendelsesRapportById(String reportId) {
		return getReports().stream()
				.filter(rapport -> rapport.getRapportId().equals(reportId))
				.findFirst().orElse(null);
	}

	public void selectReportAndOpenParameterDialog(ValghendelsesRapport rapport) {
		execute(() -> {
			parametersBean = new ParametersDialogBean(this, rapport);
			getReportParametersDialog().setTitleAndOpen(messageProvider.get(rapport.getNameKey()));
		});
	}

	public StreamedContent getReportContent(String format) {
		try {
			ReportExecution reportResult = getReportExecutionResult(format);
			if (reportResult.isReady()) {
				return new DefaultStreamedContent(new ByteArrayInputStream(reportResult.getContent()), contentTypeFromFormat(format), reportResult.getFileName());
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			process(e);
			return null;
		}
	}

	public void pollReportContent(String format) {
		if (getAsyncReportExecution() == null) {
			asyncReportExecution = getReportExecutionResult(format);
		} else {
			try {
				asyncReportExecution = jasperReportService.pollReportExecution(getUserData(), asyncReportExecution);
			} catch (Exception e) {
				asyncReportExecution = getAsyncReportExecution().failed();
				throw e;
			}
			if (getAsyncReportExecution().isReady()) {
				reportContentEvent.fire(getAsyncReportExecution());
			}
		}
	}

	public Dialog getReportParametersDialog() {
		return REPORT_PARAMETER;
	}

	public String getReportDownloadUrl() {
		return SECURE_REPORTING_REPORT_CONTENT_URL + "?requestId=" + getAsyncReportExecution().getRequestId();
	}

	public List<Menu> getMenus() {
		return builder.getMenus();
	}

	public List<ValghendelsesRapport> getReports() {
		return reports;
	}

	public ReportExecution getAsyncReportExecution() {
		return asyncReportExecution;
	}

	public ParametersDialogBean getParametersBean() {
		return parametersBean;
	}

	Map<String, String> getCanonicalReportParameterParentIdMap() {
		if (canonicalReportParameterParentIdMap == null) {
			canonicalReportParameterParentIdMap = jasperReportService.getCanonicalReportParameterParentIdMap(getUserData());
		}
		return canonicalReportParameterParentIdMap;
	}

	UserData getUserData() {
		return userDataController.getUserData();
	}

	JasperReportService getJasperReportService() {
		return jasperReportService;
	}

	MessageProvider getMessageProvider() {
		return messageProvider;
	}

	VotingService getVotingService() {
		return votingService;
	}

	private ReportExecution getReportExecutionResult(String format) {
		if (!parametersBean.isAvkrysningsmanntallReady()) {
			// Safeguard
			throw new IllegalStateException("Kan ikke kjøre Avkrysningsmanntallrapport med uprøvde forhåndsstemmer");
		}
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(
				getUserData(), parametersBean.getSelectedReport().getReportUri());
		// overwrite parameter values with pre filled values if exist
		for (ReportParameter reportParameter : reportTemplate.getParameters()) {
			if (reportParameter.isInferred()) {
				parametersBean.getArguments().put(reportParameter.getId(), reportParameter.getDefaultValue().toString());
			}
		}
		if (parametersBean.isAvkrysningsmanntall()) {
			parametersBean.getArguments().put("IS_TEST", parametersBean.isAvkrysningsmanntallTest());
		}
		parametersBean.getArguments().put("DATE", getDateFormat().print(DateTime.now()));
		parametersBean.getArguments().put("TIME", getTimeFormat().print(DateTime.now()));
		Map<String, String> parametersAsStrings = Maps.transformEntries(parametersBean.getArguments(), OBJECT_STRING_ENTRY_TRANSFORMER);
		ReportExecution result = jasperReportService.executeReport(getUserData(), reportTemplate.getReportUri(),
				newHashMap(parametersAsStrings), format);
		if (result.isReady() && parametersBean.isAvkrysningsmanntall() && !parametersBean.isAvkrysningsmanntallTest()) {
			municipalityService.markerAvkryssningsmanntallKjort(getUserData(), getUserData().getOperatorAreaPath(), true);
		}
		return result;
	}

	private DateTimeFormatter getDateFormat() {
		return DateTimeFormat.forPattern("yyyy-MM-dd");
	}

	private DateTimeFormatter getTimeFormat() {
		return DateTimeFormat.forPattern("HH:mm:ss");
	}

	private String contentTypeFromFormat(String format) {
		String contentType = EvoteConstants.MIME_TYPES.get(format);
		if (contentType == null) {
			contentType = "application/pdf";
		}
		return contentType;
	}
}
