package no.valg.eva.admin.frontend.user;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.frontend.common.menu.EnumUserMenuIcons;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.security.PageAccess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;

public class UserMenuBuilder implements Serializable {

    private static final long serialVersionUID = -5196655608290322026L;

    private static final String MESSAGE_PROPERTY_MENU_EARLY_VOTING_HEADER = "@menu.earlyVoting.header";

    private static final String MESSAGE_PROPERTY_MENU_SETTLEMENT_HEADING = "@menu.settlement.heading";

    private final List<Menu> menus;
    private final PageAccess pageAccess;
    private final UserData userData;
    private final UserMenuMetadata userMenuMetadata;
    private final List<ValghendelsesRapport> electionEventReports;
    private final MessageProvider messageProvider;

    public UserMenuBuilder(UserMenuMetadata userMenuMetadata, PageAccess pageAccess, UserData userData, MessageProvider messageProvider, boolean menusEnabled, List<ValghendelsesRapport> electionEventReports) {
        this.userMenuMetadata = userMenuMetadata;
        this.pageAccess = pageAccess;
        this.userData = userData;
        this.messageProvider = messageProvider;
        this.electionEventReports = electionEventReports;

        menus = new ArrayList<>();

        createMenus(menusEnabled);
    }

    private void createMenus(boolean isMenusEnabled) {
        createPreliminariesMenu();
        createVotingMenu(isMenusEnabled);
        createCountingMenu(isMenusEnabled);
        createSettlementMenu(isMenusEnabled);
    }

    private void createPreliminariesMenu() {
        Menu preliminariesMenu = new Menu("@menu.preliminaries.header", true, EnumUserMenuIcons.PRELIMINARIES);

        // Config menu
        Menu electionConfigMenu = new Menu("@menu.electionConfig", true);
        addIfAccess(electionConfigMenu, "@menu.config.local", "/secure/config/local/local.xhtml");
        addIfAccess(electionConfigMenu, "@menu.operators", "/secure/rbac/operatorAdmin.xhtml");
        addIfNotEmpty(preliminariesMenu, electionConfigMenu);

        Menu electionConfigMenu2 = electionConfigMenu.isEmpty() ? new Menu("@menu.electionConfig", true) : new Menu("");
        addIfAccess(electionConfigMenu2, "@menu.config.local.electoral_roll_overview", "/secure/config/local/electoralRollOverview.xhtml");
        addIfNotEmpty(preliminariesMenu, electionConfigMenu2);

        // List proposal
        Menu listProposalMenu = new Menu("@menu.listProposal", true);
        if (userMenuMetadata.hasElectionsWithTypeProportionalRepresentation()) {
            addIfAccess(listProposalMenu, "@menu.listProposal.create", "/secure/listProposal/createListProposal.xhtml");
            addIfAccess(listProposalMenu, "@menu.listProposal.edit", "/secure/listProposal/chooseEditListProposal.xhtml");
            addIfAccess(listProposalMenu, "@menu.listProposal.print_base", "/secure/listProposal/lastNedStemmeseddelfil.xhtml");
        }
        addIfNotEmpty(preliminariesMenu, listProposalMenu);

        // Electoral roll
        Menu electoralRollMenu = new Menu("@menu.electoralRoll.header", true);
        addIfAccess(electoralRollMenu, "@menu.electoralRoll.search", "/secure/manntall/sok.xhtml");
        if (userMenuMetadata.hasMinAreaLevel(AreaLevelEnum.MUNICIPALITY)) {
            addIfAccess(electoralRollMenu, "@menu.electoralRoll.create", "/secure/manntall/opprett.xhtml");
        }
        addIfNotEmpty(preliminariesMenu, electoralRollMenu);

        Menu electoralRollMenu2 = electoralRollMenu.isEmpty() ? new Menu("@menu.electoralRoll.header", true) : new Menu("");
        if (userMenuMetadata.hasMinAreaLevel(AreaLevelEnum.MUNICIPALITY)) {
            addIfAccess(electoralRollMenu2, "@menu.electoralRoll.history", "/secure/manntall/listVoterAudit.xhtml");
        }
        addIfNotEmpty(preliminariesMenu, electoralRollMenu2);

        // Reports
        Menu reportMenu = new Menu("@common.rapporter", true);
        addIfAccess(reportMenu, "@menu.reports.links.heading", "/secure/reporting/reportLinks.xhtml");
        addIfNotEmpty(preliminariesMenu, reportMenu);

        if (!preliminariesMenu.isEmpty()) {
            getMenus().add(preliminariesMenu);
        }

    }

