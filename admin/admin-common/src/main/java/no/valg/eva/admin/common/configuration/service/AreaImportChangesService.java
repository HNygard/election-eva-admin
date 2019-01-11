package no.valg.eva.admin.common.configuration.service;

import java.io.IOException;
import java.io.Serializable;

import no.evote.security.UserData;

public interface AreaImportChangesService extends Serializable{

	byte[] importAreaHierarchyChanges(UserData userData, byte[] contents) throws IOException;
}
