package no.valg.eva.admin.configuration.domain.service;

import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.application.OpeningHoursSorter;
import no.valg.eva.admin.configuration.application.PollingPlaceMapper;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.valg.eva.admin.configuration.application.OpeningHoursMapper.toDomainModelList;
import static no.valg.eva.admin.configuration.application.OpeningHoursMapper.toDtoList;
import static no.valg.eva.admin.configuration.application.OpeningHoursSorter.toSortedList;
import static no.valg.eva.admin.configuration.application.PollingPlaceMapper.toPollingPlace;

@NoArgsConstructor //For testing
public class PollingPlaceDomainService {

    private static final Logger LOGGER = Logger.getLogger(PollingPlaceDomainService.class);

    @Inject
    private MunicipalityRepository municipalityRepository;

    @Inject
    private PollingDistrictRepository pollingDistrictRepository;

    @Inject
    private PollingStationRepository pollingStationRepository;

    @Inject
    private PollingPlaceRepository pollingPlaceRepository;

    public List<ElectionDayPollingPlace> findElectionDayPollingPlacesByArea(UserData userData, AreaPath areaPath) {
        areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);

        Municipality municipality = getMunicipality(userData, areaPath);

        return getMunicipality(userData, areaPath)
                .regularPollingDistricts(false, true)
                .stream()
                .map(currentPollingDistrict -> toElectionDayPollingPlace(currentPollingDistrict, municipality))
                .collect(toList());
    }

    public ElectionDayPollingPlace findElectionDayPollingPlaceByAreaAndId(UserData userData, AreaPath areaPath, String id) {
        Municipality municipality = getMunicipality(userData, areaPath);
        return toElectionDayPollingPlace(municipality.regularPollingDistrictById(id, false, true), municipality);
    }

    private ElectionDayPollingPlace toElectionDayPollingPlace(PollingDistrict district, Municipality municipality) {
        if (district == null) {
            return null;
        }
        Set<PollingPlace> pollingPlaces = district.getPollingPlaces();
        PollingPlace pollingPlace = pollingPlaces != null && !pollingPlaces.isEmpty() ? pollingPlaces.iterator().next() : null;
        ElectionDayPollingPlace electionDayPollingPlace = PollingPlaceMapper.toElectionDayPollingPlace(district, pollingPlace, toOpeningHoursDto(pollingPlace));
        if (electionDayPollingPlace.isUsePollingStations() && pollingPlace != null) {
            long count = pollingStationRepository.countByPollingPlace(pollingPlace.getPk());
            electionDayPollingPlace.setHasPollingStations(count > 0);
        }

        List<MunicipalityOpeningHour> defaultOpeningHours = municipalityOpeningHours(municipality);
        if (filterPollingPlacesWithCustomOpeningHours(defaultOpeningHours).test(pollingPlace)) {
            electionDayPollingPlace.setOpeningHoursDiffsFromMunicipality(true);
        }

        return electionDayPollingPlace;
    }

    public ElectionDayPollingPlace saveElectionDayPollingPlace(UserData userData, ElectionDayPollingPlace electionDayPollingPlace) {
        Municipality municipality = getMunicipality(userData, electionDayPollingPlace.getPath());
        if (municipality.isElectronicMarkoffs()) {
            electionDayPollingPlace.setUsePollingStations(false);
        }

        PollingDistrict pollingDistrict;
        PollingPlace pollingPlace;
        if (electionDayPollingPlace.getPk() == null) {
            electionDayPollingPlace.getPath().assertLevel(POLLING_DISTRICT);
            pollingDistrict = pollingDistrictRepository.findByPk(electionDayPollingPlace.getParentPk());
            pollingPlace = toPollingPlace(new PollingPlace(), electionDayPollingPlace);
            pollingPlace.setPollingDistrict(pollingDistrict);
            pollingPlace = create(userData, pollingPlace);
        } else {
            electionDayPollingPlace.getPath().assertLevel(POLLING_PLACE);
            pollingPlace = pollingPlaceRepository.findByPk(electionDayPollingPlace.getPk());
            pollingPlace.checkVersion(electionDayPollingPlace);
            pollingDistrict = pollingPlace.getPollingDistrict();
            pollingPlace = toPollingPlace(pollingPlace, electionDayPollingPlace);
            pollingPlace = pollingPlaceRepository.update(userData, pollingPlace);
        }

        saveOpeningHoursForPollingPlace(userData, pollingPlace, toDomainModelList(electionDayPollingPlace.getOpeningHours()));

        return PollingPlaceMapper.toElectionDayPollingPlace(pollingDistrict, pollingPlace, toOpeningHoursDto(pollingPlace));
    }

    private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
        return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
    }

    List<OpeningHours> toOpeningHoursDto(PollingPlace pollingPlace) {
        List<no.valg.eva.admin.configuration.domain.model.OpeningHours> pollingPlaceOpeningHours = new ArrayList<>();
        if (pollingPlace != null) {
            pollingPlaceOpeningHours.addAll(pollingPlace.getOpeningHours());
        }

        return toDtoList(pollingPlaceOpeningHours);
    }

    public void saveOpeningHoursForPollingPlacesInArea(UserData userData, Municipality municipality,
                                                       List<no.valg.eva.admin.configuration.domain.model.OpeningHours> defaultOpeningHourList,
                                                       AreaPath selectedAreaPath,
                                                       boolean shouldOverwriteExisting) {
        List<PollingPlace> allPollingPlacesForArea = findPollingPlacesByArea(userData, selectedAreaPath);
        List<PollingPlace> pollingPlacesWithCustomOpeningHours = pollingPlacesWithCustomOpeningHours(municipality, allPollingPlacesForArea);

        allPollingPlacesForArea.forEach(currentPollingPlace -> {
            if (shouldOverwriteExisting || noCustomOpeningHours(currentPollingPlace, pollingPlacesWithCustomOpeningHours)) {
                saveOpeningHoursForPollingPlace(userData, currentPollingPlace, defaultOpeningHourList);
            }
        });
    }

    private boolean noCustomOpeningHours(PollingPlace pollingPlace, List<PollingPlace> pollingPlacesWithCustomOpeningHours) {
        return !pollingPlacesWithCustomOpeningHours.contains(pollingPlace);
    }

    private List<PollingPlace> findPollingPlacesByArea(UserData userData, AreaPath areaPath) {
        areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
        return getMunicipality(userData, areaPath)
                .regularPollingDistricts(false, true)
                .stream()
                .flatMap(pollingDistrict -> pollingDistrict.getPollingPlaces().stream())
                .collect(toList());
    }

    void saveOpeningHoursForPollingPlace(UserData userData, PollingPlace pollingPlace,
                                         List<no.valg.eva.admin.configuration.domain.model.OpeningHours> openingHours) {
        try {

            PollingPlace domainPollingPlace = pollingPlaceRepository.getReference(pollingPlace);
            pollingPlaceRepository.deleteOpeningHours(userData, pollingPlace);

            domainPollingPlace.addOpeningHours(openingHours);
            pollingPlaceRepository.update(userData, domainPollingPlace);
        } catch (Exception e) {
            LOGGER.error("Exception saving default opening hours[userData: " + userData + ", openingHours: " + openingHours + ": " + e.getMessage(), e);
            throw new EvoteException(e.getMessage(), e);
        }
    }

    public PollingPlace create(UserData userData, PollingPlace pollingPlace) {
        // Checks if the electionDayVoting already exists
        PollingPlace pollingPlaceByElectionDayVoting = pollingPlaceRepository.findPollingPlaceByElectionDayVoting(pollingPlace.getPollingDistrict().getPk());
        if (pollingPlace.isElectionDayVoting()
                && pollingPlaceByElectionDayVoting != null) {
            throw new EvoteException("@common.message.evote_application_exception.DUPLICATE_ELECTION_DAY_VOTING");
        }
        return pollingPlaceRepository.create(userData, pollingPlace);
    }

    public List<ElectionDayPollingPlace> pollingPlacesWithCustomOpeningHours(UserData userData, Municipality municipality, AreaPath areaPath) {
        List<PollingPlace> pollingPlaces = findPollingPlacesByArea(userData, areaPath);
        return pollingPlacesWithCustomOpeningHours(municipality, pollingPlaces).stream()
                .map(currentPollingPlace ->
                        PollingPlaceMapper
                                .toElectionDayPollingPlace(currentPollingPlace.getPollingDistrict(), currentPollingPlace, toOpeningHoursDto(currentPollingPlace)))
                .collect(toList());
    }

    private List<PollingPlace> pollingPlacesWithCustomOpeningHours(Municipality municipality,
                                                                   List<PollingPlace> pollingPlacesForArea) {
        List<MunicipalityOpeningHour> municipalityOpeningHours = municipalityOpeningHours(municipality);

        return pollingPlacesForArea.stream()
                .filter(filterPollingPlacesWithCustomOpeningHours(municipalityOpeningHours))
                .collect(toList());
    }

    private List<MunicipalityOpeningHour> municipalityOpeningHours(Municipality municipality) {
        Municipality dbMunicipality = municipalityRepository.findByPk(municipality.getPk());
        return OpeningHoursSorter.toSortedMunicipalityOpeningHourList(dbMunicipality.getOpeningHours());
    }

    Predicate<PollingPlace> filterPollingPlacesWithCustomOpeningHours(List<MunicipalityOpeningHour> defaultOpeningHours) {
        return pollingPlace -> {
            if (pollingPlace == null || pollingPlace.getOpeningHours() == null || pollingPlace.getOpeningHours().isEmpty()) {
                return false;
            }

            final List<no.valg.eva.admin.configuration.domain.model.OpeningHours> pollingPlaceOpeningHours = toSortedList(pollingPlace.getOpeningHours());

            if (defaultOpeningHours.size() != pollingPlaceOpeningHours.size()) {
                return true;
            } else {
                for (int i = 0; i < defaultOpeningHours.size(); i++) {
                    MunicipalityOpeningHour defaultOpeningHour = defaultOpeningHours.get(i);
                    no.valg.eva.admin.configuration.domain.model.OpeningHours pollingPlaceOpeningHour = pollingPlaceOpeningHours.get(i);

                    if (!defaultOpeningHour.sameDayAndTime(pollingPlaceOpeningHour)) {
                        return true;
                    }
                }
            }

            return false;
        };
    }
}