    private void createVotingMenu(boolean enabled) {
        Menu votingMenu = new Menu("@menu.voting.votings.header", true, EnumUserMenuIcons.VOTING);

        Menu votingInEnvelopeMenu = new Menu("@menu.voting.envelopes.header", true);
        addIfAccess(votingInEnvelopeMenu, "@menu.voting.registerInEnvelope", "/secure/voting/registerVotingInEnvelope.xhtml", enabled);
        addIfAccess(votingInEnvelopeMenu, "@menu.voting.envelope.confirmation", "/secure/voting/confirming/votingConfirmation.xhtml", enabled);
        addIfNotEmpty(votingMenu, votingInEnvelopeMenu);

        Menu overViewSubMenu = new Menu("", false);
        addIfAccess(overViewSubMenu, "@menu.voting.envelope.overview_unconfirmed", "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=false", enabled);
        addIfAccess(overViewSubMenu, "@menu.voting.envelope.overview_confirmed", "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=true", enabled);
        addIfNotEmpty(votingMenu, overViewSubMenu);

        Menu votingToOtherMunicipalitySubMenu = new Menu("", false);
        addIfAccess(votingToOtherMunicipalitySubMenu, "@menu.earlyVoting.faVotings", "/secure/stemmegivning/faVotingsSentFromMunicipality.xhtml", enabled);
        addIfNotEmpty(votingMenu, votingToOtherMunicipalitySubMenu);

        // Forhåndsstemmeperiode
        Menu advanceVotingPeriodMenu = new Menu(MESSAGE_PROPERTY_MENU_EARLY_VOTING_HEADER, true);
        addIfAccess(advanceVotingPeriodMenu, "@menu.earlyVoting.manual", "/secure/stemmegivning/forhandOrdinaer.xhtml", enabled);
        addIfNotEmpty(votingMenu, advanceVotingPeriodMenu);

        // Election day menu
        Menu electionDayMenu = new Menu("@menu.electionDay.header", true);
        if (userMenuMetadata.isElectronicMarkOffsConfigured()) {
            addIfAccess(electionDayMenu, "@menu.electionDay.register", "/secure/stemmegivning/valgtingOrdinaer.xhtml", enabled);
        }

        addIfNotEmpty(votingMenu, electionDayMenu);

        Menu electionDayMenu2 = electionDayMenu.isEmpty() ? new Menu("@menu.electionDay.header", true) : new Menu("");
        addIfNotEmpty(votingMenu, electionDayMenu2);

        // Felles funksjoner
        Menu commonVotingFunctionsMenu = new Menu("@menu.commonVoting.header", true);
        addIfAccess(commonVotingFunctionsMenu, "@menu.report.empty_election_card", "/secure/stemmegivning/emptyElectionCard.xhtml", enabled);
        if (userMenuMetadata.hasMinimumMunicipalityAndElectionGroup() && userMenuMetadata.isElectronicMarkOffsConfigured()) {
            addIfAccess(commonVotingFunctionsMenu, "@menu.voting.voting_status", "/secure/stemmegivning/votingStatus.xhtml", enabled);
        }
        addIfNotEmpty(votingMenu, commonVotingFunctionsMenu);

        //Deprecated menu items from here -----

        Menu deprecatedMenuItems = new Menu("", false, true);
        addIfAccess(deprecatedMenuItems, "@menu.electionDay.approve", "/secure/stemmegivning/valgtingProvingVelger.xhtml", enabled, true);
        if (userMenuMetadata.isElectronicMarkOffsConfigured()) {
            addIfAccess(deprecatedMenuItems, "@menu.approveVoting.approveVotingNegativeElectionDay", "/secure/stemmegivning/valgtingProvingSamlet.xhtml", enabled, true);
        }
        addIfAccess(deprecatedMenuItems, "@menu.earlyVoting.approve", "/secure/stemmegivning/forhandProvingVelger.xhtml", enabled, true);
        addIfAccess(deprecatedMenuItems, "@menu.approveVoting.approveVotingNegative", "/secure/stemmegivning/forhandProvingSamlet.xhtml", enabled, true);
        addIfAccess(deprecatedMenuItems, "@menu.approveVoting.rejectedVotings", "/secure/stemmegivning/rejectedVotingsReport.xhtml", enabled, true);
        addIfNotEmpty(votingMenu, deprecatedMenuItems);
        //--- deprecated menu items until here

        if (!votingMenu.isEmpty()) {
            getMenus().add(votingMenu);
        }
    }

