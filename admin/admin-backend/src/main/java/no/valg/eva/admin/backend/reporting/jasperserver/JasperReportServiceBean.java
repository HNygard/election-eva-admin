package no.valg.eva.admin.backend.reporting.jasperserver;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.UncheckedExecutionException;
import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.exception.ValidateException;
import no.evote.security.UserData;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.backend.application.schedule.RefreshResourceBundlesEvent;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.backend.reporting.jasperserver.api.DataType;
import no.valg.eva.admin.backend.reporting.jasperserver.api.FileResource;
import no.valg.eva.admin.backend.reporting.jasperserver.api.InputControl;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecution;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecutionRequest;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperReport;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperResources;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiNoTimeout;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiWithTimeout;
import no.valg.eva.admin.backend.reporting.jasperserver.api.PregeneratedContentRetriever;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static no.evote.constants.AreaLevelEnum.NONE;
import static no.evote.exception.ErrorCode.ERROR_CODE_0590_REPORT_SERVER_ERROR;
import static no.evote.exception.ErrorCode.ERROR_CODE_0591_REPORT_AREA_LEVEL_ACCESS_ERROR;
import static no.evote.util.EvoteProperties.JASPERSERVER_BASE_REPORT_FORMATS;
import static no.evote.util.EvoteProperties.JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi.ResourceType.reportUnit;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus.Status.FAILED;
import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.AccessDeniedInBackend;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;
import static org.apache.commons.lang3.text.StrSubstitutor.replace;
import static org.apache.log4j.Logger.getLogger;

/**
 * Service that offers an adaptation of the JasperServer REST API and path-based report parameter handling.
 */
@Default
@ApplicationScoped
public class JasperReportServiceBean implements Serializable {
	public static final String ELECTION_EVENT = "EE1";
	public static final String COUNTRY = "EE1.CO1";
	public static final String COUNTY = "EE1.CO1.CNT1";
	private static final ImmutableSet<AreaLevelEnum> COUNTY_LEVELS = of(AreaLevelEnum.COUNTY, AreaLevelEnum.ROOT);
	private static final ImmutableSet<AreaLevelEnum> MUNICIPALITY_LEVELS = of(AreaLevelEnum.MUNICIPALITY, AreaLevelEnum.ROOT);
	private static final ImmutableSet<AreaLevelEnum> ROOT_AREA_LEVEL = of(AreaLevelEnum.COUNTRY, AreaLevelEnum.ROOT);
	private static final ImmutableSet<AreaLevelEnum> LOWEST_LEVELS = of(AreaLevelEnum.ROOT, AreaLevelEnum.MUNICIPALITY,
			AreaLevelEnum.POLLING_DISTRICT, AreaLevelEnum.POLLING_STATION);
	public static final String MUNICIPALITY = "EE1.CO1.CNT1.MUN1";
	private static final List<String> PARAMETERS_FOR_PATH_TO_MUNICIPALITY = newArrayList(ELECTION_EVENT, COUNTRY, COUNTY, MUNICIPALITY);
	private static final List<String> PARAMETERS_FOR_PATH_TO_COUNTY = newArrayList(ELECTION_EVENT, COUNTRY, COUNTY);
	private static final String BOROUGH_WHICH_CAN_BE_EMPTY = "EE1.CO1.CNT1.MUN1.BOR1_EMPTY";
	public static final String BOROUGH = "EE1.CO1.CNT1.MUN1.BOR1";
	public static final String POLLING_DISTRICT = "EE1.CO1.CNT1.MUN1.BOR1.PD1";
	public static final String POLLING_PLACE = "EE1.CO1.CNT1.MUN1.BOR1.PD1.PP1";
	public static final String ELECTION_GROUP = "EE1.EG1";
	public static final String ELECTION = "EE1.EG1.EL1";
	public static final String CONTEST = "EE1.EG1.EL1.CT1";
	private static final String CONTEST_PK = "EE1.EG1.EL1.CT1_PK";
	private static final String CONTEST_PK_COUNTY_OR_MUN = "EE1.EG1.EL1.CT1_FOR_CNT_OR_MUN_ONLY_PK";
	public static final String REPORT_LOCALE = "REPORT_LOCALE";
	private static final ReportMetaData.Format DEFAULT_FORMAT = ReportMetaData.Format.PDF;
	private static final Predicate<String> FILTER_EMPTY_STRINGS = input -> !input.isEmpty();
	private static final boolean CONFINE_CONTESTS_TO_USER_AREA_LEVEL = true;

	private String baseReportFormats = "";
	private Iterable<String> retrievePregensFrom = newArrayList("jasperserver");

	private static final Logger LOGGER = getLogger(JasperReportServiceBean.class);
	private static final String[] AREA_PATH_PARAMETERS_EMPTY_BOROUGH = {
			ELECTION_EVENT, COUNTRY, COUNTY, MUNICIPALITY, BOROUGH_WHICH_CAN_BE_EMPTY, POLLING_DISTRICT, POLLING_PLACE
	};
	private static final String[] AREA_PATH_PARAMETERS = {
			ELECTION_EVENT, COUNTRY, COUNTY, MUNICIPALITY, BOROUGH, POLLING_DISTRICT, POLLING_PLACE
	};
	private static final String[] ELECTION_PATH_PARAMETERS = {
			ELECTION_EVENT, ELECTION_GROUP, ELECTION, CONTEST
	};
	private static final String[] ELECTION_PATH_PARAMETERS_CONTEST_PK = {
			ELECTION_EVENT, CONTEST_PK
	};
	private static final String[] ELECTION_PATH_PARAMETERS_CONTEST_AT_COUNTY_OR_MUNICIPALITY_PK = {
			ELECTION_EVENT, CONTEST_PK_COUNTY_OR_MUN
	};
	private static final Map<String, Integer> AREA_PARAMETER_PATH_ORDER = new HashMap<>();
	private static final Map<String, Integer> ELECTION_PARAMETER_PATH_ORDER = new HashMap<>();
	private static final Cache<String, ReportTemplate> REPORT_TEMPLATE_CACHE = CacheBuilder.newBuilder().maximumSize(50).expireAfterWrite(10, TimeUnit.MINUTES)
			.build();
	private static final Cache<Cache, Collection<ReportTemplate>> AVAILABLE_REPORT_TEMPLATE_CACHE = CacheBuilder.newBuilder().maximumSize(1L)
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build();

	private static final Set<String> PARAMETERS_NAMES_OF_PATH_PARAMETERS;
	private static final Map<String, String> CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP = new HashMap<>();

