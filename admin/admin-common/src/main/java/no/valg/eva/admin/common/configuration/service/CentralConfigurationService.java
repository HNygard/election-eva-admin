package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.central.CentralConfigurationSummary;

public interface CentralConfigurationService extends Serializable {
	
	CentralConfigurationSummary getCentralConfigurationSummary(UserData userData);
}
