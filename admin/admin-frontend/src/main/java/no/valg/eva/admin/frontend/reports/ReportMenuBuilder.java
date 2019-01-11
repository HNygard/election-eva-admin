package no.valg.eva.admin.frontend.reports;

import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.frontend.common.menu.EnumUserMenuIcons;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.compare;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.BEREGNINGER;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.ENDELIG_RESULTAT;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.FORHÅNDSSTEMMEPERIODE;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.LISTEFORSLAG;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MANNTALL;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MØTEBØKER;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.OPPTELLING_ADMIN;
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

public class ReportMenuBuilder implements Serializable {

    private static final EnumMap<ReportCategory, List<Accesses>> REPORT_CATEGORY_ACCESS_MAP = new EnumMap<>(ReportCategory.class);

    private static final long serialVersionUID = -9082481905059691459L;

    private List<Menu> menus;

    static {
        REPORT_CATEGORY_ACCESS_MAP.put(GRUNNLAGSDATA, primaryConfigurationDataAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(MANNTALL, electoralRollAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(FORHÅNDSSTEMMEPERIODE, advanceVotePeriodAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(VALGTING, electionDayAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(OPPTELLING_ADMIN, adminVoteCountAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(MØTEBØKER, meetingProtocolsAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(ENDELIG_RESULTAT, finalResultAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(BEREGNINGER, calculationReportAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(LISTEFORSLAG, listProposalAccessList());
        REPORT_CATEGORY_ACCESS_MAP.put(VALGOPPGJØR_ADMIN, electionSettlementAccessList());
    }

    public ReportMenuBuilder(UserDataController userDataController, List<ValghendelsesRapport> reports) {
        menus = new ArrayList<>();
        boolean menusEnabled = isMenusEnabled(userDataController);
        createPreliminariesMenu(reports);
        createVotingMenu(reports, menusEnabled);
        createCountingMenu(reports, menusEnabled);
        createSettlementMenu(reports, menusEnabled);
    }

    static Comparator<ValghendelsesRapport> sortBySortOrder(ReportCategory category) {
        return (rapport1, rapport2) -> {
            int c1 = getSortKey(category, rapport1.getAccess());
            int c2 = getSortKey(category, rapport2.getAccess());
            return compare(c1, c2);
        };
    }

    private static int getSortKey(ReportCategory category, Access access) {
        List<Accesses> sortOrder = getSortedAccesses(category);
        for (int i = 0; i < sortOrder.size(); i++) {
            Access key = sortOrder.get(i).getAccess();
            if (key.equals(access)) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    static List<Accesses> getSortedAccesses(ReportCategory category) {
        return Optional.ofNullable(REPORT_CATEGORY_ACCESS_MAP.get(category)).orElse(emptyList());
    }

    private static List<Accesses> electionSettlementAccessList() {
        return singletonList(Rapport_Valgoppgjør_Admin_Flettefil_Valgte_Kandidater);
    }

    private static List<Accesses> calculationReportAccessList() {
        return asList(
                Rapport_Beregninger_Kandidatstemmetall_Kommune,
                Rapport_Beregninger_Kandidatstemmetall_Storting,
                Rapport_Beregninger_Mandatberegning_Pr_Parti,
                Rapport_Beregninger_Partisammendrag_Slengere);
    }

    private static List<Accesses> finalResultAccessList() {
        return asList(
                Rapport_Resultat_Valgte_Representanter,
                Rapport_Resultat_Mandatfordeling,
                Rapport_Resultat_Kandidatrangering_Pr_Parti,
                Rapport_Resultat_Utjevningsmandater);
    }

    private static List<Accesses> meetingProtocolsAccessList() {
        return asList(
                Rapport_Møtebøker_Stemmestyre,
                Rapport_Møtebøker_Valgstyre,
                Rapport_Møtebøker_Fylkesvalgstyre_Fylke,
                Rapport_Møtebøker_Fylkesvalgstyre_Storting,
                Rapport_Møtebøker_Bydelsutvalg,
                Rapport_Møtebøker_Samevalgstyre,
                Rapport_Møtebøker_Valgstyre_Longyearbyen,
                Rapport_Møtebøker_Opptellingsvalgstyre);
    }

    private static List<Accesses> adminVoteCountAccessList() {
        return asList(
                Rapport_Opptelling_Admin_Partifordeling_Pr_Krets,
                Rapport_Opptelling_Admin_Valgdeltakelse);
    }

    private static List<Accesses> electionDayAccessList() {
        return singletonList(
                Rapport_Valgting_Til_Prøving);
    }

    private static List<Accesses> advanceVotePeriodAccessList() {
        return asList(
                Rapport_Forhåndsstemmeperiode_Til_Prøving,
                Rapport_Forhåndsstemmeperiode_Antall_Mottatte,
                Rapport_Forhåndsstemmeperiode_Andre_Kommuner);
    }

    private static List<Accesses> listProposalAccessList() {
        return singletonList(Rapport_Listeforslag_Lister_Og_Kandidater);
    }

    private static List<Accesses> electoralRollAccessList() {
        return asList(
                Rapport_Manntall_Avkrysningsmanntall,
                Rapport_Manntall_Avkrysningsmanntall_Minus30,
                Rapport_Manntall_Utleggingsmanntall_Kretsvis,
                Rapport_Manntall_Utleggingsmanntall_Alfabetisk,
                Rapport_Manntall_Partiservice_Fylke,
                Rapport_Manntall_Partiservice_Kommune,
                Rapport_Manntall_Partiservice_Valgkrets);
    }

    private static List<Accesses> primaryConfigurationDataAccessList() {
        return asList(
                Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets,
                Rapport_Grunnlagsdata_Generell_Kretsinformasjon,
                Rapport_Grunnlagsdata_Brukere_Og_Roller_Fylke,
                Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune,
                Rapport_Grunnlagsdata_Opptellingsmåter);
    }

    private void createPreliminariesMenu(List<ValghendelsesRapport> reports) {
        Menu root = new Menu("@menu.preliminaries.header", true, EnumUserMenuIcons.PRELIMINARIES);
        add(root, reports, GRUNNLAGSDATA);
        add(root, reports, LISTEFORSLAG);
        add(root, reports, MANNTALL);
        if (!root.isEmpty()) {
            getMenus().add(root);
        }
    }

    private void createVotingMenu(List<ValghendelsesRapport> reports, boolean enabled) {
        Menu root = new Menu("@voting.approveVoting.votings", true, EnumUserMenuIcons.VOTING);
        add(root, reports, FORHÅNDSSTEMMEPERIODE, enabled);
        add(root, reports, VALGTING, enabled);
        if (!root.isEmpty()) {
            getMenus().add(root);
        }
    }

    private void createCountingMenu(List<ValghendelsesRapport> reports, boolean enabled) {
        Menu root = new Menu("@menu.counting.heading", true, EnumUserMenuIcons.COUNTING);
        add(root, reports, OPPTELLING_ADMIN, enabled);
        add(root, reports, MØTEBØKER, enabled);
        if (!root.isEmpty()) {
            getMenus().add(root);
        }
    }

    private void createSettlementMenu(List<ValghendelsesRapport> reports, boolean enabled) {
        Menu root = new Menu("@menu.settlement.heading", true, EnumUserMenuIcons.SETTLEMENT);
        add(root, reports, ENDELIG_RESULTAT, enabled);
        add(root, reports, BEREGNINGER, enabled);
        add(root, reports, VALGOPPGJØR_ADMIN, enabled);
        if (!root.isEmpty()) {
            getMenus().add(root);
        }
    }

    private void add(Menu root, List<ValghendelsesRapport> reports, ReportCategory cat) {
        add(root, reports, cat, true);
    }

    private void add(Menu root, List<ValghendelsesRapport> reports, ReportCategory cat, boolean enabled) {
        List<ValghendelsesRapport> filtered = reports
                .stream()
                .filter(rapport -> rapport.getKategori() == cat)
                .sorted(sortBySortOrder(cat))
                .collect(toList());
        Menu result = new Menu(cat.getKey(), true);
        for (ValghendelsesRapport rapport : filtered) {
            result.addChild(new Menu(rapport.getAccess().getName(), rapport.getRapportId()).setSource(rapport)
                    .setEnabled(enabled));
        }
        if (!result.isEmpty()) {
            root.addChild(result);
        }
    }

    public List<Menu> getMenus() {
        return menus;
    }

    private boolean isMenusEnabled(UserDataController userDataController) {
        return userDataController.isOverrideAccess() || userDataController.getElectionEvent().isDemoElection()
                || userDataController.getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.APPROVED_CONFIGURATION.id();
    }
}
