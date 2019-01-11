package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;

import no.valg.eva.admin.counting.domain.model.BallotCount;

import org.testng.annotations.Test;

public class BallotCountAuditDetailsTest {
	@Test
	public void toJsonObject_givenBlankBallotCount_returnCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.BallotCountConfig config = new CountingAuditEventTestObjectMother.BallotCountConfig(1);
		BallotCount ballotCount = CountingAuditEventTestObjectMother.ballotCount(config);
		JsonObject jsonObject = new BallotCountAuditDetails(ballotCount, false, false).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.ballotCountJsonObject(config));
	}

	@Test
	public void toJsonObject_givenBallotCount_returnCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.BallotCountConfig config = new CountingAuditEventTestObjectMother.BallotCountConfig(1, 2);
		BallotCount ballotCount = CountingAuditEventTestObjectMother.ballotCount(config);
		JsonObject jsonObject = new BallotCountAuditDetails(ballotCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.ballotCountJsonObject(config));
	}

	@Test
	public void toJsonObject_givenRejectedBallotCount_returnCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.BallotCountConfig config = new CountingAuditEventTestObjectMother.BallotCountConfig("BALLOT_REJECTION_ID", 1);
		BallotCount ballotCount = CountingAuditEventTestObjectMother.ballotCount(config);
		JsonObject jsonObject = new BallotCountAuditDetails(ballotCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.ballotCountJsonObject(config));
	}
}
