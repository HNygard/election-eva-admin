package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.json.JsonArray;

import no.valg.eva.admin.counting.domain.model.BallotCount;

import org.testng.annotations.Test;

public class BallotCountsAuditDetailsTest {
	@Test
	public void toJsonArray_givenBallotCounts_returnCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.BallotCountConfig[] configs = CountingAuditEventTestObjectMother.ballotCountConfigs();
		Set<BallotCount> ballotCounts = CountingAuditEventTestObjectMother.ballotCounts(configs);
		JsonArray jsonArray = new BallotCountsAuditDetails(ballotCounts, true, true).toJsonArray();
		assertThat(jsonArray).isEqualTo(CountingAuditEventTestObjectMother.ballotCountJsonArray(configs));
	}
}
