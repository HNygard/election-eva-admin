package no.evote.util;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteInitiationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class EvoteProperties {
	private static final String EVOTE_PROPERTIES_ENV_VARIABLE = "EVOTE_PROPERTIES";
	
	// Liste over properties som er i bruk
	public static final String DEPLOY_URL = "DeployURL";

	public static final String NO_VALG_EVA_ADMIN_BANNER_ENABLED = "no.valg.eva.admin.banner.enabled";
	public static final String NO_VALG_EVA_ADMIN_BANNER_TEXT = "no.valg.eva.admin.banner.text";
	public static final String NO_VALG_EVA_ADMIN_BANNER_BACKGROUND_COLOR = "no.valg.eva.admin.banner.backgroundColor";
	public static final String NO_VALG_EVA_ADMIN_BANNER_TEXT_COLOR = "no.valg.eva.admin.banner.textColor";

	public static final String EXPORT_TOKEN_EXPIRATION_TIME = "exportToken.expirationTime";
	public static final String BUYPASS_PERFORM_SCHEDULED_CRL_IMPORT = "buypass.perform.scheduled.crl.import";
	public static final String BYPASS_CRL_CA1_URL = "bypass.crl.ca1.url";
	public static final String BYPASS_CRL_CA3_URL = "bypass.crl.ca3.url";
	public static final String NO_EVOTE_UTIL_ANTI_SAMY_FILTER_POLICY_FILE = "no.valg.eva.admin.util.AntiSamyFilter.policy_file";

	public static final String MANNTALLSIMPORT_FIL_KRETSMAPPING = "manntallsimport.fil.kretsmapping";
	public static final String ELECTORAL_ROLL_PERFORM_SCHEDULED_INCREMENTAL_IMPORT = "electoralRoll.perform.scheduled.incremental.import";
	public static final String MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR = "manntallsimport.ignorer.manglende.stemmerettsalder.for";

	public static final String NO_EVOTE_I18N_DEFAULT_LOCALE = "no.evote.i18n.defaultLocale";

	public static final String JASPERSERVER_USERNAME = "jasperserver.username";
	public static final String JASPERSERVER_PASSWORD = "jasperserver.password";
	public static final String JASPERSERVER_BASE_URL = "jasperserver.base.url";
	public static final String JASPERSERVER_CONTEXT = "jasperserver.context";
	public static final String JASPERSERVER_ENABLE_AUTOUPLOAD = "jasperserver.enable.autoupload";
	public static final String JASPERSERVER_ENABLE_AUTO_REPORT_TEMPLATES_UPLOADING = "jasperserver.enable.auto.report.templates.uploading";
	public static final String JASPERSERVER_ENABLE_REPORT_TEMPLATES_UPLOAD_TRIGGER_SERVLET = "jasperserver.enable.report.templates.upload.trigger.servlet";
	public static final String JASPERSERVER_CONNECTION_TIMEOUT_SECONDS = "jasperserver.connection.timeout.seconds";
	public static final String JASPERSERVER_TEMPLATE_SOURCE_DIR = "jasperserver.template.source.dir";
	public static final String JASPERSERVER_NIGHTLY_JOBS_FOR_ELECTION_EVENTS = "jasperserver.nightly.jobs.for.election.events";
	public static final String JASPERSERVER_NIGHTLY_JOBS_START_TIME = "jasperserver.nightly.jobs.start.time";
	public static final String JASPERSERVER_BASE_REPORT_FORMATS = "jasperserver.base.report.formats";
	public static final String JASPERSERVER_USE_LIVE_TEMPLATES_INSTEAD_OF_BLANKS = "jasperserver.use.live.templates.instead.of.blanks";
	public static final String JASPERSERVER_PREGENERATED_FTP_HOST = "jasperserver.pregenerated.ftp.host";
	public static final String JASPERSERVER_PREGENERATED_FTP_USER = "jasperserver.pregenerated.ftp.user";
	public static final String JASPERSERVER_PREGENERATED_FTP_PWD = "jasperserver.pregenerated.ftp.pwd";
	public static final String JASPERSERVER_RETRIEVE_PREGENERATED_REPORTS_FROM = "jasperserver.retrieve.pregenerated.reports.from";

	public static final String TEST_CAN_CHANGE_TIME = "test.can.change.time";
	public static final String TEST_KAN_LESE_GEOGRAFI = "test.kan.lese.geografi";
	public static final String TEST_KAN_REDUSERE_GEOGRAFI = "test.kan.redusere.geografi";
	public static final String TEST_KAN_KLONE_ENDELIGE_TELLINGER = "test.kan.klone.endelige.tellinger";
	public static final String NO_VALG_EVA_ADMIN_LOGIN_TMP_ENABLED = "no.valg.eva.admin.login.tmp.enabled";

	public static final String VALGNATT_BASE_URL = "valgnatt.base.url";
	public static final String VALGNATT_CONTEXT = "valgnatt.context";
	public static final String VALGNATT_CONNECTION_TIMEOUT_SECONDS = "valgnatt.connection.timeout.seconds";

	public static final String NO_VALG_EVA_ADMIN_AUDIT_LOG_FILE_NAME = "no.valg.eva.admin.auditLog.fileName";
	public static final String NO_VALG_EVA_ADMIN_SECURABLE_LOG_FILE_NAME = "no.valg.eva.admin.securableLog.fileName";
	public static final String NO_EVOTE_LOGGING_EVOTE_APPENDER_FILE = "no.evote.logging.EvoteAppender.file";
	public static final String NO_VALG_EVA_ADMIN_LOG_CONFIG_FILE_NAME = "no.valg.eva.admin.logConfig.fileName";

	public static final String NO_EVOTE_PRESENTATION_UTIL_FILTERS_IEMODE_FILTER_MODE = "no.evote.presentation.util.filters.IEModeFilter.mode";
	public static final String NO_VALG_EVA_ADMIN_COUNTING_UPLOAD_SKIP_SIGNATURE_CHECK = "no.valg.eva.admin.counting.upload.skip.signature.check";
	public static final String NO_EVOTE_SERVICE_UTIL_SQLCOLLECTOR_INTERCEPTOR_ENABLED = "no.evote.service.util.SQLCollectorInterceptor.enabled";

	public static final String NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER = "no.valg.eva.admin.valgkortgrunnlag.folder";
	public static final String NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER_DEFAULT = "/opt/valgkortgrunnlag";

	public static final String GEONORGE_WS_ENDPOINT = "geonorge.ws.endpoint";
	public static final String GEONORGE_WS_ENDPOINT_DEFAULT = "https://ws.geonorge.no";
	
	private static String propertyFilePath;

	private static Properties properties = new Properties();

	private EvoteProperties() {
	}

	/* USING DEFAULT VALUES FOR ALL VALUES
	static {
		readProperties();
	}

	static void readProperties() {
		loadPropertyFilePath();

		try {
			if (propertyFilePath == null) {
				throw new IllegalArgumentException(EVOTE_PROPERTIES_ENV_VARIABLE + " is not defined");
			} else {
				FileInputStream fi = new FileInputStream(propertyFilePath);
				properties.load(fi);
				fi.close();
			}
		} catch (IOException e) {
			throw new EvoteInitiationException("Failed to load evote properties", e);
		}
	}
	*/

	private static synchronized void loadPropertyFilePath() {
		propertyFilePath = System.getenv(EVOTE_PROPERTIES_ENV_VARIABLE);
		if (propertyFilePath == null) {
			propertyFilePath = System.getProperty(EVOTE_PROPERTIES_ENV_VARIABLE);
		}
	}

	/** Intended for test use only */
	static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	/** Intended for test use only */
	static void clearProperties() {
		properties.clear();
	}

	public static String getProperty(final String key) {
		return getProperty(key, false);
	}

	public static String getProperty(final String key, final String defaultValue) {
		String value = getProperty(key, true);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		String value = getProperty(key, true);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}
	
	public static String getProperty(final String key, final boolean allowNull) {
		// String value = (String) properties.get(key);
		String value = null;
		if (value == null && !allowNull) {
			throw new EvoteException("Property missing in evote.properties: " + key);
		}
		return value;
	}

}
