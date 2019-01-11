package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

/**
 * Audit event for centrally registered votings. Lazy-loads polling place from voting.
 */
public class CentrallyRegisteredVotingAuditEvent extends VotingAuditEvent {
	public CentrallyRegisteredVotingAuditEvent(UserData userData, Voter voter, Voting voting, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, pollingPlace(voting), voter, voting, auditEventType, outcome, detail);
	}

	private static PollingPlace pollingPlace(Voting voting) {
		if (voting != null) {
			return voting.getPollingPlace();
		} 
		return null;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Create.equals(auditEventType)) {
			return new Class[]{Voter.class, Voting.class};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
