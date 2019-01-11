package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.counting.validator.ApprovePreliminaryCountValidator;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.counting.view.DailyMarkOffCountsModel;
import no.valg.eva.admin.frontend.counting.view.LateValidationCoversModel;
import no.valg.eva.admin.frontend.counting.view.MarkOffCountsModel;
import no.valg.eva.admin.frontend.counting.view.MarkOffCountsModelForAllPollingDistricts;
import no.valg.eva.admin.frontend.util.MessageUtil;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.util.List;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_APPROVE_PRELIMINARY_COUNT;

@Named
@ConversationScoped
public class PreliminaryCountController extends CountController {
	private Integer selectedProtocolCountIndex;

	@Override
	public void initCountController() {
		selectProtocolCount(null);
	}

	public void sjekkAntallStemmesedlerLagtTilSideLagret() {
		if (!isAntallStemmesedlerLagtTilSideLagret() && isEarlyVoting() && super.isCountEditable()) {
			MessageUtil.buildMessageForClientId("preliminaryCount", FacesMessage.SEVERITY_ERROR,
					getMessageProvider().get("@opptelling.antallStemmesedlerLagtTilSide.ikkeLagret"));
		}
	}

    private boolean isEarlyVoting() {
        return getCount().getCategory().isEarlyVoting();
    }

	@Override
	public Dialog getApproveDialog() {
		return CONFIRM_APPROVE_PRELIMINARY_COUNT;
	}