	static {
		for (int i = 0; i < AREA_PATH_PARAMETERS.length; i++) {
			String areaPathParameter = AREA_PATH_PARAMETERS[i];
			AREA_PARAMETER_PATH_ORDER.put(areaPathParameter, i);
			if (i > 0) {
				CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP.put(areaPathParameter, AREA_PATH_PARAMETERS[i - 1]);
			}
		}
		for (int i = 0; i < AREA_PATH_PARAMETERS_EMPTY_BOROUGH.length; i++) {
			String areaPathParameter = AREA_PATH_PARAMETERS_EMPTY_BOROUGH[i];
			AREA_PARAMETER_PATH_ORDER.put(areaPathParameter, i);
		}
		for (int i = 0; i < ELECTION_PATH_PARAMETERS.length; i++) {
			String electionPathParameter = ELECTION_PATH_PARAMETERS[i];
			ELECTION_PARAMETER_PATH_ORDER.put(electionPathParameter, i);
			if (i > 0) {
				CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP.put(electionPathParameter, ELECTION_PATH_PARAMETERS[i - 1]);
			}
		}
		for (int i = 0; i < ELECTION_PATH_PARAMETERS_CONTEST_PK.length; i++) {
			String electionPathParameter = ELECTION_PATH_PARAMETERS_CONTEST_PK[i];
			ELECTION_PARAMETER_PATH_ORDER.put(electionPathParameter, i);
		}
		PARAMETERS_NAMES_OF_PATH_PARAMETERS = new LinkedHashSet<>();
		PARAMETERS_NAMES_OF_PATH_PARAMETERS.addAll(Arrays.asList(AREA_PATH_PARAMETERS));
		PARAMETERS_NAMES_OF_PATH_PARAMETERS.addAll(Arrays.asList(AREA_PATH_PARAMETERS_EMPTY_BOROUGH));
		PARAMETERS_NAMES_OF_PATH_PARAMETERS.addAll(Arrays.asList(ELECTION_PATH_PARAMETERS));
		PARAMETERS_NAMES_OF_PATH_PARAMETERS.addAll(Arrays.asList(ELECTION_PATH_PARAMETERS_CONTEST_PK));
		CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP.put(CONTEST_PK, ELECTION);
		PARAMETERS_NAMES_OF_PATH_PARAMETERS.addAll(Arrays.asList(ELECTION_PATH_PARAMETERS_CONTEST_AT_COUNTY_OR_MUNICIPALITY_PK));
		CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP.put(CONTEST_PK_COUNTY_OR_MUN, ELECTION);
	}

	private static final String REPORT_PARAMETER_LABEL_PREFIX = "@reporting.report.parameter.label.";

	private static final Function<ContestArea, AreaLevelEnum> AREAL_EVEL_FROM_CONTEST_AREA =
			contestArea -> contestArea != null ? contestArea.getActualAreaLevel() : null;
	public static final Predicate<ReportTemplate> EXCLUDE_HIDDEN_REPORT_TEMPLATES = reportTemplate -> reportTemplate != null && !reportTemplate.isHidden();

	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private CountryServiceBean countryService;
	@Inject
	private MunicipalityRepository municipalityRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private AuditLogServiceBean auditLogService;
	@Inject
	private JasperRestApiNoTimeout jasperRestApiNoTimeout;
	@Inject
	private JasperRestApiWithTimeout jasperRestApiWithTimeout;
	@Inject
	private BoroughRepository boroughRepository;
	@Inject
	private CountyRepository countyRepository;
	@Inject
	private CountryRepository countryRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;
	@Inject
	private ElectionGroupRepository electionGroupRepository;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private Event<RefreshResourceBundlesEvent> uploadReportTemplatesEventEvent;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;

	@Inject
	@Any
	private Instance<PregeneratedContentRetriever> pregeneratedContentRetrievers;

	public JasperReportServiceBean() {
		// Brukes av BackendWirer i integrasjonstester
	}

	@Inject
	public JasperReportServiceBean(
			@EvaConfigProperty @Named(JASPERSERVER_BASE_REPORT_FORMATS) String baseFormats,
			@EvaConfigProperty @Named(JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM) String retrievePregensFrom) {
		baseReportFormats = baseFormats;
		this.retrievePregensFrom = Splitter.onPattern(",|\\s").split(retrievePregensFrom);
	}

	public static AreaPath areaPathFrom(Map<String, String> parameters) {
		return parameters.get(MUNICIPALITY) != null
				? AreaPath.from(parameters.get(ELECTION_EVENT), parameters.get(COUNTRY),
						parameters.get(COUNTY), parameters.get(MUNICIPALITY))
				: AreaPath.from(parameters.get(ELECTION_EVENT), parameters.get(COUNTRY),
						parameters.get(COUNTY));
	}

	public static String lastPathElement(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	public void clearReportTemplateCache(@Observes UploadReportTemplatesDoneEvent uploadReportTemplatesDoneEvent) {
		REPORT_TEMPLATE_CACHE.invalidateAll();
		AVAILABLE_REPORT_TEMPLATE_CACHE.invalidateAll();
	}

	public ReportTemplate getReportTemplate(final UserData userData, final String reportUri) {
		ReportTemplate reportTemplate;
		try {
			reportTemplate = getReportTemplateFromCache(userData, reportUri);
		} catch (ExecutionException e) {
			throw new EvoteException("Failed retrieveing report template " + reportUri, e);
		}
		return validateAreaLevel(userData, reportTemplate);
	}

	public ReportTemplate getReportTemplate(UserData userData, ValghendelsesRapport rapport) {
		for (ReportTemplate template : getCachedAllAvailableReports()) {
			if (template.getReportName().equals(rapport.getNameKey())) {
				// We need to call getReportTemplate with uri to load the actual report from server.
				return getReportTemplate(userData, template.getReportUri());
			}
		}
		throw new EvoteException("Report template not found " + rapport.getNameKey());
	}

	private ReportTemplate validateAreaLevel(UserData userData, ReportTemplate reportTemplate) {
		if (!deniedAccessBasedOnUserAreaLevel(userData, reportTemplate)) {
			return cloneAndPrepareTemplateWithParameterRelationshipsAndValues(userData, reportTemplate);
		} else {
			String msg = MessageFormat.format(
					"User {0} with area level {1} does not have access to report \"{2}\" on area level {3}",
					userData.getUid(), userData.getOperatorRole().getMvArea().getAreaLevel(), reportTemplate.getReportName(), reportTemplate.getAreaLevels());
			auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(AccessDeniedInBackend).withDetail(msg).build());
			throw new EvoteSecurityException(ERROR_CODE_0591_REPORT_AREA_LEVEL_ACCESS_ERROR, null);
		}
	}

