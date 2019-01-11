package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.UpdatableComponent;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingProcessingTypeTab;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_APPROVED;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_REJECTED;
import static no.valg.eva.admin.frontend.util.FacesUtil.getUserData;
import static no.valg.eva.admin.frontend.voting.ctrls.ConfirmVotingTabs.ViewModel.getFilteredVotingListForProcessingType;

@Named
@ViewScoped
@NoArgsConstructor
public class ConfirmVotingTabs extends BaseController implements UpdatableComponent<ConfirmVotingTabs.ContextViewModel>, 
        VotingSuggestedRejectedDialog.Handler, 
        VotingApprovalDialog.Handler, 
        VotingRejectionDialog.Handler, 
        ApproveSuggestedApprovedDialog.Handler {

    private static final long serialVersionUID = 3703470387846469694L;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;
    @Inject
    private MessageProvider messageProvider;
    @Inject
    private VotingRejectionDialog votingRejectionDialog;
    @Inject
    private VotingSuggestedRejectedDialog votingSuggestedRejectedDialog;
    @Inject
    private VotingApprovalDialog votingApprovalDialog;
    @Inject
    private ApproveSuggestedApprovedDialog approveSuggestedApprovedDialog;
    @Getter
    @Setter
    private ViewModel viewModel;
    @Getter
    @Setter
    private ContextViewModel contextViewModel;
    private UpdatableComponentHandler componentHandler;

    public void onTabChange(TabChangeEvent tabChangeEvent) {
        VotingProcessingTypeTab votingProcessingTypeTab = (VotingProcessingTypeTab) tabChangeEvent.getData(); 
        createViewModel(votingProcessingTypeTab.getProcessingType());
    }

    public interface Handler {
        void onSelectedVoting(VotingViewModel votingViewModel);
    }

    @Override
    public void initComponent(ContextViewModel context, UpdatableComponentHandler handler) {
        this.componentHandler = handler;
        this.contextViewModel = context;

        createViewModel();
    }

    public void onViewVoterDetails(VotingViewModel selectedVotingViewModel) {
        getViewModel().setSelectedVoting(selectedVotingViewModel);

        notifyParentComponentOnSelectedVoting(selectedVotingViewModel);
    }

    void notifyParentComponentOnSelectedVoting(VotingViewModel selectedVotingViewModel) {
        contextViewModel.getConfirmVotingContentHandler().onSelectedVoting(selectedVotingViewModel);
    }

    private void createViewModel() {
        createViewModel(null);
    }

    private void createViewModel(ProcessingType processingType) {
        List<VotingViewModel> currentVotingList = ViewModel.getFilteredVotingListForProcessingType(contextViewModel.votingList, processingType);
        List<VotingViewModel> rejectionList = ViewModel.getFilteredVotingListForProcessingType(contextViewModel.votingList, SUGGESTED_REJECTED);

        viewModel = ViewModel.builder()
                .processingTypeTabs(createProcessingTypeTabs())
                .selectedProcessingType(processingType)
                .votingList(contextViewModel.getVotingList())
                .suggestedApprovedVotingList(currentVotingList)
                .suggestedRejectedVotingList(rejectionList)
                .currentVotingList(currentVotingList)
                .build();

        if (getViewModel().getSelectedProcessingType() == null) {
            resolveProcessingType();
        }
    }

    private List<VotingProcessingTypeTab> createProcessingTypeTabs() {
        return Arrays.asList(
                VotingProcessingTypeTab.builder().processingType(SUGGESTED_REJECTED).build(),
                VotingProcessingTypeTab.builder().processingType(SUGGESTED_APPROVED).build()
        );
    }

    void resolveProcessingType() {
        getViewModel().setSelectedProcessingType(findLargestVotingListForProcessingType(getViewModel().getVotingList()));
        FacesUtil.executeJS("PF('votingConfirmationTabWidget').select(" + getViewModel().getSelectedProcessingTypeIndex() + ")");
    }

    ProcessingType findLargestVotingListForProcessingType(List<VotingViewModel> votingList) {
        return getFilteredVotingListForProcessingType(votingList, SUGGESTED_APPROVED).size() >
                getFilteredVotingListForProcessingType(votingList, SUGGESTED_REJECTED).size()
                ? SUGGESTED_APPROVED : SUGGESTED_REJECTED;
    }

    @Override
    public void componentDidUpdate(ContextViewModel context) {
        this.contextViewModel = context;
        createViewModelKeepingProcessingType();
    }

    private void createViewModelKeepingProcessingType() {
        createViewModel(processingTypeNullSafe());
    }

    ProcessingType processingTypeNullSafe() {
        return viewModel != null ? viewModel.getSelectedProcessingType() : null;
    }

    public boolean isApproveVotingListButtonDisabled() {
        return isNoVotingsSelected();
    }

    private boolean isNoVotingsSelected() {
        return viewModel.getSelectedVotingList() == null ||
                (viewModel.getSelectedVotingList() != null &&
                        viewModel.getSelectedVotingList().isEmpty());
    }

    public boolean isRenderMoveToSuggestedRejectedButton() {
        return SUGGESTED_REJECTED != getViewModel().getSelectedProcessingType();
    }

    public boolean isRejectVotingListButtonDisabled() {
        return SUGGESTED_REJECTED == getViewModel().getSelectedProcessingType() &&
                isNoVotingsSelected();
    }

    public boolean isRenderRejectVotingListButton() {
        return SUGGESTED_REJECTED == getViewModel().getSelectedProcessingType();
    }

    public boolean isMoveToSuggestedRejectedButtonDisabled() {
        return SUGGESTED_APPROVED == getViewModel().getSelectedProcessingType() &&
                isNoVotingsSelected();
    }

    public void initAndDisplayVotingSuggestedRejectedDialog() {
        votingSuggestedRejectedDialog.initComponent(VotingSuggestedRejectedDialog.ContextViewModel.builder()
                .votingCategory(contextViewModel.getVotingCategory())
                .callbackHandler(this)
                .userData(getUserData())
                .build());

        votingSuggestedRejectedDialog.show();
    }

    public void initAndDisplayVotingRejectionDialog() {
        votingRejectionDialog.initComponent(VotingRejectionDialog.ContextViewModel.builder()
                .electionGroup(contextViewModel.getElectionGroup())
                .votingCategory(contextViewModel.getVotingCategory())
                .callbackHandler(this)
                .selectedVotings(getViewModel().getSelectedVotingList())
                .build());

        votingRejectionDialog.show();
    }

    public void approveSelectedVotingList() {
        if (SUGGESTED_APPROVED == getViewModel().getSelectedProcessingType()) {
            initAndShowApproveSuggestedApprovedDialog();
        } else {
            initAndShowVotingApprovalDialog();
        }
    }

    private void initAndShowApproveSuggestedApprovedDialog() {
        approveSuggestedApprovedDialog.initComponent(ApproveSuggestedApprovedDialog.Context.builder()
                .selectedVotings(viewModel.getSelectedVotingList())
                .handler(this)
                .build());
        approveSuggestedApprovedDialog.show();
    }

    private void initAndShowVotingApprovalDialog() {
        votingApprovalDialog.initComponent(VotingApprovalDialog.ContextViewModel.builder()
                .electionGroup(contextViewModel.getElectionGroup())
                .municipality(contextViewModel.getMunicipality())
                .handler(this)
                .votings(viewModel.getSelectedVotingList())
                .build()
        );
        votingApprovalDialog.show();
    }

    private void reloadContent() {
        componentHandler.forceUpdate(this);
        FacesUtil.updateDom("form:confirmVotingAccordionPanel");
    }

    @Override
    public void onApproveSuggestedApprovedVotings(List<VotingViewModel> votings) {
        approveSuggestedApprovedDialog.hide();
        approveSelectedSuggestedApprovedVotings();
    }

    void approveSelectedSuggestedApprovedVotings() {
        final List<VotingDto> votingDtoList = viewModel.selectedVotingDtoList();
        execute(() -> {
            votingInEnvelopeService.approveVotingList(getUserData(), votingDtoList, contextViewModel.getMunicipality());
            reloadContent();
        });
    }

    @Override
    public void onRejectVotings(VotingRejectionDto votingRejection) {
        votingRejectionDialog.hide();

        List<VotingDto> votingDtoList = viewModel.selectedVotingDtoList();
        execute(() -> votingInEnvelopeService.rejectVotingList(getUserData(), votingDtoList, votingRejection, contextViewModel.getMunicipality()));

        reloadContent();
    }

    @Override
    public void onMoveVotingsToSuggestedRejected(VotingRejectionDto votingRejection) {
        votingSuggestedRejectedDialog.hide();

        execute(() -> votingInEnvelopeService.moveVotingToSuggestedRejected(getUserData(),
                viewModel.selectedVotingDtoList(),
                votingRejection,
                contextViewModel.getMunicipality())
        );
        reloadContent();
    }

    @Override
    public void onApprovalDialogApproveVoting(VotingViewModel voting) {
        votingApprovalDialog.hide();
        approveSelectedSuggestedApprovedVotings();
    }

    @Override
    public void onApprovalDialogOneAndOneVoting(VotingViewModel voting) {
        votingApprovalDialog.hide();
        notifyParentComponentOnSelectedVoting(voting);
    }

    public String getProcessingTypeTabDisplayName(VotingProcessingTypeTab typeTab) {
        String localizedMsg = messageProvider.get(typeTab.getDisplayName());

        if (getViewModel() != null) {
            List<VotingViewModel> filtered = ViewModel.getFilteredVotingListForProcessingType(getViewModel().getVotingList(), typeTab.getProcessingType());
            localizedMsg += String.format(" (%d)", filtered.size());
        }
        return localizedMsg;
    }

    public String getProcessingHeading() {
        if (getViewModel() != null) {
            return messageProvider.get(getViewModel().getSelectedProcessingType() == SUGGESTED_REJECTED
                    ? "@voting.approveVoting.suggestedRejection"
                    : "@voting.envelope.overview.heading.ordinary"
            );
        }
        return "";
    }

    @Builder
    @Getter
    public static class ContextViewModel {
        private Handler confirmVotingContentHandler;
        private List<VotingViewModel> votingList;
        private VotingCategory votingCategory;
        private ElectionGroup electionGroup;
        private Municipality municipality;
    }

    @Getter
    @Builder
    public static class ViewModel {
        private List<VotingViewModel> suggestedApprovedVotingList;
        private List<VotingViewModel> suggestedRejectedVotingList;
        private List<VotingProcessingTypeTab> processingTypeTabs;
        private List<VotingViewModel> currentVotingList;
        private List<VotingViewModel> votingList;

        @Setter
        private ProcessingType selectedProcessingType;
        @Setter
        @Builder.Default
        private List<VotingViewModel> selectedVotingList = new ArrayList<>();
        @Setter
        private VotingViewModel selectedVoting;

        public void setSelectedProcessingTypeIndex(int index) {
            setSelectedProcessingType(index == 0 ? SUGGESTED_REJECTED : SUGGESTED_APPROVED);
            resetSelectedVotingList();
        }

        private void resetSelectedVotingList() {
            selectedVotingList = new ArrayList<>();
        }

        public int getSelectedProcessingTypeIndex() {
            return getSelectedProcessingType() == SUGGESTED_REJECTED ? 0 : 1;
        }

        public List<VotingViewModel> getFilteredVotingList() {
            return getFilteredVotingListForProcessingType(votingList, selectedProcessingType);
        }

        public static List<VotingViewModel> getFilteredVotingListForProcessingType(List<VotingViewModel> votingList, ProcessingType processingType) {
            return processingType == SUGGESTED_APPROVED || processingType == null ? getSuggestedApprovedFromVotingList(votingList) : getSuggestedRejectedFromVotingList(votingList);
        }

        private static List<VotingViewModel> getSuggestedRejectedFromVotingList(List<VotingViewModel> votingList) {
            return votingList.stream().filter(VotingViewModel::isSuggestedRejected).collect(Collectors.toList());
        }

        private static List<VotingViewModel> getSuggestedApprovedFromVotingList(List<VotingViewModel> votingList) {
            return votingList.stream().filter(VotingViewModel::isSuggestedApproved).collect(Collectors.toList());
        }

        public List<VotingDto> selectedVotingDtoList() {
            return selectedVotingList != null ?
                    selectedVotingList.stream()
                            .filter(Objects::nonNull)
                            .map(VotingViewModel::toVotingDto)
                            .collect(Collectors.toList())
                    : Collections.emptyList();
        }
    }
}
