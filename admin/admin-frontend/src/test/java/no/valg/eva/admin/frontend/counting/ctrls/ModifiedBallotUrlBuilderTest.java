package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.frontend.counting.ctrls.ModifiedBallotUrlBuilder.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.enterprise.context.Conversation;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ModifiedBallotUrlBuilderTest {

	public static final BallotCountRef BALLOT_COUNT_REF = new BallotCountRef(0);
	public static final String CID = "2";
	private static final BatchId BATCH_ID = new BatchId("100_1_10");
	private BallotCount fakeBallotCount;
	private Conversation fakeConversation;

	@BeforeMethod
	public void setUp() throws Exception {
		fakeBallotCount = mock(BallotCount.class);
		when(fakeBallotCount.getBallotCountRef()).thenReturn(BALLOT_COUNT_REF);

		fakeConversation = mock(Conversation.class);
		when(fakeConversation.getId()).thenReturn(CID);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void buildRegisterModifiedBallotUrlWithoutConversation_ShouldFail() {
		assertThat(from(fakeBallotCount).buildRegisterModifiedBallotUrl());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void buildRegisterModifiedBallotBatchUrlWithoutConversation_ShouldFail() {
		assertThat(from(fakeBallotCount).buildRegisterModifiedBallotBatchUrl());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void buildReviewModifiedBallotsUrlWithoutConversation_fails() {
		assertThat(from(fakeBallotCount).buildReviewModifiedBallotsUrl());
	}

	@Test
	public void testBuildRegisterModifiedBallotBatchUrl() throws Exception {
		assertThat(
				from(BATCH_ID)
						.with(fakeConversation)
						.buildRegisterModifiedBallotBatchUrl())
				.isEqualTo("/secure/counting/registerModifiedBallotBatch.xhtml?modifiedBallotBatchId=100_1_10&cid=2");
	}

	@Test
	public void testBuildRegisterModifiedBallotUrl() throws Exception {
		assertThat(
				from(fakeBallotCount)
						.with(fakeConversation)
						.buildRegisterModifiedBallotUrl())
				.isEqualTo("/secure/counting/registerModifiedBallot.xhtml?ballotCountRef=0&cid=2");
	}

	@Test
	public void testBuildReviewModifiedBallotsUrl() throws Exception {
		assertThat(
				from(fakeBallotCount)
						.with(fakeConversation)
						.buildReviewModifiedBallotsUrl())
				.isEqualTo("/secure/counting/showModifiedBallot.xhtml?ballotCountRef=0&cid=2");
	}
}
