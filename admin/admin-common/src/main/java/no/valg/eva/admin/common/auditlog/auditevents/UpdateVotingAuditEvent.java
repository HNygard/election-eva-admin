package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

/**
 * Audit event for updates to votings. Lazy-loads Voter and PollingPlace from Voting, so must only be passed JPA-attached (or pre-populated) Votings.
 */
public class UpdateVotingAuditEvent extends VotingAuditEvent {

	public UpdateVotingAuditEvent(UserData userData, Voting voting, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, pollingPlace(voting), voter(voting), voting, auditEventType, outcome, detail);
	}

	private static PollingPlace pollingPlace(Voting voting) {
		if (voting != null) {
			return voting.getPollingPlace();
		}
		return null;
	}

	private static Voter voter(Voting voting) {
		if (voting != null) {
			return voting.getVoter();
		}
		return null;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Update.equals(auditEventType)) {
			return new Class[]{Voting.class};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
