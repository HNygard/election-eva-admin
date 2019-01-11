package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.counting.view.Tab;
import no.valg.eva.admin.frontend.counting.view.TopInfoProvider;
import no.valg.eva.admin.frontend.counting.view.ballotcount.BallotCountsModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Endelig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;

public abstract class CountController extends BaseCountController implements TopInfoProvider {
	private static final String CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION = "controller does not support this operation";

	protected StartCountingController startCountingController;
	private UserDataController userDataController;

	private int tabIndex;

	public CountController() {
	}

	public abstract void initCountController();
	
	public abstract boolean isCommentRequired();

	@Override
	protected void doInit() {
		// do nothing
	}

	public void setStartCountingController(StartCountingController startCountingController) {
		this.startCountingController = startCountingController;
	}

	public Button button(ButtonType type) {
		switch (type) {
		case SAVE:
		case APPROVE:
			if (hasWriteAccess()) {
				return enabled(isCountEditable());
			}
			return notRendered();
		case REVOKE:
			if (hasOpptellingOpphevForeløpigTelling()) {
				return enabled(isApproved() && !isNextApproved());
			}
			return notRendered();
		default:
			return notRendered();
		}
	}

	public Dialog getApproveDialog() {
		return null;
	}

	/**
	 * @return true if user can edit, else false
	 */
	protected boolean hasWriteAccess() {
		return userAccess.hasAccess(Opptelling_Forhånd_Rediger, Opptelling_Valgting_Rediger);
	}

	/**
	 * @return true if user can revoke (oppheve godkjenning) protocol or preliminary count, else false
	 */
	protected boolean hasOpptellingOpphevForeløpigTelling() {
		return userAccess.hasAccess(Opptelling_Opphev_Foreløpig_Telling);
	}

	protected boolean hasOpptellingOpphevEndeligTelling() {
		return userAccess.hasAccess(Opptelling_Opphev_Endelig_Telling);
	}

	@Override
	public MessageProvider getMessageProvider() {
		return startCountingController.getMessageProvider();
	}

	/**
	 * Convenient for test
	 */
	UserDataController getUserDataController() {
		return userDataController;
	}

	public void setUserDataController(UserDataController userDataController) {
		this.userDataController = userDataController;
	}

	@Override
	public String getDisplayAreaName() {
		if (getCount().getAreaPath().isMunicipalityPollingDistrict()) {
			return getMessageProvider().get("@area.polling_district.municipality");
		} else {
			return (getCount().getAreaPath().isPollingDistrictLevel() ? getCount().getAreaPath().getPollingDistrictId()
					: getCount().getAreaPath().getBoroughId())
					+ " " + getCount().getAreaName();
		}
	}

	public Counts getCounts() {
		return startCountingController.getCounts();
	}

	public void back() {
	}

	public void revokeApprovedCount() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public void modifiedBallotProcessed() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public boolean isRejected() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public boolean isElectronicMarkOffs() {
		return false;
	}

	public DailyMarkOffCounts getDailyMarkOffCounts() {
		return new DailyMarkOffCounts();
	}

	public boolean isIncludeMarkOffCount() {
		return true;
	}

	public boolean isIncludeProtocolCount() {
		return false;
	}

	public int getOrdinaryBallotCountDifferenceFromPreviousCount() {
		return 0;
	}

	public int getBlankBallotCountDifferenceFromPreviousCount() {
		return 0;
	}

	public int getQuestionableBallotCountDifferenceFromPreviousCount() {
		return 0;
	}

	public Integer getTotalMarkOffCount() {
		return getCounts().getMarkOffCount();
	}

	public CountContext getContext() {
		return getCounts().getContext();
	}

	public String saveCountAndRegisterCountCorrections() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public void openConfirmApproveCountDialog() {
		getApproveDialog().open();
	}

	public boolean isCountEditable() {
		return getCount().isEditable() && !isApproved() && isPreviousApproved() && !isNextApproved();
	}

	public boolean isManualCount() {
		return getCount().isManualCount();
	}

	public boolean isManualCountAndEditable() {
		return isManualCount() && isCountEditable();
	}

	public void saveCount() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public boolean isApproved() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public boolean isSplitBallotCounts() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public Count getCount() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public void setCount(Count count) {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public int getTotalBallotCountDifferenceFromPreviousCount() {
		throw new UnsupportedOperationException(CONTROLLER_DOES_NOT_SUPPORT_THIS_OPERATION);
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	public CountController getPreviousController() {
		if (startCountingController.getTabs().isEmpty() || getTabIndex() <= 0) {
			return null;
		}
		return startCountingController.getTabs().get(getTabIndex() - 1).getController();
	}

	public boolean isPreviousApproved() {
		CountController ctrl = getPreviousController();
		return ctrl == null || ctrl.isApproved();
	}

	public CountController getNextController() {
		if (startCountingController.getTabs().isEmpty() || getTabIndex() >= startCountingController.getTabs().size() - 1) {
			return null;
		}
		return startCountingController.getTabs().get(getTabIndex() + 1).getController();
	}

	public boolean isNextApproved() {
		CountController ctrl = getNextController();
		return ctrl != null && ctrl.isApproved();
	}

	public Tab getPreviousTab() {
		CountController previous = getPreviousController();
		if (previous != null) {
			return startCountingController.getTabs().get(previous.getTabIndex());
		}
		return null;
	}

	public Tab getTab() {
		return startCountingController.getTabs().get(getTabIndex());
	}

	public BallotCountsModel getBallotCountsModel() {
		return new BallotCountsModel(this);
	}

	@Override
	public String getMunicipalityName() {
		return getCounts().getMunicipalityName();
	}

	@Override
	public String getElectionName() {
		return getCounts().getElectionName();
	}

	@Override
	public String getCategoryName() {
		return getCountContext().getCategoryMessageProperty();
	}

	@Override
	public CountContext getCountContext() {
		return startCountingController.getCountContext();
	}

	@Override
	public boolean isUserOnCountyLevel() {
		return startCountingController.isUserOnCountyLevel();
	}

	public boolean hasCorrections() {
		return false;
	}

	public String getReminderComment() {
		return getMessageProvider().get(
				isCommentRequired()
						? "@count.dialog.comment.reminder.required"
						: "@count.dialog.comment.reminder"
		);
	}
}
