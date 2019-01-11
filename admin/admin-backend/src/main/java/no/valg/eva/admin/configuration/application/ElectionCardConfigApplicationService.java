package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.ElectionCardConfigAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.service.ElectionCardConfigService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsvalgstyrer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

@Stateless(name = "ElectionCardConfigService")
@Remote(ElectionCardConfigService.class)
@NoArgsConstructor
public class ElectionCardConfigApplicationService implements ElectionCardConfigService {

    private static final long serialVersionUID = -4325665189949505386L;

    private ReportingUnitDomainService reportingUnitDomainService;
    private MunicipalityRepository municipalityRepository;
    private PollingDistrictRepository pollingDistrictRepository;
    private PollingPlaceDomainService pollingPlaceApplicationService;
    private PollingPlaceRepository pollingPlaceRepository;


    @Inject
    public ElectionCardConfigApplicationService(ReportingUnitDomainService reportingUnitDomainService,
                                                PollingPlaceDomainService pollingPlaceApplicationService,
                                                MunicipalityRepository municipalityRepository,
                                                PollingDistrictRepository pollingDistrictRepository,
                                                PollingPlaceRepository pollingPlaceRepository) {
        this.reportingUnitDomainService = reportingUnitDomainService;
        this.pollingPlaceApplicationService = pollingPlaceApplicationService;
        this.municipalityRepository = municipalityRepository;
        this.pollingDistrictRepository = pollingDistrictRepository;
        this.pollingPlaceRepository = pollingPlaceRepository;
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = READ)
    public ElectionCardConfig findElectionCardByArea(UserData userData, AreaPath path) {

        assertLevel(path);

        ReportingUnit reportingUnit = reportingUnitDomainService.getReportingUnit(userData, path);
        int version = path.isMunicipalityLevel() ? reportingUnit.getMvArea().getMunicipality().getAuditOplock() : 0;
        ElectionCardConfig result = new ElectionCardConfig(ReportingUnitMapper.toReportingUnit(reportingUnit), version);

        if (path.isMunicipalityLevel()) {
            result.setInfoText(reportingUnit.getMvArea().getMunicipality().getElectionCardText());
            List<ElectionDayPollingPlace> places = pollingPlaceApplicationService.findElectionDayPollingPlacesByArea(userData, path);
            result.setPlaces(places);
            places.stream().filter(place -> place.getPk() == null).forEach(place -> place.setInfoText(result.getInfoText()));
        }
        return result;
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer}, type = WRITE)
    @AuditLog(eventClass = ElectionCardConfigAuditEvent.class, eventType = Save)
    public ElectionCardConfig save(UserData userData, ElectionCardConfig electionCard) {

        AreaPath path = electionCard.getReportingUnit().getAreaPath();
        assertLevel(path);

        ReportingUnit reportingUnit = reportingUnitDomainService.getReportingUnit(userData, path);

        // Store parent data
        reportingUnit.checkVersion(electionCard.getReportingUnit());
        reportingUnit.setAddressLine1(electionCard.getReportingUnit().getAddress());
        reportingUnit.setAddressLine2(null);
        reportingUnit.setAddressLine3(null);
        reportingUnit.setPostalCode(electionCard.getReportingUnit().getPostalCode());
        reportingUnit.setPostTown(electionCard.getReportingUnit().getPostTown());

        if (path.isMunicipalityLevel()) {
            Municipality municipality = municipalityRepository.findByPk(reportingUnit.getMvArea().getMunicipality().getPk());
            municipality.checkVersion(electionCard);
            municipality.setElectionCardText(electionCard.getInfoText());

            // Store child data
            for (ElectionDayPollingPlace place : electionCard.getPlaces()) {
                if (place.getPk() == null) {
                    // Create polling place with default values if it does not exist.
                    PollingDistrict dbDistrict = pollingDistrictRepository.findByPk(place.getParentPk());
                    PollingPlace dbPlace = dbDistrict.createElectionDayPollingPlace();
                    dbPlace.setInfoText(place.getInfoText());
                    pollingPlaceApplicationService.create(userData, dbPlace);
                } else {
                    PollingPlace dbPlace = pollingPlaceRepository.findByPk(place.getPk());
                    dbPlace.checkVersion(place);
                    dbPlace.setInfoText(place.getInfoText());
                    pollingPlaceRepository.update(userData, dbPlace);
                }
            }
        }
        return findElectionCardByArea(userData, path);
    }

    private void assertLevel(AreaPath areaPath) {
        if (!(areaPath.isRootLevel() || areaPath.isMunicipalityLevel())) {
            throw new IllegalArgumentException(format("illegal path: %s", areaPath));
        }
    }
}
