package no.valg.eva.admin.backend.reporting.jasperserver;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.EvoteConstants.DEFAULT_LOCALE;
import static no.evote.util.EvoteProperties.JASPERSERVER_BASE_URL;
import static no.evote.util.EvoteProperties.JASPERSERVER_CONTEXT;
import static no.evote.util.EvoteProperties.JASPERSERVER_ENABLE_AUTOUPLOAD;
import static no.evote.util.EvoteProperties.JASPERSERVER_NIGHTLY_JOBS_FOR_ELECTION_EVENTS;
import static no.evote.util.EvoteProperties.JASPERSERVER_NIGHTLY_JOBS_START_TIME;
import static no.evote.util.EvoteProperties.JASPERSERVER_PASSWORD;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_HOST;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_PWD;
import static no.evote.util.EvoteProperties.JASPERSERVER_PREGENERATED_FTP_USER;
import static no.evote.util.EvoteProperties.JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM;
import static no.evote.util.EvoteProperties.JASPERSERVER_TEMPLATE_SOURCE_DIR;
import static no.evote.util.EvoteProperties.JASPERSERVER_USERNAME;
import static no.evote.util.EvoteProperties.JASPERSERVER_USE_LIVE_TEMPLATES_INSTEAD_OF_BLANKS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotFoundException;

import no.evote.constants.AreaLevelEnum;
import no.evote.util.EvaConfigProperty;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.application.schedule.RefreshResourceBundlesEvent;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiWithTimeout;
import no.valg.eva.admin.backend.reporting.jasperserver.api.VersionInfo;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.reports.jasper.ReportUploadConfiguration;
import no.valg.eva.admin.reports.jasper.ReportsUploader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

