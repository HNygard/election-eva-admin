package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.common.Button.renderedAndEnabled;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_APPROVE_PROTOCOL_COUNT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialog;

@Named
@ConversationScoped
public class ProtocolCountController extends CountController {
	@Inject
	private PreliminaryCountController preliminaryCountController;

	@Override
	public void initCountController() {
		// do nothing
	}

	@Override
	public Dialog getApproveDialog() {
		return CONFIRM_APPROVE_PROTOCOL_COUNT;
	}

	@Override
	public void saveCount() {
		try {
			ProtocolCount protocolCount = getProtocolCount();
			protocolCount.validate();
			setCount(countingService.saveCount(userData, getCountContext(), protocolCount));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isSaved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	public void approveCount() {
		try {
			ProtocolCount protocolCount = getProtocolCount();
			protocolCount.validateForApproval();
			setCount(countingService.approveCount(userData, getCountContext(), protocolCount));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isApproved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
		getApproveDialog().closeAndUpdate("countingForm");
	}

	@Override
	public void revokeApprovedCount() {
		try {
			setCount(countingService.revokeCount(userData, getCountContext(), getProtocolCount()));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isNotApprovedAnymore"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	@Override
	public boolean isApproved() {
		List<ProtocolCount> protocolCounts = getCounts().getProtocolCounts();
		if (protocolCounts.isEmpty()) {
			return false;
		}
		for (ProtocolCount protocolCount : protocolCounts) {
			if (!protocolCount.isApproved()) {
				return false;
			}
		}
		return true;
	}

	public ProtocolCount getProtocolCount() {
		return (ProtocolCount) getCount();
	}

	@Deprecated
	public void setProtocolCount(ProtocolCount protocolCount) {
		setCount(protocolCount);
	}

	@Override
	public Button button(ButtonType type) {
		if (type == ButtonType.BACK) {
			return preliminaryCountController.isSelectedProtocolCount() ? renderedAndEnabled() : notRendered();
		}
		if (type == ButtonType.REVOKE && hasOpptellingOpphevForeløpigTelling()) {
			boolean approved = getProtocolCount() != null && getProtocolCount().isApproved();
			return enabled(approved && !isNextApproved());
		}
		return super.button(type);
	}

	@Override
	public boolean isSplitBallotCounts() {
		return false;
	}

	@Override
	public Count getCount() {
		if (preliminaryCountController.isSelectedProtocolCount()) {
			return getCounts().getProtocolCounts().get(preliminaryCountController.getSelectedProtocolCountIndex());
		}
		return getCounts().getFirstProtocolCount();
	}

	@Override
	public void setCount(Count count) {
		Counts counts = getCounts();
		if (preliminaryCountController.isSelectedProtocolCount()) {
			counts.getProtocolCounts().set(preliminaryCountController.getSelectedProtocolCountIndex(), (ProtocolCount) count);
		} else {
			counts.setFirstProtocolCount((ProtocolCount) count);
		}
	}

	@Override
	public int getTotalBallotCountDifferenceFromPreviousCount() {
		return getProtocolCount().getDifferenceBetweenTotalBallotCountsAndMarkOffCount();
	}

	@Override
	public boolean isElectronicMarkOffs() {
		return getProtocolCount().isElectronicMarkOffs();
	}

	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return getProtocolCount().getDailyMarkOffCounts();
	}

	@Override
	public void back() {
		preliminaryCountController.selectProtocolCount(null);
	}

	@Override
	public String getDisplayAreaName() {
		if (getCounts().getProtocolCounts().size() > 1 && !preliminaryCountController.isSelectedProtocolCount()) {
			return preliminaryCountController.getDisplayAreaName();
		}
		return getCount().getAreaPath().getPollingDistrictId() + " " + getCount().getAreaName();
	}

	@Override
	public boolean isCommentRequired() {
		return getProtocolCount().isCommentRequired();
	}

	public String getProtocolBallotCountsHeader() {
		if (getProtocolCount().getBallotCountForOtherContests() == null) {
			return getMessageProvider().get("@count.votes.contentsPolls");
		} else {
			return getMessageProvider().get("@count.votes.contentsPolls") + " (" + getMessageProvider().get("@area_level[4].name") + " "
					+ getCounts().getContestName() + ")";
		}
	}

	public List<ProtocolBallotCount> getProtocolBallotCounts() {
		List<ProtocolBallotCount> result = new ArrayList<>();
		result.add(new OrdinaryCount());
		result.add(new QuestionableCount());
		return result;
	}

	public List<ProtocolCount> getBallotCountForOtherContests() {
		if (getProtocolCount().getBallotCountForOtherContests() != null) {
			return Arrays.asList(getProtocolCount());
		}
		return Collections.emptyList();
	}

	public interface ProtocolBallotCount {

		String getAft();

		String getRowStyleClass();

		String getTitle();

		int getValue();

		void setValue(int value);
	}

	public class OrdinaryCount implements ProtocolBallotCount {
		@Override
		public String getAft() {
			return "ordinary";
		}

		@Override
		public String getRowStyleClass() {
			return "row_ordinary";
		}

		@Override
		public String getTitle() {
			return "@count.votes.ordinary";
		}

		@Override
		public int getValue() {
			return getProtocolCount().getOrdinaryBallotCount();
		}

		@Override
		public void setValue(int value) {
			getProtocolCount().setOrdinaryBallotCount(value);
		}
	}

	public class BlankCount implements ProtocolBallotCount {
		@Override
		public String getAft() {
			return "blank";
		}

		@Override
		public String getRowStyleClass() {
			return "row_blank";
		}

		@Override
		public String getTitle() {
			return "@count.votes.blanc";
		}

		@Override
		public int getValue() {
			return getProtocolCount().getBlankBallotCount();
		}

		@Override
		public void setValue(int value) {
			getProtocolCount().setBlankBallotCount(value);
		}
	}

	public class QuestionableCount implements ProtocolBallotCount {
		@Override
		public String getAft() {
			return "questionable";
		}

		@Override
		public String getRowStyleClass() {
			return "row_questionable";
		}

		@Override
		public String getTitle() {
			return "@count.votes.questionable";
		}

		@Override
		public int getValue() {
			return getProtocolCount().getQuestionableBallotCount();
		}

		@Override
		public void setValue(int value) {
			getProtocolCount().setQuestionableBallotCount(value);
		}
	}
}
