package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;

public class UpdateElectionDayVotingsApprovedAuditEvent extends AuditEvent {

	private final long municipalityPk;
	private final Long electionGroupPk;
	private final int votingNumberStart;
	private final int votingNumberEnd;
	private final List<String> votingCategories;
	private final int numberUpdated;

	public UpdateElectionDayVotingsApprovedAuditEvent(UserData userData, long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd,
													  String[] votingCategories, int numberUpdated, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, DateTime.now(), crudType, Process.VOTING, outcome, detail);
		this.municipalityPk = municipalityPk;
		this.electionGroupPk = electionGroupPk;
		this.votingNumberStart = votingNumberStart;
		this.votingNumberEnd = votingNumberEnd;
		this.votingCategories = votingCategories != null ? Arrays.asList(votingCategories) : new ArrayList<String>();
		this.numberUpdated = numberUpdated;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("municipalityPk", municipalityPk);
		builder.add("electionGroupPk", electionGroupPk);
		builder.add("votingNumberStart", votingNumberStart);
		builder.add("votingNumberEnd", votingNumberEnd);
		builder.addStringArray("votingCategories", votingCategories);
		builder.add("numberUpdated", numberUpdated);

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Voting.class;
	}

	/**
	 * Arguments are limited to database primary keys:
	 * municipalityPk, electionGroupPk, votingNumberStart, votingNumberEnd, votingCats
	 * + return value int.
	 */
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.UpdateAll.equals(auditEventType)) {
			return new Class[]{Long.TYPE, Long.class, Integer.TYPE, Integer.TYPE, String[].class, Integer.TYPE};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
