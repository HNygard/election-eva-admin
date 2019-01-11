package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.party.Parti;

public interface PartyService extends Serializable {

	List<String> validateParty(UserData userData, Parti newParty);

	Parti create(UserData userData, Parti parti);

	Parti update(UserData newParam, Parti parti);

	List<String> validatePartyForDelete(UserData userData, Parti parti);

	void delete(UserData userData, final Parti parti);

	List<Parti> findAllPartiesButNotBlank(UserData userData, Long electionEventPk);
}
