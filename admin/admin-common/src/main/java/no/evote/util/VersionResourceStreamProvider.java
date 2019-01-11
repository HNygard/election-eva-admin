package no.evote.util;

import java.io.InputStream;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;

@ApplicationScoped
public class VersionResourceStreamProvider implements Serializable {
	private final String versionResourcePath;
	
	@Produces
	@Named("defaultVersionPropertyResourcePath")
	private static final String DEFAULT_VERSION_PROPERTY_RESOURCE_PATH = "/version.properties";

	/**
	 * Required by CDI
	 */
	public VersionResourceStreamProvider() {
		versionResourcePath = DEFAULT_VERSION_PROPERTY_RESOURCE_PATH;
	}

	@Inject
	public VersionResourceStreamProvider(@Named("defaultVersionPropertyResourcePath") String versionResourcePath) {
		this.versionResourcePath = versionResourcePath;
	}

	public InputStream getVersionPropertiesInputStream() {
		InputStream resourceAsStream = getClass().getResourceAsStream(versionResourcePath);
		if (resourceAsStream != null) {
			return resourceAsStream;
		} else {
			throw new EvoteException("Could not find version properties file " + versionResourcePath);
		}
	}
}
