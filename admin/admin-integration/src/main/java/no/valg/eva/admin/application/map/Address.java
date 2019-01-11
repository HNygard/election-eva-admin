package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.common.configuration.model.Municipality;

@Getter
@Builder
@ToString
public class Address implements HasGpsCoordinates {
    private String streetName;
    private String houseNumber;
    private String houseLetter;
    private String postalCode;
    private String postTown;
    private Municipality municipality;
    private GpsCoordinates gpsCoordinates;
}
