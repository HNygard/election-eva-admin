package no.valg.eva.admin.common.configuration.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.configuration.SaveElectionResponse;

import java.io.Serializable;
import java.util.List;

public interface ElectionGroupService extends Serializable {

	List<ElectionGroup> getElectionGroups(UserData userData);

	ElectionGroup get(UserData userData, ElectionPath electionGroupPath);

	SaveElectionResponse save(UserData userData, ElectionGroup electionGroup);

	void delete(UserData userData, ElectionPath electionGroupPath);

	boolean isScanningEnabled(UserData userData);
}
