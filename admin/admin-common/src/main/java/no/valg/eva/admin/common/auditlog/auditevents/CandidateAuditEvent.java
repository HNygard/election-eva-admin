package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Candidate;

import org.joda.time.DateTime;

public class CandidateAuditEvent extends AuditEvent {
	private final Candidate candidate;
	private final List<Candidate> candidateList;
	private final Integer reorderFrom;
	private final Integer reorderTo;

	public CandidateAuditEvent(UserData userData, Candidate candidate, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.candidate = candidate;
		this.candidateList = null;
		this.reorderFrom = null;
		this.reorderTo = null;
	}

	public CandidateAuditEvent(UserData userData, Candidate candidate, Integer reorderFrom, Integer reorderTo, AuditEventTypes auditEventType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.candidate = candidate;
		this.candidateList = null;
		this.reorderFrom = reorderFrom;
		this.reorderTo = reorderTo;
	}

	public CandidateAuditEvent(UserData userData, List<Candidate> candidateList, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.candidate = null;
		this.candidateList = candidateList;
		this.reorderFrom = null;
		this.reorderTo = null;
	}

	@Override
	public String toJson() {
		CandidateJsonBuilder candidateJsonBuilder = new CandidateJsonBuilder();
		if (AuditEventTypes.DeleteAll.equals(eventType()) || AuditEventTypes.CreateAll.equals(eventType())) {
			return candidateJsonBuilder.toJson(candidateList);
		} else if (AuditEventTypes.DisplayOrderChanged.equals(eventType())) {
			return candidateJsonBuilder.toJson(candidate, reorderFrom, reorderTo);
		} else {
			return candidateJsonBuilder.toJson(candidate);
		}
	}

	@Override
	public Class objectType() {
		return Candidate.class;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.DisplayOrderChanged.equals(auditEventType)) {
			return new Class[] { Candidate.class, Integer.class, Integer.class };
		}
		if (AuditEventTypes.DeleteAll.equals(auditEventType)
				|| AuditEventTypes.CreateAll.equals(auditEventType)) {
			return new Class[] { List.class };
		}
		return new Class[] { Candidate.class };
	}
}