	public ReportExecution executeReport(final UserData userData, final String reportUri, final Map<String, String> arguments, final String format) {
		validateAreaLevel(userData, getReportTemplate(userData, reportUri));

		Map<String, String> argumentsCopy = new HashMap<>(arguments);
		// Overwrite with pre fill values to prevent forgery
		fillReportArgumentsFromUserData(argumentsCopy, userData, reportUri);
		ReportTemplate reportTemplate = reportTemplate(userData, reportUri);
		// overwrite with any fixed parameter values
		for (ReportParameter reportParameter : reportTemplate.getParameters()) {
			if (reportParameter.isFixed()) {
				arguments.put(reportParameter.getId(), reportParameter.getDefaultValue().toString());
			}
		}
		Map<String, String> parameterLabels = new HashMap<>();
		for (ReportParameter parameter : reportTemplate.getParameters()) {
			parameterLabels.put(parameter.getId(), parameterLabelForId(userData, parameter));
		}
		String reportFilename = reportFilename(userData, arguments, format, reportTemplate);
		String reportName = reportName(userData, reportTemplate);

		ReportExecution reportExecution = null;
		if (reportTemplate.isMaybePreGenerated()) {
			List<String> triedRepositoryTypes = new ArrayList<>();
			String repositoryFileName = String.format("%s_%s.%s",
					lastPathElement(reportUri),
					areaPathFrom(arguments).path().replace(".", "_"), format);
			for (PregeneratedContentRetriever retriever : orderedContentRetrievers()) {
				byte[] reportContent = retriever.tryPreGeneratedReport(repositoryFileName);
				triedRepositoryTypes.add(retriever.getRepositoryType());
				if (reportContent != null) {
					reportExecution = new ReportExecution(reportContent, reportName, reportFilename, format, arguments, parameterLabels);
					break;
				}
			}
			if (reportExecution == null) {
				LOGGER.error(String.format(
						"Report '%s' should be available pre-generated as '%s', but wasn't. Tried to retrieve from %s. Report parameters were [%s]",
						reportName, reportFilename, Joiner.on(" and ").join(triedRepositoryTypes),
						Joiner.on(',').withKeyValueSeparator("=").join(argumentsCopy)));
			}
		}

		if (reportExecution == null) {
			JasperExecution jasperExecution = jasperExecution(userData, reportUri, format, argumentsCopy, reportTemplate);
			if (resultMustBePolled(reportTemplate, jasperExecution)) {
				reportExecution = unfinishedReport(argumentsCopy, format, parameterLabels, reportFilename, reportName, jasperExecution);
			} else {
				reportExecution = finishedReport(argumentsCopy, jasperExecution.getExportId(), parameterLabels, reportFilename, reportName, jasperExecution,
						format, userData.getJavaLocale().toString());
			}
		}
		return reportExecution;
	}

	private List<PregeneratedContentRetriever> orderedContentRetrievers() {
		List<PregeneratedContentRetriever> canonicalRetrievers = new ArrayList<>();
		for (String pregeneratedReportSource : retrievePregensFrom) {
			for (PregeneratedContentRetriever retriever : pregeneratedContentRetrievers) {
				if (retriever.getRepositoryType().equals(pregeneratedReportSource)) {
					canonicalRetrievers.add(retriever);
				}
			}
		}
		return canonicalRetrievers;
	}

	ReportExecution pollReportExecution(UserData userData, ReportExecution reportInProgress) {
		Response response = (Response) executeOnJasperServer(() -> jasperRestApiNoTimeout.getReportExecutionStatus(reportInProgress.getRequestId()));
		if (response == null) {
			throw new EvoteException(format(
					"Report execution failed, received null Response from server based on requestId %s",
					reportInProgress.getRequestId()));
		}
		if (response.getStatus() == SC_OK) {
			switch (executionStatus(reportInProgress, response)) {
			case READY:
				return reportContent(userData, reportInProgress);
			case EXECUTION:
				return reportInProgress;
			case FAILED:
			default:
				throw new EvoteException("Report execution failed");
			}
		} else {
			Response.StatusType statusInfo = response.getStatusInfo();
			throw new EvoteException(format(
					"Report execution failed, received status %d from report server. Reason: %s",
					response.getStatus(),
					statusInfo != null ? statusInfo.getReasonPhrase() : "<Unknown>"));
		}
	}

	private Object executeOnJasperServer(JasperServerCall call) {
		try {
			return call.execute();
		} catch (ProcessingException e) {
			throw new EvoteException(ERROR_CODE_0590_REPORT_SERVER_ERROR, new Exception(e.getMessage()));
		}
	}

	private ReportExecution reportContent(UserData userData, ReportExecution reportInProgress) {
		Response reportContentResponse = (Response) executeOnJasperServer(
				() -> jasperRestApiNoTimeout.getReportOutput(reportInProgress.getRequestId(), reportInProgress.getExportId(), userData
						.getJavaLocale()
						.toString()));
		if (reportContentResponse.getStatus() == SC_OK) {
			return new ReportExecution(reportInProgress, reportContentResponse.readEntity(byte[].class));
		} else {
			throw new EvoteException("Failed to get report content: " + reportContentResponse.getStatus());
		}
	}

	private ReportExecutionStatus.Status executionStatus(ReportExecution reportInProgress, Response response) {
		ReportExecutionStatus.Status status = null;
		String contentType = response.getHeaderString("Content-Type");
		if (contentType == null) {
			throw new EvoteException(format(
					"Report execution failed, received Response from server based on requestId %s with missing Content-Type",
					reportInProgress.getRequestId()));
		}
		switch (contentType) {
		case "application/status+xml":
			ReportExecutionStatus reportExecutionStatus = response.readEntity(ReportExecutionStatus.class);
			if (reportExecutionStatus != null) {
				status = reportExecutionStatus.getValue();
				if (status != null && status == FAILED) {
					throw new EvoteException(format(
							"Report execution failed: %s: %s",
							reportExecutionStatus.getErrorDescriptor().getErrorCode(),
							reportExecutionStatus.getErrorDescriptor().getMessage()));
				}
			}
			break;
		case MediaType.APPLICATION_XML:
			status = response.readEntity(ReportExecutionStatus.Status.class);
			break;
		default:
			throw new EvoteException(format("Unknown Content-Type received from report server: %s", contentType));
		}
		if (status == null) {
			throw new EvoteException(format(
					"Report execution failed, received Response from server based on requestId %s with missing ReportExecutionStatus",
					reportInProgress.getRequestId()));
		}
		return status;
	}

