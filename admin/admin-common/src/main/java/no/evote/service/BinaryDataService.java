package no.evote.service;

import java.io.Serializable;

import no.evote.model.BinaryData;
import no.evote.security.UserData;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface BinaryDataService extends Serializable {
	BinaryData findByPk(UserData userData, Long pk);
}
