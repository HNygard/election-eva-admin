package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.PollingStationServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.PollingStationAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.common.configuration.service.PollingStationService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;

@Stateless(name = "PollingStationService")
@Remote(PollingStationService.class)
public class PollingStationApplicationService implements PollingStationService {

	private PollingStationServiceBean pollingStationService;
	private MunicipalityRepository municipalityRepository;

	@Inject
	public PollingStationApplicationService(PollingStationServiceBean pollingStationService, MunicipalityRepository municipalityRepository) {
		this.pollingStationService = pollingStationService;
		this.municipalityRepository = municipalityRepository;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<Rode> findPollingStationsByArea(UserData userData, AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.POLLING_PLACE);
		PollingPlace pollingPlace = getPollingPlace(userData, areaPath);
		if (pollingPlace == null) {
			return new ArrayList<>();
		}
		return pollingStationService.getDivisionListForPollingPlace(pollingPlace);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<Rode> findPollingStationsByAreaCalculated(UserData userData, AreaPath areaPath, int numberOfPollingStations) {
		areaPath.assertLevel(AreaLevelEnum.POLLING_PLACE);
		PollingPlace pollingPlace = getPollingPlace(userData, areaPath);
		return pollingStationService.getPollingStationDivision(numberOfPollingStations, pollingPlace);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<Rode> recalculatedPollingStationsByArea(UserData userData, AreaPath areaPath,
														List<Rode> divisionList) {
		areaPath.assertLevel(AreaLevelEnum.POLLING_PLACE);
		PollingPlace pollingPlace = getPollingPlace(userData, areaPath);
		return pollingStationService.getPollingStationDivision(divisionList, pollingPlace);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = PollingStationAuditEvent.class, eventType = AuditEventTypes.Create)
	public List<Rode> save(UserData userData, AreaPath areaPath, List<Rode> divisionList) {
		areaPath.assertLevel(AreaLevelEnum.POLLING_PLACE);
		PollingPlace pollingPlace = getPollingPlace(userData, areaPath);
		pollingStationService.savePollingStationConfiguration(userData, pollingPlace, divisionList);
		return findPollingStationsByArea(userData, areaPath);
	}

	private PollingPlace getPollingPlace(UserData userData, AreaPath areaPath) {
		Municipality municipality = getMunicipality(userData, areaPath);
		if (municipality.isElectronicMarkoffs()) {
			// No polling place with polloing station if electronic markoffs
			return null;
		}
		// Find pollingStations for polling place
		for (PollingPlace pp : municipality.pollingPlaces()) {
			if (pp.getUsingPollingStations() && pp.areaPath().equals(areaPath)) {
				return pp;
			}
		}
		return null;
	}

	private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
		return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
	}
}
