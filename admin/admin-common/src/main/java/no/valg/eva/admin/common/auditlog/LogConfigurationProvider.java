package no.valg.eva.admin.common.auditlog;

import static no.evote.util.EvoteProperties.NO_VALG_EVA_ADMIN_AUDIT_LOG_FILE_NAME;
import static no.evote.util.EvoteProperties.NO_VALG_EVA_ADMIN_SECURABLE_LOG_FILE_NAME;
import static no.evote.util.EvoteProperties.getProperty;

import no.evote.util.EvoteProperties;

/**
 * Provides the configuration for audit log appender.
 * This abstraction over {@link EvoteProperties} decouples tests from requiring an {@code evote.properties} file on the file system.
 */
public interface LogConfigurationProvider {
	/**
	 * @return filename of audit log
	 */
	String getLogLocation();

	class EvotePropertiesAuditLogConfigurationProvider implements LogConfigurationProvider {
		// This class is not tested, because EvoteProperties is immutable

		static final String DEFAULT_LOG_FILENAME = "../logs/admin-audit.log";

		@Override
		public String getLogLocation() {
			return getProperty(NO_VALG_EVA_ADMIN_AUDIT_LOG_FILE_NAME, DEFAULT_LOG_FILENAME);
		}
	}

	class EvotePropertiesSecuredObjectsLogConfigurationProvider implements LogConfigurationProvider {
		// This class is not tested, because EvoteProperties is immutable

		static final String DEFAULT_LOG_FILENAME = "../logs/admin-securable.log";

		@Override
		public String getLogLocation() {
			return getProperty(NO_VALG_EVA_ADMIN_SECURABLE_LOG_FILE_NAME, DEFAULT_LOG_FILENAME);
		}
	}
}
