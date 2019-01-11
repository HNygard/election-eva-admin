package no.valg.eva.admin.frontend.counting.view;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.counting.ctrls.CompareCountsController;

public class CompareCountsSelect {

	private CompareCountsController ctrl;
	private String id = "";
	private String name = "";

	public CompareCountsSelect(CompareCountsController ctrl, String name) {
		this.ctrl = ctrl;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void approve() {
		ctrl.approve(this);
	}

	public void openConfirmApproveCountDialog() {
		ctrl.setCurrentCountsSelect(this);
		ctrl.getApproveDialog().open();
	}

	public void confirmConfirmApproveCountDialog() {
		ctrl.setCurrentCountsSelect(null);
		approve();
		ctrl.getApproveDialog().closeAndUpdate("countingForm");
	}

	public void cancelConfirmApproveCountDialog() {
		ctrl.setCurrentCountsSelect(null);
		ctrl.getApproveDialog().close();
	}

	public void saveComment() {
		ctrl.saveComment(this);
	}

	public void revoke() {
		ctrl.revoke(this);
	}

	public FinalCount getFinalCount() {
		return ctrl.getCount(this);
	}

	public boolean hasFinalCount() {
		return getFinalCount() != null;
	}

	public boolean isFinalCountApproved() {
		return hasFinalCount() && getFinalCount().isApproved();
	}

	public Button getApproveButton() {
		return ctrl.getApproveButton(this);
	}

	public Button getRevokeButton() {
		return ctrl.getRevokeButton(this);
	}
	
	public String getName() {
		return name;
	}
}
