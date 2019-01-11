package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.model.views.PollingPlaceVoting;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

public interface LegacyPollingPlaceService extends Serializable {
	PollingPlace create(UserData userData, PollingPlace pollingPlace);

	PollingPlace update(UserData userData, PollingPlace pollingPlace);

	void delete(UserData userData, PollingPlace pollingPlace);

	PollingPlace findByPk(UserData userData, Long pk);

	PollingPlace findPollingPlaceById(UserData userData, Long pollingDistrictPk, String id);

	List<PollingPlaceVoting> findAdvancedPollingPlaceByMunicipality(UserData userData, final Long electionEventPk, final String municipalityId);
}
