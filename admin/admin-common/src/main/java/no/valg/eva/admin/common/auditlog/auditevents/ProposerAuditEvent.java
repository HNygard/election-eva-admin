package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Proposer;

import org.joda.time.DateTime;

public class ProposerAuditEvent extends AuditEvent {
	private final Proposer proposer;
	private final Integer reorderFrom;
	private final Integer reorderTo;

	public ProposerAuditEvent(UserData userData, Proposer proposer, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.proposer = proposer;
		this.reorderFrom = null;
		this.reorderTo = null;
	}

	@SuppressWarnings("unused")
	public ProposerAuditEvent(UserData userData, Proposer proposer, Integer reorderFrom, Integer reorderTo, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.proposer = proposer;
		this.reorderFrom = reorderFrom;
		this.reorderTo = reorderTo;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		String contestName = getContestName();
		String partyId = getPartyId();

		builder.add("contestName", contestName);
		builder.add("partyId", partyId);

		if (proposer.isIdSet()) {
			builder.add("id", proposer.getId());
		} else {
			builder.addNull("id");
		}

		builder.add("function", getProposerRole());
		builder.add("firstName", proposer.getFirstName());
		builder.add("lastName", proposer.getLastName());
		builder.addDate("dateOfBirth", proposer.getDateOfBirth());
		builder.add("addressLine1", proposer.getAddressLine1());
		builder.add("postalCode", proposer.getPostalCode());
		builder.add("postTown", proposer.getPostTown());

		if (reorderFrom != null && reorderTo != null) {
			builder.add("reorderFrom", reorderFrom);
			builder.add("reorderTo", reorderTo);
		} else {
			builder.add("displayOrder", proposer.getDisplayOrder());
		}

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Proposer.class;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.DisplayOrderChanged.equals(auditEventType)) {
			return new Class[] { Proposer.class, Integer.class, Integer.class };
		}
		return new Class[] { Proposer.class };
	}

	private String getContestName() {
		try {
			if (proposer.getBallot() != null) {
				if (proposer.getBallot().getContest() != null) {
					return proposer.getBallot().getContest().getName();
				}
			}
			return null;
		} catch (Exception e) {
			return null; // in case of uninitialized proxies
		}
	}

	private String getPartyId() {
		try {
			if (proposer.getBallot() != null) {
				if (proposer.getBallot().getAffiliation() != null) {
					if (proposer.getBallot().getAffiliation().getParty() != null) {
						return proposer.getBallot().getAffiliation().getParty().getId();
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null; // in case of uninitialized proxies
		}
	}

	private String getProposerRole() {
		if (proposer.getProposerRole() == null) {
			return null;
		}
		return proposer.getProposerRole().getId();
	}
}
