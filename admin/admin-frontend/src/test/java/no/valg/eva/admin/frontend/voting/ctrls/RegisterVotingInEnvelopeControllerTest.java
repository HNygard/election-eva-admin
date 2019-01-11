package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.melding.Alvorlighetsgrad;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.MeldingerWidget;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.util.FacesUtilHelper;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.AvgittStemme;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerMeldingType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.component.accordionpanel.AccordionPanel;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.voting.LockType.UNLOCKED;
import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.INFO;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class RegisterVotingInEnvelopeControllerTest extends BaseFrontendTest {

    private RegisterVotingInEnvelopeController registerVotingInEnvelopeController;

    private MeldingerWidget messagesWidget;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        registerVotingInEnvelopeController = initializeMocks(RegisterVotingInEnvelopeController.class);
        messagesWidget = mockField("messagesWidget", MeldingerWidget.class);
    }

    @Test(dataProvider = "getKontekstVelgerOppsettTestData")
    public void testGetKontekstvelgerOppsett(KontekstvelgerOppsett expectedSetup) {
        KontekstvelgerOppsett kontekstVelgerOppsett = registerVotingInEnvelopeController.getKontekstVelgerOppsett();

        for (int i = 0; i < expectedSetup.getElementer().size() - 1; i++) {
            assertEquals(kontekstVelgerOppsett.getElementer().get(i), expectedSetup.getElementer().get(i));
        }

        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        setup.leggTil(hierarki(VALGGRUPPE));
        setup.leggTil(geografi(KOMMUNE));
    }

    @DataProvider
    public Object[][] getKontekstVelgerOppsettTestData() {
        KontekstvelgerOppsett kontekstVelgerOppsett = new KontekstvelgerOppsett();
        kontekstVelgerOppsett.leggTil(hierarki(VALGGRUPPE));
        kontekstVelgerOppsett.leggTil(geografi(KOMMUNE));
        return new Object[][]{
                {kontekstVelgerOppsett}
        };
    }

    @Test
    public void testInitialized() {
        Kontekst context = getTestContext();

        AreaPath selectedAreaPath = context.getValggeografiSti().areaPath().toAreaLevelPath(KOMMUNE.tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(selectedAreaPath);
        MvArea mvArea = MvArea.builder().build();
        when(getInjectMock(MvAreaService.class).findSingleByPath(valggeografiSti)).thenReturn(mvArea);

        UserData userDataMock = getUserDataMock();
        List<VotingCategoryStatus> votingCategoryStatuses = new ArrayList<>();
        votingCategoryStatuses.add(buildVotingCategoryStatus(VotingCategory.FI));
        votingCategoryStatuses.add(buildVotingCategoryStatus(VotingCategory.FU));
        when(getInjectMock(VotingInEnvelopeService.class).votingCategoryStatuses(userDataMock, mvArea)).thenReturn(votingCategoryStatuses);
        
        registerVotingInEnvelopeController.initialized(context);
        
        verify(getInjectMock(RegisterVotingInEnvelopeMenuFactory.class)).buildMenuItems(eq(votingCategoryStatuses), any());
        verify(getInjectMock(RegisterVotingInEnvelopeSearchListener.class)).addListener(registerVotingInEnvelopeController);
    }

    private Kontekst getTestContext() {
        Kontekst context = new Kontekst();
        context.setCountCategory(FO);
        context.setValggeografiSti(STEMMEKRETS_STI);
        context.setValghierarkiSti(ValghierarkiSti.valggruppeSti(ELECTION_PATH_CONTEST));

        return context;
    }

    @Test(dataProvider = "onTabChangeTestData")
    public void testOnTabChange_GivenCategory_VerifiesResetTabContent(String clientId) {
        TabChangeEvent tabChangeEvent = getTabChangeEvent(clientId);

        mockOnSelectedCategory(tabChangeEvent);

        registerVotingInEnvelopeController.onTabChange(tabChangeEvent);

        verifyResetTabContent();
    }

    private void verifyResetTabContent() {
        assertNull(registerVotingInEnvelopeController.getVoter());
        assertFalse(registerVotingInEnvelopeController.isEmptySearchResult());
        assertTrue(registerVotingInEnvelopeController.isShowElectoralRollSearchView());
        assertFalse(registerVotingInEnvelopeController.isShowRegisterVotingView());

        verifyClearMessagesWidget();
        verifyResetElectoralRollSearchWidget();
    }

    @DataProvider
    public Object[][] onTabChangeTestData() {
        return new Object[][]{
                {"tab:1"}
        };
    }

    @Test(dataProvider = "onTabChangeVerifiesTabIndexTestData")
    public void testOnTabChange_GivenCategory_VerifiesSetActiveTabIndex(String clientId, int expectedActiveTabIndex) {
        TabChangeEvent tabChangeEvent = getTabChangeEvent(clientId);
        mockOnSelectedCategory(tabChangeEvent);

        registerVotingInEnvelopeController.onTabChange(tabChangeEvent);

        assertEquals(registerVotingInEnvelopeController.getActiveTabIndex(), expectedActiveTabIndex);
    }

    @DataProvider
    public Object[][] onTabChangeVerifiesTabIndexTestData() {
        return new Object[][]{
                {"tab:1", 1},
                {"tab:2", 2},
                {"tab:3", 3}
        };
    }

    @Test(dataProvider = "onTabChangeTestData")
    public void testOnTabChange_Given_VerifiesMenuItemSet(String clientId) {
        TabChangeEvent tabChangeEvent = getTabChangeEvent(clientId);
        RegisterVotingInEnvelopeMenuItem menuItem = mockOnSelectedCategory(tabChangeEvent);

        registerVotingInEnvelopeController.onTabChange(tabChangeEvent);

        assertEquals(registerVotingInEnvelopeController.getSelectedMenuItem(), menuItem);
    }

    @Test(dataProvider = "onTabChangeTestData")
    public void testOnTabChange_GivenClientId_verifiesOnSelectedVotingCategoryCalled(String clientId) {
        TabChangeEvent tabChangeEvent = getTabChangeEvent(clientId);
        RegisterVotingInEnvelopeMenuItem menuItem = mockOnSelectedCategory(tabChangeEvent);

        registerVotingInEnvelopeController.onTabChange(tabChangeEvent);

        verify(menuItem.getVotingController(), times(1))
                .onSelectedVotingCategory(any(), any(), any());
    }

    @Test(dataProvider = "onTabChangeTestData")
    public void testOnTabChange_GivenClientId_verifiesSelectedView(String clientId) {
        TabChangeEvent tabChangeEvent = getTabChangeEvent(clientId);
        RegisterVotingInEnvelopeMenuItem menuItem = mockOnSelectedCategory(tabChangeEvent);

        registerVotingInEnvelopeController.onTabChange(tabChangeEvent);

        assertEquals(registerVotingInEnvelopeController.getView(), menuItem.getView());
    }

    @Test
    public void testGetElectionGeoLevel_VerifiesLevel() {
        assertEquals(registerVotingInEnvelopeController.getElectionGeoLevel(), KOMMUNE);
    }

    @Test(dataProvider = "isAdvanceVoteInBallotBoxTestData")
    public void testIsAdvanceVoteInBallotBox_VerifiesIsAdvanceVoteInBallotBox(boolean isAdvanceVoteInBallotBox) throws NoSuchFieldException, IllegalAccessException {
        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setAdvanceVoteInBallotBox(isAdvanceVoteInBallotBox);
        MvArea mvArea = mockField("selectedMvArea", MvArea.class);
        when(mvArea.getPollingPlace()).thenReturn(pollingPlace);

        assertEquals(registerVotingInEnvelopeController.isForhandsstemmeRettIUrne(), isAdvanceVoteInBallotBox);
    }

    @DataProvider
    public Object[][] isAdvanceVoteInBallotBoxTestData() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @Test(dataProvider = "registerVotingTestData")
    public void testRegisterVoting_GivenVoter_VerifiesClearMessages(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        mockForRegisterVoting();

        registerVotingInEnvelopeController.registerVoting(voter);

        verifyClearMessagesWidget();
    }

    @Test(dataProvider = "registerVotingTestData")
    public void testRegisterVoting_GivenVoter_VerifiesShowSearchView(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        mockForRegisterVoting();

        registerVotingInEnvelopeController.registerVoting(voter);
        assertFalse(registerVotingInEnvelopeController.isShowRegisterVotingView());
        assertTrue(registerVotingInEnvelopeController.isShowElectoralRollSearchView());
    }

    @Test(dataProvider = "registerVotingTestData")
    public void testRegisterVoting_GivenVoter_VerifiesResetElectoralRollSearchWidget(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        mockForRegisterVoting();

        registerVotingInEnvelopeController.registerVoting(voter);

        verifyResetElectoralRollSearchWidget();
    }

    @Test(dataProvider = "registerVotingTestData")
    public void testRegisterVoting_GivenVoter_VerifiesCallMenuItemController(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuItem menuItem = mockForRegisterVoting();

        registerVotingInEnvelopeController.registerVoting(voter);

        verify(menuItem.getVotingController(), times(1)).registerVoting(eq(voter), any(), any(Municipality.class), any(VotingPhase.class));
    }

    @DataProvider
    public Object[][] registerVotingTestData() {
        return new Object[][]{
                {Voter.builder().build()}
        };
    }

    @Test
    public void testOnCancelRegistrationClick_VerifiesResetTabContent() {
        registerVotingInEnvelopeController.onCancelRegistrationClick();

        verifyResetTabContent();
    }

    @Test(dataProvider = "addMessageTestData")
    public void testAddMessage_GivenMessage_VerifiesAddedToMessageWidget(Melding melding) {
        registerVotingInEnvelopeController.addMessage(melding);
        verify(messagesWidget, times(1)).add(melding);
    }

    @DataProvider
    public Object[][] addMessageTestData() {
        return new Object[][]{
                {new Melding(INFO, "melding")},
                {new Melding(Alvorlighetsgrad.ERROR, "melding")}
        };
    }

    @Test(dataProvider = "showRegisterFictitiousVoterLinkTestData")
    public void testShowRegisterFictitiousVoterLink_VerifiesLinkShown(VotingCategory votingCategory,
                                                                      boolean emptySearchResult,
                                                                      boolean expectsLink,
                                                                      boolean isAdvanceVoteInBallotBox)
            throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuItem menuItem = mockForRegisterVoting();
        when(menuItem.getVotingCategory()).thenReturn(votingCategory);

        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setAdvanceVoteInBallotBox(isAdvanceVoteInBallotBox);
        MvArea mvArea = mockField("selectedMvArea", MvArea.class);
        when(mvArea.getPollingPlace()).thenReturn(pollingPlace);

        mockFieldValue("emptySearchResult", emptySearchResult);
        mockFieldValue("selectedMvArea", mvArea);

        assertEquals(registerVotingInEnvelopeController.isShowRegisterFictitiousVoterLink(), expectsLink);
    }

    @DataProvider
    public Object[][] showRegisterFictitiousVoterLinkTestData() {
        return new Object[][]{
                {VotingCategory.VS, true, true, false},
                {VotingCategory.VS, false, false, false},
                {VotingCategory.FI, true, false, true},
                {VotingCategory.FI, true, true, false}
        };
    }

    private RegisterVotingInEnvelopeMenuItem mockForRegisterVoting() throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuItem menuItem = mockField("selectedMenuItem", RegisterVotingInEnvelopeMenuItem.class);
        mockField("selectedMvArea", MvArea.class);
        
        TabChangeEvent tabChangeEvent = getTabChangeEvent("tab:1");
        tabChangeEvent.setData(menuItem);
        AdvanceVotingInEnvelopeCentralController menuItemController = mock(AdvanceVotingInEnvelopeCentralController.class);
        when(menuItem.getVotingController()).thenReturn(menuItemController);
        when(menuItem.getVotingPhase()).thenReturn(VotingPhase.ADVANCE);
        doNothing().when(menuItemController).registerVoting(any(Voter.class), any(MvElection.class), any(Municipality.class), any(VotingPhase.class));

        return menuItem;
    }

    private TabChangeEvent getTabChangeEvent(String clientId) {
        Tab tab = mock(Tab.class);
        when(tab.getClientId()).thenReturn(clientId);
        return new TabChangeEvent(new AccordionPanel(), new AjaxBehavior(), tab);
    }

    private RegisterVotingInEnvelopeMenuItem mockOnSelectedCategory(TabChangeEvent tabChangeEvent) {
        RegisterVotingInEnvelopeMenuItem menuItem = mock(RegisterVotingInEnvelopeMenuItem.class);
        tabChangeEvent.setData(menuItem);
        AdvanceVotingInEnvelopeCentralController menuItemController = mock(AdvanceVotingInEnvelopeCentralController.class);

        when(menuItem.getVotingController()).thenReturn(menuItemController);
        doNothing().when(menuItemController)
                .onSelectedVotingCategory(isA(MvArea.class), isA(VotingCategory.class), isA(VotingPhase.class));

        return menuItem;
    }

    private void verifyClearMessagesWidget() {
        verify(messagesWidget, times(1)).clear();
    }

    private void verifyResetElectoralRollSearchWidget() {
        verify(getInjectMock(ManntallsSokWidget.class)).reset();
    }

    private VotingCategoryStatus buildVotingCategoryStatus(VotingCategory fi) {
        return new VotingCategoryStatus("messageProperty", fi, VotingPhase.ADVANCE, PRESENT, UNLOCKED, LocalDate.now());
    }

    @Test
    public void testOnEmptyElectoralRollSearch_givenEmptySearchResult_verifyControllerEmptySearchResult() {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = setupControllerForEmptySearchResultTest();
        registerVotingInEnvelopeController.onEmptyElectoralRollSearchResult();
        assertTrue(registerVotingInEnvelopeController.isEmptySearchResult());
    }

    @Test
    public void testOnEmptyElectoralRollSearch_givenEmptySearchResult_verifyVotingControllerVoterFoundInSearch() {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = setupControllerForEmptySearchResultTest();
        registerVotingInEnvelopeController.onEmptyElectoralRollSearchResult();
        assertFalse(registerVotingInEnvelopeController.getVotingController().isVoterFoundInSearch());
    }

    @Test
    public void testOnEmptyElectoralRollSearch_givenEmptySearchResult_verifyDomUpdate() {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = setupControllerForEmptySearchResultTest();
        registerVotingInEnvelopeController.onEmptyElectoralRollSearchResult();
        verify(getInjectMock(FacesUtilHelper.class), times(1)).updateDom("registerVotesInEnvelopePanel");
    }

    private RegisterVotingInEnvelopeController setupControllerForEmptySearchResultTest() {
        VotingControllerMock votingController = new VotingControllerMock();
        registerVotingInEnvelopeController.setSelectedMenuItem(RegisterVotingInEnvelopeMenuItem.builder()
                .votingController(votingController)
                .build());

        doNothing().when(getInjectMock(FacesUtilHelper.class)).updateDom(anyString());

        return registerVotingInEnvelopeController;
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenFictitiousVoter_VerifyCanRegisterVoting()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingView();
        assertTrue(registerVotingInEnvelopeController.isCanRegisterVoting());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenFictitiousVoter_VerifyShowElectoralRollSearchView()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingView();
        assertFalse(registerVotingInEnvelopeController.isShowElectoralRollSearchView());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenFictitiousVoter_VerifyShowRegisterVotingView()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingView();
        assertTrue(registerVotingInEnvelopeController.isShowRegisterVotingView());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenFictitiousVoter_VerifyEmptySearchResult()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingView();
        assertFalse(registerVotingInEnvelopeController.isEmptySearchResult());
    }

    private void mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingView() throws NoSuchFieldException, IllegalAccessException {
        mockField("selectedMvArea", MvArea.class);
        when(getInjectMock(VoterService.class).createFictitiousVoter(any(UserData.class), any(AreaPath.class))).thenReturn(new Voter());
        registerVotingInEnvelopeController.registerFictitiousVoterAndPrepareRegisterVotingView();
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenEvoteException_VerifyEmptySearchResult()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingViewThrowingException();
        assertFalse(registerVotingInEnvelopeController.isEmptySearchResult());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenEvoteException_VerifyCanNotRegisterVoting()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingViewThrowingException();
        assertFalse(registerVotingInEnvelopeController.isCanRegisterVoting());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenEvoteException_VerifyShowRegistrationViewFalse()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingViewThrowingException();
        assertFalse(registerVotingInEnvelopeController.isShowRegisterVotingView());
    }

    @Test
    public void testRegisterFictitiousVoterAndPrepareRegisterVotingView_GivenEvoteException_VerifyEmptySearchResultTrue()
            throws NoSuchFieldException, IllegalAccessException {
        mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingViewThrowingException();
        assertFalse(registerVotingInEnvelopeController.isEmptySearchResult());
    }

    private void mockAndInvokeRegisterFictitiousVoterAndPrepareRegisterVotingViewThrowingException() throws NoSuchFieldException, IllegalAccessException {
        mockField("selectedMvArea", MvArea.class);
        when(getInjectMock(VoterService.class).createFictitiousVoter(any(UserData.class), any(AreaPath.class))).thenThrow(new EvoteException("Something went wrong.."));
        registerVotingInEnvelopeController.registerFictitiousVoterAndPrepareRegisterVotingView();
    }

    @Test(dataProvider = "renderCurrentMenuItemDataProvider")
    public void testRenderCurrentMenuItem_GivenMenuItem_verifyIfShouldBeRendered(RegisterVotingInEnvelopeMenuItem selectedMenuItem,
                                                                                 RegisterVotingInEnvelopeMenuItem menuItemToVerify, boolean expectsToBeRendered) {
        registerVotingInEnvelopeController.setSelectedMenuItem(selectedMenuItem);
        assertEquals(registerVotingInEnvelopeController.renderCurrentMenuItem(menuItemToVerify), expectsToBeRendered);
    }

    @DataProvider
    public static Object[][] renderCurrentMenuItemDataProvider() {
        return new Object[][]{
                {getMenuItem(true, VotingCategory.FI, VotingPhase.ADVANCE), getMenuItem(true, VotingCategory.FI, VotingPhase.LATE), false},
                {getMenuItem(true, VotingCategory.FI, VotingPhase.LATE), getMenuItem(true, VotingCategory.FI, VotingPhase.LATE), true},
                {getMenuItem(false, VotingCategory.FI, VotingPhase.LATE), getMenuItem(true, VotingCategory.FI, VotingPhase.LATE), false},
        };
    }

    private static RegisterVotingInEnvelopeMenuItem getMenuItem(boolean openForRegistration, VotingCategory votingCategory, VotingPhase votingPhase) {
        RegisterVotingInEnvelopeMenuItem menuItem = RegisterVotingInEnvelopeMenuItem.builder().build();
        menuItem.setOpenForRegistration(openForRegistration);
        menuItem.setVotingCategory(votingCategory);
        menuItem.setVotingPhase(votingPhase);

        return menuItem;
    }

    @Test(dataProvider = "buildVoterMessagesTestData")
    public void testBuildVoterMessages_GivenVoterMessageType_Verify(VelgerSomSkalStemme votingVoter, boolean showVoteToOtherMunicipalityDialog, Melding expectedMessage) {
        when(getInjectMock(MessageProvider.class).get("@common.date.time_pattern")).thenReturn("HH.mm");
        registerVotingInEnvelopeController.setVoter(new Voter());
        registerVotingInEnvelopeController.buildVoterMessages(votingVoter);
        assertEquals(registerVotingInEnvelopeController.isVoteToOtherMunicipalityConfirmDialog(), showVoteToOtherMunicipalityDialog);
        verify(registerVotingInEnvelopeController.getMessagesWidget(), times(1)).add(expectedMessage);
    }

    @DataProvider
    public Object[][] buildVoterMessagesTestData() {

        DateTime dateTime = new DateTime();
        VotingCategory votingCategory = VotingCategory.VS;

        return new Object[][]{
                {getVelgerSomSkalStemme(VelgerMeldingType.FORHANDSTEMME_ANNEN_KOMMUNE, dateTime, votingCategory), true, new Melding(INFO,
                        "@voting.search.voterNotInMunicipalityAdvance")},
                {getVelgerSomSkalStemme(VelgerMeldingType.ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET, dateTime, votingCategory), false, new Melding(INFO,
                        messageWithDateTimeInfo("@voting.search.votingAlreadyCastedRejected", dateTime, votingCategory))},
                {getVelgerSomSkalStemme(VelgerMeldingType.ALLEREDE_IKKE_GODKJENT_STEMME, dateTime, votingCategory), false, new Melding(INFO,
                        messageWithDateTimeInfo("@voting.search.votingAlreadyCastedNotApproved", dateTime, votingCategory))},
                {getVelgerSomSkalStemme(VelgerMeldingType.ALLEREDE_GODKJENT_STEMME, dateTime, votingCategory), false, new Melding(INFO,
                        messageWithDateTimeInfo("@voting.search.votingAlreadyCasted", dateTime, votingCategory))},
                {getVelgerSomSkalStemme(VelgerMeldingType.VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN, dateTime, votingCategory), false, new Melding(INFO,
                        "@voting.search.electionDay.notInMunicipality @voting.search.electionDay.notInMunicipality")},
                {getVelgerSomSkalStemme(VelgerMeldingType.VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN, dateTime, votingCategory), false, new Melding(INFO,
                        "@voting.search.electionDay.notInPollingDistrict @voting.search.electionDay.notInPollingDistrict")},
        };
    }

    private String messageWithDateTimeInfo(String messageProperty, DateTime dateTime, VotingCategory votingCategory) {
        return "[" + messageProperty + ", null, " + votingCategory.getId() + ", @voting_category[" + votingCategory.getId() + "].name, " +
                currentWeekDayMessageProperty() + ", " + DateUtil.getFormattedShortDate(dateTime) + ", " + currentHourAndMinutes(dateTime) + "]";
    }

    private String currentHourAndMinutes(DateTime dateTime) {
        return DateUtil.getFormattedTime(dateTime.toLocalTime(), DateTimeFormat.forPattern("HH.mm"));
    }

    private String currentWeekDayMessageProperty() {
        Calendar calendar = Calendar.getInstance();
        return format("@common.date.weekday[%s].name", calendar.get(Calendar.DAY_OF_WEEK));
    }

    private VelgerSomSkalStemme getVelgerSomSkalStemme(VelgerMeldingType velgerMeldingType, DateTime dateTime, VotingCategory votingCategory) {
        VelgerSomSkalStemme votingVoter = new VelgerSomSkalStemme(Collections.singletonList(new no.valg.eva.admin.configuration.domain.model.VotingCategory()));
        VelgerMelding velgerMelding = new VelgerMelding(velgerMeldingType, INFO, new AvgittStemme(votingCategory, dateTime));
        velgerMelding.setTilleggsMelding(velgerMelding);
        votingVoter.getVelgerMeldinger().add(velgerMelding);
        return votingVoter;
    }
}
