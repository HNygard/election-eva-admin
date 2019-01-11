package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;

public interface CountingConfigurationService extends Serializable {
	/**
	 * Get the configuration for the current count
	 * 
	 * @param userData
	 *            user context
	 * @param countContext
	 *            contest and category for current count
	 * @param areaPath area
	 * @return a counting configuration
	 */
	CountingConfiguration getCountingConfiguration(UserData userData, CountContext countContext, AreaPath areaPath);
}
