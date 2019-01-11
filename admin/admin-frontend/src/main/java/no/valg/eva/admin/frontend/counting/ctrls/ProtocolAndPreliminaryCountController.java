package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_APPROVE_PROTOCOL_AND_PRELIMINARY_COUNT;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolAndPreliminaryCount;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.counting.view.DailyMarkOffCountsModel;
import no.valg.eva.admin.frontend.counting.view.MarkOffCountsModel;

@Named
@ConversationScoped
public class ProtocolAndPreliminaryCountController extends CountController {
	@Override
	public void initCountController() {
		// do nothing
	}

	@Override
	public Dialog getApproveDialog() {
		return CONFIRM_APPROVE_PROTOCOL_AND_PRELIMINARY_COUNT;
	}

	@Override
	public void saveCount() {
		try {
			getProtocolAndPreliminaryCount().validate();
			setCount(countingService.saveCount(userData, getCountContext(), getProtocolAndPreliminaryCount()));
			getCounts().setFirstProtocolCount(getProtocolAndPreliminaryCount().getProtocolCount());
			getCounts().setPreliminaryCount(getProtocolAndPreliminaryCount().getPreliminaryCount());
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isSaved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	public void approveCount() {
		try {
			getProtocolAndPreliminaryCount().validateForApproval();
			setCount(countingService.approveCount(userData, getCountContext(), getProtocolAndPreliminaryCount()));
			getCounts().setFirstProtocolCount(getProtocolAndPreliminaryCount().getProtocolCount());
			getCounts().setPreliminaryCount(getProtocolAndPreliminaryCount().getPreliminaryCount());
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isApproved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
		getApproveDialog().closeAndUpdate("countingForm");
	}

	@Override
	public void revokeApprovedCount() {
		try {
			setCount(countingService.revokeCount(userData, getCountContext(), getProtocolAndPreliminaryCount()));
			getCounts().setFirstProtocolCount(getProtocolAndPreliminaryCount().getProtocolCount());
			getCounts().setPreliminaryCount(getProtocolAndPreliminaryCount().getPreliminaryCount());
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isNotApprovedAnymore"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	@Override
	public boolean isApproved() {
		return getCount() != null && getProtocolAndPreliminaryCount().isApproved();
	}

	public ProtocolAndPreliminaryCount getProtocolAndPreliminaryCount() {
		return (ProtocolAndPreliminaryCount) getCount();
	}

	public boolean isSplitBallotCounts() {
		return false;
	}

	@Override
	public Count getCount() {
		return getCounts().getProtocolAndPreliminaryCount();
	}

	@Override
	public void setCount(Count count) {
		getCounts().setProtocolAndPreliminaryCount((ProtocolAndPreliminaryCount) count);
	}

	@Override
	public int getTotalBallotCountDifferenceFromPreviousCount() {
		return getProtocolAndPreliminaryCount().getDifferenceBetweenTotalBallotCountsAndMarkOffCount();
	}

	@Override
	public boolean isElectronicMarkOffs() {
		return getProtocolAndPreliminaryCount().isElectronicMarkOffs();
	}

	@Override
	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return getCount() != null ? getProtocolAndPreliminaryCount().getDailyMarkOffCounts() : super.getDailyMarkOffCounts();
	}

	@Override
	public boolean isCommentRequired() {
		return getProtocolAndPreliminaryCount().isCommentRequired();
	}

	public DailyMarkOffCountsModel getDailyMarkOffCountsModel() {
		if (!getDailyMarkOffCounts().isEmpty()) {
			return new DailyMarkOffCountsModel(this);
		}
		return null;
	}

	public MarkOffCountsModel getMarkOffCountsModel() {
		return new MarkOffCountsModel(this);
	}
}
