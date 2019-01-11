package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.validation.PollingStationsDivisionValidator;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.common.configuration.service.PollingStationService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.frontend.common.Button.enabled;

@Named
@ViewScoped
public class PollingStationConfigurationController extends PlacesConfigurationController<ElectionDayPollingPlace> {

    // Injected
    private PollingStationService pollingStationService;

    private List<Rode> pollingStations = new ArrayList<>();
    private Boolean voterNumbersGenerated;
    private boolean dirty;

    public PollingStationConfigurationController() {
        // For CDI
    }

    @Inject
    public PollingStationConfigurationController(PollingStationService pollingStationService) {
        this.pollingStationService = pollingStationService;
    }

    @Override
    public void init() {
        super.init();
        if (getVoterNumbersGenerated()) {
            String message = getMessageProvider().get("@config.local.manntallsnummerErGenerert",
                    getUserDataController().getElectionEvent().getName());
            MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_ERROR);
        }
        if (getPlaces().isEmpty()) {
            String message = getMessageProvider().get("@config.local.polling_station.noPollingStationsConfigured");
            MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_INFO);
        } else {
            if (getPlace() != null) {
                collectPollingPlace(getPlace().getId());
            }
        }
    }

    @Override
    public ConfigurationView getView() {
        return ConfigurationView.POLLING_STATION;
    }

    @Override
    public String getName() {
        return "@config.local.accordion.polling_stations.name";
    }

    @Override
    boolean hasAccess() {
        if (!isMunicipalityLevel()) {
            return false;
        }
        if (isUseElectronicMarkoffsConfigured()) {
            return false;
        }
        return getController(ElectionDayPollingPlacesConfigurationController.class).isDoneStatus() && hasPollingStations();
    }

    private boolean hasPollingStations() {
        return !collectPollingPlaces().isEmpty();
    }

    @Override
    public Button button(ButtonType type) {
        int maxPollingStations = EvoteConstants.ALPHABET.length() / 2;
        switch (type) {
            case PREV:
                return enabled(isEditable() && getPlace() != null && getPollingStations().size() > 1);
            case NEXT:
                return enabled(isEditable() && getPlace() != null && getPollingStations().size() < maxPollingStations);
            case UPDATE:
                return enabled(isEditable() && getPlace() != null && isWriteMode() && isDirty());
            case SAVE:
                return enabled(isEditable() && getPlace() != null && !isWriteMode() && isDirty());
            case CANCEL:
                return enabled(isEditable() && getPlace() != null && isDirty());
            case DONE:
                if (getVoterNumbersGenerated()) {
                    return enabled(true);
                }
                return super.button(type);
            default:
                return super.button(type);
        }
    }

    @Override
    public boolean isEditable() {
        return isParentEditable() && !getVoterNumbersGenerated();
    }

    @Override
    void setDoneStatus(boolean value) {
        if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setPollingStations(value);
        }
    }

    @Override
    public boolean isDoneStatus() {
        return isMunicipalityLevel() && getMunicipalityConfigStatus().isPollingStations();
    }

    @Override
    boolean canBeSetToDone() {
        if (getVoterNumbersGenerated()) {
            return true;
        }
        if (isDirty()) {
            return false;
        }
        for (ElectionDayPollingPlace place : getPlaces()) {
            if (!place.isHasPollingStations()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void cancelWrite() {
        super.cancelWrite();
        dirty = false;
        if (getPlace() != null) {
            collectPollingPlace(getPlace().getId());
        }
    }

    @Override
    public void setUpdateMode() {
        super.setUpdateMode();
        dirty = true;
        unlockAndUpdateDOM();
    }

    @Override
    List<ElectionDayPollingPlace> collectPollingPlaces() {
        return getPollingPlaceService().findElectionDayPollingPlacesByArea(getUserData(), getAreaPath()).stream()
                .filter(ElectionDayPollingPlace::isUsePollingStations)
                .collect(Collectors.toList());
    }

    @Override
    ElectionDayPollingPlace collectPollingPlace(String id) {
        for (ElectionDayPollingPlace place : getPlaces()) {
            if (place.getId().equals(id)) {
                pollingStations = pollingStationService.findPollingStationsByArea(getUserData(), place.getPath());
                if (pollingStations == null) {
                    pollingStations = new ArrayList<>();
                }
                place.setHasPollingStations(!pollingStations.isEmpty());
                return place;
            }
        }
        return null;
    }

    @Override
    ElectionDayPollingPlace save(ElectionDayPollingPlace place) {
        return place;
    }

    @Override
    Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
        if (getMainController().getElectionGroup().isElectronicMarkoffs() && !getVoterNumbersGenerated()) {
            return new Class[]{ElectronicMarkoffsConfigurationController.class};
        }
        return new Class[0];
    }

    @Override
    Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
        if (getVoterNumbersGenerated()) {
            return new Class[0];
        }
        return new Class[]{ElectionDayPollingPlacesConfigurationController.class};
    }

    public Boolean getVoterNumbersGenerated() {
        if (voterNumbersGenerated == null) {
            voterNumbersGenerated = checkIsVoterNumbersGenerated();
        }
        return voterNumbersGenerated;
    }

    public void addPollingStation() {
        if (isEditable()) {
            execute(() -> changeNumberOfPollingStations(pollingStations.size() + 1));
        }
    }

    public void subtractPollingStation() {
        if (isEditable()) {
            execute(() -> changeNumberOfPollingStations(pollingStations.size() - 1));
        }
    }

    public void recalculateChanges() {
        if (!isEditable() || !divisionListIsValid(pollingStations)) {
            return;
        }
        execute(() -> {
            pollingStations = pollingStationService.recalculatedPollingStationsByArea(getUserData(), getPlace().getPath(), pollingStations);
            MessageUtil.buildDetailMessage("@config.local.polling_station.recalculateChangesOK", FacesMessage.SEVERITY_INFO);
            checkRecalculation();
            setMode(ConfigurationMode.READ);
        });
    }

    public void saveChanges() {
        if (!isEditable() || !divisionListIsValid(pollingStations)) {
            return;
        } else {
            execute(() -> {
                pollingStations = pollingStationService.save(getUserData(), getPlace().getPath(), pollingStations);
                MessageUtil.buildDetailMessage("@config.local.polling_station.saveChangesOK", FacesMessage.SEVERITY_INFO);
                checkRecalculation();
                dirty = false;
            });
        }
    }

    public List<Rode> getPollingStations() {
        return pollingStations;
    }

    public boolean isDirty() {
        return dirty;
    }

    private void changeNumberOfPollingStations(int number) {
        unlockAndUpdateDOM();
        pollingStations = pollingStationService.findPollingStationsByAreaCalculated(getUserData(), getPlace().getPath(), number);
        dirty = true;
        setMode(ConfigurationMode.READ);
        checkRecalculation();
    }

    @Override
    public void unlockAndUpdateDOM() {
        if (isDoneStatus()) {
            super.unlockAndUpdateDOM();
        } else {
            FacesUtil.updateDom(Arrays.asList(getWithBaseId("pollingStation:form")));
        }
    }

    private void checkRecalculation() {
        if (pollingStations == null) {
            MessageUtil.buildDetailMessage("@config.polling_stations.emptyElectoralRoll", FacesMessage.SEVERITY_ERROR);
            pollingStations = new ArrayList<>();
        }
        getPlace().setHasPollingStations(!pollingStations.isEmpty());
    }

    private boolean divisionListIsValid(final List<Rode> divisionList) {
        PollingStationsDivisionValidator validator = new PollingStationsDivisionValidator();
        if (!validator.isValid(divisionList)) {
            MessageUtil.buildDetailMessage(validator.getValidationFeedback(), FacesMessage.SEVERITY_ERROR);
            return false;
        }
        return true;
    }

    boolean isParentEditable() {
        return super.isEditable();
    }
}
