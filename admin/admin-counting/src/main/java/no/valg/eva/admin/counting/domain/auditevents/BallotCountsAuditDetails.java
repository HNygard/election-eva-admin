package no.valg.eva.admin.counting.domain.auditevents;

import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.counting.domain.model.BallotCount;

public class BallotCountsAuditDetails {
	private BallotCountAuditDetails[] ballotCountAuditDetailses;

	public BallotCountsAuditDetails(Set<BallotCount> ballotCountSet, boolean splitCount, boolean includeCastBallots) {
		ballotCountAuditDetailses = new BallotCountAuditDetails[ballotCountSet.size()];
		Iterator<BallotCount> ballotCountIterator = ballotCountSet.iterator();
		for (int i = 0; ballotCountIterator.hasNext(); i++) {
			ballotCountAuditDetailses[i] = new BallotCountAuditDetails(ballotCountIterator.next(), splitCount, includeCastBallots);
		}
	}

	public JsonArray toJsonArray() {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (BallotCountAuditDetails ballotCountAuditDetails : ballotCountAuditDetailses) {
			arrayBuilder.add(ballotCountAuditDetails.toJsonObject());
		}
		return arrayBuilder.build();
	}
}
