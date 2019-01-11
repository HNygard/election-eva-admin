package no.valg.eva.admin.felles.valghierarki.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.constants.ElectionLevelEnum;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiNivaaTest {
    @Test(dataProvider = "valghierarkiNivaaOgNivaa")
    public void nivaa_gittValghierarkiNivaa_returnererNivaa(ValghierarkiNivaa valghierarkiNivaa, int nivaa) {
        assertThat(valghierarkiNivaa.nivaa()).isEqualTo(nivaa);
    }

    @DataProvider
    public Object[][] valghierarkiNivaaOgNivaa() {
        return new Object[][]{
                new Object[]{VALGHENDELSE, 0},
                new Object[]{VALGGRUPPE, 1},
                new Object[]{VALG, 2},

                new Object[]{VALGDISTRIKT, 3}

        };
    }

    @Test(dataProvider = "electionLevelEnumOgValghierarkiNivaa")
    public void fra_gittAreaLevelEnum_returnererValggeografiNivaa(ElectionLevelEnum electionLevelEnum, ValghierarkiNivaa valghierarkiNivaa) {
        assertThat(ValghierarkiNivaa.fra(electionLevelEnum)).isEqualTo(valghierarkiNivaa);
    }

    @DataProvider
    public Object[][] electionLevelEnumOgValghierarkiNivaa() {
        return new Object[][]{
                new Object[]{ELECTION_EVENT, VALGHENDELSE},
                new Object[]{ELECTION_GROUP, VALGGRUPPE},
                new Object[]{ELECTION, VALG},
                new Object[]{CONTEST, VALGDISTRIKT}
        };
    }

    @Test(dataProvider = "forListIncluding")
    public void listIncluding_givenElectionHierarchyLevels_verifiesElectionHierarchyLevels(
            ValghierarkiNivaa valghierarkiNivaa, List<ValghierarkiNivaa> valghierarkiNivaaer) {
        assertThat(ValghierarkiNivaa.listIncluding(valghierarkiNivaa)).containsExactlyElementsOf(valghierarkiNivaaer);
    }

    @DataProvider
    public Object[][] forListIncluding() {
        return new Object[][]{
                new Object[]{VALGHENDELSE, singletonList(VALGHENDELSE)},
                new Object[]{VALGGRUPPE, asList(VALGHENDELSE, VALGGRUPPE)},
                new Object[]{VALG, asList(VALGHENDELSE, VALGGRUPPE, VALG)},
                new Object[]{VALGDISTRIKT, asList(VALGHENDELSE, VALGGRUPPE, VALG, VALGDISTRIKT)}
        };
    }

    @Test
    public void id_gittValghierarkiNivaa_returnerName() {
        for (ValghierarkiNivaa valghierarkiNivaa : ValghierarkiNivaa.values()) {
            assertThat(valghierarkiNivaa.id()).isEqualTo(valghierarkiNivaa.name());
        }
    }

    @Test
    public void visningsnavn_gittValghierarkiNivaa_returnerVisningsnavn() {
        for (ValghierarkiNivaa valghierarkiNivaa : ValghierarkiNivaa.values()) {
            assertThat(valghierarkiNivaa.visningsnavn()).isEqualTo("@election_level[" + valghierarkiNivaa.nivaa() + "].name");
        }
    }

}
