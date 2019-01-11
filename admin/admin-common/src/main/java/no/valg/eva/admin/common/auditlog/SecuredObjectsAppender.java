package no.valg.eva.admin.common.auditlog;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

/**
 * Log appender for secured objects log.
 * <ul>
 * <li>Krever at nøkkelen {@value no.valg.eva.admin.common.auditlog.LogConfigurationProvider.EvotePropertiesSecuredObjectsLogConfigurationProvider#LOG_FILENAME_KEY}
 * i {@link no.evote.util.EvoteProperties}
 * angir plassering av loggfil. Hvis ikke, brukes
 * {@value no.valg.eva.admin.common.auditlog.LogConfigurationProvider.EvotePropertiesSecuredObjectsLogConfigurationProvider#DEFAULT_LOG_FILENAME}</li>
 * <li>Loggen roterer daglig, og kan konfigureres som {@link org.apache.log4j.DailyRollingFileAppender} i log4j.xml.</li>
 */
public class SecuredObjectsAppender extends DailyRollingFileAppender {

	@SuppressWarnings("unused")
	public SecuredObjectsAppender() {
		this(new LogConfigurationProvider.EvotePropertiesSecuredObjectsLogConfigurationProvider());
	}

	public SecuredObjectsAppender(LogConfigurationProvider logConfiguration) {
		String logFilename = logConfiguration.getLogLocation();
		setFile(logFilename);
		setLayout(createPatternLayout());
	}

	private PatternLayout createPatternLayout() {
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("time=\"%d{yyyy-MM-dd HH:mm:ss,SSS Z}\", thread=%t, priority=%p, %m%n");
		return layout;
	}

}
