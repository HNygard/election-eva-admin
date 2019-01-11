package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;

import org.joda.time.DateTime;

public class BallotAuditEvent extends AuditEvent {
	private final Ballot ballot;

	public BallotAuditEvent(UserData userData, Ballot ballot, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.ballot = ballot;
	}

	@SuppressWarnings("unused")
	public BallotAuditEvent(UserData userData, Affiliation affiliation, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.ballot = affiliation.getBallot();
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("partyId", ballot.getAffiliation().getParty().getId());
		builder.add("contestName", ballot.getContest().getName());
		builder.add("status", ballot.getBallotStatus().getBallotStatusValue().name());

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Ballot.class;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Delete.equals(auditEventType)) {
			return new Class[] { Ballot.class };
		} else if (AuditEventTypes.StatusChanged.equals(auditEventType)) {
			return new Class[] { Affiliation.class };
		} else {
			throw new UnsupportedOperationException(auditEventType.name());
		}
	}
}