/**
 * A bean that automatically uploads all report templates to jasperServer at starup.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Startup
public class ReportTemplateUploader {
	private static final Logger LOG = Logger.getLogger(ReportTemplateUploader.class);
	public static final boolean ONLY_REFRESH_RESOURCE_BUNDLES = true;
	public static final boolean REFRESH_ALL = false;
	public static final int FIVE_SECONDS = 5000;
	public static final String REPORT_CONFIG_XML = "/ReportConfig.xml";
	public static final String EVA_RESOURCES = "EvaResources";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public static final String EMPTY = "";
	public static final String COMMA_OR_WHITE_SPACE = ",|\\s";

	@Resource
	private TimerService timerService;

	@Inject
	private JasperRestApiWithTimeout jasperRestApi;

	@Inject
	private ElectionEventResourceBundleProvider resourceBundleProvider;

	@Inject
	private MvAreaRepository mvAreaRepository;

	@Inject
	private Event<UploadReportTemplatesDoneEvent> uploadReportTemplatesDoneEventEvent;

	@Inject
	private Event<RefreshResourceBundlesEvent> uploadReportTemplatesEventEvent;

	private String baseUrl;
	private String context;
	private boolean autoUploadEnabled;
	private String commitId;
	private String digest;
	private String templateSourceDir;
	private List<String> nighltyJobsForElectionEvents;
	private boolean doneInitialWork;
	private ReportsUploader reportsUploader;

	
	@Inject
	public ReportTemplateUploader(
			@EvaConfigProperty @Named(JASPERSERVER_USERNAME) final String username,
			@EvaConfigProperty @Named(JASPERSERVER_PASSWORD) final String password,
			@EvaConfigProperty @Named(JASPERSERVER_BASE_URL) final String baseUrl,
			@EvaConfigProperty @Named(JASPERSERVER_CONTEXT) final String context,
			@EvaConfigProperty @Named(JASPERSERVER_ENABLE_AUTOUPLOAD) final boolean enableAutoUpload,
			@Named("commitId") String commitId,
			@Named("reportTemplatesDigest") String digest,
			@EvaConfigProperty @Named(JASPERSERVER_TEMPLATE_SOURCE_DIR) String templateSourceDir,
			@EvaConfigProperty @Named(JASPERSERVER_NIGHTLY_JOBS_FOR_ELECTION_EVENTS) String nightlyJobsForElectionEvents,
			@EvaConfigProperty @Named(JASPERSERVER_NIGHTLY_JOBS_START_TIME) String nightlyStartTime,
			@EvaConfigProperty @Named(JASPERSERVER_USE_LIVE_TEMPLATES_INSTEAD_OF_BLANKS) boolean useLiveTemplates,
			@EvaConfigProperty @Named(JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM) String pregenRepositories,
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_HOST) String ftpHost,
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_USER) String ftpUser,
			@EvaConfigProperty @Named(JASPERSERVER_PREGENERATED_FTP_PWD) String ftpPwd) {
		this.baseUrl = baseUrl;
		this.context = context;
		this.autoUploadEnabled = enableAutoUpload;
		this.commitId = commitId;
		this.digest = digest;
		this.templateSourceDir = templateSourceDir;
		this.nighltyJobsForElectionEvents = newArrayList(filter(asList(nightlyJobsForElectionEvents.split("\\D+")), new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return StringUtils.isNotBlank(input);
			}
		}));
		reportsUploader = new ReportsUploader(baseUrl, username, password, context, nightlyStartTime, templateSourceDir, useLiveTemplates,
				Splitter.onPattern(COMMA_OR_WHITE_SPACE).split(Optional.fromNullable(pregenRepositories).or(EMPTY)), ftpHost, ftpUser, ftpPwd);
	}

	public ReportTemplateUploader() {
	}

	private final AtomicBoolean busy = new AtomicBoolean();
	private final AtomicBoolean uploadRequested = new AtomicBoolean();

	@PostConstruct
	private void init() {
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setInfo("Upload report templates");
		timerConfig.setPersistent(false);
		timerService.createSingleActionTimer(FIVE_SECONDS, timerConfig);
	}

	@Timeout
	public void uploadReportTemplates(Timer timer) {
		if (autoUploadEnabled) {
			LOG.info("Received server startup event: Commencing to upload report templates and resource bundles");
			if (!doneInitialWork) {
				doneInitialWork = true;
				scheduleWork(ifReportTemplatesNeedRefreshing(), REFRESH_ALL, null);
			}
		} else {
			LOG.info("Report templates uploading disabled, skipping.");
		}
	}

	@SecurityNone
	@Asynchronous
	public void consumeRefreshResourceBundlesEvent(@Observes RefreshResourceBundlesEvent refreshResourceBundlesEvent) {
		if (autoUploadEnabled) {
			LOG.info("Received refresh resource bundles event: Commencing to upload resource bundles");
			scheduleWork(ALWAYS, ONLY_REFRESH_RESOURCE_BUNDLES, refreshResourceBundlesEvent.getLastDatabaseLocaleTextTimeStamp());
		}
	}

	@SecurityNone
	public void consumeUploadReportTemplatesEvent(@Observes UploadReportTemplatesEvent event) {
		LOG.info("Received upload report templates event: Commencing to upload report templates and resource bundles");
		scheduleWork(ALWAYS, REFRESH_ALL, null);
	}

	protected void deleteTempFiles(List<Pair<String, File>> resourceBundleArgumentsAndTempFiles) {
		for (Pair<String, File> stringFilePair : resourceBundleArgumentsAndTempFiles) {
			boolean deleteSuccess = stringFilePair.getValue().delete();
			if (!deleteSuccess) {
				LOG.error("Failed to delete temporary file " + stringFilePair.getValue().getAbsolutePath());
			}
		}
	}

	private boolean checkShouldUploadTemplates() {
		boolean shouldUploadReportTemplates = false;
		try {
			VersionInfo versionInfo = jasperRestApi.getVersionInfo();
			String deployedDigest = versionInfo.getDigest();
			if (StringUtils.isNotBlank(deployedDigest)) {
				if (!digest.equals(deployedDigest)) {
					shouldUploadReportTemplates = true;
				}
			} else {
				shouldUploadReportTemplates = !commitId.equals(versionInfo.getCommitId());
			}
		} catch (NotFoundException e) {
			shouldUploadReportTemplates = true;
		} catch (Exception e) {
			LOG.error("Failed to obtain version info from JasperServer (" + baseUrl + context + ")");
			shouldUploadReportTemplates = true;
		}
		return shouldUploadReportTemplates;
	}

	private void scheduleWork(NeedForUpdateAssessor assessor, boolean refreshResourceBundles, DateTime lastDatabaseLocaleTextTimeStamp) {
		if (!busy.get()) {
			if (assessor.shouldUpdate()) {
				try {
					LOG.info("Uploading " + buildArgumentsString(assessor, refreshResourceBundles, lastDatabaseLocaleTextTimeStamp));
					busy.set(true);
					runUploader(refreshResourceBundles, lastDatabaseLocaleTextTimeStamp);
					if (uploadRequested.get()) {
						LOG.info("Commencing with postponed upload " + buildArgumentsString(assessor, refreshResourceBundles, lastDatabaseLocaleTextTimeStamp));
						uploadRequested.set(false);
						runUploader(refreshResourceBundles, lastDatabaseLocaleTextTimeStamp);
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				} finally {
					busy.set(false);
				}
			} else {
				LOG.info("Upload not necessary: skipping");
				// Always upload resource bundle since they are not part of "digest"
				getUploadReportTemplatesEventEvent().fire(new RefreshResourceBundlesEvent());
			}
		} else {
			LOG.info("Already busy uploading: postponing upload " + buildArgumentsString(assessor, refreshResourceBundles, lastDatabaseLocaleTextTimeStamp));
			uploadRequested.set(true);
		}
	}

	private String buildArgumentsString(NeedForUpdateAssessor assessor, boolean refreshResourceBundles, DateTime lastDatabaseLocaleTextTimeStamp) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("needForUpdate=").append(assessor.shouldUpdate()).append(", ");
		sb.append("refreshResourceBundles=").append(refreshResourceBundles).append(", ");
		sb.append("lastDatabaseLocaleTextTimeStamp=").append(DATE_TIME_FORMATTER.print(lastDatabaseLocaleTextTimeStamp));
		sb.append(")");

		return sb.toString();
	}

	protected void runUploader(boolean refreshResourceBundles, DateTime lastDatabaseLocaleTextTimeStamp) {
		String defaultLocale = EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_I18N_DEFAULT_LOCALE, DEFAULT_LOCALE);
		try (InputStream configurationAsStream = getConfigurationAsStream()) {
			ReportUploadConfiguration configuration = new ReportUploadConfiguration(configurationAsStream);
			List<Pair<String, byte[]>> resourceBundles = resourceBundleProvider.resourceBundles(EVA_RESOURCES, defaultLocale);
			List<Map<String, String>> allMunicipalitiesParameters = new ArrayList<>();
			List<Map<String, String>> allCountiesParameters = new ArrayList<>();
			for (String electionEventId : nighltyJobsForElectionEvents) {
				List<MvArea> mvAreasForMunicipalities = mvAreaRepository.findByPathAndLevel(electionEventId, MUNICIPALITY.getLevel());
				allMunicipalitiesParameters.addAll(parametersForAllMvAreas(mvAreasForMunicipalities, MUNICIPALITY));
				List<MvArea> mvAreasForCounties = mvAreaRepository.findByPathAndLevel(electionEventId, COUNTY.getLevel());
				allCountiesParameters.addAll(parametersForAllMvAreas(mvAreasForCounties, COUNTY));
			}
			reportsUploader
					.uploadAllTemplates(configuration, resourceBundles, refreshResourceBundles, lastDatabaseLocaleTextTimeStamp, allMunicipalitiesParameters,
							allCountiesParameters);
			getUploadReportTemplatesDoneEventEvent().fire(new UploadReportTemplatesDoneEvent());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private List<Map<String, String>> parametersForAllMvAreas(List<MvArea> mvAreas, AreaLevelEnum finalLevel) {
		List<Map<String, String>> retVal = new ArrayList<>();
		for (MvArea mvArea : mvAreas) {
			Map<String, String> jrParams = new HashMap<>();
			switch (finalLevel) {
			case MUNICIPALITY:
				jrParams.put("EE1.CO1.CNT1.MUN1", mvArea.getMunicipalityId());
				break;
			case COUNTY:
				jrParams.put("EE1.CO1.CNT1", mvArea.getCountyId());
				break;
			case COUNTRY:
				jrParams.put("EE1.CO1", mvArea.getCountryId());
				break;
			case ROOT:
			default:
				jrParams.put("EE1", mvArea.getElectionEventId());
			}
			retVal.add(jrParams);
		}
		return retVal;
	}

	protected InputStream getConfigurationAsStream() {
		String filePath = null;
		try {
			filePath = templateSourceDir + REPORT_CONFIG_XML;
			return templateSourceDir == null ? getClass().getResourceAsStream(REPORT_CONFIG_XML) : new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Missing reports configuration file " + filePath);
		}
	}

	private NeedForUpdateAssessor ifReportTemplatesNeedRefreshing() {
		return new NeedForUpdateAssessor() {
			@Override
			public boolean shouldUpdate() {
				return checkShouldUploadTemplates();
			}
		};
	}

	interface NeedForUpdateAssessor {
		boolean shouldUpdate();
	}

	public static final NeedForUpdateAssessor ALWAYS = new NeedForUpdateAssessor() {
		@Override
		public boolean shouldUpdate() {
			return true;
		}
	};

	protected Event<UploadReportTemplatesDoneEvent> getUploadReportTemplatesDoneEventEvent() {
		return uploadReportTemplatesDoneEventEvent;
	}

	protected Event<RefreshResourceBundlesEvent> getUploadReportTemplatesEventEvent() {
		return uploadReportTemplatesEventEvent;
	}
}
