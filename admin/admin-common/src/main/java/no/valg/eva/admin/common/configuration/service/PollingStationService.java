package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.Rode;

public interface PollingStationService extends Serializable {

	List<Rode> findPollingStationsByArea(UserData userData, AreaPath areaPath);

	List<Rode> findPollingStationsByAreaCalculated(UserData userData, AreaPath areaPath, int numberOfPollingStations);

	List<Rode> recalculatedPollingStationsByArea(UserData userData, AreaPath areaPath, List<Rode> divisionList);

	List<Rode> save(UserData userData, AreaPath areaPath, List<Rode> divisionList);

}
