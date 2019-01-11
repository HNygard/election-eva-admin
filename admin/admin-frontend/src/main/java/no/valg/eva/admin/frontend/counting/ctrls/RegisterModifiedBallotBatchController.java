package no.valg.eva.admin.frontend.counting.ctrls;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;

@Named
@ConversationScoped
public class RegisterModifiedBallotBatchController extends RegisterModifiedBallotController implements Serializable {

	@Inject
	protected ModifiedBallotBatchService modifiedBallotBatchService;

	@Override
	protected void doInit() {
		// Nothing to do right now
	}

	@Override
	protected void loadBallots() {
		modifiedBallots = modifiedBallotBatchService.findActiveBatchByBatchId(userData, readBatchIdFromRequest());
	}

	private BatchId readBatchIdFromRequest() {
		return new BatchId(getRequestParameter("modifiedBallotBatchId"));
	}

	@Override
	protected void currentModifiedBallotIsDone() {
		currentModifiedBallot.setDone(true);
	}
}