	@Override
	public void saveCount() {
		try {
			PreliminaryCount preliminaryCount = getPreliminaryCount();
			preliminaryCount.validate();
			setCount(countingService.saveCount(userData, getCountContext(), preliminaryCount));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isSaved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	public void approveCount() {
		try {
			PreliminaryCount preliminaryCount = getPreliminaryCount();
			getValidator().validate(preliminaryCount);
			setCount(countingService.approveCount(userData, getCountContext(), preliminaryCount));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isApproved"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
		getApproveDialog().closeAndUpdate("countingForm");
	}

	@Override
	public void revokeApprovedCount() {
		try {
			setCount(countingService.revokeCount(userData, getCountContext(), getPreliminaryCount()));
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@count.isNotApprovedAnymore"));
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	@Override
	public boolean isCountEditable() {
		if (!isAntallStemmesedlerLagtTilSideLagret() && isEarlyVoting()) {
			return false;
		}
		if (isUserOnCountyLevel()) {
			return false;
		}
		return super.isCountEditable();
	}

	@Override
	public boolean isApproved() {
		PreliminaryCount preliminaryCount = getPreliminaryCount();
		return preliminaryCount != null && preliminaryCount.isApproved();
	}

	public PreliminaryCount getPreliminaryCount() {
		return (PreliminaryCount) getCount();
	}

	public boolean hasProtocolCounts() {
		return getCounts().hasProtocolCounts();
	}

	public List<ProtocolCount> getProtocolCounts() {
		return getCounts().getProtocolCounts();
	}

	public ProtocolCount getProtocolCount() {
		if (isSelectedProtocolCount()) {
			return getProtocolCounts().get(getSelectedProtocolCountIndex());
		}
		return getProtocolCounts().get(0);
	}

	@Override
	public boolean isIncludeProtocolCount() {
		return getCounts().hasProtocolCounts();
	}

	public boolean isSplitBallotCounts() {
		return false;
	}

	@Override
	public Count getCount() {
		return getCounts().getPreliminaryCount();
	}

	@Override
	public void setCount(Count count) {
		getCounts().setPreliminaryCount((PreliminaryCount) count);
	}

	@Override
	public int getOrdinaryBallotCountDifferenceFromPreviousCount() {
		return getCounts().getOrdinaryBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);
	}

	@Override
	public int getBlankBallotCountDifferenceFromPreviousCount() {
		return getCounts().getBlankBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);
	}

	@Override
	public int getQuestionableBallotCountDifferenceFromPreviousCount() {
		return getCounts().getQuestionableBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);
	}

	@Override
	public int getTotalBallotCountDifferenceFromPreviousCount() {
		if (isIncludeProtocolCount()) {
			return getCounts().getTotalBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);
		}
		PreliminaryCount preliminaryCount = getPreliminaryCount();
		if (preliminaryCount.getExpectedBallotCount() != null) {
			return preliminaryCount.getTotalBallotCount() - preliminaryCount.getExpectedBallotCount();
		}
		return preliminaryCount.getTotalBallotCount() - getTotalMarkOffCount();
	}

	@Override
	public Integer getTotalMarkOffCount() {
		Integer markOffCount = getCounts().getMarkOffCount();
		if (getDailyMarkOffCounts() != null) {
			markOffCount = getDailyMarkOffCounts().getMarkOffCount();
		}
		if (markOffCount == null) {
			return null;
		}
		switch (getCountContext().getCategory()) {
		case FO:
			return markOffCount - getPreliminaryCount().getLateValidationCovers();
		case FS:
			return markOffCount + getPreliminaryCount().getLateValidationCovers();
		default:
			return markOffCount;
		}
	}

	public boolean renderMultipleProtocolCountsView() {
		return !isSelectedProtocolCount() && hasProtocolCounts() && getProtocolCounts().size() > 1;
	}

	public boolean renderSingleProtocolCountView() {
		return isSelectedProtocolCount() || getProtocolCounts().size() == 1;
	}

	public String getAreaName(ProtocolCount protocolCount) {
		if (protocolCount.getAreaPath().isPollingDistrictLevel()) {
			return protocolCount.getAreaPath().getPollingDistrictId() + " " + protocolCount.getAreaName();
		}
		return protocolCount.getAreaName();
	}

	public void selectProtocolCount(Integer selectedProtocolCountIndex) {
		this.selectedProtocolCountIndex = selectedProtocolCountIndex;
	}

	public Integer getSelectedProtocolCountIndex() {
		return selectedProtocolCountIndex;
	}

	public boolean isSelectedProtocolCount() {
		return selectedProtocolCountIndex != null;
	}

	@Override
	public boolean isCommentRequired() {
		return getValidator().isCommentRequired(getPreliminaryCount());
	}

	public LateValidationCoversModel getLateValidationCoversModel() {
		if (isIncludeMarkOffCount() && getCounts().getContext().getCategory().equals(FO)) {
			return new LateValidationCoversModel(this);
		}
		return null;
	}

	@Override
	public boolean isElectronicMarkOffs() {
		return getPreliminaryCount().isElectronicMarkOffs();
	}

	public DailyMarkOffCountsModel getDailyMarkOffCountsModel() {
		if (getDailyMarkOffCounts() != null) {
			return new DailyMarkOffCountsModel(this);
		}
		return null;
	}

	@Override
	public DailyMarkOffCounts getDailyMarkOffCounts() {
		if (getPreliminaryCount() == null) {
			return null;
		}
		return getPreliminaryCount().getDailyMarkOffCounts();
	}

	public MarkOffCountsModel getMarkOffCountsModel() {
		return new MarkOffCountsModel(this);
	}

	public MarkOffCountsModelForAllPollingDistricts getMarkOffCountsModelForAllPollingDistricts() {
		if (getPreliminaryCount().getTotalBallotCountForOtherPollingDistricts() != null) {
			return new MarkOffCountsModelForAllPollingDistricts(this);
		}
		return null;
	}

	@Override
	public boolean isIncludeMarkOffCount() {
		return getPreliminaryCount().getMarkOffCount() != null || getPreliminaryCount().getDailyMarkOffCounts() != null;
	}

	public boolean isIncludeExpectedBallotCount() {
		PreliminaryCount preliminaryCount = getPreliminaryCount();
		return preliminaryCount.getExpectedBallotCount() != null && preliminaryCount.getMarkOffCount() == null;
	}

	public Integer getExpectedBallotCount() {
		return getPreliminaryCount().getExpectedBallotCount();
	}

	public boolean isIncludeTotalBallotCount() {
		return !isIncludeTotalBallotCountForAllPollingDistricts();
	}

	public boolean isIncludeTotalBallotCountForAllPollingDistricts() {
		return getPreliminaryCount().getTotalBallotCountForAllPollingDistricts() != null;
	}

	ApprovePreliminaryCountValidator getValidator() {
		PreliminaryCount preliminaryCount = getPreliminaryCount();
		if (preliminaryCount.getCategory() == VO) {
			int total;
			if (getDailyMarkOffCounts() == null) {
				total = getCounts().getTotalBallotCountForProtocolCounts();
			} else {
				total = getDailyMarkOffCounts().getMarkOffCount();
			}
			return ApprovePreliminaryCountValidator.forVo(total);
		} else {
			return ApprovePreliminaryCountValidator.forOtherCategoriesThanVo();
		}
	}

	private boolean isAntallStemmesedlerLagtTilSideLagret() {
		PreliminaryCount preliminaryCount = getPreliminaryCount();
		return preliminaryCount != null && preliminaryCount.isAntallStemmesedlerLagtTilSideLagret();
	}

}
