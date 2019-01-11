package no.valg.eva.admin.frontend.configuration.models;

import lombok.Getter;
import lombok.Setter;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.BOROUGH;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.MUNICIPALITY;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.POLLING_PLACE;
import static no.valg.eva.admin.util.StringUtil.isSet;

@Getter
@Setter
public class ElectionDayPollingPlaceViewModel implements Displayable, Serializable {

    private static final long serialVersionUID = 3212244842391785063L;

    public enum ViewModelType {
        MUNICIPALITY, BOROUGH, POLLING_PLACE
    }

    private ViewModelType type;
    private boolean valid;
    private int version;
    private Long pk;
    private String id;
    private AreaPath areaPath;
    private String name;
    private String pollingDistrictName;
    private long pollingDistrictPk;
    private String address;
    private String postalCode;
    private String postTown;
    private String gpsCoordinates;
    private boolean usePollingStations;
    private boolean openingHoursDiffsFromMunicipality;
    private List<ElectionDayViewModel> electionDayViewModels = new ArrayList<>();
    private String infoText;

    public ElectionDayPollingPlaceViewModel(Municipality municipality, List<OpeningHours> municipalityOpeningHours, List<ElectionDay> electionDays) {
        this.version = municipality.getAuditOplock();
        this.type = ViewModelType.MUNICIPALITY;
        this.pk = municipality.getPk();
        this.id = municipality.getId();
        this.name = municipality.getName();
        setElectionDayViewModels(municipalityOpeningHours, electionDays);
    }

    public ElectionDayPollingPlaceViewModel(Borough borough) {
        this.version = borough.getVersion();
        this.type = BOROUGH;
        this.pk = borough.getPk();
        this.id = borough.getId();
        this.areaPath = borough.getPath();
        this.name = borough.getName();
    }

    public ElectionDayPollingPlaceViewModel(ElectionDayPollingPlace pollingPlace, List<ElectionDay> electionDays) {
        this.version = pollingPlace.getVersion();
        this.type = ViewModelType.POLLING_PLACE;
        this.pk = pollingPlace.getPk();
        this.id = pollingPlace.getId();
        this.areaPath = pollingPlace.getPath();
        this.name = pollingPlace.getName();
        this.pollingDistrictName = pollingPlace.getParentName();
        this.pollingDistrictPk = pollingPlace.getParentPk();
        this.address = pollingPlace.getAddress();
        this.postalCode = pollingPlace.getPostalCode();
        this.postTown = pollingPlace.getPostTown();
        this.gpsCoordinates = pollingPlace.getGpsCoordinates();
        this.usePollingStations = pollingPlace.isUsePollingStations();
        this.openingHoursDiffsFromMunicipality = pollingPlace.isOpeningHoursDiffsFromMunicipality();
        this.infoText = pollingPlace.getInfoText();
        setElectionDayViewModels(pollingPlace.getOpeningHours(), electionDays);
    }

    private void setElectionDayViewModels(List<OpeningHours> openingHours, List<ElectionDay> electionDays) {
        buildElectionDayViewModels(openingHours);
        addDefaultOpeningHoursIfAppropriate(electionDays);
        sortElectionDayViewModels();
    }

    private void buildElectionDayViewModels(List<OpeningHours> openingHours) {

        openingHours.forEach(currentOpeningHours -> {

            ElectionDayViewModel currentViewModel = electionDayViewModels.stream()
                    .filter(vm -> vm.getElectionDay().getPk().equals(currentOpeningHours.getElectionDay().getPk()))
                    .findFirst()
                    .orElse(null);

            if (currentViewModel == null) {
                currentViewModel = ElectionDayViewModel.builder()
                        .activated(true)
                        .electionDay(currentOpeningHours.getElectionDay())
                        .build();
                electionDayViewModels.add(currentViewModel);
            }

            currentViewModel.addOpeningHour(currentOpeningHours);
        });
    }

    private void addDefaultOpeningHoursIfAppropriate(List<ElectionDay> electionDays) {
        electionDays.stream()
                .filter(this::electionDaysWithoutOpeningHours)
                .forEach(electionDay -> electionDayViewModels.add(getDefaultOpeningHours(electionDay)));
    }

    private void sortElectionDayViewModels() {
        electionDayViewModels.sort(comparing(o -> o.getElectionDay().getDate()));
        electionDayViewModels.forEach(vm -> vm.getOpeningHours().sort(comparing(OpeningHours::getStartTime)));
    }

