package no.valg.eva.admin.common.application.service;

import java.util.Properties;

/**
 * Ment brukt av monitoreringsverktøy og andre som ønsker å vite status til admin-backend
 */
public interface StatusService {
	@Deprecated
	String getStatus();
	
	Properties getStatusProperties();
}
