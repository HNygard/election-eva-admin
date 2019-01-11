package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Party;

import org.joda.time.DateTime;

public class AffiliationAuditEvent extends AuditEvent {
	private final Affiliation affiliation;

	private final Integer reorderFrom;
	private final Integer reorderTo;

	public AffiliationAuditEvent(UserData userData, Affiliation affiliation, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.affiliation = affiliation;
		this.reorderFrom = null;
		this.reorderTo = null;
	}

	@SuppressWarnings("unused")
	public AffiliationAuditEvent(UserData userData, Affiliation affiliation, Integer reorderFrom, Integer reorderTo, AuditEventTypes crudType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.affiliation = affiliation;
		this.reorderFrom = reorderFrom;
		this.reorderTo = reorderTo;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("partyId", partyId());
		builder.add("contestName", contestName());

		if (AuditEventTypes.DisplayOrderChanged.equals(eventType())) {
			builder.add("reorderFrom", reorderFrom);
			builder.add("reorderTo", reorderTo);
		} else {
			builder.add("displayOrder", affiliation == null ? null : affiliation.getDisplayOrder());
		}

		return builder.toJson();
	}

	private String partyId() {
		if (affiliation == null) {
			return null;
		} else {
			Party party = affiliation.getParty();
			if (party != null) {
				return party.getId();
			}
			return "";
		}
	}

	private String contestName() {
		if (affiliation == null) {
			return null;
		} else {
			Ballot ballot = affiliation.getBallot();
			if (ballot != null && ballot.getContest() != null) {
				return ballot.getContest().getName();
			}
			return "";
		}
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public Integer getReorderTo() {
		return reorderTo;
	}

	public Integer getReorderFrom() {
		return reorderFrom;
	}

	@Override
	public Class objectType() {
		return Affiliation.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.DisplayOrderChanged.equals(auditEventType)) {
			return new Class[] { Affiliation.class, Integer.class, Integer.class };
		}
		return new Class[] { Affiliation.class };
	}
}