    private void createCountingMenu(boolean enabled) {
        Menu countingMenu = new Menu("@menu.counting.heading", true, EnumUserMenuIcons.COUNTING);

        Menu administrationMenu = new Menu("@menu.counting.administration.heading", true);
        addIfAccess(administrationMenu, "@menu.opptelling.antallStemmesedlerLagtTilSide", "/secure/counting/antallStemmesedlerLagtTilSide.xhtml", enabled);
        addIfNotEmpty(countingMenu, administrationMenu);

        Menu administrationMenu2 = administrationMenu.isEmpty() ? new Menu("@menu.counting.administration.heading", true) : new Menu("");
        if (userMenuMetadata.isScanningEnabled()) {
            addIfAccess(administrationMenu2, "@menu.import.heading", "/secure/counting/batches.xhtml", enabled);
        }
        addIfAccess(administrationMenu2, "@menu.counting.barcode_sticker", "/secure/opptelling/genererStrekkodelapper.xhtml", enabled);
        addIfAccess(administrationMenu2, "@menu.statistic.reporting", "/secure/reporting/statistics/evaResultatRapportering.xhtml", enabled);
        if (userMenuMetadata.hasElectionsWithTypeProportionalRepresentation()) {
            addIfAccess(administrationMenu2, "@menu.counting.countyResults", "/secure/counting/countyCollatedCountResults.xhtml", enabled);
        }
        addIfNotEmpty(countingMenu, administrationMenu2);

        Menu countingCategoriesMenu = new Menu("@menu.counting.countingCategories.heading", true);
        addIfAccess(countingCategoriesMenu, "@menu.counting.overview", "/secure/counting/countingOverview.xhtml", enabled);
        addIfNotEmpty(countingMenu, countingCategoriesMenu);

        Menu addCountNumbersMenu = countingCategoriesMenu.isEmpty() ? new Menu("@menu.counting.countingCategories.heading", true) : new Menu("");
        addCountNumbersMenu.setCssClass("startCountingMenu");

        addCountNumbersMenu.addChild(new Menu("@menu.counting.addCountings.heading", true));
        String registerCountsXHTML = "/secure/opptelling/registrerOpptellinger.xhtml?kontekst=countCategory|%s";
        registerVoteCountsMenu(enabled, addCountNumbersMenu, FO, "@menu.counting.ordinary_advance_votes", format(registerCountsXHTML, FO));
        registerVoteCountsMenu(enabled, addCountNumbersMenu, FS, "@menu.counting.late_advance_votes", format(registerCountsXHTML, FS));
        registerVoteCountsMenu(enabled, addCountNumbersMenu, VO, "@menu.counting.regular_electionday_votes", format(registerCountsXHTML, VO));
        registerVoteCountsMenu(enabled, addCountNumbersMenu, VS, "@menu.counting.special_cover_votes", format(registerCountsXHTML, VS));
        registerVoteCountsMenu(enabled, addCountNumbersMenu, VB, "@menu.counting.emergency_envelopes", format(registerCountsXHTML, VB));
        registerVoteCountsMenu(enabled, addCountNumbersMenu, VF, "@menu.counting.foreign_votes", format(registerCountsXHTML, VF));

        if (userMenuMetadata.hasAccessToBoroughs()) {
            registerVoteCountsMenu(enabled, addCountNumbersMenu, BF, "@menu.counting.foreign_votes_borough", format(registerCountsXHTML, BF));
        }

        if (addCountNumbersMenu.getChildren().size() > 1) {
            countingMenu.addChild(addCountNumbersMenu);
        }

        Menu rejectionMenu = new Menu("@menu.counting.approve_rejected.heading", true);
        addIfAccess(rejectionMenu, "@menu.counting.approve_rejected.manual", "/secure/opptelling/behandleManueltForkastede.xhtml", enabled);
        if (userMenuMetadata.isScanningEnabled()) {
            addIfAccess(rejectionMenu, "@menu.counting.approve_rejected.scan", "/secure/opptelling/behandleSkannetForkastede.xhtml", enabled);
        }
        addIfNotEmpty(countingMenu, rejectionMenu);

        Menu meetingProtocolsMenu = meetingProtocolsMenu(enabled);
        addIfNotEmpty(countingMenu, meetingProtocolsMenu);

        if (!countingMenu.isEmpty()) {
            getMenus().add(countingMenu);
        }
    }

