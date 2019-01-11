package no.valg.eva.admin.common.auditlog.auditevents.counting;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.Candidate;

import org.joda.time.DateTime;

public class IgnoredWriteInCandidateVoteAuditEvent extends AuditEvent {
	private final String candidateId;

	public IgnoredWriteInCandidateVoteAuditEvent(UserData userData, String candidateId, String detail) {
		super(userData, new DateTime(), AuditEventTypes.CandidateVoteSkipped, Process.COUNTING, Outcome.Success, detail);
		this.candidateId = candidateId;
	}

	@Override
	public Class objectType() {
		return Candidate.class;
	}

	@Override
	public String toJson() {
		return new JsonBuilder().add("candidateId", candidateId).toJson();
	}
}