    private boolean electionDaysWithoutOpeningHours(ElectionDay electionDay) {
        return electionDayViewModels.stream()
                .noneMatch(currentModel -> currentModel.getElectionDay().getPk().equals(electionDay.getPk()));
    }

    private ElectionDayViewModel getDefaultOpeningHours(ElectionDay electionDay) {
        final ElectionDayViewModel viewModel = ElectionDayViewModel.builder()
                .activated(false)
                .electionDay(electionDay)
                .build();

        viewModel.addOpeningHour(
                OpeningHours.builder()
                        .electionDay(electionDay)
                        .startTime(electionDay.getStartTime())
                        .endTime(electionDay.getEndTime())
                        .build()
        );
        return viewModel;
    }

    public String getLabel() {
        String labelPrefix = getId() + " - ";
        switch (type) {
            case MUNICIPALITY:
                return labelPrefix + getName();
            case BOROUGH:
                return getName();
            default:
                return labelPrefix + getPollingDistrictName();
        }
    }

    public boolean hasCustomOpeningHours() {
        return type == POLLING_PLACE && openingHoursDiffsFromMunicipality;
    }

    public boolean hasValidData() {
        switch (type) {
            case MUNICIPALITY:
                return pk != null && isSet(id, name) && doesSuccessfullyValidateOpeningHours();
            case BOROUGH:
                return pk != null && isSet(id, name);
            default:
                return pk != null && isSet(id, name, address, postalCode, postTown, gpsCoordinates) && doesSuccessfullyValidateOpeningHours();
        }
    }

    private boolean doesSuccessfullyValidateOpeningHours() {
        try {
            validateOpeningHours();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void validateOpeningHours() {

        boolean noElectionDaysChecked = electionDayViewModels.stream().noneMatch(ElectionDayViewModel::isActivated);

        if (noElectionDaysChecked && type == MUNICIPALITY) {
            return; // Opening hours on municipality level are optional
        } else if (noElectionDaysChecked) {
            throw new EvoteException("@config.local.election_day_polling_place.validate.missing_opening_hours");
        } else if (lastElectionDayNotActivated()) {
            throw new EvoteException("@config.local.election_day_polling_place.validate.missing_max_when_one_selected");
        }

        electionDayViewModels.stream()
                .filter(ElectionDayViewModel::isActivated)
                .forEach(ElectionDayViewModel::validate);
    }

    private boolean lastElectionDayNotActivated() {
        return !electionDayViewModels.get(electionDayViewModels.size() - 1).isActivated();
    }

    @Override
    public String display() {
        return name;
    }

    public ElectionDayPollingPlace toDto() {
        ElectionDayPollingPlace dto = new ElectionDayPollingPlace(areaPath, version);
        dto.setPk(getPk());
        dto.setId(getId());
        dto.setName(getName());
        dto.setAddress(getAddress());
        dto.setPostalCode(getPostalCode());
        dto.setPostTown(getPostTown());
        dto.setGpsCoordinates(getGpsCoordinates());
        dto.setOpeningHours(activatedOpeningHours());
        dto.setUsePollingStations(isUsePollingStations());
        dto.setInfoText(getInfoText());
        dto.setParentPk(getPollingDistrictPk());
        return dto;
    }

    private List<OpeningHours> activatedOpeningHours() {
        return getElectionDayViewModels().stream()
                .filter(ElectionDayViewModel::isActivated)
                .flatMap(ed -> ed.getOpeningHours().stream())
                .collect(Collectors.toList());
    }


    public boolean sameTypeAndPk(ElectionDayPollingPlaceViewModel otherViewModel) {
        return otherViewModel != null
                && (samePollingDistrict(otherViewModel)
                || samePollingPlace(otherViewModel));
    }

    private boolean samePollingPlace(ElectionDayPollingPlaceViewModel otherViewModel) {
        return getType() == otherViewModel.getType()
                && getPk() != null
                && getPk().equals(otherViewModel.getPk());
    }

    private boolean samePollingDistrict(ElectionDayPollingPlaceViewModel otherViewModel) {
        return otherViewModel != null &&
                otherViewModel.getPollingDistrictName() != null
                && otherViewModel.getPollingDistrictPk() == getPollingDistrictPk();
    }
}
