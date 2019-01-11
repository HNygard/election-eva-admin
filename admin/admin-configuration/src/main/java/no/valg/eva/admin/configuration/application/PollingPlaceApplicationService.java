package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.AdvancePollingPlaceAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.config.ElectionDayPollingPlaceAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

@Stateless(name = "PollingPlaceService")
@Remote(PollingPlaceService.class)
@NoArgsConstructor //CDI
public class PollingPlaceApplicationService implements PollingPlaceService {

    private static final long serialVersionUID = 7208614027467995395L;

    // Injected
    private PollingPlaceRepository pollingPlaceRepository;
    private PollingPlaceDomainService pollingPlaceDomainService;
    private MunicipalityRepository municipalityRepository;
    private MvElectionRepository mvElectionRepository;

    @Inject
    public PollingPlaceApplicationService(PollingPlaceRepository pollingPlaceRepository, PollingPlaceDomainService pollingPlaceDomainService,
                                          MunicipalityRepository municipalityRepository, MvElectionRepository mvElectionRepository) {
        this.pollingPlaceRepository = pollingPlaceRepository;
        this.pollingPlaceDomainService = pollingPlaceDomainService;
        this.municipalityRepository = municipalityRepository;
        this.mvElectionRepository = mvElectionRepository;
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
    @AuditLog(eventClass = AdvancePollingPlaceAuditEvent.class, eventType = Save)
    public AdvancePollingPlace saveAdvancePollingPlace(UserData userData, ElectionPath electionGroupPath, AdvancePollingPlace advancePollingPlace) {
        PollingPlace pollingPlace;

        if (advancePollingPlace.getPk() == null) {
            advancePollingPlace.getPath().assertLevel(AreaLevelEnum.MUNICIPALITY);
            MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionGroupPath.tilValghierarkiSti());
            pollingPlace = PollingPlaceMapper.toPollingPlace(new PollingPlace(), advancePollingPlace);
            pollingPlace.setPollingDistrict(getMunicipality(userData, advancePollingPlace.getPath()).getMunicipalityPollingDistrict());
            pollingPlace.setAdvanceVoteInBallotBox(mvElection.getElectionGroup().isAdvanceVoteInBallotBox());
            pollingPlace = pollingPlaceRepository.create(userData, pollingPlace);
        } else {
            advancePollingPlace.getPath().assertLevel(AreaLevelEnum.POLLING_PLACE);
            pollingPlace = pollingPlaceRepository.findByPk(advancePollingPlace.getPk());
            pollingPlace.checkVersion(advancePollingPlace);
            pollingPlace = PollingPlaceMapper.toPollingPlace(pollingPlace, advancePollingPlace);
            pollingPlace = pollingPlaceRepository.update(userData, pollingPlace);
        }
        return PollingPlaceMapper.toAdvancePollingPlace(pollingPlace);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
    @AuditLog(eventClass = ElectionDayPollingPlaceAuditEvent.class, eventType = Save)
    public ElectionDayPollingPlace saveElectionDayPollingPlace(UserData userData, ElectionDayPollingPlace electionDayPollingPlace) {
        return pollingPlaceDomainService.saveElectionDayPollingPlace(userData, electionDayPollingPlace);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
    @AuditLog(eventClass = AdvancePollingPlaceAuditEvent.class, eventType = Delete)
    public void deleteAdvancePollingPlace(UserData userData, AdvancePollingPlace advancePollingPlace) {
        PollingPlace pollingPlace = pollingPlaceRepository.findByPk(advancePollingPlace.getPk());
        pollingPlace.checkVersion(advancePollingPlace);
        pollingPlaceRepository.delete(userData, pollingPlace.getPk());
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public List<AdvancePollingPlace> findAdvancePollingPlacesByArea(UserData userData, AreaPath areaPath) {
        areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
        Municipality municipality = getMunicipality(userData, areaPath);
        return municipality.pollingPlacesAdvance()
                .stream()
                .filter(place -> !AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID.equals(place.getId()))
                .map(PollingPlaceMapper::toAdvancePollingPlace)
                .collect(Collectors.toList());
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public List<ElectionDayPollingPlace> findElectionDayPollingPlacesByArea(UserData userData, AreaPath areaPath) {
        return pollingPlaceDomainService.findElectionDayPollingPlacesByArea(userData, areaPath);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public AdvancePollingPlace findAdvancePollingPlaceByAreaAndId(UserData userData, AreaPath areaPath, String id) {
        areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
        return PollingPlaceMapper.toAdvancePollingPlace(getMunicipality(userData, areaPath).pollingPlacesAdvanceById(id));
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public ElectionDayPollingPlace findElectionDayPollingPlaceByAreaAndId(UserData userData, AreaPath areaPath, String id) {
        return pollingPlaceDomainService.findElectionDayPollingPlaceByAreaAndId(userData, areaPath, id);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public List<ElectionDayPollingPlace> findPollingPlacesWithCustomOpeningHours(UserData userData, Municipality municipality, AreaPath areaPath) {
        return pollingPlaceDomainService.pollingPlacesWithCustomOpeningHours(userData, municipality, areaPath);

    }

    private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
        return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
    }
}
