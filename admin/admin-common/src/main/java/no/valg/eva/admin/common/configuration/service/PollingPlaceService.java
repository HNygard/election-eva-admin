package no.valg.eva.admin.common.configuration.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import java.io.Serializable;
import java.util.List;

public interface PollingPlaceService extends Serializable {

	AdvancePollingPlace saveAdvancePollingPlace(UserData userData, ElectionPath electionGroupPath, AdvancePollingPlace advancePollingPlace);

	ElectionDayPollingPlace saveElectionDayPollingPlace(UserData userData, ElectionDayPollingPlace electionDayPollingPlace);

	void deleteAdvancePollingPlace(UserData userData, AdvancePollingPlace advancePollingPlace);

	List<AdvancePollingPlace> findAdvancePollingPlacesByArea(UserData userData, AreaPath areaPath);

	List<ElectionDayPollingPlace> findElectionDayPollingPlacesByArea(UserData userData, AreaPath areaPath);

	AdvancePollingPlace findAdvancePollingPlaceByAreaAndId(UserData userData, AreaPath areaPath, String id);

	ElectionDayPollingPlace findElectionDayPollingPlaceByAreaAndId(UserData userData, AreaPath areaPath, String id);

    List<ElectionDayPollingPlace> findPollingPlacesWithCustomOpeningHours(UserData userData, Municipality municipality, AreaPath areaPath);
}
