package no.valg.eva.admin.frontend.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class VersionProperties {
	private static final Logger LOG = Logger.getLogger(VersionProperties.class);
	private static final Properties PROPERTIES = new Properties();
	private static final String HOSTID;
	private static final String PROPERTY_VERSION = "version";
	private static final String PROPERTY_BRANCH = "branch";
	private static final String PROPERTY_COMMIT_ID = "commitId";
	
	private static final String SNAPSHOT_BUILD = "SNAPSHOT";
	private static final String DEVELOPER_BUILD = "dev";

	static {
		// Load properties file containing version number
		try (InputStream is = VersionProperties.class.getResourceAsStream("version.properties")) {
			PROPERTIES.load(is);
		} catch (IOException e) {
			LOG.debug(e);
		}
		HOSTID = findHostId();
	}

	/**
	 * Look up host id, i.e. last four characters of the first part of the host name (i.e. fe01 if host name is admin-fe01.example.com)
	 */
	public static String findHostId() {
		String hostName = null;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			LOG.warn("Unable to find host name", e);
		}

		return findHostId(hostName);
	}

	public static String findHostId(String hostName) {
		String hostId = "??";
		if (hostName != null) {
			int beginIndexFe = hostName.indexOf("-fe") + 1;
			int beginIndexDotAfterFe = hostName.indexOf('.', beginIndexFe);
			
			if (beginIndexFe != 0) {
				if (beginIndexDotAfterFe != -1) {
					hostId = hostName.substring(beginIndexFe, beginIndexDotAfterFe);
				} else if (beginIndexFe + 4 <= hostName.length()) {
					hostId = hostName.substring(beginIndexFe, beginIndexFe + 4);
				}
			} else {
				hostId = hostName;
			}
			
		}

		return hostId;
	}

	protected static String getVersion(Properties properties) {
		String version = properties.getProperty(PROPERTY_VERSION);
		if (version.contains(SNAPSHOT_BUILD)) {
			version += "-" + DEVELOPER_BUILD + " (" + properties.getProperty(PROPERTY_BRANCH) + ", " + properties.getProperty(PROPERTY_COMMIT_ID) + ")";
		} else {
			version += " (" + properties.getProperty(PROPERTY_BRANCH) + ")";
		}
		return version;
	}

	public String getVersion() {
		return getVersion(PROPERTIES);
	}

	public String getHostId() {
		return HOSTID;
	}
	
	public static Properties getProperties() {
		return PROPERTIES;
	}
}
