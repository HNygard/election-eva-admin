package no.valg.eva.admin.common.auditlog;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

/**
 * Log appender for audit log.
 * <ul>
 * <li>Krever at nøkkelen {@value no.evote.util.EvoteProperties#NO_VALG_EVA_ADMIN_AUDIT_LOG_FILE_NAME}
 * i {@link no.evote.util.EvoteProperties} angir plassering av loggfil. Hvis ikke, brukes
 * {@value no.valg.eva.admin.common.auditlog.LogConfigurationProvider.EvotePropertiesAuditLogConfigurationProvider#DEFAULT_LOG_FILENAME}</li>
 * <li>Loggen roterer daglig, og kan konfigureres som {@link DailyRollingFileAppender} i log4j.xml.</li>
 */
public class AuditLogAppender extends DailyRollingFileAppender {

	@SuppressWarnings("unused")
	public AuditLogAppender() {
		this(new LogConfigurationProvider.EvotePropertiesAuditLogConfigurationProvider());
	}

	public AuditLogAppender(LogConfigurationProvider logConfiguration) {
		String logFilename = logConfiguration.getLogLocation();
		setFile(logFilename);
		setLayout(createPatternLayout());
	}

	private PatternLayout createPatternLayout() {
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%m%n");
		return layout;
	}

}
