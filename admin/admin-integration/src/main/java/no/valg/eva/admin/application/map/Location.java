package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.common.configuration.model.County;
import no.valg.eva.admin.common.configuration.model.Municipality;


@Getter
@Builder
@ToString
public class Location implements HasGpsCoordinates {
    private String nameType;
    private String locationName;
    private Municipality municipality;
    private County county;
    private GpsCoordinates gpsCoordinates;
}
