package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Component;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.frontend.util.FacesUtil;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.valg.eva.admin.configuration.application.MunicipalityMapper.toDto;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildMessageForClientId;

@Named
@ViewScoped
@NoArgsConstructor
public class VoterConfirmation extends BaseController implements Component<VoterConfirmation.VoterConfirmationContext>, VotingSuggestedRejectedDialog.Handler {

    private static final long serialVersionUID = 3227792464898522921L;

    @Getter
    private VoterConfirmationContext contextViewModel;
    @Getter
    private List<VotingDto> votingsToConfirm;
    @Getter
    private List<VotingDto> rejectedVotings;
    @Getter
    @Setter
    private VotingDto selectedVoting;
    @Getter
    private List<VotingDto> approvedVotings;

    @Getter
    private boolean canApproveVoting;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;
    @Inject
    private UserDataController userDataController;
    @Inject
    private VotingSuggestedRejectedDialog votingSuggestedRejectedDialog;
    @Inject
    private MessageProvider messageProvider;
    @Inject
    private VoterElectoralRollHistory voterElectoralRollHistory;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString
    public static class VoterConfirmationContext implements Serializable {

        private static final long serialVersionUID = -4258489595088734866L;

        @EqualsAndHashCode.Include
        private VoterDto voterDto;
        private Manntallsnummer electoralRollNumber;
        private VoterConfirmation.Handler handler;
        private UserData userData;
        private MvArea mvArea;
        private ElectionGroup electionGroup;
        private VotingCategory votingCategory;
    }

    @Override
    public void initComponent(VoterConfirmationContext contextViewModel) {
        this.contextViewModel = contextViewModel;

        fetchVotingsToBeConfirmed(contextViewModel);
        fetchRejectedVotings(contextViewModel);
        fetchApprovedVotings(contextViewModel);

        resolveCanApproveVoting(contextViewModel);
    }

    private void fetchVotingsToBeConfirmed(final VoterConfirmationContext viewModel) {
        execute(() -> this.votingsToConfirm = votingInEnvelopeService.votingsToBeConfirmedForVoter(viewModel.getUserData(),
                viewModel.getElectionGroup(), viewModel.getVoterDto().getId()));
    }

    private void fetchRejectedVotings(final VoterConfirmationContext viewModel) {
        execute(() -> this.rejectedVotings = votingInEnvelopeService.rejectedVotings(viewModel.getUserData(),
                viewModel.getElectionGroup(), viewModel.getVoterDto().getId()));
    }

    private void fetchApprovedVotings(final VoterConfirmationContext viewModel) {
        execute(() -> {
            this.approvedVotings = votingInEnvelopeService.approvedVotings(viewModel.getUserData(),
                    viewModel.getElectionGroup(), viewModel.getVoterDto().getId());

            if (showAcceptedVotingAlreadyExistsMessage()) {
                String municipalityName = viewModel.getMvArea().getMunicipalityName();
                String[] parameters = {municipalityName};
                buildDetailMessage("@voting.approveBallot.acceptedVoting", parameters, SEVERITY_WARN);
            }
        });
    }

    private boolean showAcceptedVotingAlreadyExistsMessage() {
        return hasApprovedVotings() && hasVotingsToConfirm();
    }

    public boolean hasApprovedVotings() {
        return approvedVotings != null && !approvedVotings.isEmpty();
    }

    private boolean hasVotingsToConfirm() {
        return this.votingsToConfirm != null && !this.votingsToConfirm.isEmpty();
    }

    private void resolveCanApproveVoting(VoterConfirmationContext viewModel) {
        VoterDto voterDto = viewModel.getVoterDto();

        canApproveVoting = approvedVotings.isEmpty();

        if (voterDto.getMvArea() == null || voterMunicipalitySameAsCurrent(viewModel, voterDto)) {
            canApproveVoting = false;
            buildDetailMessage("@voting.approveBallot.notSameMunicipality", SEVERITY_WARN);
        }

        if ((!voterDto.isEligible() ||
                !voterDto.isApproved())
                && !voterDto.isFictitious()) {
            canApproveVoting = false;
        }
    }

    private boolean voterMunicipalitySameAsCurrent(VoterConfirmationContext viewModel, VoterDto voterDto) {
        return !viewModel.getMvArea().getMunicipality().getPk().equals(voterDto.getMvArea().getMunicipality().getPk());
    }

    public String formatElectoralRollNumber() {
        Manntallsnummer number = contextViewModel.getElectoralRollNumber();
        if (number == null) {
            return "";
        }
        return number.getKortManntallsnummerMedZeroPadding() + " " + number.getSluttsifre();
    }

    public boolean isUnconfirmedVotings() {
        return hasVotingsToConfirm();
    }

