package no.evote.logging;

import no.evote.util.EvoteProperties;

import org.apache.log4j.DailyRollingFileAppender;

/**
 * Reads and uses logging filename from evote.properties if set, or falls back to a nice default.
 */
public class EvoteAppender extends DailyRollingFileAppender {

	private static final String DEFAULT_LOG_FILENAME = "../logs/notsecure.log";

	public EvoteAppender() {
		super();

		String logFilename = EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_LOGGING_EVOTE_APPENDER_FILE, true);
		if (logFilename != null) {
			this.setFile(logFilename);
		} else {
			this.setFile(DEFAULT_LOG_FILENAME);
		}
	}
}
