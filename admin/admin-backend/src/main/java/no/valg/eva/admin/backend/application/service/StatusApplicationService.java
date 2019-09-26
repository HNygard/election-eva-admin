package no.valg.eva.admin.backend.application.service;

import static no.valg.eva.admin.util.PropertyUtil.addPrefixToPropertyKeys;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import no.evote.util.VersionResourceStreamProvider;

import no.valg.eva.admin.backend.service.impl.DatabaseSchemaCheckerBean;
import org.apache.log4j.Logger;

public class StatusApplicationService {
	private static final Logger LOG = Logger.getLogger(StatusApplicationService.class);
	private static final String PING_SQL = "SELECT 'ping'";

	static final String OK = "OK";
	static final String SYSTEM_PWD_NOT_SET = "System password not set";
	static final String DATABASE_UNAVAILABLE = "Database is unavailable";
	private static final String PREFIX = "backend-";

	@Inject
	private SystemPasswordApplicationService systemPasswordService;

	@Inject
	private VersionResourceStreamProvider versionResourceStreamProvider;
	
	@Deprecated // Use getStatusAndConfiguredVersionProperties() instead
	public String getStatus() {
		if (!systemPasswordService.isPasswordSet()) {
			return SYSTEM_PWD_NOT_SET;
		}

		if (!databaseAvailable()) {
			return DATABASE_UNAVAILABLE;
		}

		return OK;
	}

	private boolean databaseAvailable() {
		try (Connection conn = DatabaseSchemaCheckerBean.evoteDatasource.getConnection(); Statement statement = conn.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery(PING_SQL)) {
				if (resultSet.next()) {
					return "ping".equals(resultSet.getString(1));
				}
				throw new IllegalStateException("Empty result set - should never happen!");
			}
		} catch (RuntimeException | SQLException e) {
			LOG.info("Backend is not connected to database", e);
			return false;
		}
	}

	public Properties getStatusAndConfiguredVersionProperties() {
		Properties properties = new Properties();
		properties.putAll(getConfiguredVersionProperties());
		properties.putAll(getStatusProperties());
		return properties;
	}

	private Properties getStatusProperties() {
		Properties properties = new Properties();
		properties.setProperty("databaseConnectionOk", Boolean.toString(databaseAvailable()));
		properties.setProperty("systemPasswordSet", Boolean.toString(systemPasswordService.isPasswordSet()));
		return addPrefixToPropertyKeys(PREFIX, properties);
	}
	
	private Properties getConfiguredVersionProperties() {
		Properties properties = new Properties();
		try (InputStream is = versionResourceStreamProvider.getVersionPropertiesInputStream()) {
			properties.load(is);
		} catch (IOException e) {
			LOG.warn("Finner ikke fil med versjonsinformasjon", e);
		}
		return addPrefixToPropertyKeys(PREFIX, properties);
	}

}
