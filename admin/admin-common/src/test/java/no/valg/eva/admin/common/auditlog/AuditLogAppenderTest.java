package no.valg.eva.admin.common.auditlog;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.log4j.DailyRollingFileAppender;
import org.testng.annotations.Test;

public class AuditLogAppenderTest {

	private static final String LOG_LOCATION = "auditlog.log";

	@Test
	public void logLocationIsAsConfigured() {
		AuditLogAppender appender = new AuditLogAppender(createLogConfiguration());
		assertThat(appender.getFile()).isEqualTo(LOG_LOCATION);
	}

	@Test
	public void logRotatesDaily() {
		AuditLogAppender appender = new AuditLogAppender();
		assertThat(appender instanceof DailyRollingFileAppender).isTrue();
	}

	private LogConfigurationProvider createLogConfiguration() {
		return new LogConfigurationProvider() {
			@Override
			public String getLogLocation() {
				return LOG_LOCATION;
			}
		};
	}

}
