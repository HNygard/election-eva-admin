package no.valg.eva.admin.frontend;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.VersionProperties;

@Named
@ApplicationScoped
public class VersionController {
	private final VersionProperties versionProperties = new VersionProperties();

	public String getVersion() throws IOException {
		return versionProperties.getVersion();
	}

	public String getHostId() {
		return versionProperties.getHostId();
	}
}
