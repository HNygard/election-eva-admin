package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
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
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static no.valg.eva.admin.frontend.util.FacesUtil.getUserData;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.CONFIRM_WARNING;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.DIFFERENT_REJECTION_REASONS;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingRejectionDialog.DialogState.NEED_ONE_AND_ONE;

@Named
@ViewScoped
@NoArgsConstructor
public class VotingRejectionDialog extends BaseController implements Component<VotingRejectionDialog.ContextViewModel> {

    private static final long serialVersionUID = 8823132590045945058L;
    
    @Inject
    private MessageProvider messageProvider;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    @Getter
    @Setter
    private List<VotingRejectionDto> votingRejectionDtoList;

    @Getter
    @Setter
    private ViewModel viewModel;

    private Handler callbackHandler;

    @Override
    public void initComponent(ContextViewModel context) {
        validateContext(context);

        this.callbackHandler = context.getCallbackHandler();

        createViewModel(context);

        findRejectionReasonsForVotingCategoryIfAppropriate(context.getVotingCategory());
    }

    private void validateContext(ContextViewModel contextViewModel) {
        if (invalidContext(contextViewModel)) {
            throw new IllegalStateException("Election group, voting category, component handler or voting list is missing");
        }
    }

    private boolean invalidContext(ContextViewModel contextViewModel) {
        return contextViewModel == null
                || contextViewModel.getSelectedVotings() == null
                || contextViewModel.getSelectedVotings().isEmpty()
                || contextViewModel.getCallbackHandler() == null
                || contextViewModel.getElectionGroup() == null
                || contextViewModel.getVotingCategory() == null;
    }

    private void createViewModel(ContextViewModel context) {

        if (hasDifferentSuggestedRejectionReasons(context.getSelectedVotings())) {
            setViewModel(ViewModel.builder()
                    .dialogState(DIFFERENT_REJECTION_REASONS)
                    .build());
        } else {
            final List<VoterDto> votersNeedHandling = votingInEnvelopeService.checkIfUnconfirmedVotingsHasVotersThatNeedToBeHandledOneByOne(
                    getUserData(), context.getElectionGroup(),
                    context.getSelectedVotings().stream().map(VotingViewModel::toVotingDto).collect(Collectors.toList()));

            setViewModel(ViewModel.builder()
                    .dialogState(votersNeedHandling.isEmpty() ? CONFIRM_WARNING : NEED_ONE_AND_ONE)
                    .votersNeedHandling(votersNeedHandling)
                    .selectedVotings(context.getSelectedVotings())
                    .build());
        }
    }

    private boolean hasDifferentSuggestedRejectionReasons(List<VotingViewModel> vvms) {
        String suggestRejectionReason = vvms.get(0).getSuggestedRejectionReason();

        boolean allWithSameReason = vvms.stream()
                .allMatch(vvm -> vvm.getSuggestedRejectionReason().equals(suggestRejectionReason));

        return !allWithSameReason;
    }

    private void findRejectionReasonsForVotingCategoryIfAppropriate(VotingCategory votingCategory) {
        if (viewModel.getDialogState() == CONFIRM_WARNING) {
            execute(() -> votingRejectionDtoList = votingInEnvelopeService.votingRejections(getUserData(), votingCategory));
        }
    }

    public String getTitleKey() {
        if (viewModel == null || viewModel.getDialogState() == null) {
            return "";
        }
        return viewModel.getDialogState().titleKey;
    }

    public String getMessage() {
        if (viewModel == null || viewModel.getDialogState() == null) {
            return "";
        }
        return getLocalizedMessageFromDialogState();
    }

    private String getLocalizedMessageFromDialogState() {
        final DialogState dialogState = viewModel.getDialogState();
        if (dialogState == CONFIRM_WARNING) {

            int votings = viewModel.getSelectedVotings().size();

            return messageProvider.getWithTranslatedParams(
                    dialogState.msgKey,
                    String.format("%d stemmegivning%s", votings, votings > 1 ? "er" : ""),
                    viewModel.getSelectedVotings().get(0).getSuggestedRejectionReason()
            );
        }

        return messageProvider.getWithTranslatedParams(dialogState.msgKey);
    }

    public boolean isVotersNeedHandledListRendered() {
        return stateIs(NEED_ONE_AND_ONE);
    }

    private boolean stateIs(DialogState... dialogState) {
        return viewModel != null && stream(dialogState).anyMatch(ds -> ds == viewModel.getDialogState());
    }

    public boolean isVotingRejectionReasonListRendered() {
        return stateIs(CONFIRM_WARNING);
    }

    public boolean isOkayButtonRendered() {
        return stateIs(DIFFERENT_REJECTION_REASONS, NEED_ONE_AND_ONE);
    }

    public boolean isRejectButtonRendered() {
        return stateIs(CONFIRM_WARNING);
    }

    public boolean isRejectButtonDisabled() {
        return getViewModel().getSelectedVotingRejectionId() == null;
    }

    public boolean isCancelLinkRendered() {
        return stateIs(CONFIRM_WARNING);
    }

    public void onSelectedRejectionReason() {
        final VotingRejectionDto dto = fromSelectedRejectionId();
        callbackHandler.onRejectVotings(dto);
    }

    private VotingRejectionDto fromSelectedRejectionId() {
        return getVotingRejectionDtoList().stream()
                .filter(rejectionDto -> rejectionDto.getId().equals(getViewModel().getSelectedVotingRejectionId()))
                .findFirst()
                .orElse(null);
    }

    public void show() {
        FacesUtil.executeJS("PF('votingRejectionDialogWidget').show()");
    }

    public void hide() {
        FacesUtil.executeJS("PF('votingRejectionDialogWidget').hide()");
    }


    @Builder
    @Getter
    static class ContextViewModel {
        private VotingCategory votingCategory;
        private ElectionGroup electionGroup;
        private Handler callbackHandler;
        private List<VotingViewModel> selectedVotings;
    }

    public interface Handler {
        void onRejectVotings(VotingRejectionDto votingRejection);
    }

    @Builder
    @Getter
    public static class ViewModel {
        private DialogState dialogState;
        private List<VoterDto> votersNeedHandling;
        private List<VotingViewModel> selectedVotings;

        @Setter
        private String selectedVotingRejectionId;
    }

    public enum DialogState {
        DIFFERENT_REJECTION_REASONS("@common.warning", "@voting.confirmation.multipleRejection.multipleSuggestedRejectionReasons"),
        NEED_ONE_AND_ONE("@common.warning", "@voting.confirmation.multipleRejection.votersNeedHandledOneByOne"),
        CONFIRM_WARNING("@voting.confirmation.reject", "@voting.confirmation.multipleRejection.confirmWarning");

        String titleKey;
        String msgKey;

        DialogState(String titleKey, String msgKey) {
            this.titleKey = titleKey;
            this.msgKey = msgKey;
        }
    }
}