    public void approveSelectedVoting(VotingDto votingDto) {
        execute(() -> {
            votingInEnvelopeService.approveVoting(userDataController.getUserData(), votingDto, toDto(municipality()));
            String message = approvedVotingMessage(votingDto);
            addInfoMessage(message);

        });

        refreshView();
    }

    private Municipality municipality() {
        return contextViewModel.getMvArea().getMunicipality();
    }

    private String approvedVotingMessage(VotingDto votingDto) {
        return messageProvider.get("@voting.confirmation.voter.voting.approved", votingDto.getVotingNumberDisplay(),
                votingDto.getVoterDto().getNameLine());
    }

    private void addInfoMessage(String message) {
        buildMessageForClientId("main-messages", message, FacesMessage.SEVERITY_INFO);
    }

    private void refreshView() {
        initComponent(contextViewModel);
        FacesUtil.updateDom("form");
    }

    public boolean renderApproveVotingLink(VotingDto votingDto) {
        if (!canApproveVoting) {
            return false;
        } else if (isElectronicMarkoffs()) {
            return true;
        }
        return isEarlyVoting(votingDto);
    }

    private boolean isElectronicMarkoffs() {
        return getContextViewModel().getMvArea().getMunicipality().isElectronicMarkoffs();
    }

    private boolean isEarlyVoting(VotingDto votingDto) {
        return votingDto.getVotingCategory().isEarlyVoting();
    }

    public boolean isShowUnconfirmedLinks(VotingDto votingDto) {
        return (userHasAccessToAdvanceVotingConfirmationSingle() && votingDto.isEarlyVoting()) ||
                (userHasAccessToElectionDayVotingConfirmationSingle() && !votingDto.isEarlyVoting());
    }

    private boolean userHasAccessToAdvanceVotingConfirmationSingle() {
        return getUserAccess().isStemmegivingPrøvingForhåndEnkelt();
    }

    private UserAccess getUserAccess() {
        return userDataController.getUserAccess();
    }

    private boolean userHasAccessToElectionDayVotingConfirmationSingle() {
        return getUserAccess().isStemmegivingPrøvingValgtingEnkelt();
    }

    public boolean isRenderRejectVotingLink() {
        return true;
    }

    public void setVotingToReject(VotingDto votingDto) {
        setSelectedVoting(votingDto);

        votingSuggestedRejectedDialog.initComponent(VotingSuggestedRejectedDialog.ContextViewModel.builder()
                .votingCategory(contextViewModel.getVotingCategory())
                .callbackHandler(this)
                .userData(contextViewModel.getUserData())
                .build());
        votingSuggestedRejectedDialog.show();
    }

    public void cancelRejection(VotingDto votingDto) {
        execute(() -> {
                    votingInEnvelopeService.cancelRejection(userDataController.getUserData(), votingDto, toDto(municipality()));
            String message = messageProvider.get("@voting.confirmation.voter.voting.rejection.cancelled", votingDto.getVotingNumberDisplay(),
                            votingDto.getVoterDto().getNameLine());
                    addInfoMessage(message);
                }
        );

        refreshView();
    }

    public boolean isRenderCancelRejectionLink() {
        return true;
    }

    public boolean hasRejectedVotings() {
        return rejectedVotings != null && !rejectedVotings.isEmpty();
    }

    public String rowStyleClass(VotingDto votingDto) {
        return votingDto.getVotingCategory().getId() + "-" + votingDto.getVotingNumber();
    }

    public void showElectoralRollHistoryDialog() {
        voterElectoralRollHistory.initComponent(VoterElectoralRollHistory.VoterElectoralRollHistoryContext.builder()
                .userData(contextViewModel.getUserData())
                .voterDto(contextViewModel.getVoterDto())
                .build());
        voterElectoralRollHistory.show();
    }

    @Override
    public void onMoveVotingsToSuggestedRejected(final VotingRejectionDto votingRejection) {
        execute(() -> {
            votingInEnvelopeService.rejectVoting(userDataController.getUserData(),
                    toDto(municipality()),
                    selectedVoting,
                    votingRejection);

            String message = rejectedVotingMessage(selectedVoting, votingRejection);
            addInfoMessage(message);
        });

        votingSuggestedRejectedDialog.hide();
        refreshView();
    }

    private String rejectedVotingMessage(VotingDto votingDto, VotingRejectionDto votingRejectionDto) {
        return messageProvider.get("@voting.confirmation.voter.voting.rejected", votingDto.getVotingNumberDisplay(),
                votingDto.getVoterDto().getNameLine(),
                messageProvider.get(votingRejectionDto.getName()));
    }

    public void onBackToVotingConfirmation() {
        getContextViewModel().getHandler().onVoterConfirmationDismiss();
    }

    interface Handler {

        void onVoterConfirmationDismiss();
    }
}
