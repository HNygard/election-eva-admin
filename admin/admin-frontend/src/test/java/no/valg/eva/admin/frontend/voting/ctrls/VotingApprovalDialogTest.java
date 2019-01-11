package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.exception.EvoteSecurityException;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingApprovalState;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.voting.model.VotingApprovalState.MULTIPLE_UNCONFIRMED_VOTINGS;
import static no.valg.eva.admin.common.voting.model.VotingApprovalState.NO_OTHER_VOTINGS;
import static no.valg.eva.admin.common.voting.model.VotingApprovalState.PREVIOUSLY_APPROVED_VOTING;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.APPROVED_VOTING_EXIST;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.MULTIPLE_UNCONFIRMED_VOTINGS_EXIST;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.MULTIPLE_VOTINGS_SELECTED;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.WARNING_BEFORE_APPROVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class VotingApprovalDialogTest extends BaseFrontendTest {

    private VotingApprovalDialog votingApprovalDialog;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        votingApprovalDialog = initializeMocks(VotingApprovalDialog.class);
        when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
                .thenReturn(getUserDataMock());
    }

    @Test(dataProvider = "okayButtonIsRenderedTestData")
    public void testOkayButtonIsRendered(VotingApprovalDialog.DialogState activeState, boolean expectedRendered) {
        mockViewModelWithState(activeState);
        assertEquals(votingApprovalDialog.isOkayButtonRendered(), expectedRendered);
    }

    private void mockViewModelWithState(VotingApprovalDialog.DialogState dialogState) {
        votingApprovalDialog.setViewModel(
                VotingApprovalDialog.ViewModel.builder()
                        .dialogState(dialogState)
                        .build()
        );
    }

    @DataProvider
    private Object[][] okayButtonIsRenderedTestData() {
        return new Object[][]{
                {null, false},
                {MULTIPLE_VOTINGS_SELECTED, true},
                {MULTIPLE_UNCONFIRMED_VOTINGS_EXIST, false},
                {APPROVED_VOTING_EXIST, true},
                {WARNING_BEFORE_APPROVE, false},
        };
    }

    @Test(dataProvider = "oneAndOneButtonRenderedTestData")
    public void testOneAndOneButtonRendered(VotingApprovalDialog.DialogState activeState, boolean expectedRendered) {
        mockViewModelWithState(activeState);
        assertEquals(votingApprovalDialog.isOneAndOneButtonRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] oneAndOneButtonRenderedTestData() {
        return new Object[][]{
                {null, false},
                {MULTIPLE_VOTINGS_SELECTED, false},
                {MULTIPLE_UNCONFIRMED_VOTINGS_EXIST, true},
                {APPROVED_VOTING_EXIST, false},
                {WARNING_BEFORE_APPROVE, false},
        };
    }

    @Test(dataProvider = "approveButtonRenderedTestData")
    public void testApproveButtonRendered(VotingApprovalDialog.DialogState activeState, boolean expectedRendered) {
        mockViewModelWithState(activeState);
        assertEquals(votingApprovalDialog.isApproveButtonRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] approveButtonRenderedTestData() {
        return new Object[][]{
                {null, false},
                {MULTIPLE_VOTINGS_SELECTED, false},
                {MULTIPLE_UNCONFIRMED_VOTINGS_EXIST, false},
                {APPROVED_VOTING_EXIST, false},
                {WARNING_BEFORE_APPROVE, true},
        };
    }

    @Test(dataProvider = "cancelLinkRenderedTestData")
    public void testCancelLinkRendered(VotingApprovalDialog.DialogState activeState, boolean expectedRendered) {
        mockViewModelWithState(activeState);
        assertEquals(votingApprovalDialog.isCancelLinkRendered(), expectedRendered);
    }

    @DataProvider
    private Object[][] cancelLinkRenderedTestData() {
        return new Object[][]{
                {null, false},
                {MULTIPLE_VOTINGS_SELECTED, false},
                {MULTIPLE_UNCONFIRMED_VOTINGS_EXIST, true},
                {APPROVED_VOTING_EXIST, false},
                {WARNING_BEFORE_APPROVE, true},
        };
    }

    @Test(dataProvider = "initOfDialogWithContext_andVotingApprovalStatusFromBackend_resolvesDialogStateTestData")
    public void testInitOfDialogWithContext_andVotingApprovalStatusFromBackend_resolvesDialogState(
            VotingApprovalDialog.ContextViewModel context, VotingApprovalStatus approvalStatus, VotingApprovalDialog.DialogState expectedState) {

        when(getInjectMock(VotingInEnvelopeService.class).checkIfSuggestedRejectedVotingCanBeApproved(any(), any(), any(), any()))
                .thenReturn(approvalStatus);

        votingApprovalDialog.initComponent(context);

        assertEquals(votingApprovalDialog.getViewModel().getDialogState(), expectedState);
    }

    @DataProvider
    private Object[][] initOfDialogWithContext_andVotingApprovalStatusFromBackend_resolvesDialogStateTestData() {
        return new Object[][]{
                {selectedVotings(2), someApprovalStateFromBackend(), MULTIPLE_VOTINGS_SELECTED},
                {selectedVotings(3), someApprovalStateFromBackend(), MULTIPLE_VOTINGS_SELECTED},
                {selectedVotings(1), approvalStateFromBackend(PREVIOUSLY_APPROVED_VOTING), APPROVED_VOTING_EXIST},
                {selectedVotings(1), approvalStateFromBackend(MULTIPLE_UNCONFIRMED_VOTINGS), MULTIPLE_UNCONFIRMED_VOTINGS_EXIST},
                {selectedVotings(1), approvalStateFromBackend(NO_OTHER_VOTINGS), WARNING_BEFORE_APPROVE}
        };
    }

    private VotingApprovalDialog.ContextViewModel selectedVotings(int numberOfSelectedVotings) {

        final List<VotingViewModel> selectedVotings = new ArrayList<>(numberOfSelectedVotings);
        for (int i = 0; i < numberOfSelectedVotings; i++) {
            VotingViewModel vvm = VotingViewModel.builder()
                    .votingNumber(i)
                    .voter(VoterDto.builder().build())
                    .build();
            selectedVotings.add(vvm);
        }

        return VotingApprovalDialog.ContextViewModel.builder()
                .electionGroup(mock(ElectionGroup.class))
                .municipality(mock(Municipality.class))
                .handler(mock(VotingApprovalDialog.Handler.class))
                .votings(selectedVotings)
                .build();
    }

    private VotingApprovalStatus someApprovalStateFromBackend() {
        return approvalStateFromBackend(asList(VotingApprovalState.values()).get(new Random().nextInt(VotingApprovalState.values().length)));
    }

    private VotingApprovalStatus approvalStateFromBackend(VotingApprovalState state) {

        VotingDto prevVoting = null;
        if (state == PREVIOUSLY_APPROVED_VOTING) {
            prevVoting = VotingDto.builder()
                    .votingNumber(2)
                    .votingCategory(mock(VotingCategory.class))
                    .voterDto(VoterDto.builder().build())
                    .build();
        }

        return VotingApprovalStatus.builder()
                .state(state)
                .previouslyApprovedVoting(prevVoting)
                .build();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testDialog_withNullContext_throwsIllegalStateException() {
        votingApprovalDialog.initComponent(null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testDialog_withZeroSelectedVotings_throwsIllegalStateException() {
        VotingApprovalDialog.ContextViewModel context = selectedVotings(0);
        votingApprovalDialog.initComponent(context);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testDialog_withSelectedVotingsListAsNull_throwsIllegalStateException() {
        VotingApprovalDialog.ContextViewModel context = VotingApprovalDialog.ContextViewModel.builder()
                .electionGroup(mock(ElectionGroup.class))
                .municipality(mock(Municipality.class))
                .votings(null)
                .build();
        votingApprovalDialog.initComponent(context);
    }

    @Test
    public void testDialog_whenNullApprovalStateFromBackend_causesUnexpectedError() {

        when(getInjectMock(VotingInEnvelopeService.class).checkIfSuggestedRejectedVotingCanBeApproved(any(), any(), any(), any()))
                .thenReturn(approvalStateFromBackend(null));

        VotingApprovalDialog.ContextViewModel context = createExpectedContext();
        votingApprovalDialog.initComponent(context);

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.error.unexpected, 8fab47d0]");
    }

    private VotingApprovalDialog.ContextViewModel createExpectedContext() {
        return selectedVotings(1);
    }

    @Test
    public void testDialog_doesApprovalCall_throughFrontendExecute() {
        when(getInjectMock(VotingInEnvelopeService.class).checkIfSuggestedRejectedVotingCanBeApproved(any(), any(), any(), any()))
                .thenThrow(EvoteSecurityException.class);

        VotingApprovalDialog.ContextViewModel context = createExpectedContext();
        votingApprovalDialog.initComponent(context);

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.SECURITY");
    }

    @Test
    public void testDialogShow() {
        votingApprovalDialog.show();
        verifyShowHasBeenCalled();
    }

    private void verifyShowHasBeenCalled() {
        verify(getRequestContextMock()).execute("PF('votingApprovalDialogWidget').show()");
    }

    @Test
    public void testDialogHide() {
        votingApprovalDialog.hide();
        verifyHideHasBeenCalled();
    }

    private void verifyHideHasBeenCalled() {
        verify(getRequestContextMock()).execute("PF('votingApprovalDialogWidget').hide()");
    }

    @Test
    public void testOnApprove_callsHandlerApprove() {
        mockServiceWithApprovalStatus();

        VotingApprovalDialog.ContextViewModel context = createExpectedContext();
        votingApprovalDialog.initComponent(context);

        votingApprovalDialog.onApproveVoting();

        verify(context.getHandler()).onApprovalDialogApproveVoting(context.getVotings().get(0));
    }

    private void mockServiceWithApprovalStatus() {
        when(getInjectMock(VotingInEnvelopeService.class).checkIfSuggestedRejectedVotingCanBeApproved(any(), any(), any(), any()))
                .thenReturn(someApprovalStateFromBackend());
    }

    @Test
    public void testOnOneAndOneConfirming_callsHandlerOneAndOn() {
        mockServiceWithApprovalStatus();

        VotingApprovalDialog.ContextViewModel context = createExpectedContext();
        votingApprovalDialog.initComponent(context);

        votingApprovalDialog.onOneAndOneConfirming();

        verify(context.getHandler()).onApprovalDialogOneAndOneVoting(context.getVotings().get(0));
    }
}