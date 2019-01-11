package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.model.VotingApprovalState;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Component;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static java.util.Arrays.stream;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.APPROVED_VOTING_EXIST;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.MULTIPLE_UNCONFIRMED_VOTINGS_EXIST;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.MULTIPLE_VOTINGS_SELECTED;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingApprovalDialog.DialogState.WARNING_BEFORE_APPROVE;

// Denne dialogen skal kun vises ved godkjenning av stemmegivninger som er til særskilt behandling

@Named
@ViewScoped
@NoArgsConstructor
public class VotingApprovalDialog extends BaseController implements Component<VotingApprovalDialog.ContextViewModel> {

    private static final String TITLE_KEY_APPROVE_VOTING = "@voting.confirmation.action.approveVoting";
    private static final String TITLE_KEY_WARNING = "@common.warning";
    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    @Inject
    private MessageProvider messageProvider;

    @Getter
    private String title;

    @Getter
    private String message;

    @Getter
    @Setter
    private ViewModel viewModel;

    @Getter
    @Setter
    private Handler handler;

    @Override
    public void initComponent(ContextViewModel context) {
        validateContext(context);
        this.handler = context.getHandler();

        createViewModel(context);

        resolveMessageAndTitle();
    }

    private void validateContext(ContextViewModel contextViewModel) {
        if (contextViewModel == null || contextViewModel.getVotings() == null || contextViewModel.getVotings().isEmpty() || contextViewModel.getHandler() == null) {
            throw new IllegalStateException("Voting approval dialog expects at least 1 selected voting and a component handler.");
        }
    }

    private void createViewModel(ContextViewModel context) {

        if (context.getVotings().size() > 1) {
            setViewModel(ViewModel.builder()
                    .dialogState(MULTIPLE_VOTINGS_SELECTED)
                    .build());
            return;
        }

        final VotingViewModel unconfirmedVoting = context.getVotings().get(0);

        execute(() -> {
            VotingApprovalStatus approvalStatus = votingInEnvelopeService.checkIfSuggestedRejectedVotingCanBeApproved(
                    FacesUtil.getUserData(), context.getElectionGroup(), context.getMunicipality(), VotingViewModel.toVotingDto(unconfirmedVoting)
            );

            setViewModel(
                    ViewModel.builder()
                            .selectedUnconfirmedVoting(unconfirmedVoting)
                            .dialogState(resolveDialogStateFromApprovalStatus(approvalStatus))
                            .previouslyApprovedVoting(approvalStatus.getPreviouslyApprovedVoting())
                            .build()
            );
        });
    }

    private DialogState resolveDialogStateFromApprovalStatus(VotingApprovalStatus status) {

        final VotingApprovalState state = status.getState();
        if (state == null) {
            throw new IllegalStateException("A voting approval state cannot be null");
        }

        switch (state) {
            case NO_OTHER_VOTINGS:
                return WARNING_BEFORE_APPROVE;
            case MULTIPLE_UNCONFIRMED_VOTINGS:
                return MULTIPLE_UNCONFIRMED_VOTINGS_EXIST;
            case PREVIOUSLY_APPROVED_VOTING:
                return APPROVED_VOTING_EXIST;
            default:
                throw new IllegalStateException("A voting approval status must have an known approval state: " + state);
        }
    }

    private void resolveMessageAndTitle() {
        if (viewModel == null) {
            return;
        }

        switch (viewModel.getDialogState()) {

            case MULTIPLE_VOTINGS_SELECTED:
                setTitleAndMessage(TITLE_KEY_WARNING);
                break;
            case MULTIPLE_UNCONFIRMED_VOTINGS_EXIST:
                setTitleAndMessage(TITLE_KEY_APPROVE_VOTING);
                break;
            case APPROVED_VOTING_EXIST:
                setTitleAndMessage(
                        TITLE_KEY_WARNING,
                        votingCategoryAndNumberFrom(getViewModel().getPreviouslyApprovedVoting())
                );
                break;
            case WARNING_BEFORE_APPROVE:
                setTitleAndMessage(
                        TITLE_KEY_APPROVE_VOTING,
                        getViewModel().getSelectedUnconfirmedVoting().getSuggestedRejectionReason()
                );
                break;
            default:
                break;
        }
    }

    private void setTitleAndMessage(String title, String... optionalMsgArgs) {
        this.title = title;
        this.message = messageProvider.getWithTranslatedParams(viewModel.getDialogState().msgKey, optionalMsgArgs);
    }


    private String[] votingCategoryAndNumberFrom(VotingDto votingDto) {
        return new String[]{
                votingDto.getVotingNumber() != null 
                        ? String.format(" %s-%s.", votingDto.getVotingCategory().getId(), votingDto.getVotingNumber().toString()) 
                        : ""
        };
    }


    private boolean stateIs(DialogState... dialogState) {
        return viewModel != null && stream(dialogState).anyMatch(ds -> ds == viewModel.getDialogState());
    }

    public boolean isOkayButtonRendered() {
        return stateIs(MULTIPLE_VOTINGS_SELECTED, APPROVED_VOTING_EXIST);
    }

    public boolean isOneAndOneButtonRendered() {
        return stateIs(MULTIPLE_UNCONFIRMED_VOTINGS_EXIST);
    }

    public boolean isApproveButtonRendered() {
        return stateIs(WARNING_BEFORE_APPROVE);
    }

    public boolean isCancelLinkRendered() {
        return stateIs(WARNING_BEFORE_APPROVE, MULTIPLE_UNCONFIRMED_VOTINGS_EXIST);
    }


    public void show() {
        FacesUtil.executeJS("PF('votingApprovalDialogWidget').show()");
    }

    public void hide() {
        FacesUtil.executeJS("PF('votingApprovalDialogWidget').hide()");
    }

    public void onApproveVoting() {
        getHandler().onApprovalDialogApproveVoting(
                getViewModel().getSelectedUnconfirmedVoting()
        );
    }

    public void onOneAndOneConfirming() {
        getHandler().onApprovalDialogOneAndOneVoting(
                getViewModel().getSelectedUnconfirmedVoting()
        );
    }


    @Builder
    @Getter
    public static class ContextViewModel {
        private ElectionGroup electionGroup;
        private Municipality municipality;
        private Handler handler;
        private List<VotingViewModel> votings;
    }

    public static interface Handler {
        void onApprovalDialogApproveVoting(VotingViewModel voting);

        void onApprovalDialogOneAndOneVoting(VotingViewModel voting);
    }

    @Builder
    @Getter
    public static class ViewModel {
        private DialogState dialogState;
        private VotingViewModel selectedUnconfirmedVoting;
        private VotingDto previouslyApprovedVoting;
    }

    public enum DialogState {
        MULTIPLE_VOTINGS_SELECTED("@voting.confirmation.state.approveMultipleVotingsFromSuggestedRejected"),
        MULTIPLE_UNCONFIRMED_VOTINGS_EXIST("@voting.confirmation.state.multipleUnconfirmedVotings"),
        APPROVED_VOTING_EXIST("@voting.confirmation.state.previouslyApprovedVoting"),
        WARNING_BEFORE_APPROVE("@voting.confirmation.state.canApprove");

        String msgKey;

        DialogState(String msgKey) {
            this.msgKey = msgKey;
        }
    }
}
