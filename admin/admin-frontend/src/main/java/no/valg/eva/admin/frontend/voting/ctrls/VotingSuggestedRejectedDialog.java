package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.evote.security.UserData;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Component;
import no.valg.eva.admin.frontend.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static no.valg.eva.admin.frontend.util.FacesUtil.getUserData;

@Named
@ViewScoped
@NoArgsConstructor
public class VotingSuggestedRejectedDialog extends BaseController implements Component<VotingSuggestedRejectedDialog.ContextViewModel> {


    private static final long serialVersionUID = -8686482278181154644L;

    @Getter
    @Setter
    private String selectedVotingRejectionId;

    @Getter
    @Setter
    private List<VotingRejectionDto> votingRejectionDtoList;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    private Handler callbackHandler;

    public void show() {
        FacesUtil.executeJS("PF('votingSuggestedRejectedDialogWidget').show()");
    }

    public void hide() {
        FacesUtil.executeJS("PF('votingSuggestedRejectedDialogWidget').hide()");
    }

    @Override
    public void initComponent(ContextViewModel context) {
        this.callbackHandler = context.getCallbackHandler();

        findRejectionReasonsForVotingCategory(context.getVotingCategory());
        
        resetSelectedReason();
    }

    private void findRejectionReasonsForVotingCategory(VotingCategory votingCategory) {
        execute(() -> votingRejectionDtoList = votingInEnvelopeService.votingRejections(getUserData(), votingCategory));
    }

    private void resetSelectedReason() {
        selectedVotingRejectionId = null;
    }

    public void onSelectedRejectionReason() {
        final VotingRejectionDto dto = fromSelectedRejectionId();
        callbackHandler.onMoveVotingsToSuggestedRejected(dto);
    }

    private VotingRejectionDto fromSelectedRejectionId() {
        return getVotingRejectionDtoList().stream()
                .filter(rejectionDto -> rejectionDto.getId().equals(getSelectedVotingRejectionId()))
                .findFirst()
                .orElse(null);
    }

    public boolean isRejectButtonDisabled() {
        return selectedVotingRejectionId == null;
    }
    
    
    
    @Builder
    @Getter
    @ToString
    static class ContextViewModel {
        private Handler callbackHandler;
        private VotingCategory votingCategory;
        private UserData userData;
    }

    public interface Handler {
        void onMoveVotingsToSuggestedRejected(VotingRejectionDto votingRejection);
    }
}