    private Menu meetingProtocolsMenu(boolean enabled) {
        Menu meetingProtocolsMenu = new Menu("@rapport.category.protocols", true);
        electionEventReports.stream()
                .filter(rapport -> rapport.getKategori() == ReportCategory.MØTEBØKER)
                .forEach(rapport -> meetingProtocolsMenu
                        .addChild(new Menu(messageProvider.get(rapport.getNameKey()), rapport.getRapportId()).setSource(rapport).setEnabled(enabled)));
        return meetingProtocolsMenu;
    }

    private void registerVoteCountsMenu(boolean enabled, Menu menu, CountCategory countCategory, String text, String url) {
        if (userMenuMetadata.isValidCountCategoryId(countCategory)) {
            addIfAccess(menu, text, url, enabled);
        }
    }

    private void createSettlementMenu(boolean enabled) {
        Menu settlementMenu = new Menu(MESSAGE_PROPERTY_MENU_SETTLEMENT_HEADING, true, EnumUserMenuIcons.SETTLEMENT);

        // Settlement
        Menu settlementSubMenu = new Menu(MESSAGE_PROPERTY_MENU_SETTLEMENT_HEADING, true);
        addIfAccess(settlementSubMenu, "@menu.settlement.status", "/secure/settlement/settlementStatus.xhtml", enabled);
        addIfNotEmpty(settlementMenu, settlementSubMenu);
        Menu settlementSubMenu2 = settlementSubMenu.isEmpty() ? new Menu(MESSAGE_PROPERTY_MENU_SETTLEMENT_HEADING, true) : new Menu("");
        addIfAccess(settlementSubMenu2, "@menu.settlement.result", "/secure/settlement/settlementSummary.xhtml", enabled);
        addIfAccess(settlementSubMenu2, "@menu.settlement.mandate_distribution", "/secure/settlement/settlementResult.xhtml", enabled);
        addIfAccess(settlementSubMenu2, "@menu.settlement.candidate_announcement", "/secure/settlement/candidateAnnouncement.xhtml", enabled);
        addIfAccess(settlementSubMenu2, "@menu.settlement.leveling_seats", "/secure/settlement/levelingSeats.xhtml", enabled);
        addIfNotEmpty(settlementMenu, settlementSubMenu2);
        Menu settlementSubMenu3 = settlementSubMenu2.isEmpty() ? new Menu(MESSAGE_PROPERTY_MENU_SETTLEMENT_HEADING, true) : new Menu("");
        addIfAccess(settlementSubMenu3, "@menu.settlement.corrections_report", "/secure/settlement/correctionsReport.xhtml", enabled);
        addIfNotEmpty(settlementMenu, settlementSubMenu3);

        if (!settlementMenu.isEmpty()) {
            getMenus().add(settlementMenu);
        }
    }

    void addIfAccess(Menu menu, String text, String url) {
        addIfAccess(menu, text, url, true);
    }

    private void addIfAccess(Menu menu, String text, String url, boolean enabled) {
        if (pageAccess.hasAccess(userData, url)) {
            menu.addChild(new Menu(text, url).setEnabled(enabled));
        }
    }

    private void addIfAccess(Menu menu, String text, String url, boolean enabled, boolean deprecated) {
        if (pageAccess.hasAccess(userData, url)) {
            menu.addChild(new Menu(text, url, deprecated).setEnabled(enabled));
        }
    }

    void addIfNotEmpty(Menu parent, Menu childMenus) {
        if (!childMenus.isEmpty()) {
            parent.addChild(childMenus);
        }
    }

    public List<Menu> getMenus() {
        return menus;
    }
}
