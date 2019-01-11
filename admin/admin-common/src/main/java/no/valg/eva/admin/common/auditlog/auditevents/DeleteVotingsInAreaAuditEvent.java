package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;

public class DeleteVotingsInAreaAuditEvent extends AuditEvent {
	private final MvElection mvElection;
	private final MvArea mvArea;
	private final Integer votingCategoryPk;

	public DeleteVotingsInAreaAuditEvent(UserData userData, MvElection mvElection, MvArea mvArea, Integer votingCategoryPk, AuditEventTypes crudType, Outcome outcome,
										 String detail) {
		super(userData, DateTime.now(), crudType, Process.VOTING, outcome, detail);
		this.mvElection = mvElection;
		this.mvArea = mvArea;
		this.votingCategoryPk = votingCategoryPk;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("election", mvElection.getElectionPath());
		builder.add("area", mvArea.getAreaPath());
		builder.add("votingCategoryPk", votingCategoryPk);

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Voting.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.DeletedAllInArea.equals(auditEventType)) {
			return new Class[]{MvElection.class, MvArea.class, Integer.class};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
