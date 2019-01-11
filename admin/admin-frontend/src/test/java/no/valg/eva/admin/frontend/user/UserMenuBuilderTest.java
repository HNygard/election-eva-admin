package no.valg.eva.admin.frontend.user;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.reports.ctrls.ReportLinksController;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class UserMenuBuilderTest {

    private final String LOCAL_XHTML = "/secure/config/local/local.xhtml";
    private final String OPERATOR_ADMIN_XHTML = "/secure/rbac/operatorAdmin.xhtml";
    private final String REPORTING_UNITS_XHTML = "/secure/config/local/reportingUnits.xhtml";
    private final String ELECTORAL_ROLL_OVERVIEW_XHTML = "/secure/config/local/electoralRollOverview.xhtml";
    private final String CREATE_LIST_PROPOSAL_XHTML = "/secure/listProposal/createListProposal.xhtml";
    private final String EDIT_LIST_PROPOSAL_XHTML = "/secure/listProposal/chooseEditListProposal.xhtml";
    private final String DOWNLOAD_BALLOT_FILE_XHTML = "/secure/listProposal/lastNedStemmeseddelfil.xhtml";
    private final String ELECTORAL_ROLL_SEARCH_XHTML = "/secure/manntall/sok.xhtml";

    private final String COUNTING_BATCHES_XHTML = "/secure/counting/batches.xhtml";
    private final String GENERATE_BARCODES_XHTML = "/secure/opptelling/genererStrekkodelapper.xhtml";
    private final String EVA_RESULT_REPORTING_XHTML = "/secure/reporting/statistics/evaResultatRapportering.xhtml";
    private final String COUNTY_COLLATED_COUNT_RESULTS_XHTML = "/secure/counting/countyCollatedCountResults.xhtml";
    private final String COUNTING_OVERVIEW_XHTML = "/secure/counting/countingOverview.xhtml";
    private final String NUMBER_OF_VOTES_PUT_ASIDE_XHTML = "/secure/counting/antallStemmesedlerLagtTilSide.xhtml";

    private final String MENU_CONFIG_LOCAL_TEXT_PROPERTY = "@menu.config.local";

    private PageAccess pageAccess; 

    private final UserData userData = getUserData(AreaLevelEnum.MUNICIPALITY);
    
    private final AreaPath AREA_PATH_MUNICIPALITY_LEVEL = AREA_PATH_111111_11_11_1111;
    private final ElectionPath ELECTION_PATH_COUNTY_LEVEL = ELECTION_PATH_111111_11_11;
    
    @BeforeTest
    public void beforeTest(){
        pageAccess = mock(PageAccess.class);
    }

    @Test
    public void testPreliminariesMenu_Complete_verifiesAllMenuItems() {
        UserMenuBuilder userMenuBuilder = prepareUserMenuBuilder();

        Menu preliminariesMenu = userMenuBuilder.getMenus().get(0);
        assertCorrectNumberOfChildMenus(preliminariesMenu, 5);
        assertMenu(preliminariesMenu, "@menu.preliminaries" + ".header", "eva-icon-signup", true, null, null);

        assertElectionConfigurationMenu(preliminariesMenu);
        assertElectoralRollOverviewMenu(preliminariesMenu);
        assertListProposalMenu(preliminariesMenu);
        assertElectoralRollMenu(preliminariesMenu);
    }

    @Test
    public void testVotingMenu_Complete_verifiesAllMenuItems() {
        UserMenuBuilder userMenuBuilder = prepareUserMenuBuilder(false);
        Menu votingMainMenu = getVotingMainMenu(userMenuBuilder);
        assertCorrectNumberOfChildMenus(votingMainMenu, 7);
        assertMenu(votingMainMenu, "@menu.voting.votings.header", "eva-icon-paper", true, null, null);

        assertVotingInEnvelopeMenu(votingMainMenu);
        assertEarlyVotingMenu(votingMainMenu);
        assertElectionDayMenu(votingMainMenu);
        assertCommonVotingFunctionsMenu(votingMainMenu);
    }

    @Test(dataProvider = "forTestCountingMenu", dataProviderClass = UserMenuBuilderTestDataProvider.class)
    public void testCountingMenu_givenShouldHaveBoroughs_verifiesNumberOfMenus(boolean shouldHaveBoroughs, int expectedNumberOfVoteCountMenus) {
        UserData userData = getUserData(AreaLevelEnum.MUNICIPALITY);
        mockCountingMenuPageAccessGiveAccessToAllPages(pageAccess, userData);

        UserMenuBuilder userMenuBuilder = prepareUserMenuBuilder(shouldHaveBoroughs);
        Menu countingMainMenu = getCountingAdminMenu(userMenuBuilder);
        assertCorrectNumberOfChildMenus(countingMainMenu, 5);
        assertMenu(countingMainMenu, "@menu.counting.heading", "eva-icon-box", true, null, null);

        assertAdministrationMenu(countingMainMenu);
        assertBatchImportParentMenu(countingMainMenu);
        assertCountingCategoriesMenu(countingMainMenu);
        assertAddVoteCountsMenu(countingMainMenu, expectedNumberOfVoteCountMenus);
        assertInsertVotingNumbersMenu(countingMainMenu);
    }

    @Test(dataProvider = "forTestAddIfAccess", dataProviderClass = UserMenuBuilderTestDataProvider.class)
    public void testAddIfAccess_givenHasAccess_verifiesChildMenus(boolean hasAccess) {
        ReportLinksController reportLinksController = mock(ReportLinksController.class);

        UserMenuMetadata userMenuMetadata = getUserMenuMetadata(ELECTION_PATH_COUNTY_LEVEL, AREA_PATH_MUNICIPALITY_LEVEL, true,
                hasAccess, true);

        UserMenuBuilder userMenuBuilder = new UserMenuBuilder(userMenuMetadata, pageAccess, userData, null,
                true, reportLinksController.getReports());

        String text = "text";
        String pageUrl = "themagicpage.xthml";

        Menu aMenu = new Menu(text, "url");

        when(pageAccess.hasAccess(userData, pageUrl)).thenReturn(hasAccess);

        userMenuBuilder.addIfAccess(aMenu, text, pageUrl);

        assertEquals(aMenu.getChildren().isEmpty(), !hasAccess);
    }

    @Test(dataProvider = "forTestCountingMenu", dataProviderClass = UserMenuBuilderTestDataProvider.class)
    public void testUserMenuBuilder_AccessToBorough_verifiesNumberOfVoteCountMenus(boolean shouldHaveBoroughs, int expectedNumberOfVoteCountMenus) {
        UserMenuBuilder userMenuBuilder = prepareUserMenuBuilder(shouldHaveBoroughs);

        //Make sure the counting menu does not contain the foreign votes since this is not a borough election
        Menu addCountsMenu = getAddCountsMenu(userMenuBuilder);
        assertCorrectNumberOfChildMenus(addCountsMenu, expectedNumberOfVoteCountMenus);

        assertMenuItemInMenu(addCountsMenu, shouldHaveBoroughs);
    }

    @Test(dataProvider = "forTestAddIfNotEmpty", dataProviderClass = UserMenuBuilderTestDataProvider.class)
    public void testAddIfNotEmpty_givenChildMenu_verifiesChildMenus(Menu childMenu) {
        ReportLinksController reportLinksController = mock(ReportLinksController.class);

        UserMenuMetadata userMenuMetadata = getUserMenuMetadata(ELECTION_PATH_COUNTY_LEVEL, AREA_PATH_MUNICIPALITY_LEVEL, true, true, true);

        UserMenuBuilder userMenuBuilder = new UserMenuBuilder(userMenuMetadata, pageAccess, userData, null,
                true, reportLinksController.getReports());

        Menu aMenu = new Menu("text", "url");

        userMenuBuilder.addIfNotEmpty(aMenu, childMenu);

        assertEquals(aMenu.isEmpty(), childMenu.getChildren().isEmpty());
    }

    @Test
    public void testCreateVotingMenu() {
        UserMenuMetadata userMenuMetadata = getUserMenuMetadata(ELECTION_PATH_COUNTY_LEVEL, AREA_PATH_MUNICIPALITY_LEVEL, true, true, true);

        UserMenuBuilder userMenuBuilder = new UserMenuBuilder(userMenuMetadata, pageAccess, userData, null,
                true, Collections.emptyList());
    }

    private void assertMenuItemInMenu(Menu menu, boolean shouldContain) {
        assertEquals(menusWithTextProperty(menu.getChildren(), "@menu.counting.foreign_votes_borough").count() > 0, shouldContain, "Menu item should contain [" + "@menu.counting.foreign_votes_borough" + "] -");
    }

    private void assertElectionDayMenu(Menu votingMainMenu) {
        Menu electionDayMenu = votingMainMenu.getChildren().get(4);
        assertCorrectNumberOfChildMenus(electionDayMenu, 1);
        assertMenu(electionDayMenu, "@menu.electionDay.header", null, true, null, null);

        Menu electionDayRegisterMenu = electionDayMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(electionDayRegisterMenu, 0);
        assertMenu(electionDayRegisterMenu, "@menu.electionDay.register", null, false, "valgtingOrdinaer",
                "/secure/stemmegivning/valgtingOrdinaer.xhtml");
    }

    private void assertAdvanceVotingPeriodMenu3(Menu votingMainMenu) {
        Menu advanceVotingPeriodMenu3 = votingMainMenu.getChildren().get(2);
        assertCorrectNumberOfChildMenus(advanceVotingPeriodMenu3, 1);
        assertMenu(advanceVotingPeriodMenu3, "@menu.commonVoting.header", null, true, null, null);

        Menu approveVotesMenu = advanceVotingPeriodMenu3.getChildren().get(0);
        assertCorrectNumberOfChildMenus(approveVotesMenu, 0);
        assertMenu(approveVotesMenu, "@menu.report.empty_election_card", null, false, "emptyElectionCard",
                "/secure/stemmegivning/emptyElectionCard.xhtml");
    }

    private void assertCommonVotingFunctionsMenu(Menu votingMainMenu) {
        Menu commonVotingFunctionsMenu = votingMainMenu.getChildren().get(5);
        assertCorrectNumberOfChildMenus(commonVotingFunctionsMenu, 1);
        assertMenu(commonVotingFunctionsMenu, "@menu.commonVoting.header", null, true, null, null);

        Menu emptyElectionCardMenu = commonVotingFunctionsMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(emptyElectionCardMenu, 0);
        assertMenu(emptyElectionCardMenu, "@menu.report.empty_election_card", null, false, "emptyElectionCard",
                "/secure/stemmegivning/emptyElectionCard.xhtml");
    }

    private Menu getVotingMainMenu(UserMenuBuilder userMenuBuilder) {
        return userMenuBuilder.getMenus().get(1);
    }
    
    private UserMenuMetadata getUserMenuMetadata(ElectionPath electionPath, AreaPath areaPath, boolean hasElectionsWithTypeProportionalRepresentation, 
                                                 boolean shouldHaveBoroughs, boolean scanningEnabled) {
        return UserMenuMetadata.builder()
                .electionPath(electionPath)
                .areaPath(areaPath)
                .hasElectionsWithTypeProportionalRepresentation(hasElectionsWithTypeProportionalRepresentation)
                .validVoteCountCategories(getCountCategories())
                .electronicMarkOffsConfigured(true)
                .accessToBoroughs(shouldHaveBoroughs)
                .scanningEnabled(scanningEnabled)
                .build();
    }

    private Menu getAddCountsMenu(UserMenuBuilder userMenuBuilder) {
        return getCountingAdminMenu(userMenuBuilder).getChildren().get(3);
    }

    private Menu getCountingAdminMenu(UserMenuBuilder userMenuBuilder) {
        return userMenuBuilder.getMenus().get(2);
    }

    private void assertCorrectNumberOfChildMenus(Menu menu, int expectedNumberOfChildMenus) {
        assertEquals(menu.getChildren().size(), expectedNumberOfChildMenus, "Wrong number of child menus - ");
    }

    private void assertInsertVotingNumbersMenu(Menu countingMainMenu) {
        //Insert voting numbers menu
        Menu insertVotingNumbersMenu = countingMainMenu.getChildren().get(4);
        assertCorrectNumberOfChildMenus(insertVotingNumbersMenu, 2);
        assertMenu(insertVotingNumbersMenu, "@menu.counting.approve_rejected.heading", null, true, null, null);
    }

    private void assertVotingInEnvelopeMenu(Menu votingMainMenu) {
        Menu votingInEnvelopeMenu = votingMainMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(votingInEnvelopeMenu, 2);
        assertMenu(votingInEnvelopeMenu, "@menu.voting.envelopes.header", null, true, null, null);

        Menu registerInEnvelopeMenu = votingInEnvelopeMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(registerInEnvelopeMenu, 0);
        assertMenu(registerInEnvelopeMenu, "@menu.voting.registerInEnvelope", null, false,
                "registerVotingInEnvelope",
                "/secure/voting/registerVotingInEnvelope.xhtml");

        Menu votingConfirmationInEnvelopeMenu = votingInEnvelopeMenu.getChildren().get(1);
        assertCorrectNumberOfChildMenus(votingConfirmationInEnvelopeMenu, 0);
        assertMenu(votingConfirmationInEnvelopeMenu, "@menu.voting.envelope.confirmation", null, false,
                "votingConfirmation",
                "/secure/voting/confirming/votingConfirmation.xhtml");

        Menu confirmedVotingsOverviewMenu = votingMainMenu.getChildren().get(1);
        assertCorrectNumberOfChildMenus(confirmedVotingsOverviewMenu, 2);
        
        Menu unconfirmedVotingsOverviewMenu = confirmedVotingsOverviewMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(unconfirmedVotingsOverviewMenu, 0);
        assertMenu(unconfirmedVotingsOverviewMenu, "@menu.voting.envelope.overview_unconfirmed", null, false,
                "votingConfirmationOverview_validated_false",
                "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=false");

        Menu confirmedVotingsOverviewSubMenu = confirmedVotingsOverviewMenu.getChildren().get(1);
        assertMenu(confirmedVotingsOverviewSubMenu, "@menu.voting.envelope.overview_confirmed", null, false,
                "votingConfirmationOverview_validated_true",
                "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=true");

    }

    private void assertEarlyVotingMenu(Menu votingMainMenu) {
        Menu manualEarlyVotingMenu = votingMainMenu.getChildren().get(3);
        assertCorrectNumberOfChildMenus(manualEarlyVotingMenu, 1);
        assertMenu(manualEarlyVotingMenu, "@menu.earlyVoting.header", null, true, null, null);

        Menu earlyVotingStandardMenu = manualEarlyVotingMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(earlyVotingStandardMenu, 0);
        assertMenu(earlyVotingStandardMenu, "@menu.earlyVoting.manual", null, false, "forhandOrdinaer",
                "/secure/stemmegivning/forhandOrdinaer.xhtml");
    }

    private void assertAddVoteCountsMenu(Menu countingMainMenu, int expectedNumberOfVoteCountChildMenus) {
        //Insert voting numbers menu
        Menu addVoteCountsMenu = countingMainMenu.getChildren().get(3);
        assertCorrectNumberOfChildMenus(addVoteCountsMenu, expectedNumberOfVoteCountChildMenus);
        assertMenu(addVoteCountsMenu, "", null, false, "startCountingMenu", null);

    }

    private void assertCountingCategoriesMenu(Menu countingMainMenu) {
        //counting categories menu
        Menu countingCategoriesMenu = countingMainMenu.getChildren().get(2);
        assertCorrectNumberOfChildMenus(countingCategoriesMenu, 1);
        assertMenu(countingCategoriesMenu, "@menu.counting.countingCategories.heading", null, true, null, null);

        Menu countingOverviewMenu = countingCategoriesMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(countingOverviewMenu, 0);
        assertMenu(countingOverviewMenu, "@menu.counting.overview", null, false, "countingOverview", COUNTING_OVERVIEW_XHTML);
    }

    private void assertBatchImportParentMenu(Menu countingMainMenu) {
        //batch import menu
        Menu batchImportParentMenu = countingMainMenu.getChildren().get(1);
        assertCorrectNumberOfChildMenus(batchImportParentMenu, 4);
        assertMenu(batchImportParentMenu, "", null, false, null, null);

        Menu batchImportMenu = batchImportParentMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(batchImportMenu, 0);
        assertMenu(batchImportMenu, "@menu.import.heading", null, false, "batches", COUNTING_BATCHES_XHTML);

        Menu barcodeStickerMenu = batchImportParentMenu.getChildren().get(1);
        assertCorrectNumberOfChildMenus(barcodeStickerMenu, 0);
        assertMenu(barcodeStickerMenu, "@menu.counting.barcode_sticker", null, false, "genererStrekkodelapper", GENERATE_BARCODES_XHTML);

        Menu evaResultReportingMenu = batchImportParentMenu.getChildren().get(2);
        assertCorrectNumberOfChildMenus(evaResultReportingMenu, 0);
        assertMenu(evaResultReportingMenu, "@menu.statistic.reporting", null, false, "evaResultatRapportering", EVA_RESULT_REPORTING_XHTML);

        Menu countyCollatedCountResultsMenu = batchImportParentMenu.getChildren().get(3);
        assertCorrectNumberOfChildMenus(countyCollatedCountResultsMenu, 0);
        assertMenu(countyCollatedCountResultsMenu, "@menu.counting.countyResults", null, false, "countyCollatedCountResults",
                COUNTY_COLLATED_COUNT_RESULTS_XHTML);
    }

    private void assertAdministrationMenu(Menu countingMainMenu) {
        Menu administrationMenu = countingMainMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(administrationMenu, 1);
        assertMenu(administrationMenu, "@menu.counting.administration.heading", null, true, null, null);

        //Number-of-ballots-put-aside-menu... :|
        Menu numberOfBallotsPutAsideMenu = administrationMenu.getChildren().get(0);
        assertCorrectNumberOfChildMenus(numberOfBallotsPutAsideMenu, 0);
        assertMenu(numberOfBallotsPutAsideMenu, "@menu.opptelling.antallStemmesedlerLagtTilSide", null, false,
                "antallStemmesedlerLagtTilSide", NUMBER_OF_VOTES_PUT_ASIDE_XHTML);
    }

    private Stream<Menu> menusWithTextProperty(List<Menu> menus, String textProperty) {
        return menus
                .stream()
                .filter(currentMenu -> currentMenu.getText().equals(textProperty));
    }

    private UserMenuBuilder prepareUserMenuBuilder() {
        return prepareUserMenuBuilder(true);
    }

    private UserMenuBuilder prepareUserMenuBuilder(boolean shouldHaveBoroughs) {
        return prepareUserMenuBuilder(shouldHaveBoroughs, AREA_PATH_MUNICIPALITY_LEVEL, ELECTION_PATH_COUNTY_LEVEL);
    }

    private UserMenuBuilder prepareUserMenuBuilder(boolean shouldHaveBoroughs, AreaPath areaPath,
                                                   ElectionPath electionPath) {
        ReportLinksController reportLinksController = mock(ReportLinksController.class);
        UserDataController userDataController = mock(UserDataController.class);

        ElectionEvent electionEvent = getParliamentElectionEvent();

        mockPreliminaryMenuAccessGiveAccessToAllPages(pageAccess, userData);
        mockVotingMenuAccessGiveAccessToAllPages(pageAccess, userData);
        mockCountingMenuPageAccessGiveAccessToAllPages(pageAccess, userData);

        when(userDataController.getElectionEvent()).thenReturn(electionEvent);
        when(userDataController.isOverrideAccess()).thenReturn(false);

        UserMenuMetadata userMenuMetadata = getUserMenuMetadata(electionPath, areaPath, true, shouldHaveBoroughs, true);
        UserMenuBuilder userMenuBuilder = new UserMenuBuilder(userMenuMetadata, pageAccess, userData, null, true,
                reportLinksController.getReports());

        //Checking the correct number of main menus
        assertEquals(userMenuBuilder.getMenus().size(), 3);

        return userMenuBuilder;
    }

    private UserData getUserData(AreaLevelEnum areaLevel) {
        OperatorRole operatorRole = new OperatorRole();

        ElectionEvent electionEvent = new ElectionEvent(1L);

        MvElection mvElection = new MvElection();
        mvElection.setAreaLevel(areaLevel.getLevel());
        operatorRole.setMvElection(mvElection);

        Operator operator = new Operator();
        operator.setPk(1L);
        operator.setElectionEvent(electionEvent);

        operatorRole.setOperator(operator);

        UserData userData = new UserData();
        userData.setOperatorRole(operatorRole);

        return userData;
    }

    private void mockPreliminaryMenuAccessGiveAccessToAllPages(PageAccess pageAccess, UserData userData) {
        when(pageAccess.hasAccess(userData, LOCAL_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, OPERATOR_ADMIN_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, REPORTING_UNITS_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, ELECTORAL_ROLL_OVERVIEW_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, CREATE_LIST_PROPOSAL_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, EDIT_LIST_PROPOSAL_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, DOWNLOAD_BALLOT_FILE_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, ELECTORAL_ROLL_SEARCH_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/manntall/opprett.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/manntall/listVoterAudit.xhtml")).thenReturn(true);
    }

    private void mockVotingMenuAccessGiveAccessToAllPages(PageAccess pageAccess, UserData userData) {
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/forhandOrdinaer.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/forhandKonvolutterSentralt.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/forhandSentInnkommet.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/forhandProvingVelger.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/forhandProvingSamlet.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/faVotingsSentFromMunicipality.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/valgtingOrdinaer.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/valgtingKonvolutterSentralt.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/valgtingProvingVelger.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/valgtingProvingSamlet.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/emptyElectionCard.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/rejectedVotingsReport.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/votingStatus.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/stemmegivning/votingStatus.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/voting/registerVotingInEnvelope.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/voting/confirming/votingConfirmation.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=true")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/voting/confirming/votingConfirmationOverview.xhtml?validated=false")).thenReturn(true);
    }

    private void mockCountingMenuPageAccessGiveAccessToAllPages(PageAccess pageAccess, UserData userData) {
        when(pageAccess.hasAccess(userData, NUMBER_OF_VOTES_PUT_ASIDE_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, COUNTING_BATCHES_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, GENERATE_BARCODES_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, EVA_RESULT_REPORTING_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, COUNTY_COLLATED_COUNT_RESULTS_XHTML)).thenReturn(true);
        when(pageAccess.hasAccess(userData, COUNTING_OVERVIEW_XHTML)).thenReturn(true);

        //Making sure all count categories are covered access wise
        for (CountCategory countCategory : EnumSet.allOf(CountCategory.class)) {
            when(pageAccess.hasAccess(userData, "/secure/opptelling/registrerOpptellinger.xhtml?kontekst=countCategory|".concat(countCategory.name()))).thenReturn(true);
        }

        when(pageAccess.hasAccess(userData, "/secure/opptelling/behandleManueltForkastede.xhtml")).thenReturn(true);
        when(pageAccess.hasAccess(userData, "/secure/opptelling/behandleSkannetForkastede.xhtml")).thenReturn(true);
    }

    private List<CountCategory> getCountCategories() {
        return new ArrayList<>(EnumSet.allOf(CountCategory.class));
    }

    private void assertElectionConfigurationMenu(Menu rootMenu) {
        Menu electionConfigurationMenu = rootMenu.getChildren().get(0);
        assertMenu(electionConfigurationMenu, "@menu.electionConfig", null, true, null, null);

        Menu localConfigMenu = electionConfigurationMenu.getChildren().get(0);
        assertMenu(localConfigMenu, MENU_CONFIG_LOCAL_TEXT_PROPERTY, null, false, "local", LOCAL_XHTML);

        Menu operatorAdminMenu = electionConfigurationMenu.getChildren().get(1);
        assertMenu(operatorAdminMenu, "@menu.operators", null, false, "operatorAdmin", OPERATOR_ADMIN_XHTML);
    }

    private void assertElectoralRollMenu(Menu rootMenu) {
        Menu electoralRollMenu = rootMenu.getChildren().get(3);
        assertMenu(electoralRollMenu, "@menu.electoralRoll.header", null, true, null, null);

        Menu electoralSearchMenu = electoralRollMenu.getChildren().get(0);
        assertMenu(electoralSearchMenu, "@menu.electoralRoll.search", null, false, "sok", ELECTORAL_ROLL_SEARCH_XHTML);
    }

    private void assertElectoralRollOverviewMenu(Menu rootMenu) {
        Menu electoralOverviewMenuParent = rootMenu.getChildren().get(1);
        assertMenu(electoralOverviewMenuParent, "", null, false, null, null);

        Menu electoralRollOverviewMenu = electoralOverviewMenuParent.getChildren().get(0);
        assertMenu(electoralRollOverviewMenu, MENU_CONFIG_LOCAL_TEXT_PROPERTY + ".electoral_roll_overview", null, false,
                "electoralRollOverview", ELECTORAL_ROLL_OVERVIEW_XHTML);
    }

    private void assertListProposalMenu(Menu rootMenu) {
        Menu listProposalMenu = rootMenu.getChildren().get(2);
        assertMenu(listProposalMenu, "@menu.listProposal", null, true, null, null);
        assertCorrectNumberOfChildMenus(listProposalMenu, 3);

        Menu createListProposalMenu = listProposalMenu.getChildren().get(0);
        assertMenu(createListProposalMenu, "@menu.listProposal.create", null, false, "createListProposal",
                CREATE_LIST_PROPOSAL_XHTML);

        Menu editListProposalMenu = listProposalMenu.getChildren().get(1);
        assertMenu(editListProposalMenu, "@menu.listProposal.edit", null, false, "chooseEditListProposal",
                EDIT_LIST_PROPOSAL_XHTML);

        Menu printBaseListProposalMenu = listProposalMenu.getChildren().get(2);
        assertMenu(printBaseListProposalMenu, "@menu.listProposal.print_base", null, false, "lastNedStemmeseddelfil",
                DOWNLOAD_BALLOT_FILE_XHTML);
    }

    private void assertMenu(Menu menu, String text, String icon, boolean hasHeader, String cssClass, String url) {
        assertEquals(menu.getText(), text);
        assertEquals(menu.hasHeader(), hasHeader);
        assertEquals(menu.getCssClass(), cssClass);
        assertEquals(menu.getIcon(), icon);
        assertEquals(menu.getUrl(), url);
    }

    private ElectionEvent getParliamentElectionEvent() {
        return getParliamentElectionEvent(new Locale());
    }

    private ElectionEvent getParliamentElectionEvent(Locale locale) {
        ElectionEvent electionEvent = new ElectionEvent("900010", "Stortingsvalg", locale);
        ElectionEventStatus electionEventStatus = new ElectionEventStatus();
        electionEvent.setElectionEventStatus(electionEventStatus);

        return electionEvent;
    }
}
