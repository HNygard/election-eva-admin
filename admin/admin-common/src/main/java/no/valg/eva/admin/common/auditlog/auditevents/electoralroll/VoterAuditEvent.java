package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.DateTime;

public class VoterAuditEvent extends AuditEvent {

	private final Voter voter;

	public VoterAuditEvent(UserData userData, Voter voter, AuditEventTypes auditEventTypes, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventTypes, Process.ELECTORAL_ROLL, outcome, detail);
		this.voter = voter;
	}

	@Override
	public Class objectType() {
		return Voter.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("id", voter.getId());
		builder.add("nameLine", voter.getNameLine());
		builder.add("addressLine1", voter.getAddressLine1());
		if (voter.getElectoralRollAreaPath() == null) {
			builder.add("countryId", voter.getCountryId());
			builder.add("countyId", voter.getCountyId());
			builder.add("municipalityId", voter.getMunicipalityId());
			builder.add("boroughId", voter.getBoroughId());
			builder.add("pollingDistrictId", voter.getPollingDistrictId());
		} else {
			builder.add("areaPath", voter.getElectoralRollAreaPath().path());
		}
		builder.add("mailingAddressSpecified", voter.isMailingAddressSpecified());
		builder.add("eligible", voter.isEligible());
		builder.add("approved", voter.isApproved());

		if (Update.equals(eventType())) {
			builder.add("changeType", voter.getEndringstype().toString());
		}
		builder.add("aarsakskode", voter.getAarsakskode());

		return builder.toJson();

	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Voter.class };
	}
}
