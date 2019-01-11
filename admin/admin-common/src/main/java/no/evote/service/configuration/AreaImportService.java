package no.evote.service.configuration;

import java.io.IOException;

import java.io.Serializable;
import no.evote.security.UserData;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface AreaImportService extends Serializable{
	void importAreaHierarchy(UserData userData, byte[] data) throws IOException;
}
