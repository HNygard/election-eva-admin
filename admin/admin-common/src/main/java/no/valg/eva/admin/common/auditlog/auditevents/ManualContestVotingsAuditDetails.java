package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

public class ManualContestVotingsAuditDetails {
	public static final ThreadLocal<ManualContestVotingsAuditDetails> THREAD_LOCAL = new ThreadLocal<>();

	private final ManualVotingAuditDetails[] manualVotingAuditDetailses;

	public ManualContestVotingsAuditDetails(List<ManualVotingAuditDetails> manualContestVotings) {
		this.manualVotingAuditDetailses = new ManualVotingAuditDetails[manualContestVotings.size()];
		Iterator<ManualVotingAuditDetails> manualContestVotingIterator = manualContestVotings.iterator();
		for (int i = 0; manualContestVotingIterator.hasNext(); i++) {
            this.manualVotingAuditDetailses[i] = manualContestVotingIterator.next();
		}
	}

	public JsonArray toJsonArray() {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (ManualVotingAuditDetails manualVotingAuditDetails : manualVotingAuditDetailses) {
			arrayBuilder.add(manualVotingAuditDetails.toJsonObject());
		}
		return arrayBuilder.build();
	}
	
}
