package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Component;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

// Denne dialogen skal kun vises ved godkjenning av stemmegivninger som er til ordinær behandling / foreslått godkjent

@Named
@ViewScoped
@NoArgsConstructor
public class ApproveSuggestedApprovedDialog extends BaseController implements Component<ApproveSuggestedApprovedDialog.Context> {

    private static final long serialVersionUID = 865367713952158304L;
    
    @Inject
    private MessageProvider messageProvider;
    
    @Getter
    private String message;
    
    private Context context;

    @Override
    public void initComponent(Context context) {
        validateContext(context);
        this.context = context;
        resolveMessage();
    }
    private void validateContext(Context context) {
        if (context == null || context.getSelectedVotings() == null || context.getSelectedVotings().isEmpty() || context.getHandler() == null) {
            throw new IllegalStateException("Approve suggested approval dialog expects at least 1 selected voting and a component handler.");
        }
    }
    
    private void resolveMessage() {
        int numberOfVotings = context.getSelectedVotings().size();
        if (numberOfVotings > 1) {
            message = messageProvider.getWithTranslatedParams("@voting.confirmation.confirmApproveMessage", String.valueOf(numberOfVotings));
        }
        else {
            message = messageProvider.get("@voting.confirmation.confirmApproveMessageSingle");
        }
    }
    
    public void onApprove() {
        context.getHandler().onApproveSuggestedApprovedVotings(context.getSelectedVotings());
    }

    public void show() {
        FacesUtil.executeJS("PF('approveSuggestedApprovedDialogWidget').show()");
    }

    public void hide() {
        FacesUtil.executeJS("PF('approveSuggestedApprovedDialogWidget').hide()");
    }
    
    
    
    public static interface Handler {
        void onApproveSuggestedApprovedVotings(List<VotingViewModel> votings);
    }
    
    @Builder
    @Getter
    public static class Context {
        List<VotingViewModel> selectedVotings;
        private Handler handler;
    }
}
