package no.evote.util;

import static no.evote.util.EvoteProperties.JASPERSERVER_BASE_REPORT_FORMATS;
import static no.evote.util.EvoteProperties.JASPERSERVER_BASE_URL;
import static no.evote.util.EvoteProperties.JASPERSERVER_CONNECTION_TIMEOUT_SECONDS;
import static no.evote.util.EvoteProperties.JASPERSERVER_CONTEXT;
import static no.evote.util.EvoteProperties.JASPERSERVER_ENABLE_AUTOUPLOAD;
import static no.evote.util.EvoteProperties.JASPERSERVER_ENABLE_AUTO_REPORT_TEMPLATES_UPLOADING;
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
import static no.evote.util.EvoteProperties.TEST_KAN_KLONE_ENDELIGE_TELLINGER;
import static no.evote.util.EvoteProperties.TEST_KAN_LESE_GEOGRAFI;
import static no.evote.util.EvoteProperties.TEST_KAN_REDUSERE_GEOGRAFI;
import static no.evote.util.EvoteProperties.VALGNATT_BASE_URL;
import static no.evote.util.EvoteProperties.VALGNATT_CONNECTION_TIMEOUT_SECONDS;
import static no.evote.util.EvoteProperties.VALGNATT_CONTEXT;
import static no.evote.util.EvoteProperties.getBooleanProperty;
import static no.evote.util.EvoteProperties.getProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * This class injects evote.properties to whomever wants them, thus uncouples the receivers from the {@link no.evote.util.EvoteProperties} and thereby the
 * evote.properties file, which is nice when unit testing.
 */
@ApplicationScoped
public class EvaPropertiesProducer {

	public static final String TRUE = "true";
	public static final String JASPERSERVER = "jasperserver";

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_USERNAME)
	public String getJasperServerUsername() {
		return getProperty(JASPERSERVER_USERNAME, "jasperadmin");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_PASSWORD)
	public String getJasperServerPassword() {
		return getProperty(JASPERSERVER_PASSWORD, "jasperadmin");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_BASE_URL)
	public String getJasperServerBaseUrl() {
		return getProperty(JASPERSERVER_BASE_URL, "http://localhost:8081/");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_CONTEXT)
	public String getJasperServerContext() {
		return getProperty(JASPERSERVER_CONTEXT, JASPERSERVER);
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_ENABLE_AUTOUPLOAD)
	public boolean isReportTemplateUploadToJasperServerEnabled() {
		return "true".equals(getProperty(JASPERSERVER_ENABLE_AUTO_REPORT_TEMPLATES_UPLOADING, "false"));
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_CONNECTION_TIMEOUT_SECONDS)
	public long reportServerConnectionTimeout() {
		return Long.valueOf(getProperty(JASPERSERVER_CONNECTION_TIMEOUT_SECONDS, "1"));
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_TEMPLATE_SOURCE_DIR)
	public String getJasperTemplatesSourceDir() {
		return getProperty(JASPERSERVER_TEMPLATE_SOURCE_DIR, null);
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_NIGHTLY_JOBS_FOR_ELECTION_EVENTS)
	public String getJasperServerCreateNightlyJobsForElectionEvents() {
		return getProperty(JASPERSERVER_NIGHTLY_JOBS_FOR_ELECTION_EVENTS, "");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_NIGHTLY_JOBS_START_TIME)
	public String getJasperServerCreateNightlyJobsStartTime() {
		return getProperty(JASPERSERVER_NIGHTLY_JOBS_START_TIME, "02:00");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_BASE_REPORT_FORMATS)
	public String getJasperServerBaseReportFormats() {
		return getProperty(JASPERSERVER_BASE_REPORT_FORMATS, "");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_USE_LIVE_TEMPLATES_INSTEAD_OF_BLANKS)
	public boolean isUseLiveTemplates() {
		return TRUE.equals(getProperty(JASPERSERVER_USE_LIVE_TEMPLATES_INSTEAD_OF_BLANKS, "false"));
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_PREGENERATED_FTP_HOST)
	public String getJasperServerPregeneratedFtpHost() {
		return getProperty(JASPERSERVER_PREGENERATED_FTP_HOST, "localhost");
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_PREGENERATED_FTP_USER)
	public String getJasperServerPregeneratedFtpUser() {
		return getProperty(JASPERSERVER_PREGENERATED_FTP_USER, JASPERSERVER);
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_PREGENERATED_FTP_PWD)
	public String getJasperServerPregeneratedFtpPwd() {
		return getProperty(JASPERSERVER_PREGENERATED_FTP_PWD, JASPERSERVER);
	}

	@Produces
	@EvaConfigProperty
	@Named(JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM)
	public String getJasperServerRetrievePregeneratedFrom() {
		return getProperty(JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM, JASPERSERVER);
	}

	@Produces
	@EvaConfigProperty
	@Named(TEST_KAN_LESE_GEOGRAFI)
	public Boolean getTestKanLeseGeografi() {
		return getBooleanProperty(TEST_KAN_LESE_GEOGRAFI, false);
	}

	@Produces
	@EvaConfigProperty
	@Named(TEST_KAN_REDUSERE_GEOGRAFI)
	public Boolean getTestKanRedusereGeografi() {
		return getBooleanProperty(TEST_KAN_REDUSERE_GEOGRAFI, false);
	}

	@Produces
	@EvaConfigProperty
	@Named(TEST_KAN_KLONE_ENDELIGE_TELLINGER)
	public Boolean getTestKanKloneEndeligeTellinger() {
		return getBooleanProperty(TEST_KAN_KLONE_ENDELIGE_TELLINGER, false);
	}

	@Produces
	@EvaConfigProperty
	@Named(VALGNATT_BASE_URL)
	public String getValgnattBaseUrl() {
		return getProperty(VALGNATT_BASE_URL, "http://qa-pm-be02.eva.lokal:8080/");
	}

	@Produces
	@EvaConfigProperty
	@Named(VALGNATT_CONTEXT)
	public String getValgnattContext() {
		return getProperty(VALGNATT_CONTEXT, "");
	}

	@Produces
	@EvaConfigProperty
	@Named(VALGNATT_CONNECTION_TIMEOUT_SECONDS)
	public long valgnattConnectionTimeout() {
		return Long.valueOf(getProperty(VALGNATT_CONNECTION_TIMEOUT_SECONDS, "10"));
	}
}
