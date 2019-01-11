package no.valg.eva.admin.frontend.reports;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.BEREGNINGER;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.ENDELIG_RESULTAT;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.FORHÅNDSSTEMMEPERIODE;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.LISTEFORSLAG;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MANNTALL;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MØTEBØKER;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.OPPTELLING_ADMIN;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.VALGHENDELSE_ADMIN;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.VALGOPPGJØR_ADMIN;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.VALGTING;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Beregninger_Kandidatstemmetall_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Beregninger_Kandidatstemmetall_Storting;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Beregninger_Mandatberegning_Pr_Parti;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Beregninger_Partisammendrag_Slengere;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Forhåndsstemmeperiode_Andre_Kommuner;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Forhåndsstemmeperiode_Antall_Mottatte;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Forhåndsstemmeperiode_Til_Prøving;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Brukere_Og_Roller_Fylke;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Generell_Kretsinformasjon;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Listeforslag_Lister_Og_Kandidater;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Avkrysningsmanntall;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Avkrysningsmanntall_Minus30;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Partiservice_Fylke;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Partiservice_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Partiservice_Valgkrets;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Utleggingsmanntall_Alfabetisk;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Utleggingsmanntall_Kretsvis;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Bydelsutvalg;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Fylkesvalgstyre_Fylke;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Fylkesvalgstyre_Storting;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Opptellingsvalgstyre;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Samevalgstyre;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Stemmestyre;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Valgstyre;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Valgstyre_Longyearbyen;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Opptelling_Admin_Partifordeling_Pr_Krets;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Opptelling_Admin_Valgdeltakelse;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Resultat_Kandidatrangering_Pr_Parti;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Resultat_Mandatfordeling;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Resultat_Utjevningsmandater;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Resultat_Valgte_Representanter;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Valgoppgjør_Admin_Flettefil_Valgte_Kandidater;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Valgting_Til_Prøving;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Accesses;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReportMenuBuilderTest {

    @Test(dataProvider = "forTestGetSorted")
    public void testGetSorted_givenReportCategories_verifiesAccesses(ReportCategory reportCategory, List<Accesses> expectedAccesses) {
        List<Accesses> accessesList = ReportMenuBuilder.getSortedAccesses(reportCategory);
        assertNotNull(accessesList);
        assertEquals(accessesList, expectedAccesses);
    }

    @Test(dataProvider = "forTestSortByOrder")
    public void testSortByOrder_givenElectionDayReports_verifiesSortingResult(ValghendelsesRapport electionDayReport1, ValghendelsesRapport electionDayReport2,
                                                                              int expectedSortingResult) {
        assertEquals(ReportMenuBuilder.sortBySortOrder(ReportCategory.GRUNNLAGSDATA).compare(electionDayReport1, electionDayReport2), expectedSortingResult);
    }

    @DataProvider
    public static Object[][] forTestSortByOrder() {
        return new Object[][]{
                {new ValghendelsesRapport("1", ReportCategory.GRUNNLAGSDATA, Accesses.Rapport_Grunnlagsdata.getAccess()),
                        new ValghendelsesRapport("1", ReportCategory.GRUNNLAGSDATA, Accesses.Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets.getAccess()), -1}
        };
    }

    @DataProvider
    public static Object[][] forTestGetSorted() {
        return new Object[][]{
                {GRUNNLAGSDATA, asList(Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets,
                        Rapport_Grunnlagsdata_Generell_Kretsinformasjon,
                        Rapport_Grunnlagsdata_Brukere_Og_Roller_Fylke,
                        Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune,
                        Rapport_Grunnlagsdata_Opptellingsmåter)},
                {LISTEFORSLAG, singletonList(Rapport_Listeforslag_Lister_Og_Kandidater)},
                {MANNTALL, asList(
                        Rapport_Manntall_Avkrysningsmanntall,
                        Rapport_Manntall_Avkrysningsmanntall_Minus30,
                        Rapport_Manntall_Utleggingsmanntall_Kretsvis,
                        Rapport_Manntall_Utleggingsmanntall_Alfabetisk,
                        Rapport_Manntall_Partiservice_Fylke,
                        Rapport_Manntall_Partiservice_Kommune,
                        Rapport_Manntall_Partiservice_Valgkrets)},
                {FORHÅNDSSTEMMEPERIODE, asList(
                        Rapport_Forhåndsstemmeperiode_Til_Prøving,
                        Rapport_Forhåndsstemmeperiode_Antall_Mottatte,
                        Rapport_Forhåndsstemmeperiode_Andre_Kommuner)},
                {VALGTING, singletonList(
                        Rapport_Valgting_Til_Prøving)},
                {OPPTELLING_ADMIN, asList(
                        Rapport_Opptelling_Admin_Partifordeling_Pr_Krets,
                        Rapport_Opptelling_Admin_Valgdeltakelse)},
                {MØTEBØKER, asList(
                        Rapport_Møtebøker_Stemmestyre,
                        Rapport_Møtebøker_Valgstyre,
                        Rapport_Møtebøker_Fylkesvalgstyre_Fylke,
                        Rapport_Møtebøker_Fylkesvalgstyre_Storting,
                        Rapport_Møtebøker_Bydelsutvalg,
                        Rapport_Møtebøker_Samevalgstyre,
                        Rapport_Møtebøker_Valgstyre_Longyearbyen,
                        Rapport_Møtebøker_Opptellingsvalgstyre)},
                {ENDELIG_RESULTAT, asList(
                        Rapport_Resultat_Valgte_Representanter,
                        Rapport_Resultat_Mandatfordeling,
                        Rapport_Resultat_Kandidatrangering_Pr_Parti,
                        Rapport_Resultat_Utjevningsmandater)},
                {BEREGNINGER, asList(
                        Rapport_Beregninger_Kandidatstemmetall_Kommune,
                        Rapport_Beregninger_Kandidatstemmetall_Storting,
                        Rapport_Beregninger_Mandatberegning_Pr_Parti,
                        Rapport_Beregninger_Partisammendrag_Slengere)},
                {VALGOPPGJØR_ADMIN, singletonList(Rapport_Valgoppgjør_Admin_Flettefil_Valgte_Kandidater)},

                //Testing with a category that is not handled in the getSortedAccesses method
                {VALGHENDELSE_ADMIN, new ArrayList<>()}

        };
    }
}