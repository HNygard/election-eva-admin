package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Audit event for mass-approval of advance votings.
 */
public class UpdateAdvanceVotingsApprovedAuditEvent extends AuditEvent {
	private final Long pollingPlacePk;
	private final long municipalityPk;
	private final Long electionGroupPk;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final int votingNumberStart;
	private final int votingNumberEnd;
	private final int numberUpdated;

	public UpdateAdvanceVotingsApprovedAuditEvent(UserData userData, Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate,
												  LocalDate endDate, int votingNumberStart, int votingNumberEnd, int numberUpdated,
												  AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, DateTime.now(), crudType, Process.VOTING, outcome, detail);
		this.pollingPlacePk = pollingPlacePk;
		this.municipalityPk = municipalityPk;
		this.electionGroupPk = electionGroupPk;
		this.startDate = startDate;
		this.endDate = endDate;
		this.votingNumberStart = votingNumberStart;
		this.votingNumberEnd = votingNumberEnd;
		this.numberUpdated = numberUpdated;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("pollingPlacePk", pollingPlacePk);
		builder.add("municipalityPk", municipalityPk);
		builder.add("electionGroupPk", electionGroupPk);
		builder.addDate("startDate", startDate);
		builder.addDate("endDate", endDate);
		builder.add("votingNumberStart", votingNumberStart);
		builder.add("votingNumberEnd", votingNumberEnd);
		builder.add("numberUpdated", numberUpdated);

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Voting.class;
	}

	/**
	 * Arguments are limited to database primary keys:
	 * pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate, votingNumberStart, votingNumberEnd
	 * + return value int.
	 */
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.UpdateAll.equals(auditEventType)) {
			return new Class[]{Long.class, Long.TYPE, Long.class, LocalDate.class, LocalDate.class, Integer.TYPE, Integer.TYPE, Integer.TYPE};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
