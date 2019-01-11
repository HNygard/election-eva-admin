package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.ElectionType;

public interface ElectionService extends Serializable {

	Election get(UserData userData, ElectionPath electionPath);

	SaveElectionResponse save(UserData userData, Election election);

	void delete(UserData userData, ElectionPath electionPath);

	@Cacheable
	ElectionType findElectionTypeById(String id);

}
