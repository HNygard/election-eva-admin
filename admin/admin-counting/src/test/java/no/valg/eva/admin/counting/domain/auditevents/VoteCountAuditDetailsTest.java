package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.finalVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.protocolVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.voteCount;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.voteCountJsonObject;
import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;

import no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;

public class VoteCountAuditDetailsTest {
	@Test
	public void toJsonObject_whenProtocolVoteCountWithEmergencySpecialCovers_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = protocolVoteCountConfig(null, 1, null);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, false, false).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}
	
	@Test
	public void toJsonObject_whenProtocolVoteCountWithForeignSpecialCovers_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = protocolVoteCountConfig(1, null, null);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, false, false).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenProtocolVoteCountWithEmergencySpecialCoversAndBallotsForOtherContests_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = protocolVoteCountConfig(null, 1, 2);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, false, false).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenFinalVoteCount_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = finalVoteCountConfig(SAVED, false, false);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenFinalVoteCountWithModifiedBallotsProcessed_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = finalVoteCountConfig(SAVED, true, false);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenApprovedFinalVoteCountWithModifiedBallotsProcessed_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = finalVoteCountConfig(APPROVED, true, false);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenApprovedFinalVoteCountWithRejectedBallotsProcessed_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = finalVoteCountConfig(APPROVED, false, true);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}

	@Test
	public void toJsonObject_whenApprovedFinalVoteCountWithModifiedAndRejectedBallotsProcessed_returnCorrectJsonObject() throws Exception {
		VoteCountConfig config = finalVoteCountConfig(APPROVED, true, true);
		VoteCount voteCount = voteCount(config);
		JsonObject jsonObject = new VoteCountAuditDetails(voteCount, true, true).toJsonObject();
		assertThat(jsonObject).isEqualTo(voteCountJsonObject(config));
	}
}
