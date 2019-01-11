package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.Objects;

import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;

/**
 * Audit event for markoff events. Takes care to only access eager loaded objects, to avoid impacting performance.
 */
public class VotingAuditEvent extends AuditEvent {
	private final Voter voter;
	private final PollingPlace pollingPlace;
	private final Voting voting;

	public VotingAuditEvent(UserData userData, PollingPlace pollingPlace, Voter voter, Voting voting, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.VOTING, outcome, detail);
		this.voter = voter;
		this.pollingPlace = pollingPlace;
		this.voting = voting;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		addStringElementToJson(builder, "voterId", voter, this::nonNullAndNotFictitious, Voter::getId);
		addStringElementToJson(builder, "votingCategory", voting, Objects::nonNull, v -> v.getVotingCategory().getId());
		addIntegerElementToJson(builder, "votingNumber", voting, Objects::nonNull, Voting::getVotingNumber);
		addStringElementToJson(builder, "electoralRollArea", voting, Objects::nonNull, v -> v.getMvArea().getAreaPath());
		addStringElementToJson(builder, "pollingPlaceId", pollingPlace, Objects::nonNull, PollingPlace::getId);
		addStringElementToJson(builder, "pollingPlaceName", pollingPlace, Objects::nonNull, PollingPlace::getName);
		if (voting != null && voting.isAdvanceVoting()) {
			builder.add("lateValidation", voting.isLateValidation());
		}
		builder.add("approved", voting != null && voting.isApproved());
		addStringElementToJson(builder, "rejectionCategory", voting, this::nonNullAndNoVotingRejection, v -> v.getVotingRejection().getId());
		addLongElementToJson(builder, "votingPk", voting, Objects::nonNull, BaseEntity::getPk);
		addDateTimeElementToJson(builder, "castTime", voting, Objects::nonNull, Voting::getCastTimestamp);
		addDateTimeElementToJson(builder, "receivedTime", voting, Objects::nonNull, Voting::getReceivedTimestamp);
		addDateTimeElementToJson(builder, "validatedTime", voting, Objects::nonNull, Voting::getValidationTimestamp);

		return builder.toJson();
	}

	private boolean nonNullAndNotFictitious(Voter voter) {
		return voter != null && !voter.isFictitious();
	}

	private boolean nonNullAndNoVotingRejection(Voting voting) {
		return voting != null && voting.getVotingRejection() != null;
	}

	@Override
	public Class objectType() {
		return Voting.class;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Create.equals(auditEventType)) {
			return new Class[]{PollingPlace.class, Voter.class, Voting.class};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
