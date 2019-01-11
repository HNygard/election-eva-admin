package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.CONFIRM_WARNING;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.DIFFERENT_REJECTION_REASONS;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.NEED_ONE_AND_ONE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class VotingRejectionDialogTest extends BaseFrontendTest {

    private VotingRejectionDialog dialog;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        dialog = initializeMocks(VotingRejectionDialog.class);

        when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
                .thenReturn(getUserDataMock());
    }


    @Test(dataProvider = "contextValidationTestData", expectedExceptions = IllegalStateException.class)
    public void testContextValidation(VotingRejectionDialog.ContextViewModel contextViewModel) {
        dialog.initComponent(contextViewModel);
    }

    @DataProvider
    private Object[][] contextValidationTestData() {
        return new Object[][]{
                {null},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(null)
                        .votingCategory(null)
                        .callbackHandler(null)
                        .selectedVotings(null)
                        .build()},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(mock(ElectionGroup.class))
                        .votingCategory(VotingCategory.FI)
                        .callbackHandler(votingRejection -> {
                        })
                        .selectedVotings(null)
                        .build()},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(mock(ElectionGroup.class))
                        .votingCategory(VotingCategory.FI)
                        .callbackHandler(votingRejection -> {
                        })
                        .selectedVotings(emptyList())
                        .build()},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(mock(ElectionGroup.class))
                        .votingCategory(VotingCategory.FI)
                        .callbackHandler(null)
                        .selectedVotings(singletonList(VotingViewModel.builder().build()))
                        .build()},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(mock(ElectionGroup.class))
                        .votingCategory(null)
                        .callbackHandler(votingRejection -> {
                        })
                        .selectedVotings(singletonList(VotingViewModel.builder().build()))
                        .build()},
                {VotingRejectionDialog.ContextViewModel.builder()
                        .electionGroup(null)
                        .votingCategory(VotingCategory.FI)
                        .callbackHandler(votingRejection -> {
                        })
                        .selectedVotings(singletonList(VotingViewModel.builder().build()))
                        .build()}
        };
    }

    @Test(dataProvider = "show")
    public void testShow_verifyingCorrectJavaScriptExecution(String expectedJavaScriptCommand) {
        dialog.show();
        verify(getRequestContextMock()).execute(expectedJavaScriptCommand);
    }

    @DataProvider
    public Object[][] show() {
        return new Object[][]{
                {"PF('votingRejectionDialogWidget').show()"}
        };
    }

    @Test(dataProvider = "hide")
    public void testHide_verifyingCorrectJavaScriptExecution(String expectedJavaScriptCommand) {
        dialog.hide();
        verify(getRequestContextMock()).execute(expectedJavaScriptCommand);
    }

    @DataProvider
    public Object[][] hide() {
        return new Object[][]{
                {"PF('votingRejectionDialogWidget').hide()"}
        };
    }

    @Test(dataProvider = "dialogStateTestData")
    public void testDialogState(List<VotingViewModel> selectedVotings, List<VoterDto> blockingVoters, VotingRejectionDialog.DialogState expectedDialogState) {

        when(getInjectMock(VotingInEnvelopeService.class).checkIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne(any(), any(), any()))
                .thenReturn(blockingVoters);

        VotingRejectionDialog.ContextViewModel context = contextViewModel(selectedVotings);
        dialog.initComponent(context);


        VotingRejectionDialog.DialogState dialogState = dialog.getViewModel().getDialogState();
        assertEquals(dialogState, expectedDialogState);

        if (dialogState == NEED_ONE_AND_ONE) {
            assertEquals(dialog.getViewModel().getVotersNeedHandling(), blockingVoters);
        }

        if (dialogState != DIFFERENT_REJECTION_REASONS) {
            assertEquals(dialog.getViewModel().getSelectedVotings(), context.getSelectedVotings());
        }

        int expectedServiceCallCount = dialogState == CONFIRM_WARNING ? 1 : 0;
        verify(getInjectMock(VotingInEnvelopeService.class), times(expectedServiceCallCount)).votingRejections(any(), any());
    }

    private VotingRejectionDialog.ContextViewModel contextViewModel(List<VotingViewModel> votingViewModels) {
        return VotingRejectionDialog.ContextViewModel.builder()
                .electionGroup(mock(ElectionGroup.class))
                .votingCategory(VotingCategory.FI)
                .callbackHandler(votingRejection -> {
                })
                .selectedVotings(votingViewModels)
                .build();
    }

    @DataProvider
    private Object[][] dialogStateTestData() {

        String firstRejectionsReason = "test1";
        String secondRejectionsReason = "test2";

        return new Object[][]{
                {
                        votingsWithReasons(firstRejectionsReason, firstRejectionsReason),
                        noVotersNeedManualHandling(),
                        CONFIRM_WARNING
                },
                {
                        votingsWithReasons(secondRejectionsReason, firstRejectionsReason),
                        votersNeedManualHandling(1),
                        DIFFERENT_REJECTION_REASONS
                },
                {
                        votingsWithReasons(secondRejectionsReason),
                        votersNeedManualHandling(1),
                        NEED_ONE_AND_ONE
                }
        };
    }

    private List<VotingViewModel> votingsWithReasons(String... reasons) {
        List<VotingViewModel> vvms = new ArrayList<>(reasons.length);
        for (String reason : reasons) {
            vvms.add(VotingViewModel.builder()
                    .votingCategory(mock(no.valg.eva.admin.configuration.domain.model.VotingCategory.class))
                    .suggestedRejectionReason(reason)
                    .build()
            );
        }
        return vvms;
    }

    private List<VoterDto> noVotersNeedManualHandling() {
        return emptyList();
    }

    private List<VoterDto> votersNeedManualHandling(int numberOfVoters) {
        List<VoterDto> voterDtos = new ArrayList<>(numberOfVoters);
        while (numberOfVoters > 0) {
            voterDtos.add(VoterDto.builder().build());
            numberOfVoters--;
        }
        return voterDtos;
    }

    @Test
    public void testVoterNeedHandlingList_isRendered() {
        dialog.setViewModel(viewModel(DIFFERENT_REJECTION_REASONS));
        assertFalse(dialog.isVotersNeedHandledListRendered());

        dialog.setViewModel(viewModel(NEED_ONE_AND_ONE));
        assertTrue(dialog.isVotersNeedHandledListRendered());

        dialog.setViewModel(viewModel(CONFIRM_WARNING));
        assertFalse(dialog.isVotersNeedHandledListRendered());
    }

    private VotingRejectionDialog.ViewModel viewModel(VotingRejectionDialog.DialogState state) {
        return VotingRejectionDialog.ViewModel.builder()
                .dialogState(state)
                .selectedVotings(asList(
                        VotingViewModel.builder().build()
                ))
                .build();
    }

    @Test
    public void testVotingRejectionReasonList_isRendered() {
        dialog.setViewModel(viewModel(DIFFERENT_REJECTION_REASONS));
        assertFalse(dialog.isVotingRejectionReasonListRendered());

        dialog.setViewModel(viewModel(NEED_ONE_AND_ONE));
        assertFalse(dialog.isVotingRejectionReasonListRendered());

        dialog.setViewModel(viewModel(CONFIRM_WARNING));
        assertTrue(dialog.isVotingRejectionReasonListRendered());
    }

    @Test
    public void testVotingRejectionList_andVoterNeedHandlingList_isNotRendered_whenViewModelOrDialogStateNotSet() {
        dialog.setViewModel(null);
        assertFalse(dialog.isVotersNeedHandledListRendered());
        assertFalse(dialog.isVotingRejectionReasonListRendered());

        dialog.setViewModel(VotingRejectionDialog.ViewModel.builder().build());
        assertFalse(dialog.isVotersNeedHandledListRendered());
        assertFalse(dialog.isVotingRejectionReasonListRendered());
    }

    @Test
    public void testOnSelectedRejectionReason() throws NoSuchFieldException, IllegalAccessException {

        VotingRejectionDto expectedRejectionDto = VotingRejectionDto.builder().id("rejectionId").build();
        List<VotingRejectionDto> rejectionDtoList = asList(expectedRejectionDto);

        List<VotingRejectionDto> votingRejectionDtoList = (List<VotingRejectionDto>) mockField("votingRejectionDtoList", List.class);
        when(votingRejectionDtoList.stream()).thenReturn(rejectionDtoList.stream());


        VotingRejectionDialog.ViewModel viewModel = viewModel(CONFIRM_WARNING);
        viewModel.setSelectedVotingRejectionId(expectedRejectionDto.getId());
        dialog.setViewModel(viewModel);

        VotingRejectionDialog.Handler callbackHandler = mockField("callbackHandler", VotingRejectionDialog.Handler.class);
        doNothing().when(callbackHandler).onRejectVotings(any(VotingRejectionDto.class));

        dialog.onSelectedRejectionReason();

        verify(callbackHandler, times(1)).onRejectVotings(expectedRejectionDto);
    }

    @Test(dataProvider = "okayButtonRenderedTestData")
    public void testOkayButtonRendered(VotingRejectionDialog.DialogState dialogState, boolean expectedRendered) {
        dialog.setViewModel(viewModel(dialogState));
        assertEquals(dialog.isOkayButtonRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] okayButtonRenderedTestData() {
        return new Object[][]{
                {DIFFERENT_REJECTION_REASONS, true},
                {NEED_ONE_AND_ONE, true},
                {CONFIRM_WARNING, false}
        };
    }

    @Test(dataProvider = "rejectButtonRenderedTestData")
    public void testRejectButtonRendered(VotingRejectionDialog.DialogState dialogState, boolean expectedRendered) {
        dialog.setViewModel(viewModel(dialogState));
        assertEquals(dialog.isRejectButtonRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] rejectButtonRenderedTestData() {
        return new Object[][]{
                {DIFFERENT_REJECTION_REASONS, false},
                {NEED_ONE_AND_ONE, false},
                {CONFIRM_WARNING, true}
        };
    }

    @Test(dataProvider = "rejectButtonDisabledTestData")
    public void testRejectButtonDisabled(VotingRejectionDialog.ViewModel viewModel, boolean expectedRendered) {
        dialog.setViewModel(viewModel);
        assertEquals(dialog.isRejectButtonDisabled(), expectedRendered);
    }

    @DataProvider
    private Object[][] rejectButtonDisabledTestData() {
        return new Object[][]{
                {VotingRejectionDialog.ViewModel.builder().selectedVotingRejectionId(null).build(), true},
                {VotingRejectionDialog.ViewModel.builder().selectedVotingRejectionId("rejectionId").build(), false}
        };
    }

    @Test(dataProvider = "cancelLinkRenderedTestData")
    public void testCancelLinkRendered(VotingRejectionDialog.DialogState dialogState, boolean expectedRendered) {
        dialog.setViewModel(viewModel(dialogState));
        assertEquals(dialog.isCancelLinkRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] cancelLinkRenderedTestData() {
        return new Object[][]{
                {DIFFERENT_REJECTION_REASONS, false},
                {NEED_ONE_AND_ONE, false},
                {CONFIRM_WARNING, true}
        };
    }

    @Test(dataProvider = "titleKeyTestData")
    public void testTitleKey(VotingRejectionDialog.ViewModel viewModel, String expectedTitleKey) {
        dialog.setViewModel(viewModel);
        assertEquals(dialog.getTitleKey(), expectedTitleKey);
    }

    @DataProvider
    private Object[][] titleKeyTestData() {
        return new Object[][] {
                {null, ""},
                {viewModelWithDialogState(null), ""},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.DIFFERENT_REJECTION_REASONS), "@common.warning"},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.NEED_ONE_AND_ONE), "@common.warning"},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.CONFIRM_WARNING), "@voting.confirmation.reject"}
        };
    }

    @Test(dataProvider = "messageTestData")
    public void testMessage(VotingRejectionDialog.ViewModel viewModel, String expectedMessage) {
        dialog.setViewModel(viewModel);
        when(getInjectMock(MessageProvider.class).getWithTranslatedParams(any())).thenReturn("localizedValue");
        when(getInjectMock(MessageProvider.class).getWithTranslatedParams(any(), any(), any())).thenReturn("localizedValue");
        assertEquals(dialog.getMessage(), expectedMessage);
    }

    @DataProvider
    private Object[][] messageTestData() {
        return new Object[][] {
                {null, ""},
                {viewModelWithDialogState(null), ""},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.DIFFERENT_REJECTION_REASONS), "localizedValue"},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.NEED_ONE_AND_ONE), "localizedValue"},
                {viewModelWithDialogState(VotingRejectionDialog.DialogState.CONFIRM_WARNING), "localizedValue"}
        };
    }

    private VotingRejectionDialog.ViewModel viewModelWithDialogState(VotingRejectionDialog.DialogState dialogState) {
        return VotingRejectionDialog.ViewModel.builder()
                .dialogState(dialogState)
                .selectedVotings(asList(VotingViewModel.builder().rejectionReason("aReason").build()))
                .build();
    }
}