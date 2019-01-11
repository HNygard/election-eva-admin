package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.voting.ctrls.VotingConfirmationOverviewController.Component;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingConfirmationOverviewViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingOverviewType;
import no.valg.eva.admin.util.DateUtil;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.APPROVED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.REJECTED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.TO_BE_CONFIRMED;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingConfirmationTestData.votingConfirmationStatusList;
import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.primefaces.model.SortOrder.ASCENDING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class VotingConfirmationOverviewControllerTest extends BaseFrontendTest {
    private static final String VOTING_CATEGORY_REQUEST_PARAMETER = "votingCategory";
    private static final String VOTING_CATEGORY_NAME_FILTER_KEY = "votingCategory.name";
    private static final String CONFIRMATION_STATUS_FILTER_KEY = "status";
    private static final String VOTING_PHASE_REQUEST_PARAMETER = "phase";
    private static final String VALIDATED_REQUEST_PARAMETER = "validated";
    private static final String FROM_DATE_REQUEST_PARAMETER = "fromDate";
    private static final String TO_DATE_REQUEST_PARAMETER = "toDate";

    private VotingConfirmationOverviewController controller;
    private ElectionGroup electionGroupMock;
    private MvArea mvAreaMock;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        controller = initializeMocks(VotingConfirmationOverviewController.class);

        mvAreaMock = mockField("selectedMvArea", MvArea.class);
        electionGroupMock = mockField("selectedElectionGroup", ElectionGroup.class);
    }

    @Test(dataProvider = "getContextPickerSetup")
    public void testGetContextPickerSetup_verifiesCorrectSetup(KontekstvelgerOppsett expectedSetup) {
        KontekstvelgerOppsett actualSetup = controller.getKontekstVelgerOppsett();
        for (int i = 0; i < expectedSetup.getElementer().size(); i++) {
            assertEquals(actualSetup.getElementer().get(i), expectedSetup.getElementer().get(i));
        }
    }

    @DataProvider
    public Object[][] getContextPickerSetup() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        setup.leggTil(hierarki(VALGGRUPPE));
        setup.leggTil(geografi(KOMMUNE));

        return new Object[][]{
                {setup}
        };
    }

    @Test(dataProvider = "context")
    public void testInitialized_givenContext_verifiesSelectedAreaPath(Kontekst context) {
        AreaPath selectedAreaPath = context.getValggeografiSti().areaPath().toAreaLevelPath(KOMMUNE.tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(selectedAreaPath);

        controller.initialized(context);
        verify(getInjectMock(MvAreaService.class), times(1)).findSingleByPath(valggeografiSti);
    }

    @Test(dataProvider = "context")
    public void testInitialized_givenContext_verifiesSelectedElectionGroup(Kontekst context) {
        controller.initialized(context);
        verify(getInjectMock(MvElectionService.class), times(1)).findSingleByPath(context.valggruppeSti());
    }

    @DataProvider
    public Object[][] context() {
        return new Object[][]{
                {getTestContext()}
        };
    }

    @DataProvider
    public Object[][] votingCategoryFromRequest() {
        return new Object[][]{
                {getTestContext(), "fi", singletonList(VotingCategory.FI)},
                {getTestContext(), "FI", singletonList(VotingCategory.FI)},
                {getTestContext(), "VO", singletonList(VotingCategory.VO)},
                {getTestContext(), "VF", singletonList(VotingCategory.VF)},
                {getTestContext(), "VS", singletonList(VotingCategory.VS)},
                {getTestContext(), "VB", singletonList(VotingCategory.VB)},
                {getTestContext(), "FU", singletonList(VotingCategory.FU)},
                {getTestContext(), "FA", singletonList(VotingCategory.FA)},
                {getTestContext(), "FB", singletonList(VotingCategory.FB)},
                {getTestContext(), "FE", singletonList(VotingCategory.FE)},
                {getTestContext(), "", emptyList()},
        };
    }

    @DataProvider
    public Object[][] votingPhaseFromRequest() {
        return new Object[][]{
                {getTestContext(), "early", VotingPhase.EARLY},
                {getTestContext(), "advance", VotingPhase.ADVANCE},
                {getTestContext(), "election_day", VotingPhase.ELECTION_DAY},
                {getTestContext(), "lAte", VotingPhase.LATE},
                {getTestContext(), "", null},
        };
    }

    @DataProvider
    public Object[][] votingConfirmationStatusFromRequest() {
        return new Object[][]{
                {getTestContext(), "aPProved", APPROVED},
                {getTestContext(), "REJECTED", REJECTED},
                {getTestContext(), "to_be_conFirmed", TO_BE_CONFIRMED},
                {getTestContext(), "", null},
        };
    }

    @DataProvider
    public Object[][] showValidatedVotingsFromRequest() {
        return new Object[][]{
                {getTestContext(), "tRue", true},
                {getTestContext(), "", false},
                {getTestContext(), "fAlSE", false},
        };
    }

    @Test(dataProvider = "context")
    public void testInitialized_verifiesLazyDataModel(Kontekst context) {
        controller.initialized(context);
        assertNotNull(controller.getVotingLazyDataModel());
        assertThat(controller.getVotingLazyDataModel()).isInstanceOf(LazyDataModel.class);
    }

    @DataProvider
    public Object[][] lazyDataModelVotingCategoryFilter() {
        return new Object[][]{
                {getTestContext(), votingCategoryFilterMap(VotingCategory.FI), VotingCategory.FI},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.FB), VotingCategory.FB},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.FU), VotingCategory.FU},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.FE), VotingCategory.FE},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.FA), VotingCategory.FA},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.VF), VotingCategory.VF},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.VS), VotingCategory.VS},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.VB), VotingCategory.VB},
                {getTestContext(), votingCategoryFilterMap(VotingCategory.VO), VotingCategory.VO},
        };
    }

    private Map<String, List<String>> votingCategoryFilterMap(VotingCategory votingCategory) {
        return singletonMap(VOTING_CATEGORY_NAME_FILTER_KEY, singletonList(votingCategory.getId()));
    }

    @DataProvider
    public Object[][] lazyDataModelConfirmationStatusFilter() {
        return new Object[][]{
                {getTestContext(), singletonMap(CONFIRMATION_STATUS_FILTER_KEY, APPROVED), APPROVED},
                {getTestContext(), singletonMap(CONFIRMATION_STATUS_FILTER_KEY, REJECTED), REJECTED},
                {getTestContext(), singletonMap(CONFIRMATION_STATUS_FILTER_KEY, TO_BE_CONFIRMED), TO_BE_CONFIRMED},
        };
    }

    @DataProvider
    public Object[][] fetchVotingsServiceCall() {
        return new Object[][]{
                {getTestContext(),
                        votingFilters(localFromDateTime(1, 1), localToDateTime(12, 31), null, null),
                        votingSorting("nameLine", SortOrder.ASCENDING),
                        0, 10}
        };
    }

    @DataProvider
    public Object[][] verifyAllCategoriesSelected() {
        return new Object[][]{
                {getTestContext(),
                        votingFilters(localFromDateTime(1, 1), localToDateTime(12, 31), null, null),
                        votingSorting("", ASCENDING),
                        0, 10}
        };
    }

    private VotingSorting votingSorting(String sortField, SortOrder sortOrder) {
        return VotingSorting.builder()
                .sortField(sortField)
                .sortOrder(sortOrder != null ? sortOrder.name() : null)
                .build();
    }

    private VotingFilters votingFilters(LocalDateTime fromDate, LocalDateTime toDateIncluding, VotingConfirmationStatus votingConfirmationStatus, VotingCategory votingCategory) {
        return VotingFilters.builder()
                .votingConfirmationStatus(votingConfirmationStatus)
                .votingCategories(singletonList(votingCategory))
                .votingCategories(emptyList())
                .fromDate(fromDate)
                .toDateIncluding(toDateIncluding)
                .build();
    }

    private LocalDateTime localFromDateTime(int month, int dayOfMonth) {
        return DateUtil.startOfDay(LocalDate.now().withYear(2018).withMonth(month).withDayOfMonth(dayOfMonth));
    }

    private LocalDateTime localToDateTime(int month, int dayOfMonth) {
        return DateUtil.endOfDay(LocalDate.now().withYear(2018).withMonth(month).withDayOfMonth(dayOfMonth));
    }

    private Kontekst getTestContext() {
        Kontekst context = new Kontekst();
        context.setCountCategory(FO);
        context.setValggeografiSti(STEMMEKRETS_STI);
        context.setValghierarkiSti(ValghierarkiSti.valggruppeSti(ELECTION_PATH_CONTEST));

        return context;
    }

    @Test
    public void testGetPageTitleMeta_givenMvArea_verifyPageTitleMetaBuilderInvocation() throws NoSuchFieldException, IllegalAccessException {
        controller.getPageTitleMeta();
        PageTitleMetaBuilder pageTitleMetaBuilder = getPrivateField("pageTitleMetaBuilder", PageTitleMetaBuilder.class);
        verify(pageTitleMetaBuilder, times(1)).area(mvAreaMock);
    }

    @Test(dataProvider = "getVotingConfirmationStatusesTestData")
    public void testGetVotingConfirmationStatuses_givenShowValidatedVotings_verifiesConfirmationStatusList(boolean showValidatedVotings,
                                                                                                           List<VotingConfirmationStatus> expectedList)
            throws NoSuchFieldException, IllegalAccessException {
        mockViewModel(showValidatedVotings);
        assertEquals(controller.getVotingConfirmationStatuses(), expectedList);
    }

    @DataProvider
    public Object[][] getVotingConfirmationStatusesTestData() {
        return new Object[][]{
                {false, votingConfirmationStatusList(TO_BE_CONFIRMED)},
                {true, votingConfirmationStatusList(APPROVED, REJECTED)}
        };
    }

    @Test(dataProvider = "getVotingOverviewHeadingTestData")
    public void testGetVotingOverviewHeading(boolean showValidatedVotings, String expectedMessage) throws NoSuchFieldException, IllegalAccessException {
        mockViewModel(showValidatedVotings);
        assertEquals(controller.getViewModel().getVotingOverviewHeading(), expectedMessage);
    }

    @DataProvider
    public Object[][] getVotingOverviewHeadingTestData() {
        return new Object[][]{
                {false, "@voting.confirmation.heading.votingListToProcess"},
                {true, "@voting.confirmation.heading.processedVotingList"}
        };
    }

    @Test(dataProvider = "isStatusFilterDisabledTestData")
    public void testIsStatusFilterDisabled_givenShowValidatedVotings_verifiesStatusFilterDisabled(boolean showValidatedVotings, boolean expectedResult)
            throws NoSuchFieldException, IllegalAccessException {
        mockViewModel(showValidatedVotings);
        assertEquals(controller.isStatusFilterDisabled(), expectedResult);
    }

    @DataProvider
    public Object[][] isStatusFilterDisabledTestData() {
        return new Object[][]{
                {false, true},
                {true, false}
        };
    }

    @Test(dataProvider = "isStatusSortableTestData")
    public void testIsStatusSortable_givenShowValidatedVotings_verifiesSortable(boolean showValidatedVotings, boolean expectedResult)
            throws NoSuchFieldException, IllegalAccessException {
        mockViewModel(showValidatedVotings);
        assertEquals(controller.isStatusSortable(), expectedResult);
    }

    @DataProvider
    public Object[][] isStatusSortableTestData() {
        return new Object[][]{
                {false, false},
                {true, true}
        };
    }

    private void mockViewModel(boolean showValidatedVotings) throws NoSuchFieldException, IllegalAccessException {
        VotingConfirmationOverviewViewModel viewModel = VotingConfirmationOverviewViewModel.builder()
                .votingOverviewType(showValidatedVotings ? VotingOverviewType.CONFIRMED : VotingOverviewType.TO_BE_CONFIRMED)
                .build();
        mockFieldValue("viewModel", viewModel);
    }

    @Test(dataProvider = "isRenderVoterConfirmationTestData")
    public void testIsRenderVoterConfirmation(Component activeComponent, boolean expectedResult) throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("activeComponent", activeComponent);
        assertEquals(controller.isRenderVoterConfirmation(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderVoterConfirmationTestData() {
        return new Object[][]{
                {Component.VOTINGS, false},
                {Component.VOTER, true},
        };
    }

    @Test(dataProvider = "isRenderVotingOverviewTestData")
    public void testIsRenderVotingOverview(Component activeComponent, boolean expectedResult) throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("activeComponent", activeComponent);
        assertEquals(controller.isRenderVotingOverview(), expectedResult);
    }

    @DataProvider
    public Object[][] isRenderVotingOverviewTestData() {
        return new Object[][]{
                {Component.VOTINGS, true},
                {Component.VOTER, false},
        };
    }

    @Test
    public void testOnSelectedVotingRow() {
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();
        votingCategory.setId(VotingCategory.FI.getId());

        VotingViewModel votingViewModel = VotingViewModel.builder()
                .votingCategory(votingCategory)
                .build();
        SelectEvent event = createMock(SelectEvent.class);
        when(event.getObject()).thenReturn(votingViewModel);

        controller.setViewModel(VotingConfirmationOverviewViewModel.builder().build());
        controller.onSelectedVotingRow(event);
        assertEquals(controller.getViewModel().getSelectedVoting(), votingViewModel);
    }

    @Test
    public void testOnVoterConfirmationDismiss() throws NoSuchFieldException, IllegalAccessException {
        controller.onVoterConfirmationDismiss();
        assertEquals(getPrivateField("activeComponent", Component.class), Component.VOTINGS);
    }
}