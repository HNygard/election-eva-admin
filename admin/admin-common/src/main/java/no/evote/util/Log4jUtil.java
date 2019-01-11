package no.evote.util;

import java.io.File;
import java.net.URL;

import no.evote.exception.EvoteException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Reads Log4J config from the file given by evote.property {@value EvoteProperties#NO_VALG_EVA_ADMIN_LOG_CONFIG_FILE_NAME}.
 * If the property, or the file it refers to, does not exist, log4j.xml is read from classpath.
 */
public final class Log4jUtil {
	private static final int RELOAD_INTERVAL_MS = 60 * 1000;

	private Log4jUtil() {
		// Intentionally left empty
	}

	public static void configure(final ClassLoader classLoader) {
		String logConfigFilename = EvoteProperties.getProperty(EvoteProperties.NO_VALG_EVA_ADMIN_LOG_CONFIG_FILE_NAME, true);

		if (shouldUseExternalConfig(logConfigFilename)) {
			configureFromExternalConfig(logConfigFilename);
		} else {
			configureFromClasspathConfig(classLoader);
		}
	}

	private static boolean shouldUseExternalConfig(String logConfigFilename) {
		return logConfigFilename != null && new File(logConfigFilename).exists();
	}

	private static void configureFromExternalConfig(String logConfigFilename) {
		DOMConfigurator.configureAndWatch(logConfigFilename, RELOAD_INTERVAL_MS);
		info("Configured Log4J from " + logConfigFilename);
	}

	private static void configureFromClasspathConfig(ClassLoader classLoader) {
		URL log4JConfig = classLoader.getResource("log4j.xml");
		if (log4JConfig == null) {
			throw new EvoteException("Unable to find log4j.xml on classpath");
		}

		DOMConfigurator.configure(log4JConfig);
		info("Configured Log4J from " + log4JConfig);
	}

	private static void info(String message) {
		Logger.getLogger(Log4jUtil.class).info(message);
	}
}