	private Collection<ReportTemplate> getCachedAllAvailableReports() {
		try {
			Collection<ReportTemplate> templates = AVAILABLE_REPORT_TEMPLATE_CACHE.get(AVAILABLE_REPORT_TEMPLATE_CACHE, this::getAllAvailableReports);
			if (templates.isEmpty()) {
				AVAILABLE_REPORT_TEMPLATE_CACHE.invalidateAll();
			}
			return templates;
		} catch (ExecutionException e) {
			return Collections.emptyList();
		} catch (UncheckedExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof EvoteException) {
				throw (EvoteException) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * @return a list of values for desired report parameter, based on the parameter's parent's value
	 */
	Collection<SelectableReportParameterValue> getSelectableValuesForParameter(final UserData userData, final ReportParameter reportParameter,
																			   String reportUri) {
		final String parameterId = reportParameter.getId();
		Collection<SelectableReportParameterValue> values = new ArrayList<>();
		boolean parentValueIsDefined = parentValueIsDefined(reportParameter);
		if (!parentValueIsDefined || (reportParameter.getParent().isFixed() && !parentValueIsDefined(reportParameter.getParent())) || null == parameterId) {
			return values;
		}
		if (reportParameter.isFixed()) {
			values.add(new SelectableReportParameterValue(reportParameter.getDefaultValue().toString(), reportParameter.getLabel()));
			return values;
		}
		try {
			switch (parameterId) {

			case ELECTION_GROUP:
				getSelectableValuesForElectionGroup(reportParameter, values);
				break;

			case ELECTION:
				getSelectableValuesForElection(reportParameter, values, userData);
				break;

			case CONTEST:
				getSelectableValuesForContest(reportParameter, values);
				break;

			case CONTEST_PK:
				getSelectableValuesForContestPk(values, userData, CONFINE_CONTESTS_TO_USER_AREA_LEVEL);
				break;

			case CONTEST_PK_COUNTY_OR_MUN:
				getSelectableValuesForContestPk(values, userData, false);
				break;

			case COUNTRY:
				getSelectableValuesForCountry(reportParameter, values);
				break;

			case COUNTY:
				getSelectableValuesForCounty(reportParameter, values);
				break;

			case MUNICIPALITY:
				getSelectableValuesForMunicipality(reportParameter, values);
				break;

			case BOROUGH:
			case BOROUGH_WHICH_CAN_BE_EMPTY:
				getSelectableValuesForBorough(reportParameter, values);
				break;

			case POLLING_DISTRICT:
				getSelectableValuesForPollingDistrict(reportParameter, values);
				break;

			case POLLING_PLACE:
				getSelectableValuesForPollingPlace(reportParameter, values);
				break;
			default:
			}
		} catch (ExecutionException ex) {
			throw new EvoteException(ex.getMessage(), ex);
		}
		removeUnselectableParameterValues(userData, reportUri, parameterId, values);
		return values;
	}

	private boolean parentValueIsDefined(ReportParameter reportParameter) {
		return reportParameter.getParentValue() != null && isNotBlank(reportParameter.getParentValue().toString());
	}

	private void removeUnselectableParameterValues(UserData userData, String reportUri, String parameterId, Collection<SelectableReportParameterValue> values) {
		try {
			Map<Pair<String, String>, InselectableParameterValueForMvArea> unselectableParameterValues = getReportTemplateFromCache(userData, reportUri)
					.getUnselectableParameterValues();
			if (unselectableParameterValues != null) {
				for (Iterator<SelectableReportParameterValue> iterator = values.iterator(); iterator.hasNext();) {
					SelectableReportParameterValue parameterValue = iterator.next();
					if (parameterValue.getValueId() != null
							&& unselectableParameterValues.get(Pair.of(parameterId, parameterValue.getValueId())) != null
							&& unselectableParameterValues.get(Pair.of(parameterId, parameterValue.getValueId()))
									.isUnselectableForValueAndUserPath(userData.getOperatorAreaPath().path())) {
						iterator.remove();
					}
				}
			}
		} catch (ExecutionException e) {
			throw new EvoteException("Failed retrieveing report template " + reportUri, e);
		}
	}

	private ReportTemplate reportTemplate(UserData userData, String reportUri) {
		ReportTemplate reportTemplate;
		try {
			reportTemplate = getReportTemplateFromCache(userData, reportUri);
		} catch (ExecutionException e) {
			throw new EvoteException("failed to retrieve report template", e);
		}
		return reportTemplate;
	}

	private ReportExecution finishedReport(Map<String, String> parameters, String exportID, Map<String, String> parameterLabels, String reportFilename,
			String reportName, JasperExecution jasperExecution, String format, String locale) {
		Response response = (Response) executeOnJasperServer(() -> jasperRestApiNoTimeout.getReportOutput(jasperExecution.getRequestId(), exportID, locale));
		return new ReportExecution(jasperExecution.getRequestId(), response.readEntity(byte[].class), reportName, reportFilename, format, parameters,
				parameterLabels);
	}

	private ReportExecution unfinishedReport(Map<String, String> parameters, String format, Map<String, String> parameterLabels, String reportFilename,
			String reportName, JasperExecution jasperExecution) {
		return new ReportExecution(jasperExecution.getRequestId(), jasperExecution.getExportId(), reportName, reportFilename, format, parameters,
				parameterLabels);
	}

	private boolean resultMustBePolled(ReportTemplate reportTemplate, JasperExecution jasperExecution) {
		return reportTemplate.isAsync() && !jasperExecution.isReady();
	}

	private JasperExecution jasperExecution(UserData userData, String reportUri, String format, Map<String, String> parametersCopy,
			ReportTemplate reportTemplate) {
		return (JasperExecution) executeOnJasperServer(() -> jasperRestApiNoTimeout
				.executeReport(
						createJasperExecutionRequest(reportUri, parametersCopy, reportTemplate).as(format).async(reportTemplate.isAsync()),
						resolveLocale(userData, parametersCopy)));
	}

	private String reportFilename(UserData userData, Map<String, String> parameters, String format, ReportTemplate reportTemplate) {
		return replace(getMessage(reportTemplate.getFilenamePattern(), userData.getLocale()) + "." + format, parameters);
	}

	private String getMessage(String filenamePattern, no.valg.eva.admin.configuration.domain.model.Locale locale) {
		LocaleText localeText = localeTextRepository.findGlobalByLocaleAndTextId(locale.getPk(), filenamePattern);
		if (localeText == null) {
			return filenamePattern;
		}
		return localeText.getLocaleText();
	}

	private String reportName(UserData userData, ReportTemplate reportTemplate) {
		return getMessage(reportTemplate.getReportName(), userData.getLocale());
	}

	private String resolveLocale(UserData userData, Map<String, String> parameters) {
		Municipality municipality = findMunicipalityFromReportParameters(parameters);
		if (municipality != null) {
			return municipality.getLocale().toJavaLocale().toString();
		}

		County county = findCountyFromReportParameters(parameters);
		if (county != null) {
			return county.getLocale().toJavaLocale().toString();
		}

		municipality = userData.getOperatorMvArea().getMunicipality();
		if (municipality != null) {
			return municipality.getLocale().toJavaLocale().toString();
		}

		return userData.getJavaLocale().toString();
	}

	private Municipality findMunicipalityFromReportParameters(Map<String, String> parameters) {
		if (parameters.keySet().containsAll(PARAMETERS_FOR_PATH_TO_MUNICIPALITY)) {
			AreaPath municipalityPath = areaPathFrom(parameters);
			return mvAreaRepository.findSingleByPath(municipalityPath).getMunicipality();
		}
		return null;
	}

	private County findCountyFromReportParameters(Map<String, String> parameters) {
		if (parameters.keySet().containsAll(PARAMETERS_FOR_PATH_TO_COUNTY)) {
			AreaPath countyPath = areaPathFrom(parameters);
			return mvAreaRepository.findSingleByPath(countyPath).getCounty();
		}
		return null;
	}

	private void getSelectableValuesForContestPk(Collection<SelectableReportParameterValue> values, UserData userData, boolean confineContestsToUsersAreaLevel)
			throws ExecutionException {
		List<MvElection> contestsForElectionAndArea = getCachedContestForArea(userData.getOperatorElectionPath(), userData.getOperatorAreaPath());
		if (confineContestsToUsersAreaLevel) {
			contestsForElectionAndArea = confineMvElectionsToRelevantContestsForOperator(userData, contestsForElectionAndArea);
		}
		contestsForElectionAndArea = groupContestsByElectionAndSortWithinGroups(contestsForElectionAndArea);
		addContestValues(values, contestsForElectionAndArea);
	}

	private void addContestValues(Collection<SelectableReportParameterValue> values, List<MvElection> contestsForElectionAndArea) {
		boolean groupByElection = extractElectionIds(contestsForElectionAndArea).size() > 1;
		String currentElectionId = null;
		for (MvElection mvElection : contestsForElectionAndArea) {
			Contest contest = mvElection.getContest();
			if (groupByElection && (currentElectionId == null || !currentElectionId.equals(contest.getElection().getId()))) {
				values.add(new GroupSeparator(contest.getElection().getName()));
			}
			values.add(new SelectableReportParameterValue(String.valueOf(contest.getPk()), contest.getName()));
			currentElectionId = contest.getElection().getId();
		}
	}

	private Set<String> extractElectionIds(List<MvElection> contestsForElectionAndArea) {
		return new HashSet<>(Lists.transform(contestsForElectionAndArea, MvElection::getElectionId));
	}

	private List<MvElection> groupContestsByElectionAndSortWithinGroups(List<MvElection> contestsForElectionAndArea) {
		contestsForElectionAndArea.sort(
				(mve1, mve2) -> new CompareToBuilder().append(mve1.getElectionId(), mve2.getElectionId()).append(mve1.getContestName(), mve2.getContestName())
				.toComparison());
		return contestsForElectionAndArea;
	}

	private List<MvElection> confineMvElectionsToRelevantContestsForOperator(UserData userData, List<MvElection> contestsForElectionAndArea) {
		if (userData.getOperatorAreaLevel() == AreaLevelEnum.MUNICIPALITY) {
			return newArrayList(Iterables.filter(contestsForElectionAndArea,
					input -> input.getAreaLevel() == AreaLevelEnum.MUNICIPALITY.getLevel() || input.getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel()));
		}
		if (userData.getOperatorAreaLevel() == AreaLevelEnum.COUNTY) {
			return newArrayList(Iterables.filter(contestsForElectionAndArea, input -> input.getAreaLevel() == AreaLevelEnum.COUNTY.getLevel()));
		}
		return contestsForElectionAndArea;
	}

	private List<MvElection> getCachedContestForArea(final ElectionPath electionPath, final AreaPath areaPath) throws ExecutionException {
		return mvElectionRepository.findContestsForElectionAndArea(electionPath, areaPath);
	}

	private void getSelectableValuesForContest(ReportParameter reportParameter, Collection<SelectableReportParameterValue> values) throws ExecutionException {
		getSelectableValuesForContest(reportParameter, values, NONE);
	}

	private List<ReportTemplate> getAllAvailableReports() {
		return (List<ReportTemplate>) executeOnJasperServer(() -> new ArrayList<>(filter(Collections2.transform(
				fromNullable(jasperRestApiWithTimeout.getResources(reportUnit, "/reports/EVA")).or(JasperResources.empty()).getResources(),
				reportTemplateToJasperReport()), EXCLUDE_HIDDEN_REPORT_TEMPLATES)));
	}

	private Function<JasperReport, ReportTemplate> reportTemplateToJasperReport() {
		return jasperReport -> getReportTemplate(jasperReport, new ArrayList<ReportParameter>());
	}

	private String makeUniqueCacheKey(String parentId, String objectId) {
		return parentId + ':' + objectId;
	}

	private ReportTemplate cloneAndPrepareTemplateWithParameterRelationshipsAndValues(final UserData userData, final ReportTemplate reportTemplate) {
		try {
			return reportTemplate
					.clone()
					.withRelationships(reportTemplate, AREA_PATH_PARAMETERS_EMPTY_BOROUGH, AREA_PARAMETER_PATH_ORDER)
					.withRelationships(reportTemplate, AREA_PATH_PARAMETERS, AREA_PARAMETER_PATH_ORDER)
					.withRelationships(reportTemplate, ELECTION_PATH_PARAMETERS, ELECTION_PARAMETER_PATH_ORDER)
					.withRelationships(reportTemplate, ELECTION_PATH_PARAMETERS_CONTEST_PK, ELECTION_PARAMETER_PATH_ORDER)
					.withRelationships(reportTemplate, ELECTION_PATH_PARAMETERS_CONTEST_AT_COUNTY_OR_MUNICIPALITY_PK, ELECTION_PARAMETER_PATH_ORDER)
					.withParameterValues(
							inferParameterValues(userData.getOperatorRole().getMvArea().getAreaPath(), AREA_PATH_PARAMETERS, userData,
									reportTemplate.getParameters(), reportTemplate.getReportUri()))
					.withParameterValues(
							inferParameterValues(userData.getOperatorRole().getMvArea().getAreaPath(), AREA_PATH_PARAMETERS_EMPTY_BOROUGH, userData,
									reportTemplate.getParameters(), reportTemplate.getReportUri()))
					.withParameterValues(
							inferParameterValues(userData.getOperatorRole().getMvElection().getElectionPath(), ELECTION_PATH_PARAMETERS, userData,
									reportTemplate.getParameters(), reportTemplate.getReportUri()))
					.withParameterValues(
							inferParameterValues(userData.getOperatorRole().getMvElection().getElectionPath(), ELECTION_PATH_PARAMETERS_CONTEST_PK, userData,
									reportTemplate.getParameters(), reportTemplate.getReportUri()))
					.withParameterValues(
							inferParameterValues(userData.getOperatorRole().getMvElection().getElectionPath(),
									ELECTION_PATH_PARAMETERS_CONTEST_AT_COUNTY_OR_MUNICIPALITY_PK, userData,
									reportTemplate.getParameters(), reportTemplate.getReportUri()));
		} catch (CloneNotSupportedException e) {
			throw new EvoteException(e.getMessage(), e);
		}
	}

	private ReportTemplate loadReportTemplate(final String reportUri) {
		ReportTemplate reportTemplate;
		JasperReport resource = (JasperReport) executeOnJasperServer(() -> jasperRestApiWithTimeout.getJasperReportUnit(reportUri));
		List<ReportParameter> parameters = new ArrayList<>(Collections2.transform(resource.getInputControls(),
				inputControl -> {
					InputControl control = (InputControl) executeOnJasperServer(() -> jasperRestApiWithTimeout.getInputControl(inputControl.getUri()));
					DataType dataType = (DataType) executeOnJasperServer(
							() -> jasperRestApiWithTimeout.getDataType(control.getDataTypeReference().getUri()));
					String uri = control.getUri();
					ReportParameter reportParameter = new ReportParameter(
							lastPathElement(uri),
							control.getLabel(),
							control.getDescription(),
							dataType.getBaseType().toString());
					// all path-based parameters are mandatory
					if (PARAMETERS_NAMES_OF_PATH_PARAMETERS.contains(reportParameter.getId())) {
						reportParameter.setMandatory(true);
					}
					return reportParameter;
				}));
		sortParametersByPathLength(parameters);
		reportTemplate = getReportTemplate(resource, parameters);
		return reportTemplate;
	}

	private String parameterLabelForId(UserData userData, ReportParameter parameter) {
		String translatedLabel = getMessage(REPORT_PARAMETER_LABEL_PREFIX + parameter.getLabel(), userData.getLocale());
		return translatedLabel != null ? translatedLabel : parameter.getLabel();
	}

	private ReportTemplate getReportTemplateFromCache(UserData userData, final String reportUri) throws ExecutionException {
		try {
			return REPORT_TEMPLATE_CACHE.get(makeUniqueCacheKey(userData.getOperatorRole().getMvArea().getAreaPath(), reportUri),
					() -> loadReportTemplate(reportUri));
		} catch (UncheckedExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof EvoteException) {
				throw (EvoteException) e.getCause();
			}
			throw e;
		}

	}

	private boolean checkAccessDenied(final AreaLevelEnum userAreaLevel, final AreaLevelEnum reportAreaLevel) {
		boolean accessDenied = false;
		switch (reportAreaLevel) {
		case ROOT:
		case COUNTRY:
			if (!ROOT_AREA_LEVEL.contains(userAreaLevel)) {
				accessDenied = true;
			}
			break;
		case COUNTY:
			if (!COUNTY_LEVELS.contains(userAreaLevel)) {
				accessDenied = true;
			}
			break;
		case MUNICIPALITY:
			if (!MUNICIPALITY_LEVELS.contains(userAreaLevel)) {
				accessDenied = true;
			}
			break;
		case POLLING_DISTRICT:
		case POLLING_PLACE:
			if (!LOWEST_LEVELS.contains(userAreaLevel)) {
				accessDenied = true;
			}
			break;
		default:
		}
		return accessDenied;
	}

	private void fillReportArgumentsFromUserData(final Map<String, String> parametersCopy, final UserData userData, String reportUri) {
		for (Map.Entry<String, Object> preFilledParameterEntry : getPreFilledParameterValues(userData.getOperatorRole().getMvArea().getAreaPath(),
				AREA_PATH_PARAMETERS, userData, reportUri).entrySet()) {
			parametersCopy.put(preFilledParameterEntry.getKey(), preFilledParameterEntry.getValue().toString());
		}
		for (Map.Entry<String, Object> preFilledParameterEntry : getPreFilledParameterValues(userData.getOperatorRole().getMvArea().getAreaPath(),
				AREA_PATH_PARAMETERS_EMPTY_BOROUGH, userData, reportUri).entrySet()) {
			parametersCopy.put(preFilledParameterEntry.getKey(), preFilledParameterEntry.getValue().toString());
		}
		for (Map.Entry<String, Object> preFilledParameterEntry : getPreFilledParameterValues(userData.getOperatorRole().getMvElection().getElectionPath(),
				ELECTION_PATH_PARAMETERS, userData, reportUri).entrySet()) {
			parametersCopy.put(preFilledParameterEntry.getKey(), preFilledParameterEntry.getValue().toString());
		}
		if (!parametersCopy.containsKey(REPORT_LOCALE)) {
			parametersCopy.put(REPORT_LOCALE, actualLocaleWithElectionEventIdAsVariant(parametersCopy, userData));
		}
	}

	private String actualLocaleWithElectionEventIdAsVariant(Map<String, String> parametersCopy, UserData userData) {
		return resolveLocale(userData, parametersCopy) + "_" + userData.getElectionEventId();
	}

	private JasperExecutionRequest createJasperExecutionRequest(final String reportUri, final Map<String, String> reportArguments, ReportTemplate reportTemplate) {
		Map<String, ReportParameter> parameterMap = reportTemplate.getParameters().stream().collect(toMap(ReportParameter::getId, identity()));
		return new JasperExecutionRequest(reportUri, newArrayList(transform(reportArguments.entrySet(), entry -> reportParameter(parameterMap, entry))));
	}

	private JasperExecutionRequest.ReportParameter reportParameter(Map<String, ReportParameter> parameterMap, Map.Entry<String, String> entry) {
		String name = entry.getKey();
		ReportParameter reportParameter = parameterMap.get(name);
		return reportParameter(name, entry.getValue(), reportParameter);
	}

	private JasperExecutionRequest.ReportParameter reportParameter(String name, String value, ReportParameter reportParameter) {
		validateParameter(value, reportParameter);
		return new JasperExecutionRequest.ReportParameter(name, newArrayList(value));
	}

	private void validateParameter(String value, ReportParameter reportParameter) {
		if (numberWhoShouldBeValidated(reportParameter, value)) {
			validateNumber(reportParameter, value);
		}
	}

	private boolean numberWhoShouldBeValidated(ReportParameter reportParameter, String value) {
		return reportParameter != null && reportParameter.isNumber() && isNotEmpty(value);
	}

	private void validateNumber(ReportParameter reportParameter, String value) {
		if (isNotNumber(value)) {
			throw new ValidateException("@rapport.error.parameter.invalid", reportParameter.getId(), value);
		}
	}

	private boolean isNotNumber(String value) {
		return !isNumber(value);
	}

	private boolean deniedAccessBasedOnUserAreaLevel(final UserData userData, final ReportTemplate reportTemplate) {
		boolean deniedAccessBasedOnAreaLevel = false;
		Set<Integer> reportAreaLevels = reportTemplate.getAreaLevels();
		if (reportAreaLevels != null && !reportAreaLevels.isEmpty()) {
			deniedAccessBasedOnAreaLevel = true;
			for (Integer reportAreaLevel : reportAreaLevels) {
				deniedAccessBasedOnAreaLevel &= checkAccessDenied(
						userData.getOperatorRole().getMvArea().getActualAreaLevel(),
						AreaLevelEnum.getLevel(reportAreaLevel));
			}
		}
		return deniedAccessBasedOnAreaLevel;
	}

	private void getSelectableValuesForPollingPlace(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final PollingDistrict pollingDistrict = getPollingDistrict(reportParameter);
		List<PollingPlace> pollingPlaces = getCachedPollingPlaces(pollingDistrict);
		values.addAll(Lists.transform(pollingPlaces, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private void getSelectableValuesForPollingDistrict(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final Borough borough = getBorough(reportParameter);
		List<PollingDistrict> pollingDistricts = getCachedPollingDistricts(borough);
		values.addAll(Lists.transform(
				filterOutTechnicalPollingDistricts(pollingDistricts), // Technical polling districts are never used in reports, so we will filter them out
				input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private List<PollingDistrict> filterOutTechnicalPollingDistricts(List<PollingDistrict> pollingDistricts) {
		Collection<PollingDistrict> filteredPollingDistricts = filter(pollingDistricts,
				pollingDistrict -> !pollingDistrict.isTechnicalPollingDistrict());
		return new ArrayList<>(filteredPollingDistricts);
	}

	private void getSelectableValuesForBorough(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final Municipality municipality = getMunicipality(reportParameter);
		List<Borough> boroughs = getCachedBoroughs(municipality);
		values.addAll(Lists.transform(boroughs, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private void getSelectableValuesForMunicipality(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final County county = getCounty(reportParameter);
		List<Municipality> municipalities = getCachedMunicipalities(county);
		values.addAll(Lists.transform(municipalities, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private void getSelectableValuesForCounty(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final Country country = getCountry(reportParameter);
		List<County> counties = getCachedCounties(country);
		values.addAll(Lists.transform(counties, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private void getSelectableValuesForCountry(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		List<Country> countries = getCachedCountries(getElectionEvent(reportParameter));
		values.addAll(Lists.transform(countries, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private void getSelectableValuesForContest(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values, final AreaLevelEnum actualAreaLevel) throws ExecutionException {
		final Election election = getElection(reportParameter);
		List<Contest> contests = getCachedContests(election);
		contests = newArrayList(filter(contests, contestAreaLevelFilter(actualAreaLevel)));
		values.addAll(Lists.transform(contests, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private Predicate<Contest> contestAreaLevelFilter(final AreaLevelEnum actualAreaLevel) {
		return input -> actualAreaLevel.equals(NONE) || Lists.transform(input.getContestAreaList(), AREAL_EVEL_FROM_CONTEST_AREA).contains(actualAreaLevel);
	}

	private void getSelectableValuesForElection(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values, UserData userData) throws ExecutionException {
		final ElectionGroup electionGroup = getElectionGroup(reportParameter);
		List<Election> elections = getCachedElections(electionGroup);
		List<Election> electionsFiltered = removeElectionsWhichDoNotHaveAvailableContestsForUserOperatorAreaPath(elections, userData);
		values.addAll(Lists.transform(electionsFiltered, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private List<Election> removeElectionsWhichDoNotHaveAvailableContestsForUserOperatorAreaPath(List<Election> elections, UserData userData)
			throws ExecutionException {

		List<Election> electionList = new LinkedList<>(elections);
		for (Iterator<Election> iteratorElection = electionList.iterator(); iteratorElection.hasNext();) {
			Election election = iteratorElection.next();
			ValgSti valgSti = ValghierarkiSti.valgSti(election.electionPath());
			if (!mvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valgSti, userData.operatorValggeografiSti())) {
				iteratorElection.remove();
			}
		}
		return electionList;
	}

	private void getSelectableValuesForElectionGroup(final ReportParameter reportParameter,
			final Collection<SelectableReportParameterValue> values) throws ExecutionException {
		final ElectionEvent electionEvent = getElectionEvent(reportParameter);
		List<ElectionGroup> electionGroupsSorted = getCachedElectionGroups(electionEvent);
		values.addAll(Lists.transform(electionGroupsSorted, input -> new SelectableReportParameterValue(input.getId(), input.getName())));
	}

	private List<PollingPlace> getCachedPollingPlaces(final PollingDistrict pollingDistrict) throws ExecutionException {
		return pollingPlaceRepository.findByPollingDistrict(pollingDistrict.getPk());
	}

	private List<PollingDistrict> getCachedPollingDistricts(final Borough borough) throws ExecutionException {
		return pollingDistrictRepository.findPollingDistrictsForBorough(borough);
	}

	private List<Borough> getCachedBoroughs(final Municipality municipality) throws ExecutionException {
		return boroughRepository.findByMunicipality(municipality.getPk());
	}

	private List<Municipality> getCachedMunicipalities(final County county) throws ExecutionException {
		return municipalityRepository.findByCounty(county.getPk());
	}

	private List<County> getCachedCounties(final Country country) throws ExecutionException {
		return countyRepository.getCountiesByCountry(country.getPk());
	}

	private List<Election> getCachedElections(final ElectionGroup electionGroup) throws ExecutionException {
		return electionRepository.findElectionsByElectionGroup(electionGroup.getPk());
	}

	private List<Contest> getCachedContests(final Election election) throws ExecutionException {
		return contestRepository.findByElectionPk(election.getPk());
	}

	private List<Country> getCachedCountries(final ElectionEvent electionEvent) throws ExecutionException {
		return countryRepository.getCountriesForElectionEvent(electionEvent.getPk());
	}

	private List<ElectionGroup> getCachedElectionGroups(final ElectionEvent electionEvent) throws ExecutionException {
		return electionGroupRepository.getElectionGroupsSorted(electionEvent.getPk());
	}

	private Election getElection(final ReportParameter reportParameter) throws ExecutionException {
		final ElectionGroup electionGroup = getElectionGroup(reportParameter);
		final String electionId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, ELECTION));
		return electionRepository.findElectionByElectionGroupAndId(electionGroup.getPk(), electionId);
	}

	private ElectionGroup getElectionGroup(final ReportParameter reportParameter) throws ExecutionException {
		final ElectionEvent electionEvent = getElectionEvent(reportParameter);
		final String electionGroupId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, ELECTION_GROUP));
		return electionGroupRepository.findElectionGroupById(electionEvent.getPk(), electionGroupId);
	}

	private PollingDistrict getPollingDistrict(final ReportParameter reportParameter) throws ExecutionException {
		final Borough borough = getBorough(reportParameter);
		final String pollingDistrictId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, POLLING_DISTRICT));
		return pollingDistrictRepository.findPollingDistrictById(borough.getPk(), pollingDistrictId);
	}

	private Borough getBorough(final ReportParameter reportParameter) throws ExecutionException {
		final Municipality municipality = getMunicipality(reportParameter);
		final String boroughId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, BOROUGH, BOROUGH_WHICH_CAN_BE_EMPTY));
		return boroughRepository.findBoroughById(municipality.getPk(), boroughId);
	}

	private Municipality getMunicipality(final ReportParameter reportParameter) throws ExecutionException {
		final County county = getCounty(reportParameter);
		final String municipalityId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, MUNICIPALITY));
		return municipalityRepository.findMunicipalityById(county.getPk(), municipalityId);
	}

	private County getCounty(final ReportParameter reportParameter) throws ExecutionException {
		final Country country = getCountry(reportParameter);
		final String countyId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, COUNTY));
		return countyRepository.findCountyById(country.getPk(), countyId);
	}

	private Country getCountry(final ReportParameter reportParameter) throws ExecutionException {
		final ElectionEvent electionEvent = getElectionEvent(reportParameter);
		final String countryId = getParameterValueThroughParenthood(findAncestorReportParameter(reportParameter, COUNTRY));
		return countryService.findCountryById(electionEvent.getPk(), countryId);
	}

	private ElectionEvent getElectionEvent(final ReportParameter reportParameter) throws ExecutionException {
		ReportParameter electionEventParameter = findAncestorReportParameter(reportParameter, ELECTION_EVENT);
		final String electionEventId = getParameterValueThroughParenthood(electionEventParameter);
		return electionEventRepository.findById(electionEventId);
	}

	private String getParameterValueThroughParenthood(final ReportParameter electionEventParameter) {
		return String.valueOf(electionEventParameter.getDependentParameters().iterator().next()
				.getParentValue());
	}

	private ReportParameter findAncestorReportParameter(final ReportParameter reportParameter, final String... parameterId) {
		Set<String> parameterIds = new HashSet<>(Arrays.asList(parameterId));
		ReportParameter currentParameter = reportParameter;
		while (currentParameter != null && !parameterIds.contains(currentParameter.getId())) {
			currentParameter = currentParameter.getParent();
		}
		return currentParameter;
	}

	private ReportTemplate getReportTemplate(final JasperReport jasperReport, final List<ReportParameter> parameters) {
		String reportName = jasperReport.getLabel();
		String reportUri = jasperReport.getUri();
		JasperReport resource = (JasperReport) executeOnJasperServer(() -> jasperRestApiWithTimeout.getJasperReportUnit(reportUri));
		List<FileResource> resources = resource.getResources();
		String filenamePattern = reportName;
		String description = "";
		Set<String> fileFormats = new HashSet<>(newArrayList(DEFAULT_FORMAT.name()));
		Set<Integer> areaLevels = new HashSet<>();
		String areaPathMask = null;
		boolean hiddenReport = false;
		boolean async = false;
		boolean mayBePreGenerated = false;
		Map<Pair<String, String>, InselectableParameterValueForMvArea> inSelectableParameterValues = new HashMap<>();
		for (FileResource fileResource : resources) {
			if ("metaData".equals(fileResource.getName())) {
				ReportMetaData metaData = (ReportMetaData) executeOnJasperServer(
						() -> jasperRestApiWithTimeout.getReportMetaData(fileResource.getFileReference().getUri()));
				reportName = metaData.getReportName();
				filenamePattern = metaData.getFilenamePattern();
				description = metaData.getDescription();
				hiddenReport = metaData.isHidden();
				async = TRUE.equals(metaData.getAsync());
				mayBePreGenerated = TRUE.equals(metaData.getRunNightly());
				List<ReportMetaData.Format> supportedFileFormats = metaData.getFormats();
				if (!supportedFileFormats.isEmpty()) {
					fileFormats.clear();
					for (ReportMetaData.Format format : supportedFileFormats) {
						fileFormats.add(format.toString());
					}
				}
				if (metaData.getAreaLevels() != null) {
					for (ReportMetaData.AreaLevel level : metaData.getAreaLevels()) {
						areaLevels.add(level.ordinal());
					}
				}
				if (metaData.getAreaPathMask() != null) {
					areaPathMask = metaData.getAreaPathMask();
				}
				if (metaData.getMandatoryParameters() != null) {
					for (ReportParameter parameter : parameters) {
						if (metaData.getMandatoryParameters().contains(parameter.getId())) {
							parameter.setMandatory(true);
						}
					}
				}
				if (metaData.getUnselectableParameterValues() != null) {
					for (final ReportMetaData.UnselectableParameterValue unselectableParameterValue : metaData.getUnselectableParameterValues()) {
						inSelectableParameterValues.put(Pair.of(unselectableParameterValue.getParameter(), unselectableParameterValue.getValue()),
								new InselectableParameterValueForMvArea(unselectableParameterValue.getUserRoleMvAreaRegExp()));
					}
				}
				if (metaData.getOptionalPathParameter() != null) {
					for (ReportParameter mandatoryParam : parameters) {
						if (mandatoryParam.getId().startsWith(metaData.getOptionalPathParameter())) {
							mandatoryParam.setMandatory(false);
						}
					}
				}
				if (metaData.getFixedParameterValues() != null) {
					for (ReportMetaData.FixedParameterValue fixedParameterValue : metaData.getFixedParameterValues()) {
						boolean parameterIsVisible = false;
						String fixedValue = fixedParameterValue.getValue();
						for (ReportParameter parameter : parameters) {
							if (parameter.getId().equals(fixedParameterValue.getParameter())) {
								parameter.setDefaultValue(fixedValue);
								parameter.setFixed(true);
								parameterIsVisible = true;
							}
						}
						if (!parameterIsVisible) {
							ReportParameter reportParameter = new ReportParameter(fixedParameterValue.getParameter(), DataType.Type.TEXT.name(), fixedValue);
							reportParameter.setLabel(fixedParameterValue.getParameter());
							reportParameter.setFixed(true);
							parameters.add(reportParameter);
						}
					}
				}
			}
		}
		fileFormats.addAll(filter(newArrayList(baseReportFormats.split("[,\\s]+")), FILTER_EMPTY_STRINGS));
		return new ReportTemplate(reportUri, reportName, description, filenamePattern, parameters, newArrayList(fileFormats),
				areaLevels, areaPathMask, hiddenReport, async, mayBePreGenerated, inSelectableParameterValues);
	}

	/**
	 * Infers parameter values from other parameters and their names' position in path structure. E.g. from parameter EE1="201301" in context of structure
	 * ["EE1" (election event), "EE1.CO1" (country)] we can infer parameter EE1.CO1="47", since there is only one country under that election event.
	 *
	 * @param path The value path to infer values from
	 * @param pathParameterNames List of parameter names, in the same order as segments of the value path
	 * @param userData user data, used to extract hierarchical position in Area and Election
	 * @param parameters report parameters
	 * @param reportUri
	 * @return Values from supplied report parameters with additional inferred values
	 */
	Map<String, Object> inferParameterValues(final String path, final String[] pathParameterNames, final UserData userData,
			final Collection<ReportParameter> parameters, String reportUri) {
		// Based on path position, extract path segments as parameter values and store in a parameter name to value map
		String[] pathSegments = path.split("\\.");
		Map<String, Object> preFilledParameters = new HashMap<>();
		for (int i = 0; i < Math.min(pathSegments.length, pathParameterNames.length); i++) {
			// Only prefill on correct level (this method is called with for instance ELECTION_PATH_PARAMETERS_CONTEST_PK)
			String[] pathParameterNamesSegments = pathParameterNames[i].split("\\.");
			if (i == pathParameterNamesSegments.length - 1) {
				preFilledParameters.put(pathParameterNames[i], pathSegments[i]);
			}
		}

		for (ReportParameter parameter : parameters) {
			if (parameter.getParent() != null) {
				Object parentValue = preFilledParameters.get(parameter.getParent().getId());
				if (parentValue != null) {
					parameter.setParentValue(parentValue);
				}
			}
		}

		// Try to extend path further if last past segment is parent to a set of values with only one member e.g. country under election event
		boolean exhausted = false;
		while (!exhausted && preFilledParameters.size() < pathParameterNames.length) {
			// Parameter to determine is the one after the last so far, which has same index as current size of resolved parameters, i.e:
			final String lastParameterName = pathParameterNames[preFilledParameters.size()];
			Optional<ReportParameter> lastReportParameter = tryFind(parameters, withName(lastParameterName));
			if (lastReportParameter.isPresent()) {
				// Manifest the child-parent relationship by transferring the parent parameter's value to current path/parameter as parentValue
				lastReportParameter.get().setParentValue(preFilledParameters.get(lastReportParameter.get().getParent().getId()));

				// Determine which values the path segment/parameter can have
				Collection<SelectableReportParameterValue> selectableValuesForParameter = getSelectableValuesForParameter(userData, lastReportParameter.get(),
						reportUri);

				// If there is only one possible value at this segment, add it to path. Since it's only one, the corresponding parameter
				// should be treated as inferred
				if (selectableValuesForParameter.size() == 1) {
					preFilledParameters.put(lastParameterName, selectableValuesForParameter.iterator().next().getValueId());
					lastReportParameter.get().setInferred(true);
				} else {
					// In this case, however, either there were more than one possible path segment value, and the parameter must be left as
					// undetermined. Or there was none. In both cases, there is no point in examining any more path segments.
					exhausted = true;
				}
			} else {
				// This was the last parameter to check, so we're done
				exhausted = true;
			}
		}
		return preFilledParameters;
	}

	private Predicate<ReportParameter> withName(final String lastParameterName) {
		return input -> lastParameterName.equals(input.getId());
	}

	private void sortParametersByPathLength(final List<ReportParameter> parameters) {
		// Sort the parameters so that the ones with shortest paths comes first. In subsequent handling, parameters whose values depend
		// on parent parameters will be visited in the right order, i.e. after their parents
		parameters.sort(comparingInt(rp -> countMatches(rp.getId(), ".")));
	}

	private Map<String, Object> getPreFilledParameterValues(final String areaPath, final String[] areaPathParameters, final UserData userData,
			String reportUri) {
		List<ReportParameter> emptyList = emptyList();
		return inferParameterValues(areaPath, areaPathParameters, userData, emptyList, reportUri);
	}

	public void refreshResourceBundles() {
		uploadReportTemplatesEventEvent.fire(new RefreshResourceBundlesEvent());
	}

	Map<String, String> getCanonicalReportParameterParentIdMap() {
		return CANONICAL_REPORT_PARAMETER_PARENT_ID_MAP;
	}

	private interface JasperServerCall {
		Object execute();
	}
}

