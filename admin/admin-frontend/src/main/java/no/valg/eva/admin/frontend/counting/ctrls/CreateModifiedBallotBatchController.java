package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CREATE_MODIFIED_BALLOT_BATCH;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.ModifiedBallotBatchCreationFailed;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ConversationScoped
public class CreateModifiedBallotBatchController extends ConversationScopedController {

	public static final String BALLOT_COUNT_REF = "ballotCountRef";

	@Inject
	protected UserData userData;
	@Inject
	protected ModifiedBallotBatchService modifiedBallotBatchService;
	@Inject
	protected MessageProvider messageProvider;
	@Inject
	protected ModifiedBallotsStatusController modifiedBallotsStatusController;

	private Integer modifiedBallotBatchSize;
	private BallotCount ballotCount;

	@Override
	protected void doInit() {
		// No init currently needed
	}

	public void showModifiedBallotBatchDialog(BallotCount ballotCount) {
		modifiedBallotBatchSize = null;
		this.ballotCount = ballotCount;
		getCreateModifiedBatchDialog().setTitleAndOpen(messageProvider.get("@party[" + ballotCount.getId() + "].name"));
	}

	public void initiateModifiedBallotBatch() {
		try {
			ModifiedBallotBatchProcess process = modifiedBallotsStatusController.getProcess();
			String registerModifiedBallotUrl = ModifiedBallotUrlBuilder
					.from(modifiedBallotBatchService.createModifiedBallotBatch(userData, ballotCount, getModifiedBallotBatchSizeNonNull(), process)
							.getBatchId())
					.with(getConversation())
					.buildRegisterModifiedBallotBatchUrl();
			redirectTo(registerModifiedBallotUrl);
		} catch (ModifiedBallotBatchCreationFailed e) {
			buildErrorMessage(e);
		}
	}

	public int getModifiedBallotBatchSizeNonNull() {
		return modifiedBallotBatchSize == null ? 0 : modifiedBallotBatchSize;
	}

	protected void redirectTo(String urlString) {
		FacesUtil.redirect(urlString, false);
	}

	protected void buildErrorMessage(ModifiedBallotBatchCreationFailed e) {
		MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR,
				messageProvider.get("@count.votes.cast.batch.size.invalid", e.getModifiedBallotBatchSizeRequested(), e.getModifiedBallotsRemaining()));
	}

	public Integer getModifiedBallotBatchSize() {
		return modifiedBallotBatchSize;
	}

	public void setModifiedBallotBatchSize(Integer modifiedBallotBatchSize) {
		this.modifiedBallotBatchSize = modifiedBallotBatchSize;
	}

	public void goToShowModifiedBallot(BallotCount ballotCount, FinalCount finalCount) {
		if (finalCount.isModifiedBallotsProcessed()) {
			redirectTo(ModifiedBallotUrlBuilder.from(ballotCount).with(getConversation()).buildReviewModifiedBallotsUrl());
		} else {
			redirectTo(ModifiedBallotUrlBuilder.from(ballotCount).with(getConversation()).buildRegisterModifiedBallotUrl());
		}
	}

	public Dialog getCreateModifiedBatchDialog() {
		return CREATE_MODIFIED_BALLOT_BATCH;
	}

}
