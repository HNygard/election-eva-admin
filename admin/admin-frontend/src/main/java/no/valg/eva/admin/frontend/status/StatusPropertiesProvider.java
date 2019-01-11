package no.valg.eva.admin.frontend.status;

import static no.valg.eva.admin.util.PropertyUtil.addPrefixToPropertyKeys;

import java.util.Properties;

import javax.inject.Inject;

import no.valg.eva.admin.frontend.util.VersionProperties;
import no.valg.eva.admin.common.application.service.StatusService;

public class StatusPropertiesProvider {
	private static final String PREFIX = "frontend-";

	@Inject
	private StatusService statusService;

	public Properties getStatusProperties() {
		Properties status = getStatusForBackend();
		status.putAll(getStatusForFrontend());
		return status;
	}
	
	private Properties getStatusForBackend() {
		return statusService.getStatusProperties();
	}
	
	private Properties getStatusForFrontend() {
		return addPrefixToPropertyKeys(PREFIX, VersionProperties.getProperties());
	}
}
