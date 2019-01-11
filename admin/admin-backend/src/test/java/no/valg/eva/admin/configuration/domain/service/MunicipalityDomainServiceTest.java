package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.application.LocalConfigApplicationServiceTest;
import no.valg.eva.admin.configuration.application.OpeningHoursSorter;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class MunicipalityDomainServiceTest extends LocalConfigApplicationServiceTest {

    private MunicipalityDomainService municipalityDomainService;

    @BeforeMethod
    public void beforeMethod() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        municipalityDomainService = initializeMocks(MunicipalityDomainService.class);
    }

    @DataProvider
    public static Object[][] isPersistedOpeningHourTestData() {
        OpeningHours newOpeningHours = new OpeningHours();
        OpeningHours existingOpeningHours = new OpeningHours();
        existingOpeningHours.setPk(1L);


        return new Object[][]{
                {existingOpeningHours, true},
                {newOpeningHours, false}
        };
    }

    @DataProvider
    public static Object[][] tooManyOpeningHoursTestData() {

        List<no.valg.eva.admin.common.configuration.model.OpeningHours> tooBigList = new ArrayList<>();
        tooBigList.add(no.valg.eva.admin.common.configuration.model.OpeningHours.builder().build());
        tooBigList.add(no.valg.eva.admin.common.configuration.model.OpeningHours.builder().build());
        tooBigList.add(no.valg.eva.admin.common.configuration.model.OpeningHours.builder().build());

        List<no.valg.eva.admin.common.configuration.model.OpeningHours> correctList = new ArrayList<>();
        correctList.add(no.valg.eva.admin.common.configuration.model.OpeningHours.builder().build());
        correctList.add(no.valg.eva.admin.common.configuration.model.OpeningHours.builder().build());

        return new Object[][]{
                {tooBigList, true},
                {correctList, false},
                {new ArrayList<>(), false}
        };
    }

    @Test(dataProvider = "tooManyOpeningHoursTestData")
    public void testTooManyOpeningHours_givenListOfOpeningHours(List<OpeningHours> openingHours, boolean expectingTooBig) {
        assertThat(municipalityDomainService.tooManyOpeningHours(openingHours)).isEqualTo(expectingTooBig);
    }

    @Test(dataProvider = "isPersistedOpeningHourTestData")
    public void testIsPersisted_GivenOpeningHours_VerifiesIfNewOrExisting(OpeningHours openingHours, boolean expectingExisting) {
        assertThat(municipalityDomainService.isPersisted(openingHours)).isEqualTo(expectingExisting);
    }

    @Test(dataProvider = "getOpeningHoursTestData", dataProviderClass = GetMunicipalityOpeningHoursTestDataProvider.class)
    public void testGetOpeningHours(Municipality municipality) throws Exception {
        MunicipalityDomainService service = initializeMocks(MunicipalityDomainService.class);
        when(getInjectMock(MunicipalityRepository.class).findByPk(any())).thenReturn(municipality);

        List<MunicipalityOpeningHour> openingHours = service.getOpeningHours(municipality);

        List<MunicipalityOpeningHour> expectedOpeningHourList = OpeningHoursSorter.toSortedMunicipalityOpeningHourList(municipality.getOpeningHours());

        assertEquals(openingHours, expectedOpeningHourList);
    }

    @Test(dataProvider = "saveOpeningHoursTestData", dataProviderClass = SaveElectionDayOpeningHoursTestDataProvider.class)
    public void testSaveOpeningHours(Municipality municipality, List<MunicipalityOpeningHour> providedOpeningHours, boolean shouldOverWriteExisting) {
        when(getInjectMock(MunicipalityRepository.class).getReference(municipality)).thenReturn(municipality);

        UserData userData = createMock(UserData.class);

        for (MunicipalityOpeningHour currentOpeningHour : providedOpeningHours) {
            no.valg.eva.admin.configuration.domain.model.ElectionDay currentElectionDay = currentOpeningHour.getElectionDay();
            when(getInjectMock(ElectionEventRepository.class).getReference(ElectionDay.class, currentElectionDay.getPk())).thenReturn(currentElectionDay);
        }

        municipalityDomainService.saveOpeningHours(userData, municipality, providedOpeningHours);

        List<MunicipalityOpeningHour> openingHoursResultList = OpeningHoursSorter.toSortedMunicipalityOpeningHourList(municipality.getOpeningHours());

        assertEquals(openingHoursResultList, providedOpeningHours);
    }
}
