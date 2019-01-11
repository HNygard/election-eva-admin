package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.configuration.application.OpeningHoursSorter.toSortedMunicipalityOpeningHourList;

public class MunicipalityDomainService {

    private static final int MAX_NUMBER_OF_OPENING_HOURS = 2;

    @Inject
    private CountryRepository countryRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private ContestRepository contestRepository;
    @Inject
    private ContestAreaRepository contestAreaRepository;

    public List<Municipality> getMunicipalitiesWithoutBoroughs(Long electionEventPk) {
        List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEventPk);
        return municipalityRepository.findWithoutBoroughsByCountries(countries);
    }

    public boolean getHasAnyMunicipalitiesWithNonElectronicMarkOffs(UserData userData) {
        for (MvArea municipalityMvArea : mvAreaRepository.findByPathAndLevel(userData.getOperatorRole().getMvArea().areaPath(), MUNICIPALITY)) {
            if (!municipalityMvArea.getMunicipality().isElectronicMarkoffs()) {
                return true;
            }
        }
        return false;
    }

    public List<Municipality> getMunicipalitiesWithoutEncompassingPollingDistricts(Long electionEventPk) {
        return municipalityRepository.getMunicipalitiesWithoutEncompassingPollingDistricts(electionEventPk);
    }

    public List<Municipality> getMunicipalitiesWithoutEncompassingBoroughs(Long electionEventPk) {
        return municipalityRepository.getMunicipalitiesWithoutEncompassingBoroughs(electionEventPk);
    }

    public List<Municipality> getMunicipalitiesWithPollingPlacesWithoutPollingStations(UserData userData) {
        return municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(userData);
    }

    public List<Municipality> getMunicipalitiesByStatus(Long electionEventPk, Integer status) {
        return municipalityRepository.getMunicipalitiesByStatus(electionEventPk, status);
    }

    public List<MunicipalityOpeningHour> getOpeningHours(Municipality municipality) {
        Municipality dbMunicipality = municipalityRepository.findByPk(municipality.getPk());

        return toSortedMunicipalityOpeningHourList(dbMunicipality.getOpeningHours());
    }

    public void saveOpeningHours(UserData userData, Municipality municipality,
                                 List<MunicipalityOpeningHour> updatedOpeningHours) {

        Municipality domainMunicipality = municipalityRepository.getReference(municipality);
        municipalityRepository.deleteOpeningHours(userData, domainMunicipality);

        if (!updatedOpeningHours.isEmpty()) {
            for (MunicipalityOpeningHour newOpeningHour : updatedOpeningHours) {
                ElectionDay currentElectionDay = electionEventRepository.getReference(ElectionDay.class, newOpeningHour.getElectionDay().getPk());
                domainMunicipality.addOpeningHours(newOpeningHour(newOpeningHour, currentElectionDay));
            }
        }

        municipalityRepository.update(userData, domainMunicipality);
    }

    private MunicipalityOpeningHour newOpeningHour(MunicipalityOpeningHour currentUpdatedOpeningHours, ElectionDay currentElectionDay) {
        MunicipalityOpeningHour newOpeningHour = new MunicipalityOpeningHour();
        newOpeningHour.setElectionDay(currentElectionDay);
        newOpeningHour.setStartTime(currentUpdatedOpeningHours.getStartTime());
        newOpeningHour.setEndTime(currentUpdatedOpeningHours.getEndTime());
        return newOpeningHour;
    }

    boolean tooManyOpeningHours(List<OpeningHours> openingHours) {
        return openingHours.size() > MAX_NUMBER_OF_OPENING_HOURS;
    }

    boolean isPersisted(no.valg.eva.admin.configuration.domain.model.OpeningHours openingHours) {
        return openingHours.getPk() != null;
    }

    public boolean isMinusThirtyMunicipality(ElectionEvent electionEvent, Municipality municipality) {
        Optional<ContestArea> contestAreaOptional = findContest(electionEvent, municipality);
        boolean hasMultiAreaDistricts = contestRepository.antallMultiomraadedistrikter(electionEvent) > 0;

        return hasMultiAreaDistricts &&
                (contestAreaOptional.isPresent() && contestAreaOptional.get().isChildArea());
    }

    private Optional<ContestArea> findContest(ElectionEvent electionEvent, Municipality municipality) {
        List<ContestArea> contestAreas = contestAreaRepository.finnForValghendelseMedValgdistrikt(electionEvent);

        return contestAreas.stream()
                .filter(ca -> ca.getMvArea().getAreaLevel() == AreaLevelEnum.MUNICIPALITY.getLevel())
                .filter(ca -> ca.getMvArea().getMunicipalityId().equals(municipality.getId()))
                .findFirst();
    }
}
