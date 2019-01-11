package no.valg.eva.admin.frontend.counting.ctrls;

import static java.lang.String.format;

import javax.enterprise.context.Conversation;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BatchId;

class ModifiedBallotUrlBuilder {
	private static final String REGISTER_MODIFIED_BALLOTS_PAGE = "/secure/counting/registerModifiedBallot.xhtml";
	private static final String REGISTER_MODIFIED_BALLOTS_BATCH_PAGE = "/secure/counting/registerModifiedBallotBatch.xhtml";
	private static final String SHOW_MODIFIED_BALLOTS_PAGE = "/secure/counting/showModifiedBallot.xhtml";
	private BallotCount ballotCount;
	private Conversation conversation;
	private BatchId batchId;

	private ModifiedBallotUrlBuilder() {
	}

	public ModifiedBallotUrlBuilder(BallotCount ballotCount) {
		this.ballotCount = ballotCount;
	}

	public ModifiedBallotUrlBuilder(BatchId batchId) {
		this.batchId = batchId;
	}

	public String buildRegisterModifiedBallotBatchUrl() {
		checkConversation();
		return format("%s?modifiedBallotBatchId=%s&cid=%s", REGISTER_MODIFIED_BALLOTS_BATCH_PAGE, batchId.getId(), conversation.getId());
	}

	public String buildRegisterModifiedBallotUrl() {
		checkConversation();
		return format("%s?ballotCountRef=%s&cid=%s", REGISTER_MODIFIED_BALLOTS_PAGE, ballotCount.getBallotCountRef().getPk(), conversation.getId());
	}

	public String buildReviewModifiedBallotsUrl() {
		checkConversation();
		return format("%s?ballotCountRef=%s&cid=%s", SHOW_MODIFIED_BALLOTS_PAGE, ballotCount.getBallotCountRef().getPk(), conversation.getId());
	}

	public ModifiedBallotUrlBuilder with(Conversation conversation) {
		this.conversation = conversation;
		return this;
	}

	public static ModifiedBallotUrlBuilder from(BallotCount ballotCount) {
		return new ModifiedBallotUrlBuilder(ballotCount);
	}

	public static ModifiedBallotUrlBuilder from(BatchId batchId) {
		return new ModifiedBallotUrlBuilder(batchId);
	}

	private void checkConversation() {
		if (conversation == null) {
			throw new IllegalArgumentException("Missing conversation");
		}
		if (conversation.getId() == null) {
			throw new IllegalArgumentException("Conversation is missing id");
		}
	}
}
