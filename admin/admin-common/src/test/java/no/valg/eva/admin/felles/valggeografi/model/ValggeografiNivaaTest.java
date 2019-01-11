package no.valg.eva.admin.felles.valggeografi.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiNivaaTest {
    @Test(dataProvider = "valggeografiNivaaOgNivaa")
    public void nivaa_gittValggeografiNivaa_returnererNivaa(ValggeografiNivaa valggeografiNivaa, int nivaa) {
        assertThat(valggeografiNivaa.nivaa()).isEqualTo(nivaa);
    }

    @DataProvider
    public Object[][] valggeografiNivaaOgNivaa() {
        return new Object[][]{
                new Object[]{VALGHENDELSE, 0},
                new Object[]{LAND, 1},
                new Object[]{FYLKESKOMMUNE, 2},

                new Object[]{KOMMUNE, 3},
                new Object[]{BYDEL, 4},
                new Object[]{STEMMEKRETS, 5},
                new Object[]{STEMMESTED, 6},
                new Object[]{RODE, 7}

        };
    }

    @Test(dataProvider = "areaLevelEnumOgValggeografiNivaa")
    public void fra_gittAreaLevelEnum_returnererValggeografiNivaa(AreaLevelEnum areaLevelEnum, ValggeografiNivaa valggeografiNivaa) {
        assertThat(ValggeografiNivaa.fra(areaLevelEnum)).isEqualTo(valggeografiNivaa);
    }

    @DataProvider
    public Object[][] areaLevelEnumOgValggeografiNivaa() {
        return new Object[][]{
                new Object[]{ROOT, VALGHENDELSE},
                new Object[]{COUNTRY, LAND},
                new Object[]{COUNTY, FYLKESKOMMUNE},
                new Object[]{MUNICIPALITY, KOMMUNE},
                new Object[]{BOROUGH, BYDEL},
                new Object[]{POLLING_DISTRICT, STEMMEKRETS},
                new Object[]{POLLING_PLACE, STEMMESTED},
                new Object[]{POLLING_STATION, RODE}
        };
    }

    @Test(dataProvider = "fraWithExceptionTestData", expectedExceptions = IllegalArgumentException.class)
    public void fra_givenInvalidLevel_throwsException(int level) {
        ValggeografiNivaa.fra(level);
    }
    
    @DataProvider
    private Object[][] fraWithExceptionTestData() {
        return new Object[][] {
            { -1 },
            { 8 },
            { 1000 },
        };
    }

    @Test(dataProvider = "listIncludingTestData")
    public void listIncluding_gittValggeografiNivaa_returnerListeTilOgMedValggeografiNivaa(
            ValggeografiNivaa valggeografiNivaa, List<ValggeografiNivaa> valggeografiNivaaer) {
        assertThat(ValggeografiNivaa.listIncluding(valggeografiNivaa)).containsExactlyElementsOf(valggeografiNivaaer);
    }

    @DataProvider
    public Object[][] listIncludingTestData() {
        return new Object[][]{
                new Object[]{VALGHENDELSE, singletonList(VALGHENDELSE)},
                new Object[]{LAND, asList(VALGHENDELSE, LAND)},
                new Object[]{FYLKESKOMMUNE, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE)},
                new Object[]{KOMMUNE, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE, KOMMUNE)},
                new Object[]{BYDEL, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE, KOMMUNE, BYDEL)},
                new Object[]{STEMMEKRETS, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE, KOMMUNE, BYDEL, STEMMEKRETS)},
                new Object[]{STEMMESTED, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE, KOMMUNE, BYDEL, STEMMEKRETS, STEMMESTED)},
                new Object[]{RODE, asList(VALGHENDELSE, LAND, FYLKESKOMMUNE, KOMMUNE, BYDEL, STEMMEKRETS, STEMMESTED, RODE)}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void listIncluding_givenNullArgument_throwsException() {
        ValggeografiNivaa.listIncluding(null);
    }

    @Test
    public void id_gittValggeografiNivaa_returnerName() {
        for (ValggeografiNivaa valggeografiNivaa : ValggeografiNivaa.values()) {
            assertThat(valggeografiNivaa.id()).isEqualTo(valggeografiNivaa.name());
        }
    }

    @Test
    public void visningsnavn_gittValggeografiNivaa_returnerVisningsnavn() {
        for (ValggeografiNivaa valggeografiNivaa : ValggeografiNivaa.values()) {
            assertThat(valggeografiNivaa.visningsnavn()).isEqualTo("@area_level[" + valggeografiNivaa.nivaa() + "].name");
        }
    }
}
