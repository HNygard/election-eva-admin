package no.valg.eva.admin.configuration.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.test.BaseTakeTimeTest;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static no.valg.eva.admin.configuration.domain.model.Municipality.SAMI_FICTITIOUS_COUNTY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MunicipalityTest extends BaseTakeTimeTest {

    private static final Long PK_1 = 1L;
    private static final Long PK_2 = 2L;
    private static final Long PK_3 = 3L;
    private static final Long MUNICIPALITY_PD_PK = 100L;
    private static final Long TECHNICAL_PD_PK = 200L;
    private static final String A_COUNTY_ID = "01";

    @Test
    public void isSamlekommune_countyIdIs00_isTrue() {
        Municipality municipality = new Municipality();
        municipality.setCounty(makeCounty(SAMI_FICTITIOUS_COUNTY_ID));

        assertThat(municipality.isSamlekommune()).isTrue();
    }

    @Test
    public void isSamlekommune_countyIdIsNot00_isFalse() {
        Municipality municipality = new Municipality();
        municipality.setCounty(makeCounty(A_COUNTY_ID));

        assertThat(municipality.isSamlekommune()).isFalse();
    }

    private County makeCounty(String countyId) {
        County county = new County();
        county.setId(countyId);
        return county;
    }

    @Test
    public void pollingDistrictOfId_idDoesNotExist_returnsNull() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        assertThat(m.pollingDistrictOfId("finnesIkke")).isNull();
    }

    @Test
    public void pollingDistrictOfId_idExists_returnsPollingDistrict() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        assertThat(m.pollingDistrictOfId(PK_1.toString()).getId()).isEqualTo(PK_1.toString());
    }

    @Test
    public void pollingDistrictsCanBeCollectedFromBoroughs() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        assertThat(m.pollingDistricts()).hasSize(2);
    }

    @Test
    public void pollingDistrictsWithOutMunicipalityDistrictCanBeCollectedFromBoroughs() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());

        // set one to be a municipality district
        m.getBoroughs().iterator().next().getPollingDistricts().iterator().next().setMunicipality(true);

        assertThat(m.regularPollingDistricts(true, true)).hasSize(1);
    }

    @Test
    public void regularPollingDistrictById_withMatch_returnsDistrict() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());

        // set one to be a municipality district
        m.getBoroughs().iterator().next().getPollingDistricts().iterator().next().setMunicipality(true);

        assertThat(m.regularPollingDistrictById("2", true, true)).isNotNull();
    }

    @Test
    public void technicalPollingDistrictsAreFilteredOutForVO() {
        Municipality m = new Municipality();
        m.setBoroughs(oneBoroughWithOnePollingDistrictAndOneTechnicalPollingDistrict());

        assertThat(m.regularPollingDistricts(true, true)).isEmpty();
    }

    @Test
    public void technicalPollingDistricts_areReturned() {
        Municipality m = new Municipality();
        m.setBoroughs(oneBoroughWithOnePollingDistrictAndOneTechnicalPollingDistrict());

        assertThat(m.technicalPollingDistricts()).hasSize(1);
    }

    @Test
    public void technicalPollingDistricts_withNoTechnical_returnsEmptyList() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());

        assertThat(m.technicalPollingDistricts()).hasSize(0);
    }

    @Test
    public void technicalPollingDistrictById_withMatch_returnsDistrict() {
        Municipality m = new Municipality();
        m.setBoroughs(oneBoroughWithOnePollingDistrictAndOneTechnicalPollingDistrict());

        assertThat(m.technicalPollingDistrictById("200")).isNotNull();
    }

    @Test
    public void pollingPlacesCanBeFoundFromBoroughsAndPollingDistricts() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        assertThat(m.pollingPlaces()).hasSize(2);
    }

    @Test
    public void hasParentPollingDistrictsIsFalseWhenThereAreNoParentPollingDistricts() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        assertThat(m.hasParentPollingDistricts()).isFalse();
    }

    @Test
    public void hasParentPollingDistrictsIsTrueWhenThereAreParentPollingDistricts() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        m.getBoroughs().iterator().next().getPollingDistricts().add(parentPollingDistrict());
        assertThat(m.hasParentPollingDistricts()).isTrue();
    }

    @Test
    public void childPollingDistricts_withTwoChildrenDistricts_returnsTheTwo() {
        Municipality municipality = new Municipality();
        municipality.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        municipality.pollingDistricts()
                .forEach(pd -> pd.setPollingDistrict(createMock(PollingDistrict.class)));

        assertThat(municipality.childPollingDistricts()).hasSize(2);
    }

    @Test
    public void pollingPlacesAdvance_withAdvancePlaces_returnsList() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        withAdvancePollingPlaces(m);

        assertThat(m.pollingPlacesAdvance()).hasSize(2);
    }

    @Test
    public void pollingPlacesAdvanceById_withMatch_returnsPlace() {
        Municipality m = new Municipality();
        m.setBoroughs(twoBoroughsWithOnePollingDistrictEach());
        withAdvancePollingPlaces(m);

        assertThat(m.pollingPlacesAdvanceById("2")).isNotNull();
    }

    private void withAdvancePollingPlaces(Municipality municipality) {
        municipality.pollingPlaces()
                .forEach(pollingPlace -> pollingPlace.setElectionDayVoting(false));
    }

    private PollingDistrict parentPollingDistrict() {
        PollingDistrict pd = new PollingDistrict();
        pd.setPk(PK_3);
        pd.setParentPollingDistrict(true);
        return pd;
    }

    private Set<Borough> twoBoroughsWithOnePollingDistrictEach() {
        Set<Borough> boroughs = new HashSet<>();
        boroughs.add(borough(PK_1));
        boroughs.add(borough(PK_2));
        return boroughs;
    }

    private Set<Borough> oneBoroughWithOnePollingDistrictAndOneTechnicalPollingDistrict() {
        Set<Borough> boroughs = new HashSet<>();
        Set<PollingDistrict> pollingDistricts = new HashSet<>();

        pollingDistricts.add(getPollingDistrictForMunicipality());
        pollingDistricts.add(getTechnicalPollingDistrict());

        Borough b1 = new Borough();
        b1.setPollingDistricts(pollingDistricts);
        b1.setMunicipality1(true);
        boroughs.add(b1);

        return boroughs;
    }

    private PollingDistrict getTechnicalPollingDistrict() {
        PollingDistrict technicalPollingDistrict = new PollingDistrict();
        technicalPollingDistrict.setTechnicalPollingDistrict(true);
        technicalPollingDistrict.setPk(TECHNICAL_PD_PK);
        technicalPollingDistrict.setId(String.valueOf(TECHNICAL_PD_PK.intValue()));
        return technicalPollingDistrict;
    }

    private PollingDistrict getPollingDistrictForMunicipality() {
        PollingDistrict pollingDistrict = new PollingDistrict();
        pollingDistrict.setMunicipality(true);
        pollingDistrict.setPk(MUNICIPALITY_PD_PK);
        return pollingDistrict;
    }

    private Borough borough(final Long pk) {
        Borough borough = new Borough();
        borough.setPk(pk);
        borough.setId(String.valueOf(pk.intValue()));
        borough.setPollingDistricts(pollingDistrictInSet(pk));
        return borough;
    }

    private Set<PollingDistrict> pollingDistrictInSet(final Long pk) {
        Set<PollingDistrict> pollingDistricts = new HashSet<>();
        PollingDistrict pollingDistrict = pollingDistrict(pk);
        pollingDistricts.add(pollingDistrict);
        return pollingDistricts;
    }

    private PollingDistrict pollingDistrict(Long pk) {
        PollingDistrict pollingDistrict = new PollingDistrict();
        pollingDistrict.setPk(pk);
        pollingDistrict.setId(pk.toString());
        pollingDistrict.getPollingPlaces().add(pollingPlace(pk));
        return pollingDistrict;
    }

    private PollingPlace pollingPlace(Long pk) {
        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setPk(pk);
        pollingPlace.setId(String.valueOf(pk.intValue()));
        return pollingPlace;
    }

    @Test
    public void getMunicipalityBorough_givenMunicipalityWithMunicipalityBorough_returnsMunicipalityBorough() {
        Municipality municipality = new Municipality();
        Borough municipalityBorough = new Borough();
        municipalityBorough.setPk(1L);
        municipalityBorough.setMunicipality1(true);
        municipality.getBoroughs().add(municipalityBorough);
        Borough borough1 = new Borough();
        borough1.setPk(2L);
        municipality.getBoroughs().add(borough1);
        Borough borough2 = new Borough();

        borough2.setPk(3L);

        municipality.getBoroughs().add(borough2);

        Borough result = municipality.getMunicipalityBorough();

        assertThat(result).isSameAs(municipalityBorough);
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void getMunicipalityBorough_givenMunicipalityWithoutMunicipalityBorough_throwsException() {
        Municipality municipality = new Municipality();
        municipality.setId(AreaPath.OSLO_MUNICIPALITY_ID);
        Borough borough1 = new Borough();
        borough1.setPk(1L);
        municipality.getBoroughs().add(borough1);
        Borough borough2 = new Borough();
        borough2.setPk(2L);
        municipality.getBoroughs().add(borough2);
        municipality.setCounty(createMock(County.class));
        when(municipality.getCounty().areaPath()).thenReturn(AreaPath.from("111111.22.33"));

        municipality.getMunicipalityBorough();
    }

    @Test
    public void getMunicipalityPollingDistrict_givenMunicipalityWithMunicipalityBoroughAndMunicipalityPollingDistrict_returnsMunicipalityPollingDistrict() {
        Municipality municipality = new Municipality();
        Borough municipalityBorough = new Borough();
        municipalityBorough.setPk(1L);
        municipalityBorough.setMunicipality1(true);
        municipality.getBoroughs().add(municipalityBorough);
        PollingDistrict municipalityPollingDistrict = new PollingDistrict();
        municipalityPollingDistrict.setPk(2L);
        municipalityPollingDistrict.setMunicipality(true);
        municipalityBorough.getPollingDistricts().add(municipalityPollingDistrict);

        PollingDistrict result = municipality.getMunicipalityPollingDistrict();

        assertThat(result).isSameAs(municipalityPollingDistrict);
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void getMunicipalityPollingDistrict_givenMunicipalityWithoutMunicipalityBorough_throwsException() {
        Municipality municipality = new Municipality();
        municipality.setId(AreaPath.OSLO_MUNICIPALITY_ID);
        Borough borough1 = new Borough();
        borough1.setPk(1L);
        municipality.getBoroughs().add(borough1);
        Borough borough2 = new Borough();
        borough2.setPk(2L);
        municipality.getBoroughs().add(borough2);
        municipality.setCounty(createMock(County.class));
        when(municipality.getCounty().areaPath()).thenReturn(AreaPath.from("111111.22.33"));

        municipality.getMunicipalityPollingDistrict();
    }

    @Test
    public void testGetAreaPk() {
        long countyPk = 1;
        long municipalityPk = 2;
        County county = new County();
        county.setPk(countyPk);
        Municipality municipality = new Municipality();
        municipality.setPk(municipalityPk);
        municipality.setCounty(county);

        assertThat(municipality.getAreaPk(AreaLevelEnum.COUNTRY)).isNull();
        assertThat(municipality.getAreaPk(AreaLevelEnum.COUNTY)).isEqualTo(countyPk);
        assertThat(municipality.getAreaPk(AreaLevelEnum.MUNICIPALITY)).isEqualTo(municipalityPk);
        assertThat(municipality.getAreaPk(AreaLevelEnum.BOROUGH)).isNull();
        assertThat(municipality.getAreaPk(AreaLevelEnum.POLLING_DISTRICT)).isNull();
        assertThat(municipality.getAreaPk(AreaLevelEnum.POLLING_PLACE)).isNull();
    }

    @Test
    public void dateForFirstElectionDay_municipalityOpeningHours_returnsFirstDate() {
        OpeningHours openingHours1 = createMock(OpeningHours.class);
        LocalDate firstDate = new LocalDate(2018, 1, 20);
        when(openingHours1.getElectionDay().getDate()).thenReturn(firstDate);
        OpeningHours openingHours2 = createMock(OpeningHours.class);
        LocalDate secondDate = new LocalDate(2018, 2, 20);
        when(openingHours2.getElectionDay().getDate()).thenReturn(secondDate);

        Borough borough = createMock(Borough.class);
        PollingDistrict pollingDistrict = createMock(PollingDistrict.class);
        PollingPlace pollingPlace = createMock(PollingPlace.class);
        when(borough.getPollingDistricts()).thenReturn(new HashSet<>(Collections.singletonList(pollingDistrict)));
        when(pollingDistrict.getPollingPlaces()).thenReturn(new HashSet<>(Collections.singletonList(pollingPlace)));
        when(pollingPlace.getOpeningHours()).thenReturn(new HashSet<>(Arrays.asList(openingHours1, openingHours2)));
        Municipality municipality = Municipality.builder()
                .boroughs(new HashSet<>(Collections.singletonList(borough)))
                .build();

        assertThat(municipality.dateForFirstElectionDay()).isEqualTo(firstDate);
    }
}
