package no.valg.eva.admin.counting.domain.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.configuration.domain.model.Contest;

class ContestAuditDetails {
	private String electionPath;
	private String name;

	public ContestAuditDetails(Contest contest) {
		electionPath = contest.electionPath().path();
		name = contest.getName();
	}

	public JsonObject toJsonObject() {
		return new JsonBuilder()
				.add("electionPath", electionPath)
				.add("name", name)
				.asJsonObject();
	}
}
