package no.valg.eva.admin.application.map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GpsCoordinatesTest {

    @Test(dataProvider = "gpsCoordinates")
    public void toString_givenCoordinates_returnFormattedString(GpsCoordinates gpsCoordinates, String expected) {
        assertThat(gpsCoordinates.toString()).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] gpsCoordinates() {
        return new Object[][]{
                {new GpsCoordinates(59.123456789, 11.123456789), "59.12346, 11.12346"},
                {new GpsCoordinates(59.12, 11.12), "59.12, 11.12"},
                {new GpsCoordinates(0, 0), "0, 0"},
        };
    }
}