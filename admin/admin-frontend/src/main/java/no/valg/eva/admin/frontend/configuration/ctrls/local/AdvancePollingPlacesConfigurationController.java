package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.Getter;
import no.valg.eva.admin.application.MapService;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.frontend.common.DeleteAction;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;

import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.valg.eva.admin.frontend.configuration.ctrls.local.AddressLookupComponent.canLookupCoordinatesFor;
import static no.valg.eva.admin.frontend.configuration.ctrls.local.AddressLookupComponent.findGpsCoordinatesForPlace;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Named
@ViewScoped

public class AdvancePollingPlacesConfigurationController extends PlacesConfigurationController<AdvancePollingPlace> implements DeleteAction {

    private static final long serialVersionUID = 4790532486290977417L;

    @Inject
    @Getter
    private MapService mapService;

    @Override
    public ConfigurationView getView() {
        return ConfigurationView.ADVANCE_POLLING_PLACES;
    }

    @Override
    public String getName() {
        return "@config.local.accordion.advance_polling_place.name";
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean isRequiresDone() {
        return false;
    }

    @Override
    boolean hasAccess() {
        return isMunicipalityLevel();
    }

    @Override
    void setDoneStatus(boolean value) {
        if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setAdvancePollingPlaces(value);
        }
    }

    @Override
    public boolean isDoneStatus() {
        return isMunicipalityLevel() && getMunicipalityConfigStatus().isAdvancePollingPlaces();
    }

    @Override
    boolean canBeSetToDone() {
        if (getPlaces() == null || getPlaces().isEmpty()) {
            return false;
        }
        for (AdvancePollingPlace place : getPlaces()) {
            if (!place.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    List<AdvancePollingPlace> collectPollingPlaces() {
        return getPollingPlaceService().findAdvancePollingPlacesByArea(getUserData(), getAreaPath());
    }

    @Override
    AdvancePollingPlace collectPollingPlace(String id) {
        return getPollingPlaceService().findAdvancePollingPlaceByAreaAndId(getUserData(), getAreaPath(), id);
    }

    @Override
    AdvancePollingPlace save(AdvancePollingPlace pollingPlace) {
        return getPollingPlaceService().saveAdvancePollingPlace(getUserData(), getMainController().getElectionGroup().getElectionGroupPath(), pollingPlace);
    }

    @Override
    public void confirmDelete() {
        execute(() -> {
            saveDone(false);
            getPollingPlaceService().deleteAdvancePollingPlace(getUserData(), getPlace());
            MessageUtil.buildDeletedMessage(getPlace());
            setPlace(null);
            collectData();
        });
    }

    /**
     * Prepare for new polling place.
     */
    public void initCreate() {
        setPlace(new AdvancePollingPlace(getAreaPath()));
        getPlace().setPublicPlace(true);
        setMode(ConfigurationMode.CREATE);
    }

    public void doAddressLookup(@SuppressWarnings("unused") AjaxBehaviorEvent abe) {

        if (getPlace() != null && getPlace().isPublicPlace() && canLookupCoordinatesFor(getPlace())) {

            getPlace().setGpsCoordinates(findGpsCoordinatesForPlace(getPlace(), getMvArea(), mapService));

            if (isBlank(getPlace().getGpsCoordinates())) {
                MessageUtil.buildMessageForClientId(
                        getFormComponentId("gpsCoordinates"),
                        getMessageProvider().get("@count.error.gpsCoordinatesNotFound"),
                        FacesMessage.SEVERITY_ERROR
                );
            }

            FacesUtil.updateDom(asList(
                    getFormComponentId("gpsCoordinates"),
                    getFormComponentId("gpsCoordinatesError"),
                    getFormComponentId("map")
            ));
        }
    }

    private String getFormComponentId(String componentId) {
        return format("%s:%s", getFormId(), componentId);
    }

    private String getFormId() {
        return getWithBaseId("advancePollingPlace:form");
    }
}
