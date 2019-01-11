package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.voting.ctrls.VoterConfirmation.VoterConfirmationContext;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingViewModel;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import org.joda.time.LocalDate;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ConfirmVotingControllerTest extends BaseFrontendTest {

    private ConfirmVotingController confirmVotingController;

    private MvArea mvAreaMock;

    private VotingInEnvelopeService votingInEnvelopeService;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        confirmVotingController = initializeMocks(ConfirmVotingController.class);

        votingInEnvelopeService = getInjectMock(VotingInEnvelopeService.class);

        mvAreaMock = mockField("selectedMvArea", MvArea.class);
        mockField("selectedElectionGroup", ElectionGroup.class);
    }

    @Test
    public void testInitialized_confirmVotingStatuses_verifiesMenuItems() {
        List<ConfirmationCategoryStatus> confirmationCategoryStatuses = mockConfirmationCategoryStatuses();

        List<ConfirmVotingMenuItem> expectedMenuItems = new ArrayList<>();
        ConfirmVotingMenuItem expectedMenuItem = ConfirmVotingMenuItem.builder()
                .categoryClosedMessage("closedMessage")
                .categoryOpen(true)
                .backgroundCss("backgroundCss")
                .dataAftId("dataAftId")
                .menuLabel("menuLabel")
                .iconCss("iconCss")
                .build();
        expectedMenuItems.add(expectedMenuItem);

        ConfirmVotingMenuItemFactory confirmVotingMenuItemFactory = getInjectMock(ConfirmVotingMenuItemFactory.class);
        when(confirmVotingMenuItemFactory.buildMenuItems(confirmationCategoryStatuses)).thenReturn(expectedMenuItems);


        Kontekst context = getTestContext();
        confirmVotingController.initialized(context);
        ConfirmVotingMenuItem actualMenuItem = confirmVotingController.getMenuItems().get(0);

        assertEquals(confirmVotingController.getMenuItems().size(), 1);
        assertEquals(actualMenuItem.getBackgroundCss(), expectedMenuItem.getBackgroundCss());
        assertEquals(actualMenuItem.getIconCss(), expectedMenuItem.getIconCss());
        assertEquals(actualMenuItem.getCategoryClosedMessage(), expectedMenuItem.getCategoryClosedMessage());
        assertEquals(actualMenuItem.getDataAftId(), expectedMenuItem.getDataAftId());
        assertEquals(actualMenuItem.getMenuLabel(), expectedMenuItem.getMenuLabel());
        assertTrue(actualMenuItem.isCategoryOpen());

    }

    private List<ConfirmationCategoryStatus> mockConfirmationCategoryStatuses() {
        List<ConfirmationCategoryStatus> confirmationCategoryStatuses = new ArrayList<>();
        confirmationCategoryStatuses.add(confirmationCategoryStatus(FI));

        mockConfirmationCategoryStatuses(confirmationCategoryStatuses);
        return confirmationCategoryStatuses;
    }

    private void mockConfirmationCategoryStatuses(List<ConfirmationCategoryStatus> confirmationCategoryStatuses) {
        when(votingInEnvelopeService.confirmationCategoryStatuses(any(UserData.class), any(MvArea.class), any(ElectionGroup.class)))
                .thenReturn(confirmationCategoryStatuses);
    }

    private Kontekst getTestContext() {
        Kontekst context = new Kontekst();
        context.setCountCategory(FO);
        context.setValggeografiSti(STEMMEKRETS_STI);
        context.setValghierarkiSti(ValghierarkiSti.valggruppeSti(ELECTION_PATH_CONTEST));

        return context;
    }

    @Test
    public void testGetKontekstVelgerOppsett() {
        KontekstvelgerOppsett kontekstVelgerOppsett = confirmVotingController.getKontekstVelgerOppsett();

        assertEquals(kontekstVelgerOppsett.getElementer().size(), 2);
        assertEquals(kontekstVelgerOppsett.getElementer().get(0), KontekstvelgerElement.hierarki(VALGGRUPPE));
        assertEquals(kontekstVelgerOppsett.getElementer().get(1), KontekstvelgerElement.geografi(KOMMUNE));
    }

    @Test
    public void testGetPageTitleMeta_givenMvArea_verifyPageTitleMetaBuilderInvocation() throws NoSuchFieldException, IllegalAccessException {
        confirmVotingController.getPageTitleMeta();
        PageTitleMetaBuilder pageTitleMetaBuilder = getPrivateField("pageTitleMetaBuilder", PageTitleMetaBuilder.class);
        verify(pageTitleMetaBuilder, times(1)).area(mvAreaMock);
    }

    @Test(dataProvider = "onAccordionChangeConfirmVotingViewModelTestData")
    public void testOnAccordionChange_givenMenuItem_verifyConfirmVotingViewModel(ConfirmVotingMenuItem menuItem, String clientId) {
        TabChangeEvent tabChangeEvent = mock(TabChangeEvent.class);
        when(tabChangeEvent.getData()).thenReturn(menuItem);

        Tab tab = createMock(Tab.class);
        when(tab.getClientId()).thenReturn(clientId);
        when(tabChangeEvent.getTab()).thenReturn(tab);

        confirmVotingController.onAccordionChange(tabChangeEvent);

        ConfirmVotingViewModel confirmVotingViewModel = confirmVotingController.viewModel(menuItem);
        assertEquals(confirmVotingViewModel.getVotingCategory(), menuItem.getVotingCategory());
        assertEquals(confirmVotingViewModel.isCategoryOpen(), menuItem.isCategoryOpen());
        assertEquals(confirmVotingViewModel.getStartDate(), menuItem.getStartDate());
        assertEquals(confirmVotingViewModel.getEndDateIncluding(), menuItem.getEndDateIncluding());

        verify(getInjectMock(ConfirmVotingContent.class), times(1)).initComponent(confirmVotingViewModel, confirmVotingController);
    }

    @DataProvider
    public Object[][] onAccordionChangeConfirmVotingViewModelTestData() {
        return new Object[][]{
                {confirmVotingMenuItem(), "form:clientId:1"}
        };
    }

    private ConfirmationCategoryStatus confirmationCategoryStatus(VotingCategory votingCategory) {
        return ConfirmationCategoryStatus.builder()
                .startingDate(new LocalDate())
                .messageProperty("messageProperty")
                .votingPhase(VotingPhase.ADVANCE)
                .locked(LockType.UNLOCKED)
                .tense(Tense.PRESENT)
                .votingCategory(votingCategory)
                .build();
    }

    private static ConfirmVotingMenuItem confirmVotingMenuItem() {
        return confirmVotingMenuItem(false);
    }

    private static ConfirmVotingMenuItem confirmVotingMenuItem(boolean isCategoryOpen) {
        return ConfirmVotingMenuItem.builder()
                .votingCategory(VS)
                .categoryOpen(isCategoryOpen)
                .startDate(LocalDateTime.now())
                .endDateIncluding(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test(dataProvider = "currentMenuItemTestIndexFromRequestParameterTestData")
    public void testCurrentMenuItemIndexFromRequestParameter(List<ConfirmVotingMenuItem> accordionPanels, String requestParamIndex, String expectedIndex) {
        
        ConfirmVotingMenuItemFactory confirmVotingMenuItemFactory = getInjectMock(ConfirmVotingMenuItemFactory.class);
        when(confirmVotingMenuItemFactory.buildMenuItems(any())).thenReturn(accordionPanels);
        getServletContainer().setRequestParameter("menuItem", requestParamIndex);
        Kontekst context = getTestContext();
        confirmVotingController.initialized(context);
        
        if (expectedIndex != null) {
            verify(getRequestContextMock(), times(1)).execute(format("EVA.Application.getInstance().getView().selectTab(%s)", expectedIndex));
        }
        else {
            verify(getRequestContextMock(), times(0)).execute(anyString());
        }
    }

    @DataProvider
    private Object[][] currentMenuItemTestIndexFromRequestParameterTestData() {

        return new Object[][] {
                {numberOfItems(1), paramValue("0"), expectedTabIndex(0)},
                {numberOfItems(2), paramValue("1"), expectedTabIndex(1)},
                {numberOfItems(3), paramValue("2"), expectedTabIndex(2)},
                {numberOfItems(1), paramValue("2"), expectedTabIndex(0)},
                {numberOfItems(1), paramValue("-1"), expectedTabIndex(0)},
                {numberOfItems(1), paramValue("Anders"), expectedTabIndex(0)},
                {numberOfItems(1), paramValue("123"), expectedTabIndex(0)},
                {numberOfItems(1), paramValue(""), noTabSelected()},
                {numberOfItems(1), paramValue(" "), noTabSelected()},
                {numberOfItems(1), paramValue("     "), noTabSelected()},
                {numberOfItems(1), paramValue(null), noTabSelected()},
        };
    }

    private List<ConfirmVotingMenuItem> numberOfItems(int numberOfItems) {
        final List<ConfirmVotingMenuItem> items = new ArrayList<>(numberOfItems);
        while(numberOfItems > 0) {
            items.add(confirmVotingMenuItem());
            numberOfItems--;
        }
        return items;
    }

    private String paramValue(String param) {
        return param;
    }

    private String expectedTabIndex(int tab) {
        return Integer.toString(tab);
    }

    private String noTabSelected() {
        return null;
    }
    @Test
    public void testComponentDidUpdate_isCalled_whenForceUpdateTriggered() {
        
        confirmVotingController.setSelectedMenuItem(confirmVotingMenuItem());
        ConfirmVotingContent component = getInjectMock(ConfirmVotingContent.class);
        
        confirmVotingController.forceUpdate(component);
        
        verify(component).componentDidUpdate(any(ConfirmVotingViewModel.class));
    }

    @Test
    public void testOnSelectedVoting_verifiesActiveComponent() {
        confirmVotingController.setVotingConfirmationViewModel(ConfirmVotingViewModel.builder().votingCategory(VotingCategory.VS).build());
        confirmVotingController.onSelectedVoting(VotingViewModel.builder().build());
        assertEquals(confirmVotingController.getActiveComponent(), ConfirmVotingController.Component.VOTER);
    }

    @Test(dataProvider = "onSelectedVotingTestData")
    public void testOnSelectedVoting_verifiesVoterConfirmationInit(VotingViewModel votingViewModel, VoterConfirmationContext context) {
        confirmVotingController.setVotingConfirmationViewModel(ConfirmVotingViewModel.builder().votingCategory(VotingCategory.VS).build());
        confirmVotingController.onSelectedVoting(votingViewModel);
        verify(getInjectMock(VoterConfirmation.class), times(1)).initComponent(context);
        assertFacesUtilUpdateDom("form");
    }

    @DataProvider
    public Object[][] onSelectedVotingTestData() {
        VoterDto voterDto = voterDto();
        VoterConfirmationContext context = voterConfirmationContext(voterDto);
        return new Object[][]{
                {votingViewModel(voterDto), context}
        };
    }

    @Test
    public void testOnVoterConfirmationDismiss_verifiesActiveComponent() throws NoSuchFieldException, IllegalAccessException {
        mockSelectedMenuItem();

        confirmVotingController.onVoterConfirmationDismiss();
        assertEquals(confirmVotingController.getActiveComponent(), ConfirmVotingController.Component.VOTINGS);
        assertFacesUtilUpdateDom("form");
    }

    @Test
    public void testOnVoterConfirmationDismiss_verifiesUpdateDom() throws NoSuchFieldException, IllegalAccessException {
        mockSelectedMenuItem();

        confirmVotingController.onVoterConfirmationDismiss();
        assertFacesUtilUpdateDom("form");
    }

    @Test(dataProvider = "isRenderConfirmVotingAccordionTestData")
    public void testIsRenderConfirmVotingAccordion(ConfirmVotingController.Component activeComponent, boolean expectedResult)
            throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("activeComponent", activeComponent);
        assertEquals(confirmVotingController.isRenderConfirmVotingAccordion(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderConfirmVotingAccordionTestData() {
        return new Object[][]{
                {ConfirmVotingController.Component.VOTER, false},
                {ConfirmVotingController.Component.VOTINGS, true},
        };
    }

    private void mockSelectedMenuItem() throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("selectedMenuItem", ConfirmVotingMenuItem.builder()
                .categoryOpen(false)
                .build());
    }

    private VoterDto voterDto() {
        return VoterDto.builder().build();
    }

    private VoterConfirmationContext voterConfirmationContext(VoterDto voterDto) {
        return VoterConfirmationContext.builder()
                .votingCategory(VotingCategory.VS)
                .voterDto(voterDto)
                .handler(confirmVotingController)
                .userData(getUserDataMock())
                .build();
    }

    private VotingViewModel votingViewModel(VoterDto voterDto) {
        return VotingViewModel.builder()
                .voter(voterDto)
                .votingCategory(domainVotingCategory())
                .build();
    }

    private no.valg.eva.admin.configuration.domain.model.VotingCategory domainVotingCategory() {
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();
        votingCategory.setId(VotingCategory.FI.getId());
        return votingCategory;
    }
}