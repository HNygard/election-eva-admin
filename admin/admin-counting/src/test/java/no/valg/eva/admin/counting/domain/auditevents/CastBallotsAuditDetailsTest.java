package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.json.JsonArray;

import no.valg.eva.admin.counting.domain.model.CastBallot;

import org.testng.annotations.Test;

public class CastBallotsAuditDetailsTest {
	@Test
	public void toJsonArray_givenCastBallots_returnCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CastBallotConfig[] configs = CountingAuditEventTestObjectMother.castBallotConfigs();
		Set<CastBallot> castBallots = CountingAuditEventTestObjectMother.castBallots(configs);
		JsonArray jsonArray = new CastBallotsAuditDetails(castBallots).toJsonArray();
		assertThat(jsonArray).isEqualTo(CountingAuditEventTestObjectMother.castBallotJsonArray(configs));
	}
}
