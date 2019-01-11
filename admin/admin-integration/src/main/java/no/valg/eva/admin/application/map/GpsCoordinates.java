package no.valg.eva.admin.application.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Getter
@AllArgsConstructor
public class GpsCoordinates {

    private double lat;
    private double lng;
    
    @Override
    public String toString() {
        DecimalFormat decimalFormat = configureDecimalFormat();
        return decimalFormat.format(lat) + ", " + decimalFormat.format(lng);
    }

    private DecimalFormat configureDecimalFormat() {
        DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        return decimalFormat;
    }

}
