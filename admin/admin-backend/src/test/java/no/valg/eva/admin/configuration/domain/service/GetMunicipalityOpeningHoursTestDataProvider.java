package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.configuration.domain.model.Municipality;
import org.testng.annotations.DataProvider;

import java.util.HashSet;

public class GetMunicipalityOpeningHoursTestDataProvider extends AbstractMunicipalityTestDataProvider {

    @DataProvider
    public static Object[][] getOpeningHoursTestData() {
        Municipality municipality = new Municipality();
        municipality.setOpeningHours(new HashSet<>(toMunicipalityOpeningHours(completeValidOpeningHourList())));

        Municipality municipalityWithNoOpeningHours = new Municipality();

        return new Object[][]{
                {municipality},
                {municipalityWithNoOpeningHours}
        };
    }
}
