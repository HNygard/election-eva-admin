package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.common.configuration.model.local.PollingPlace;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.MapModelMaker;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverter;
import no.valg.eva.admin.frontend.configuration.converters.PlaceConverterSource;
import org.primefaces.model.map.DefaultMapModel;

import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import java.util.List;

import static no.evote.presentation.validation.GpsCoordinatesValidator.isValidGpsCoordinate;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.READ;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;
import static org.apache.commons.lang3.StringUtils.isEmpty;


public abstract class PlacesConfigurationController<T extends Place> extends ConfigurationController implements PlaceConverterSource<T> {

    @Inject
    private PollingPlaceService pollingPlaceService;
    @Inject
    private PollingDistrictService pollingDistrictService;

    private PlaceConverter placeConverter = new PlaceConverter(this);
    private DefaultMapModel mapModel = new DefaultMapModel();
    private T place;
    private List<T> places;

    @Override
    public void init() {
        setMode(READ);
        mapModel = new DefaultMapModel();
        collectData();
    }

    public String getValidGpsCoordinate(String gpsCoordinate) {
        if (isEmpty(gpsCoordinate) || !isValidGpsCoordinate(gpsCoordinate)) {
            return null;
        }
        return gpsCoordinate;
    }

    @Override
    public PlaceConverter getPlaceConverter() {
        return placeConverter;
    }

    @Override
    public List<T> getPlaces() {
        return places;
    }

    @Override
    public void cancelWrite() {
        super.cancelWrite();
        collectData();
    }

    @Override
    public Button button(ButtonType type) {
        switch (type) {
            case CREATE:
                return buttonTypeCreate();
            case EXECUTE_CREATE:
            case EXECUTE_UPDATE:
            case CANCEL:
                return buttonTypeCreateUpdateOrCancel();
            case UPDATE:
                return buttonTypeUpdate();
            case DELETE:
            case EXECUTE_DELETE:
                return buttonTypeDelete();
            default:
                return super.button(type);
        }
    }

    private Button buttonTypeDelete() {
        if (UPDATE.equals(getMode())) {
            return enabled(isEditable());
        }
        return notRendered();
    }

    private Button buttonTypeUpdate() {
        if (READ.equals(getMode())) {
            return enabled(isEditable());
        }
        return notRendered();
    }

    private Button buttonTypeCreateUpdateOrCancel() {
        if (isWriteMode()) {
            return enabled(isEditable());
        }
        return notRendered();
    }

    private Button buttonTypeCreate() {
        return enabled(isEditable() && !isWriteMode());
    }

    @SuppressWarnings("unchecked")
    public void placeSelected(ValueChangeEvent event) {
        T pp = (T) event.getNewValue();
        if (pp != null) {
            place = collectPollingPlace(pp.getId());
            setMarker();
            setMode(READ);
        }
    }

    /**
     * Save new or updated place
     */
    public void saveModel() {
        if (place == null) {
            return;
        }
        if (saveDone(false) && !execute(() -> {
            place = save(place);
            MessageUtil.buildSavedMessage(place);
            collectData();
        })) {
            setMode(UPDATE);
        }
    }

    void collectData() {
        places = collectPollingPlaces();
        if (places.isEmpty()) {
            place = null;
        } else {
            if (place != null) {
                setCurrentPlace();
            }
            if (place == null && !places.isEmpty()) {
                place = places.get(0);
            }
        }
        setMarker();
    }

    private void setCurrentPlace() {
        String id = place.getId();
        place = null;
        for (T pollingPlace : places) {
            if (pollingPlace.getId().equals(id)) {
                place = pollingPlace;
                break;
            }
        }
    }

    public T getPlace() {
        return place;
    }

    public void setPlace(T pollingPlace) {
        this.place = pollingPlace;
    }

    public DefaultMapModel getMapModel() {
        return mapModel;
    }

    PollingPlaceService getPollingPlaceService() {
        return pollingPlaceService;
    }

    PollingDistrictService getPollingDistrictService() {
        return pollingDistrictService;
    }

    void setMarker() {
        if ((place != null) && (place instanceof PollingPlace) && getValidGpsCoordinate(((PollingPlace) place).getGpsCoordinates()) != null) {
            MapModelMaker.addMarkerOverlay(
                    mapModel,
                    place.getName(),
                    ((PollingPlace) place).getGpsCoordinates()
            );
        }
    }

    abstract List<T> collectPollingPlaces();

    abstract T collectPollingPlace(String id);

    abstract T save(T place);
}
