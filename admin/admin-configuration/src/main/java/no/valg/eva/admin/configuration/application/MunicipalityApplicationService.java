package no.valg.eva.admin.configuration.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.dto.ConfigurationDto;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.evote.service.configuration.ReportCountCategoryServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionGroupAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.MunicipalityAuditEventForCentralConfiguration;
import no.valg.eva.admin.common.auditlog.auditevents.MunicipalityAuditEventForLocalConfiguration;
import no.valg.eva.admin.common.auditlog.auditevents.config.MarkerAvkryssningsmanntallKjortAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.config.MunicipalityConfigStatusAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityLocalConfigStatus;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.service.MunicipalityDomainService;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictAreaId;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.PartialUpdate;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.StatusChanged;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.ReturnValue;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Godkjenne;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oppheve;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Avkrysningsmanntall;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.configuration.application.OpeningHoursMapper.toDomainModelList;

@Stateless(name = "MunicipalityService")
@Remote(MunicipalityService.class)
public class MunicipalityApplicationService implements MunicipalityService {

    private static final String ID_SEPARATOR = ".";

    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private PollingDistrictRepository pollingDistrictRepository;
    @Inject
    private BoroughRepository boroughRepository;
    @Inject
    private LocaleRepository localeRepository;
    @Inject
    private ReportCountCategoryServiceBean reportCountCategoryServiceBean;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private MunicipalityDomainService municipalityDomainService;

    @Inject
    private PollingPlaceDomainService pollingPlaceDomainService;

    @Override
    @SecurityNone
    public Municipality findByPk(Long municipalityPk) {
        return municipalityRepository.findByPk(municipalityPk);
    }

    @Override
    @SecurityNone
    public Municipality findByPkWithScanningConfig(Long municipalityPk) {
        return municipalityRepository.findByPkWithScanningConfig(municipalityPk);
    }

