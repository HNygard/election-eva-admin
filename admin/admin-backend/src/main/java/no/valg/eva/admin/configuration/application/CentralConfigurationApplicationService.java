package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.evote.dto.ConfigurationDto;
import no.evote.dto.ReportingUnitTypeDto;
import no.evote.security.UserData;
import no.evote.service.configuration.BoroughServiceBean;
import no.evote.service.configuration.ContestServiceBean;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.configuration.ElectionGroupServiceBean;
import no.evote.service.configuration.ReportingUnitTypeServiceBean;
import no.evote.service.configuration.VoterServiceBean;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.common.configuration.model.central.CentralConfigurationSummary;
import no.valg.eva.admin.common.configuration.service.CentralConfigurationService;
import no.valg.eva.admin.common.configuration.status.ContestStatus;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.domain.service.MunicipalityDomainService;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Oversikt;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

@Stateless(name = "CentralConfigurationService")
@Remote(CentralConfigurationService.class)
@NoArgsConstructor
public class CentralConfigurationApplicationService implements CentralConfigurationService {

    // Injected
    private MunicipalityDomainService municipalityService;
    private CountryServiceBean countryService;
    private CountyServiceBean countyService;
    private BoroughServiceBean boroughService;
    private PollingDistrictRepository pollingDistrictRepository;
    private VoterServiceBean voterService;
    private ElectionGroupServiceBean electionGroupService;
    private ElectionRepository electionRepository;
    private ElectionEventDomainService electionEventService;
    private ContestServiceBean contestService;
    private ReportingUnitTypeServiceBean reportingUnitTypeService;
    private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;

    @Inject
    public CentralConfigurationApplicationService(MunicipalityDomainService municipalityService, CountryServiceBean countryService, CountyServiceBean countyService,
                                                  BoroughServiceBean boroughService, PollingDistrictRepository pollingDistrictRepository, VoterServiceBean voterService,
                                                  ElectionGroupServiceBean electionGroupService, ElectionRepository electionRepository,
                                                  ElectionEventDomainService electionEventService, ContestServiceBean contestService,
                                                  ReportingUnitTypeServiceBean reportingUnitTypeService, BakgrunnsjobbDomainService bakgrunnsjobbDomainService) {
        this.municipalityService = municipalityService;
        this.countryService = countryService;
        this.countyService = countyService;
        this.boroughService = boroughService;
        this.pollingDistrictRepository = pollingDistrictRepository;
        this.voterService = voterService;
        this.electionGroupService = electionGroupService;
        this.electionRepository = electionRepository;
        this.electionEventService = electionEventService;
        this.contestService = contestService;
        this.reportingUnitTypeService = reportingUnitTypeService;
        this.bakgrunnsjobbDomainService = bakgrunnsjobbDomainService;
    }

	@Override
	@Security(accesses = Konfigurasjon_Oversikt, type = READ)
	public CentralConfigurationSummary getCentralConfigurationSummary(UserData userData) {
		ElectionEvent electionEvent = electionEventService.findByPk(userData.getElectionEventPk());

        CentralConfigurationSummary summary = new CentralConfigurationSummary();
        summary.setHasAnyMunicipalitiesWithNonElectronicMarkOffs(municipalityService.getHasAnyMunicipalitiesWithNonElectronicMarkOffs(userData));
        summary.setCountriesWithoutCounties(countryService.getCountriesWithoutCounties(userData.getElectionEventPk()));
        summary.setCountiesWithoutMunicipalities(countyService.getCountiesWithoutMunicipalities(userData.getElectionEventPk()));
        summary.setMunicipalitiesWithoutBoroughs(municipalityService.getMunicipalitiesWithoutBoroughs(userData.getElectionEventPk()));
        summary.setBoroughsWithoutPollingDistricts(boroughService.getBoroughsWithoutPollingDistricts(userData.getElectionEventPk()));
        summary.setPollingDistrictsWithoutVoters(pollingDistrictRepository.getPollingDistrictsWithoutVoters(userData.getElectionEventPk()));
        summary.setVotersWithoutPollingDistricts(voterService.getVotersWithoutPollingDistricts(userData.getElectionEventPk()));
        summary.setGroupsWithoutElections(electionGroupService.getElectionGroupsWithoutElections(userData.getElectionEventPk()));
        summary.setElectionsWithoutContests(electionRepository.getElectionsWithoutContests(userData.getElectionEventPk()));
        summary.setMunicipalitiesWithoutEncompassingPollingDistricts(
                municipalityService.getMunicipalitiesWithoutEncompassingPollingDistricts(userData.getElectionEventPk()));
        summary.setMunicipalitiesWithoutEncompassingBoroughs(municipalityService.getMunicipalitiesWithoutEncompassingBoroughs(userData.getElectionEventPk()));
        summary.setVoterNumbersHaveBeenGenerated(bakgrunnsjobbDomainService.erManntallsnummergenereringFullfortUtenFeil(electionEvent));
        if (summary.isHasAnyMunicipalitiesWithNonElectronicMarkOffs()) {
            for (Municipality municipality : municipalityService.getMunicipalitiesWithPollingPlacesWithoutPollingStations(userData)) {
                summary.getMunicipalitiesWithPollingPlacesWithoutPollingStations().add(new ConfigurationDto(municipality.getId(), municipality.getName()));
            }
        }
        if (electionEvent.isLocalConfiguration()) {
            summary.setCountiesUnderConfiguration(countyService.getCountiesByStatus(userData.getElectionEventPk(), LOCAL_CONFIGURATION.id()));
            summary.setCountiesApprovedConfiguration(countyService.getCountiesByStatus(userData.getElectionEventPk(),
                    APPROVED_CONFIGURATION.id()));
            summary.setMunicipalitiesUnderConfiguration(municipalityService.getMunicipalitiesByStatus(userData.getElectionEventPk(), LOCAL_CONFIGURATION.id()));
            summary.setMunicipalitiesApprovedConfiguration(municipalityService.getMunicipalitiesByStatus(userData.getElectionEventPk(),
                    APPROVED_CONFIGURATION.id()));
            summary.setContestsUnderConfiguration(contestService.getContestsByStatus(userData.getElectionEventPk(), ContestStatus.LOCAL_CONFIGURATION.id()));
            summary.setContestsFinishedConfiguration(contestService.getContestsByStatus(userData.getElectionEventPk(),
                    ContestStatus.FINISHED_CONFIGURATION.id()));
            summary.setContestsApprovedConfiguration(contestService.getContestsByStatus(userData.getElectionEventPk(),
                    ContestStatus.APPROVED_CONFIGURATION.id()));
        }
        if (!electionEventService.hasGroups(userData.getElectionEventPk())) {
            summary.getElectionEventWithoutGroups().add(electionEvent);
        }
        summary.setReportingUnitTypeDtoList(reportingUnitTypeService.populateReportingUnitTypeDto(electionEvent.getId()));
        for (ReportingUnitTypeDto dto : summary.getReportingUnitTypeDtoList()) {
            if (!dto.getSelectedElections().isEmpty()) {
                summary.setReportingUnitsConfigured(true);
                break;
            }
        }
        return summary;
    }
}
