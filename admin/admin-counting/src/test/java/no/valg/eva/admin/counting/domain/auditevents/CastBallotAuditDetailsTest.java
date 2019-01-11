package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.UNMODIFIED;
import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;

import no.valg.eva.admin.counting.domain.model.CastBallot;

import org.testng.annotations.Test;

public class CastBallotAuditDetailsTest {
	@Test
	public void toJsonObject_givenRejectedCastBallotWithoutBinaryData_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CastBallotConfig config = new CountingAuditEventTestObjectMother.CastBallotConfig(false);
		CastBallot castBallot = CountingAuditEventTestObjectMother.castBallot(config);
		JsonObject jsonObject = new CastBallotAuditDetails(castBallot).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.castBallotJsonObject(config));
	}

	@Test
	public void toJsonObject_givenRejectedCastBallotWithBinaryData_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CastBallotConfig config = new CountingAuditEventTestObjectMother.CastBallotConfig(true);
		CastBallot castBallot = CountingAuditEventTestObjectMother.castBallot(config);
		JsonObject jsonObject = new CastBallotAuditDetails(castBallot).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.castBallotJsonObject(config));
	}

	@Test
	public void toJsonObject_givenModifiedCastBallot_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CastBallotConfig config = new CountingAuditEventTestObjectMother.CastBallotConfig(MODIFIED,
				CountingAuditEventTestObjectMother.candidateVoteConfigs());
		CastBallot castBallot = CountingAuditEventTestObjectMother.castBallot(config);
		JsonObject jsonObject = new CastBallotAuditDetails(castBallot).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.castBallotJsonObject(config));
	}

	@Test
	public void toJsonObject_givenUnmodifiedCastBallot_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CastBallotConfig config = new CountingAuditEventTestObjectMother.CastBallotConfig(UNMODIFIED);
		CastBallot castBallot = CountingAuditEventTestObjectMother.castBallot(config);
		JsonObject jsonObject = new CastBallotAuditDetails(castBallot).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.castBallotJsonObject(config));
	}
}
