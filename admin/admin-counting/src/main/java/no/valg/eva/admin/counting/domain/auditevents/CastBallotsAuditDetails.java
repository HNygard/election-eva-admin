package no.valg.eva.admin.counting.domain.auditevents;

import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.counting.domain.model.CastBallot;

public class CastBallotsAuditDetails {
	private CastBallotAuditDetails[] castBallotAuditDetailses;

	public CastBallotsAuditDetails(Set<CastBallot> castBallots) {
		this.castBallotAuditDetailses = new CastBallotAuditDetails[castBallots.size()];
		Iterator<CastBallot> castBallotIterator = castBallots.iterator();
		for (int i = 0; castBallotIterator.hasNext(); i++) {
			castBallotAuditDetailses[i] = new CastBallotAuditDetails(castBallotIterator.next());
		}
	}

	public JsonArray toJsonArray() {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (CastBallotAuditDetails castBallotAuditDetails : castBallotAuditDetailses) {
			arrayBuilder.add(castBallotAuditDetails.toJsonObject());
		}
		return arrayBuilder.build();
	}
}
