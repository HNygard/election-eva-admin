package no.valg.eva.admin.frontend.common;

import lombok.experimental.UtilityClass;
import org.apache.log4j.Logger;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import static java.lang.Double.parseDouble;

@UtilityClass
public class MapModelMaker {

    private static final Logger LOGGER = Logger.getLogger(MapModelMaker.class);

    public static void addMarkerOverlay(MapModel mapModel, String name, String gpsCoordinates) {
        if (mapModel == null || name == null || gpsCoordinates == null) {
            return;
        }

        mapModel.getMarkers().clear();
        LatLng latLng = parseLatLng(gpsCoordinates);
        if (latLng != null) {
            mapModel.addOverlay(new Marker(latLng, name));
        }
    }

    private static LatLng parseLatLng(String latitudeAndLongitudeAsString) {
        try {
            String[] splitLatLng = latitudeAndLongitudeAsString.split(",");
            double lat = parseDouble(splitLatLng[0]);
            double lng = parseDouble(splitLatLng[1]);
            return new LatLng(lat, lng);
        } catch (Exception e) {
            LOGGER.error("Exception parsing gps coordinates: " + e.getMessage(), e);
            return null;
        }
    }

}
