package no.valg.eva.admin.common.auditlog;

import org.apache.log4j.DailyRollingFileAppender;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SecuredObjectsAppenderTest {

	private static final String LOG_LOCATION = "admin-securable.log";

	@Test
	public void logLocationIsAsConfigured() {
		SecuredObjectsAppender appender = new SecuredObjectsAppender(createLogConfiguration());
		assertThat(appender.getFile()).isEqualTo(LOG_LOCATION);
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void logRotatesDaily() {
		SecuredObjectsAppender appender = new SecuredObjectsAppender();
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
