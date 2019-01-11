package no.valg.eva.admin.common.auditlog.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;

import org.joda.time.LocalDate;

public class ManualVotingAuditDetails {
	private final LocalDate electionDay;
	private final int votings;

	public ManualVotingAuditDetails(LocalDate electionDay, int votings) {
		this.electionDay = electionDay;
		this.votings = votings;
	}

	public JsonObject toJsonObject() {
		return new JsonBuilder()
				.add("electionDay", electionDay.toString())
				.add("votings", votings)
				.asJsonObject();
	}
}
