package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.evote.model.views.VoterAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Component;
import no.valg.eva.admin.frontend.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ViewScoped
@NoArgsConstructor
public class VoterElectoralRollHistory extends BaseController implements Component<VoterElectoralRollHistory.VoterElectoralRollHistoryContext> {

    private static final long serialVersionUID = 4229971930509702945L;

    @Getter
    private List<VoterAudit> electoralRollHistory;

    @Inject
    private VoterAuditService voterAuditService;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString
    static class VoterElectoralRollHistoryContext {

        private UserData userData;
        private VoterDto voterDto;
    }

    @Override
    public void initComponent(VoterElectoralRollHistoryContext context) {
        loadVotingHistory(context);
    }

    private void loadVotingHistory(VoterElectoralRollHistoryContext context) {
        execute(() -> this.electoralRollHistory = voterAuditService.getHistoryForVoter(context.getUserData(), context.getVoterDto().getId()));
    }

    public void show() {
        FacesUtil.executeJS("PF('electoralRollHistoryDialog').show()");
    }
}
