package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.util.MessageUtil;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

@Named
@ConversationScoped
public class ApproveManualRejectedCountController extends BaseApproveRejectedCountController {

    private static final long serialVersionUID = 5462744544854311625L;

    @Override
    protected String buildFromUrlPart() {
        return "fromApproveManualRejectedCount=true";
    }

    @Override
    protected boolean resolveErrorStateAndMessages(FinalCount finalCount) {
        if (!isReportingUnitOnContestLevel()) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.info.operator_not_contest_level.manual"), FacesMessage.SEVERITY_INFO);
        }
        if (finalCount == null) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.noCounts"), FacesMessage.SEVERITY_INFO);
            return true;
        }
        if (!finalCount.isManualCount()) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.rejected.noManualCounts"), FacesMessage.SEVERITY_ERROR);
            return true;
        }
        if (!finalCount.isModifiedBallotsProcessed() && !finalCount.isApproved() && isReportingUnitOnContestLevel()) {
            MessageUtil.buildDetailMessage(messageProvider.get("@count.ballot.approve.warning.regCorrectedNotDone"), FacesMessage.SEVERITY_ERROR);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isScanned() {
        return false;
    }

    @Override
    public boolean isEditMode() {
        return true;
    }
}