    @Override
    @Security(accesses = Konfigurasjon_Geografi, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForLocalConfiguration.class, eventType = Create, objectSource = ReturnValue)
    public Municipality create(UserData userData, @SecureEntity(areaLevel = COUNTY) final Municipality municipality) {
        if (municipality.getLocale() == null) {
            municipality.setLocale(userData.getLocale());
        }
        return municipalityRepository.create(userData, municipality);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Geografi}, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForCentralConfiguration.class, eventType = Update, objectSource = ReturnValue)
    public Municipality update(UserData userData, @SecureEntity(areaLevel = MUNICIPALITY) final Municipality municipality) {
        return municipalityRepository.update(userData, municipality);
    }

    @Override
    @Security(accesses = {Konfigurasjon_Grunnlagsdata_Redigere}, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForLocalConfiguration.class, eventType = Update, objectSource = ReturnValue)
    public Municipality updateScanningConfiguration(UserData userData, @SecureEntity(areaLevel = MUNICIPALITY) final Municipality municipality) {
        // Dette hentes ut for å unngå at denne metoden skal brukes for å endre County-objektet
        Municipality dbMunicipality = municipalityRepository.findByPk(municipality.getPk());
        dbMunicipality.setScanningConfig(municipality.getScanningConfig());
        return municipalityRepository.update(userData, municipality);

    }

    @Override
    @Security(accesses = Konfigurasjon_Geografi, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForCentralConfiguration.class, eventType = Delete)
    public void delete(UserData userData, Municipality municipality) {
        Long municipalityPk = municipality.getPk();
        municipalityRepository.delete(userData, municipalityPk);
    }

    @Override
    @SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
    @Security(accesses = Konfigurasjon_Geografi, type = READ)
    public Municipality findMunicipalityById(UserData userData, Long countyPk, String id) {
        return municipalityRepository.findMunicipalityById(countyPk, id);
    }

    @Override
    @Security(accesses = Konfigurasjon_Geografi, type = READ)
    public Locale getLocale(UserData userData, Municipality municipality) {
        return municipalityRepository.getLocale(municipality);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik, type = READ)
    public MunicipalityStatusEnum getStatus(UserData userData, KommuneSti kommuneSti) {
        return municipalityRepository.getStatus(getMunicipality(userData, kommuneSti.areaPath()).getPk()).toEnumValue();
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik, type = READ)
    public List<ConfigurationDto> findVotersWithoutPollingDistricts(UserData userData, KommuneSti kommuneSti) {

        Municipality municipality = getMunicipality(userData, kommuneSti.areaPath());
        List<PollingDistrictAreaId> ids = pollingDistrictRepository.findVotersWithoutPollingDistricts(userData.getElectionEventPk(), municipality);
        List<ConfigurationDto> result = new ArrayList<>();
        for (PollingDistrictAreaId areaId : ids) {

            String municipalityId = areaId.getMunicipalityId();
            String boroughId = areaId.getBoroughId();
            String pollingDistrictId = areaId.getPollingDistrictId();
            String id = buildId(municipalityId, boroughId, pollingDistrictId);
            String name = buildName(municipality, boroughId);

            result.add(new ConfigurationDto(id, name));
        }
        return result;
    }

    private String buildId(String municipalityId, String boroughId, String pollingDistrictId) {
        return new StringJoiner(ID_SEPARATOR)
                .add(municipalityId)
                .add(boroughId)
                .add(pollingDistrictId).toString();

    }

    private String buildName(Municipality municipality, String boroughId) {
        String name = municipality.getName();
        Borough borough = boroughRepository.findBoroughById(municipality.getPk(), boroughId);
        if (borough != null) {
            name += ", " + borough.getName();
        } else {
            name += "???";
        }
        return name;
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Oppheve, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForLocalConfiguration.class, eventType = StatusChanged, objectSource = ReturnValue)
    public Municipality reject(UserData userData, @SecureEntity(areaLevel = MUNICIPALITY) Long municipalityPk) {
        Municipality municipality = municipalityRepository.findByPk(municipalityPk);
        return municipalityRepository.update(userData,
                setMunicipalityStatus(municipality, MunicipalityStatusEnum.LOCAL_CONFIGURATION.id()));
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Godkjenne, type = WRITE)
    @AuditLog(eventClass = MunicipalityAuditEventForLocalConfiguration.class, eventType = StatusChanged, objectSource = ReturnValue)
    public Municipality approve(UserData userData, @SecureEntity(areaLevel = MUNICIPALITY) Long municipalityPk) {
        Municipality municipality = municipalityRepository.findByPk(municipalityPk);
        return municipalityRepository.update(userData,
                setMunicipalityStatus(municipality, MunicipalityStatusEnum.APPROVED_CONFIGURATION.id()));
    }

    /**
     * Sets the requiredProtocolCount on all Municipalities on selected ElectionEvent
     *
     * @param electionEventPk       pk to electionEvent whose Municipalities requiredProtocolCount should be updated
     * @param requiredProtocolCount boolean value to set requiredProtocolCount on all Municipalities on the ElectionEvent
     */
    @Override
    @Security(accesses = Konfigurasjon_Opptellingsmåter, type = WRITE)
    @AuditLog(eventClass = ElectionGroupAuditEvent.class, eventType = AuditEventTypes.PartialUpdate)
    public void setRequiredProtocolCountForElectionEvent(UserData userData, Long electionEventPk, boolean requiredProtocolCount) {
        municipalityRepository.setRequiredProtocolCountForElectionEvent(electionEventPk, requiredProtocolCount);
    }

    /**
     * Returns the boolean value for requiredProtocolCount on the first Municipality on selected electionEvent. It is the same value on all Municipalities in
     * the electionEvent
     *
     * @param electionEventPk pk to electionEvent whose Municipalities requiredProtocolCount value you want
     * @return requiredProtocolCount value for electionEventPk
     */
    @Override
    @Security(accesses = Konfigurasjon_Opptellingsmåter, type = READ)
    public boolean getRequiredProtocolCountForElectionEvent(UserData userData, Long electionEventPk) {
        return municipalityRepository.getRequiredProtocolCountForElectionEvent(electionEventPk);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public MunicipalityConfigStatus findMunicipalityStatusByArea(UserData userData, AreaPath areaPath) {
        areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
        Municipality municipality = getMunicipality(userData, areaPath);
        return MunicipalityConfigStatusMapper.toMunicipalityConfigStatus(municipality);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
    @AuditLog(eventClass = MunicipalityConfigStatusAuditEvent.class, eventType = Save)
    public MunicipalityConfigStatus saveMunicipalityConfigStatus(UserData userData, MunicipalityConfigStatus status, ElectionPath electionGroupPath) {
        electionGroupPath.assertElectionGroupLevel();
        Municipality municipality = getMunicipality(userData, status.getMunicipalityPath());

        // Look at current state and maintain some data integrity
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionGroupPath.tilValghierarkiSti());
        maintainIntegrity(userData, status, municipality, mvElection.getElectionGroup());

        Locale locale = localeRepository.findById(status.getLocaleId().getId());
        municipality.updateStatus(status);
        municipality.setLocale(locale);
        municipality.setElectronicMarkoffs(status.isUseElectronicMarkoffs());
        return findMunicipalityStatusByArea(userData, status.getMunicipalityPath());
    }

    @Override
    @Security(accesses = {Rapport_Manntall_Avkrysningsmanntall, Konfigurasjon_Geografi}, type = WRITE)
    @AuditLog(eventClass = MarkerAvkryssningsmanntallKjortAuditEvent.class, eventType = PartialUpdate)
    public void markerAvkryssningsmanntallKjort(UserData userData, AreaPath areaPath, boolean kjort) {
        Municipality municipality = getMunicipality(userData, areaPath);
        municipality.setAvkrysningsmanntallKjort(kjort);
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
    public List<no.valg.eva.admin.common.configuration.model.OpeningHours> getOpeningHours(UserData userData, Municipality municipality) {
        return MunicipalityOpeningHourMapper.toDtoList(municipalityDomainService.getOpeningHours(municipality));
    }

    @Override
    @Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
    public void saveOpeningHours(UserData userData, Municipality municipality, List<no.valg.eva.admin.common.configuration.model.OpeningHours> openingHours,
                                 boolean overwriteExisting, AreaPath selectedAreaPath) {
        List<OpeningHours> openingHoursToSave = toDomainModelList(openingHours);

        pollingPlaceDomainService.saveOpeningHoursForPollingPlacesInArea(userData, municipality, openingHoursToSave, selectedAreaPath, overwriteExisting);
        
        List<MunicipalityOpeningHour> municipalityOpeningHours = MunicipalityOpeningHourMapper.toDomainModelList(openingHours);
        
        municipalityDomainService.saveOpeningHours(userData, municipality, municipalityOpeningHours);
    }

    void maintainIntegrity(UserData userData, MunicipalityConfigStatus status, Municipality municipality, ElectionGroup electionGroup) {
        MunicipalityLocalConfigStatus old = municipality.getLocalConfigStatus();
        if (status.isUseElectronicMarkoffs()) {
            boolean electionDayDone = status.isElectionPollingPlaces() && (old == null || !old.isElectionPollingPlaces());
            if (electionDayDone) {
                // If electronic markoff and election day polling place is set to finished, cleanup polling station data
                removeUsingPollingStations(municipality);
            }
        }
        boolean countCategoriesDone = status.isCountCategories() && (old == null || !old.isCountCategories());
        if (countCategoriesDone && reportCountCategoryServiceBean.isValgtingOrdinaereAndSentraltSamlet(municipality, electionGroup)) {
            // If count categories is set to finished with VO and CENTRAL, remove parent polling districts
            removeParentPollingDistricts(userData, municipality);
        }
    }

    private void removeUsingPollingStations(Municipality municipality) {
        municipality.pollingPlaces()
                .stream()
                .filter(place -> place.isElectionDayVoting() && place.getUsingPollingStations())
                .forEach(place -> place.setUsingPollingStations(false));
    }

    private void removeParentPollingDistricts(UserData userData, Municipality municipality) {
        // First remove parents for all children
        municipality.childPollingDistricts().forEach(child -> child.setPollingDistrict(null));
        // Now remove all parents
        municipality.parentPollingDistricts().forEach(parent -> pollingDistrictRepository.delete(userData, parent.getPk()));
    }

    private Municipality setMunicipalityStatus(Municipality municipality, Integer statusId) {
        MunicipalityStatus municipalityStatus = municipalityRepository.findMunicipalityStatusById(statusId);
        municipality.setMunicipalityStatus(municipalityStatus);
        return municipality;
    }

    private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
        return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
    }

}
