package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingProcessingTypeTab;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_APPROVED;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_REJECTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ConfirmVotingTabsTest extends BaseFrontendTest {

    private ConfirmVotingTabs confirmVotingTabs;

    private ConfirmVotingTabs.ContextViewModel contextViewModel;

    private ConfirmVotingTabs.ViewModel viewModel;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        confirmVotingTabs = initializeMocks(ConfirmVotingTabs.class);
        when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
                .thenReturn(getUserDataMock());

        contextViewModel = contextViewModel();

        viewModel = ConfirmVotingTabs.ViewModel.builder().build();
    }

    private ConfirmVotingTabs.ContextViewModel contextViewModel() {

        List<VotingViewModel> votings = asList(
                VotingViewModel.builder()
                        .personId("1234")
                        .firstName("Bob")
                        .suggestedRejected(true)
                        .build(),
                VotingViewModel.builder()
                        .personId("4321")
                        .firstName("Lisa")
                        .suggestedRejected(false)
                        .build(),
                VotingViewModel.builder()
                        .personId("1324")
                        .firstName("Uncle Joe")
                        .suggestedRejected(false)
                        .build()
        );

        return ConfirmVotingTabs.ContextViewModel.builder()
                .votingCategory(VotingCategory.VS)
                .municipality(mock(Municipality.class))
                .confirmVotingContentHandler(mock(ConfirmVotingTabs.Handler.class))
                .votingList(votings)
                .build();
    }

    @Test
    public void testSelectedProcessingTypeIndexZero_equalsSuggestedRejected() {
        viewModel.setSelectedProcessingTypeIndex(0);
        assertEquals(viewModel.getSelectedProcessingType(), SUGGESTED_REJECTED);
    }

    @Test
    public void testSelectedProcessingTypeIndexOne_equalsSuggestedApproved() {
        viewModel.setSelectedProcessingTypeIndex(1);
        assertEquals(viewModel.getSelectedProcessingType(), SUGGESTED_APPROVED);
    }

    @Test
    public void testSelectedProcessingTypeRejected_equalsIndexZero() {
        viewModel.setSelectedProcessingType(SUGGESTED_REJECTED);
        assertEquals(viewModel.getSelectedProcessingTypeIndex(), 0);
    }

    @Test
    public void testSelectedProcessingTypeApproved_equalsIndexOne() {
        viewModel.setSelectedProcessingType(SUGGESTED_APPROVED);
        assertEquals(viewModel.getSelectedProcessingTypeIndex(), 1);
    }

    @Test(dataProvider = "toDtoListTestData")
    public void testToDtoList_givenList_verifyConversion(List<VotingViewModel> votingViewModels, List<VotingDto> expectedDtoList) {
        ConfirmVotingTabs.ViewModel confirmVotingViewModel = ConfirmVotingTabs.ViewModel.builder().selectedVotingList(votingViewModels).build();
        assertEquals(confirmVotingViewModel.selectedVotingDtoList(), expectedDtoList);
    }

    @DataProvider
    public Object[][] toDtoListTestData() {
        List<VotingViewModel> viewModelList = new ArrayList<>();
        viewModelList.add(votingViewModel("1"));
        viewModelList.add(votingViewModel("2"));

        List<VotingViewModel> viewModelListWithNullValue = new ArrayList<>();
        viewModelListWithNullValue.add(null);

        return new Object[][]{
                {singletonList(viewModelList.get(0)), singletonList(toVotingDto(viewModelList.get(0)))},
                {null, emptyList()},
                {viewModelListWithNullValue, emptyList()},
        };
    }

    private VotingViewModel votingViewModel(String personId) {
        return VotingViewModel.builder()
                .nameLine("fullName")
                .personId(personId)
                .suggestedRejectionReason("suggestedRejectionReason")
                .votingDate("2011-01-01")
                .build();
    }

    private static VotingDto toVotingDto(VotingViewModel votingViewModel) {
        return VotingDto.builder()
                .votingNumber(votingViewModel.getVotingNumber())
                .voterDto(votingViewModel.getVoter())
                .electionGroup(votingViewModel.getElectionGroup())
                .votingCategory(votingViewModel.getVotingCategory())
                .build();
    }


    @Test
    public void testOnInitComponent_doesNotKeepProcessingType() {

        confirmVotingTabs.initComponent(contextViewModel, null);
        final ConfirmVotingTabs.ViewModel viewModel = confirmVotingTabs.getViewModel();
        modifyProcessingType(viewModel);

        confirmVotingTabs.initComponent(contextViewModel, null);
        final ConfirmVotingTabs.ViewModel newViewModel = confirmVotingTabs.getViewModel();

        assertNotEquals(viewModel.getSelectedProcessingType(), newViewModel.getSelectedProcessingType(), "Processing type should not match");
    }

    private void modifyProcessingType(ConfirmVotingTabs.ViewModel viewModel) {
        viewModel.setSelectedProcessingType(oppositeProcessingType(viewModel.getSelectedProcessingType()));
    }

    private ProcessingType oppositeProcessingType(ProcessingType processingType) {
        return processingType == SUGGESTED_REJECTED ? SUGGESTED_APPROVED : SUGGESTED_REJECTED;
    }

    @Test
    public void testComponentDidUpdate_doesKeepProcessingType() {

        confirmVotingTabs.initComponent(contextViewModel, null);
        final ConfirmVotingTabs.ViewModel viewModel = confirmVotingTabs.getViewModel();
        modifyProcessingType(viewModel);

        confirmVotingTabs.componentDidUpdate(contextViewModel);
        final ConfirmVotingTabs.ViewModel newViewModel = confirmVotingTabs.getViewModel();

        assertEquals(viewModel.getSelectedProcessingType(), viewModel.getSelectedProcessingType(), "Processing type should match");
    }

    @Test(dataProvider = "rejectSelectedVotingListTestData")
    public void testRejectVotingList_givenVotingList_verifyServiceCall_andHide(ConfirmVotingTabs.ViewModel mockedViewModel, VotingRejectionDto votingRejection) throws NoSuchFieldException, IllegalAccessException {

        setComponentContext();
        mockField("componentHandler", UpdatableComponentHandler.class);
        confirmVotingTabs.setViewModel(mockedViewModel);
        confirmVotingTabs.onRejectVotings(votingRejection);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .rejectVotingList(getUserDataMock(),
                        mockedViewModel.selectedVotingDtoList(),
                        votingRejection,
                        contextViewModel.getMunicipality());
        
        verify(getInjectMock(VotingRejectionDialog.class)).hide();
    }

    private void setComponentContext() {
        confirmVotingTabs.setContextViewModel(contextViewModel);
    }

    @DataProvider
    public Object[][] rejectSelectedVotingListTestData() {

        List<VotingViewModel> votingViewModelList = new ArrayList<>();
        VotingViewModel votingViewModel = votingViewModel(true);
        votingViewModelList.add(votingViewModel);

        String rejectionId = "rejectionId";
        VotingRejectionDto votingRejectionDto = votingRejectionDto(rejectionId);

        ConfirmVotingTabs.ViewModel confirmVotingTabsViewModel = confirmVotingTabsViewModel(votingViewModelList);

        return new Object[][]{
                {confirmVotingTabsViewModel, votingRejectionDto}
        };
    }

    private VotingViewModel votingViewModel(boolean suggestedRejected) {
        return VotingViewModel.builder()
                .suggestedRejected(suggestedRejected)
                .build();
    }

    private VotingRejectionDto votingRejectionDto(String rejectionId) {
        return VotingRejectionDto.builder()
                .id(rejectionId)
                .build();
    }

    private ConfirmVotingTabs.ViewModel confirmVotingTabsViewModel(List<VotingViewModel> votingViewModelList) {
        return confirmVotingTabsViewModel(votingViewModelList, null);
    }

    private ConfirmVotingTabs.ViewModel confirmVotingTabsViewModel(List<VotingViewModel> votingViewModelList, ProcessingType selectedProcessingType) {
        return ConfirmVotingTabs.ViewModel.builder()
                .votingList(votingViewModelList)
                .selectedVotingList(votingViewModelList)
                .selectedProcessingType(selectedProcessingType)
                .build();
    }

    @Test(dataProvider = "approveSelectedVotingListTestData")
    public void testApproveSelectedVotingList_callsService_onlyWhenProcessingTypeIsOrdinary_elseShowsDialog(
            List<VotingViewModel> votingDtoList, ProcessingType selectedProcessingType) throws NoSuchFieldException, IllegalAccessException {

        setComponentContext();
        mockField("componentHandler", UpdatableComponentHandler.class);

        ConfirmVotingTabs.ViewModel confirmVotingTabsViewModel = mockField("viewModel", ConfirmVotingTabs.ViewModel.class);
        when(confirmVotingTabsViewModel.getSelectedVotingList()).thenReturn(votingDtoList);
        when(confirmVotingTabsViewModel.getSelectedProcessingType()).thenReturn(selectedProcessingType);

        confirmVotingTabs.approveSelectedVotingList();

        verify(getInjectMock(ApproveSuggestedApprovedDialog.class), times(selectedProcessingType == SUGGESTED_APPROVED ? 1 : 0))
                .initComponent(any());

        verify(getInjectMock(ApproveSuggestedApprovedDialog.class), times(selectedProcessingType == SUGGESTED_APPROVED ? 1 : 0))
                .show();

        verify(getInjectMock(VotingApprovalDialog.class), times(selectedProcessingType == SUGGESTED_REJECTED ? 1 : 0))
                .initComponent(any());

        verify(getInjectMock(VotingApprovalDialog.class), times(selectedProcessingType == SUGGESTED_REJECTED ? 1 : 0))
                .show();
    }

    @DataProvider
    public Object[][] approveSelectedVotingListTestData() {
        List<VotingViewModel> votingViewModelList = new ArrayList<>();
        votingViewModelList.add(VotingViewModel.builder()
                .votingNumber(1)
                .build());

        return new Object[][]{
                {votingViewModelList, SUGGESTED_APPROVED},
                {votingViewModelList, SUGGESTED_REJECTED}
        };
    }

    @Test(dataProvider = "isRenderMoveToSuggestedRejectButtonTestData")
    public void testIsRenderMoveToSuggestedRejectedButton(ProcessingType processingType, boolean expectsToBeRendered) throws NoSuchFieldException,
            IllegalAccessException {
        ConfirmVotingTabs.ViewModel confirmVotingContentViewModel = mockField("viewModel", ConfirmVotingTabs.ViewModel.class);
        when(confirmVotingContentViewModel.getSelectedProcessingType()).thenReturn(processingType);
        assertEquals(confirmVotingTabs.isRenderMoveToSuggestedRejectedButton(), expectsToBeRendered);
    }

    @DataProvider
    public Object[][] isRenderMoveToSuggestedRejectButtonTestData() {
        return new Object[][]
                {
                        {SUGGESTED_APPROVED, true},
                        {SUGGESTED_REJECTED, false}
                };
    }

    @Test(dataProvider = "isRenderRejectVotingListButtonTestData")
    public void testIsRenderRejectVotingListButton(ProcessingType processingType, boolean expectsToBeRendered) throws NoSuchFieldException,
            IllegalAccessException {
        ConfirmVotingTabs.ViewModel confirmVotingContentViewModel = mockField("viewModel", ConfirmVotingTabs.ViewModel.class);
        when(confirmVotingContentViewModel.getSelectedProcessingType()).thenReturn(processingType);
        assertEquals(confirmVotingTabs.isRenderRejectVotingListButton(), expectsToBeRendered);
    }

    @DataProvider
    public Object[][] isRenderRejectVotingListButtonTestData() {
        return new Object[][]
                {
                        {SUGGESTED_APPROVED, false},
                        {SUGGESTED_REJECTED, true}
                };
    }

    @Test
    public void testInitAndDisplayVotingSuggestedRejectedDialog() {
        confirmVotingTabs.initComponent(contextViewModel, null);
        confirmVotingTabs.initAndDisplayVotingSuggestedRejectedDialog();
        verify(getInjectMock(VotingSuggestedRejectedDialog.class)).initComponent(any(VotingSuggestedRejectedDialog.ContextViewModel.class));
    }

    @Test
    public void testInitAndDisplayVotingRejectionDialog() {
        confirmVotingTabs.initComponent(contextViewModel, null);
        confirmVotingTabs.initAndDisplayVotingRejectionDialog();
        verify(getInjectMock(VotingRejectionDialog.class)).initComponent(any(VotingRejectionDialog.ContextViewModel.class));
    }


    @Test
    public void testOnMoveVotingsToSuggestedRejected_verifiesServiceCall_andHidesDialog() throws NoSuchFieldException, IllegalAccessException {

        List<VotingViewModel> votingViewModelList = new ArrayList<>();
        VotingViewModel votingViewModel = votingViewModel(true);
        votingViewModelList.add(votingViewModel);

        ConfirmVotingTabs.ViewModel viewModel = confirmVotingTabsViewModel(votingViewModelList, null);
        List<VotingDto> votingDtoList = viewModel.selectedVotingDtoList();

        VotingRejectionDto votingRejectionDto = votingRejectionDto("rejectionId");

        setComponentContext();
        mockField("componentHandler", UpdatableComponentHandler.class);

        confirmVotingTabs.setViewModel(viewModel);
        confirmVotingTabs.onMoveVotingsToSuggestedRejected(votingRejectionDto);

        verify(getInjectMock(VotingInEnvelopeService.class)).moveVotingToSuggestedRejected(getUserDataMock(), votingDtoList,
                votingRejectionDto,
                contextViewModel.getMunicipality());
        
        verify(getInjectMock(VotingSuggestedRejectedDialog.class)).hide();
    }

    @Test(dataProvider = "isApproveVotingButtonDisabledTestData")
    public void testIsApproveVotingButtonDisabled(ConfirmVotingTabs.ViewModel viewModel, boolean expectDisabled) {
        confirmVotingTabs.setViewModel(viewModel);
        assertEquals(confirmVotingTabs.isApproveVotingListButtonDisabled(), expectDisabled);
    }

    @DataProvider
    private Object[][] isApproveVotingButtonDisabledTestData() {
        List<VotingViewModel> singleVotingViewModelInList = new ArrayList<>();
        singleVotingViewModelInList.add(votingViewModel(true));

        List<VotingViewModel> twoVotingViewModelsInList = new ArrayList<>();
        twoVotingViewModelsInList.add(votingViewModel(true));
        twoVotingViewModelsInList.add(votingViewModel(true));

        ConfirmVotingTabs.ViewModel oneSelectedVoting = confirmVotingTabsViewModel(singleVotingViewModelInList);
        ConfirmVotingTabs.ViewModel twoSelectedVotings = confirmVotingTabsViewModel(twoVotingViewModelsInList);
        ConfirmVotingTabs.ViewModel noSelectedVotings = confirmVotingTabsViewModel(emptyList());

        return new Object[][]{
                {noSelectedVotings, true},
                {oneSelectedVoting, false},
                {twoSelectedVotings, false}
        };
    }

    @Test(dataProvider = "isRejectVotingListButtonDisabledTestData")
    public void testIsRejectVotingListButtonDisabled(ConfirmVotingTabs.ViewModel viewModel, boolean expectsDisabled) {
        confirmVotingTabs.setViewModel(viewModel);
        assertEquals(confirmVotingTabs.isRejectVotingListButtonDisabled(), expectsDisabled);
    }

    @DataProvider
    public Object[][] isRejectVotingListButtonDisabledTestData() {
        List<VotingViewModel> votingViewModelList = new ArrayList<>();
        VotingViewModel votingViewModel = votingViewModel(true);
        votingViewModelList.add(votingViewModel);

        ConfirmVotingTabs.ViewModel viewModel1 = confirmVotingTabsViewModel(votingViewModelList);
        ConfirmVotingTabs.ViewModel viewModel2 = confirmVotingTabsViewModel(votingViewModelList);
        viewModel2.setSelectedProcessingType(SUGGESTED_REJECTED);
        viewModel2.setSelectedVotingList(emptyList());

        return new Object[][]{
                {viewModel1, false},
                {viewModel2, true},
        };
    }

    @Test(dataProvider = "isMoveToSuggestedRejectedButtonDisabledTestData")
    public void testIsMoveToSuggestedRejectedButtonDisabled(ConfirmVotingTabs.ViewModel viewModel, boolean expectsDisabled) {
        confirmVotingTabs.setViewModel(viewModel);
        assertEquals(confirmVotingTabs.isMoveToSuggestedRejectedButtonDisabled(), expectsDisabled);
    }

    @DataProvider
    public Object[][] isMoveToSuggestedRejectedButtonDisabledTestData() {
        List<VotingViewModel> votingViewModelList = new ArrayList<>();
        VotingViewModel votingViewModel = votingViewModel(true);
        votingViewModelList.add(votingViewModel);

        ConfirmVotingTabs.ViewModel viewModel1 = confirmVotingTabsViewModel(votingViewModelList);
        ConfirmVotingTabs.ViewModel viewModel2 = confirmVotingTabsViewModel(emptyList(), SUGGESTED_APPROVED);

        return new Object[][]{
                {viewModel1, false},
                {viewModel2, true},
        };
    }

    @Test
    public void testForceUpdateIsCalled_onApproveAndRejectVoting() {

        ConfirmVotingContent parent = mock(ConfirmVotingContent.class);

        confirmVotingTabs.initComponent(contextViewModel, parent);
        confirmVotingTabs.getViewModel().setSelectedProcessingType(SUGGESTED_APPROVED);

        confirmVotingTabs.onApproveSuggestedApprovedVotings(singletonList(VotingViewModel.builder().build()));

        confirmVotingTabs.onMoveVotingsToSuggestedRejected(VotingRejectionDto.builder().build());

        verify(parent, times(2)).forceUpdate(confirmVotingTabs);
    }

    @Test
    public void testProcessingTabs_isOnlyTwo_inTheRightOrder() {
        confirmVotingTabs.initComponent(contextViewModel, null);

        List<VotingProcessingTypeTab> tabs = confirmVotingTabs.getViewModel().getProcessingTypeTabs();

        assertEquals(2, tabs.size());
        assertEquals(SUGGESTED_REJECTED, tabs.get(0).getProcessingType());
        assertEquals(SUGGESTED_APPROVED, tabs.get(1).getProcessingType());
    }

    @Test
    public void testProcessingType_isResolvedOnInit() {
        ConfirmVotingTabs spy = spy(confirmVotingTabs);

        spy.initComponent(contextViewModel, null);

        verify(spy).resolveProcessingType();
        verify(spy).findLargestVotingListForProcessingType(contextViewModel.getVotingList());
    }

    @Test
    public void testProcessingType_isNotResolvedAgainOnUpdate() {
        ConfirmVotingTabs spy = spy(confirmVotingTabs);
        when(spy.processingTypeNullSafe()).thenReturn(SUGGESTED_REJECTED);

        spy.componentDidUpdate(contextViewModel);

        verify(spy, times(0)).resolveProcessingType();
        verify(spy, times(0)).findLargestVotingListForProcessingType(contextViewModel.getVotingList());
    }

    @Test(dataProvider = "largestVotingListProcessingTypeTestData")
    public void testLargestVotingList_hasProcessingTypeSelectedOnInit(ConfirmVotingTabs.ContextViewModel contextViewModel, ProcessingType expectedProcessingType) {
        confirmVotingTabs.initComponent(contextViewModel, null);

        assertEquals(expectedProcessingType, confirmVotingTabs.getViewModel().getSelectedProcessingType());
    }

    @DataProvider
    private Object[][] largestVotingListProcessingTypeTestData() {
        return new Object[][]{
                {contextWithVotings(), SUGGESTED_REJECTED},
                {contextWithVotings(true), SUGGESTED_REJECTED},
                {contextWithVotings(true, false), SUGGESTED_REJECTED},
                {contextWithVotings(false), SUGGESTED_APPROVED},
                {contextWithVotings(true, false, false), SUGGESTED_APPROVED},
                {contextWithVotings(true, true, true), SUGGESTED_REJECTED},
                {contextWithVotings(true, true, false), SUGGESTED_REJECTED},
                {contextWithVotings(true, false, true), SUGGESTED_REJECTED},
                {contextWithVotings(false, true, true), SUGGESTED_REJECTED},
                {contextWithVotings(false, false, true), SUGGESTED_APPROVED},
                {contextWithVotings(false, false, false), SUGGESTED_APPROVED}
        };
    }

    private ConfirmVotingTabs.ContextViewModel contextWithVotings(boolean... suggestedRejecteds) {

        List<VotingViewModel> votings = new ArrayList<>(suggestedRejecteds.length);
        for (boolean rejected : suggestedRejecteds) {
            votings.add(
                    VotingViewModel.builder().suggestedRejected(rejected).build()
            );
        }

        return ConfirmVotingTabs.ContextViewModel.builder()
                .votingCategory(VotingCategory.VS)
                .municipality(mock(Municipality.class))
                .votingList(votings)
                .build();
    }

    @Test(dataProvider = "filteredVotingListProcessingTypeTestData")
    public void testFilteredVotingList_isBasedOnSelectedProcessingType(ConfirmVotingTabs.ContextViewModel contextViewModel, ProcessingType expectedProcessingType, int expectedCountOfThatType) {
        confirmVotingTabs.initComponent(contextViewModel, null);
        List<VotingViewModel> filteredVotingList = confirmVotingTabs.getViewModel().getFilteredVotingList();
        assertEquals(expectedCountOfThatType, filteredVotingList.size());
        for (VotingViewModel vvm : filteredVotingList) {
            if (expectedProcessingType == SUGGESTED_APPROVED) {
                assertTrue(vvm.isSuggestedApproved());
            } else {
                assertTrue(vvm.isSuggestedRejected());
            }
        }
    }

    @DataProvider
    private Object[][] filteredVotingListProcessingTypeTestData() {
        return new Object[][]{
                {contextWithVotings(), SUGGESTED_REJECTED, 0},
                {contextWithVotings(true), SUGGESTED_REJECTED, 1},
                {contextWithVotings(true, false), SUGGESTED_REJECTED, 1},
                {contextWithVotings(true, true, false), SUGGESTED_REJECTED, 2},
                {contextWithVotings(true, false, false, true, false), SUGGESTED_APPROVED, 3},
                {contextWithVotings(false), SUGGESTED_APPROVED, 1},
                {contextWithVotings(false, true), SUGGESTED_REJECTED, 1},
                {contextWithVotings(false, true, false), SUGGESTED_APPROVED, 2},
        };
    }

    @Test
    public void testApprovalDialogCallback_onApprove_callsApproveSelected() {
        confirmVotingTabs.initComponent(contextViewModel, null);
        ConfirmVotingTabs spy = spy(confirmVotingTabs);

        spy.onApprovalDialogApproveVoting(any());

        verify(spy).approveSelectedSuggestedApprovedVotings();
    }

    @Test
    public void testApprovalDialogCallback_onOneAndOne_callsNotifyParent_andHide() {
        confirmVotingTabs.initComponent(contextViewModel, null);
        ConfirmVotingTabs spy = spy(confirmVotingTabs);

        spy.onApprovalDialogOneAndOneVoting(any());
        
        verify(spy).notifyParentComponentOnSelectedVoting(any());
        
        verify(getInjectMock(VotingApprovalDialog.class)).hide();
    }

    @Test
    public void testSetTabIndex_doesEmptySelectedVotings() {
        confirmVotingTabs.initComponent(contextViewModel, null);

        confirmVotingTabs.getViewModel().setSelectedVotingList(asList(VotingViewModel.builder().build()));

        assertEquals(confirmVotingTabs.getViewModel().getSelectedVotingList().size(), 1);

        confirmVotingTabs.getViewModel().setSelectedProcessingTypeIndex(0);

        assertEquals(confirmVotingTabs.getViewModel().getSelectedVotingList().size(), 0);
    }

    @Test
    public void testApproveSuggestedApprovedVotings_doesCallService_andHide() throws NoSuchFieldException, IllegalAccessException {

        setComponentContext();
        mockField("componentHandler", UpdatableComponentHandler.class);
        mockField("viewModel", ConfirmVotingTabs.ViewModel.class);

        confirmVotingTabs.onApproveSuggestedApprovedVotings(singletonList(VotingViewModel.builder().build()));

        verify(getInjectMock(VotingInEnvelopeService.class))
                .approveVotingList(eq(getUserDataMock()), any(List.class), any(no.valg.eva.admin.common.configuration.model.Municipality.class));
        
        verify(getInjectMock(ApproveSuggestedApprovedDialog.class)).hide();
    }

    @Test
    public void testApproveSuggestedRejectedVoting_doesCallService() throws NoSuchFieldException, IllegalAccessException {

        setComponentContext();
        mockField("componentHandler", UpdatableComponentHandler.class);
        mockField("viewModel", ConfirmVotingTabs.ViewModel.class);

        confirmVotingTabs.onApprovalDialogApproveVoting(VotingViewModel.builder().build());

        verify(getInjectMock(VotingInEnvelopeService.class))
                .approveVotingList(eq(getUserDataMock()), any(List.class), any(no.valg.eva.admin.common.configuration.model.Municipality.class));

        verify(getInjectMock(VotingApprovalDialog.class)).hide();
    }

    @Test(dataProvider = "processingTabName_hasVotingCountTestData")
    public void testProcessingTabName_hasVotingCount(ConfirmVotingTabs.ContextViewModel context, String expectedRejectedTabName, String expectedApproveTabName) {
        
        when(getInjectMock(MessageProvider.class).get("@voting.confirmation.processing.type.suggestedApproved")).thenReturn("Ordinær behandling");
        when(getInjectMock(MessageProvider.class).get("@voting.confirmation.processing.type.suggestedRejected")).thenReturn("Særskilt behandling");
        
        confirmVotingTabs.initComponent(context, null);
        
        assertEquals(confirmVotingTabs.getProcessingTypeTabDisplayName(VotingProcessingTypeTab.builder().processingType(SUGGESTED_REJECTED).build()), expectedRejectedTabName);
        assertEquals(confirmVotingTabs.getProcessingTypeTabDisplayName(VotingProcessingTypeTab.builder().processingType(SUGGESTED_APPROVED).build()), expectedApproveTabName);
    }
    
    @DataProvider
    private Object[][] processingTabName_hasVotingCountTestData() {
        return new Object[][] {
                {contextWithVotings(false, false, true),        "Særskilt behandling (1)", "Ordinær behandling (2)"},
                {contextWithVotings(true, false, false, true),  "Særskilt behandling (2)", "Ordinær behandling (2)"},
                {contextWithVotings(true, true),                "Særskilt behandling (2)", "Ordinær behandling (0)"},
                {contextWithVotings(false),                     "Særskilt behandling (0)", "Ordinær behandling (1)"},
                {contextWithVotings(),                                              "Særskilt behandling (0)", "Ordinær behandling (0)"},
        };
    }

    @Test
    public void testProcessingTypeHeader() {
        
        assertNull(confirmVotingTabs.getViewModel());
        assertEquals(confirmVotingTabs.getProcessingHeading(), "");
        
        when(getInjectMock(MessageProvider.class).get("@voting.envelope.overview.heading.rejectReason")).thenReturn("@voting.envelope.overview.heading.rejectReason");
        when(getInjectMock(MessageProvider.class).get("@voting.approveVoting.suggestedRejection")).thenReturn("@voting.approveVoting.suggestedRejection");
        confirmVotingTabs.initComponent(contextViewModel(), null);
        
        confirmVotingTabs.getViewModel().setSelectedProcessingType(ProcessingType.SUGGESTED_REJECTED);
        assertEquals(confirmVotingTabs.getProcessingHeading(), "@voting.approveVoting.suggestedRejection");

        confirmVotingTabs.getViewModel().setSelectedProcessingType(ProcessingType.SUGGESTED_APPROVED);
        assertEquals(confirmVotingTabs.getProcessingHeading(), "@voting.envelope.overview.heading.ordinary");
    }
}