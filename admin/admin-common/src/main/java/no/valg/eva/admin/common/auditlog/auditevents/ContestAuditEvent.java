package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.Contest;

import org.joda.time.DateTime;

public class ContestAuditEvent extends AuditEvent {
	private Contest contest;
	private ElectionPath contestPath;

	public ContestAuditEvent(UserData userData, Contest contest, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(),
				crudType == Save && contest.getPk() == null ? Create : Update,
				crudType == Save && contest.getListProposalData().getElection().isSingleArea() ? LOCAL_CONFIGURATION : CENTRAL_CONFIGURATION,
				outcome, detail);
		this.contest = contest;
	}

	public ContestAuditEvent(UserData userData, ElectionPath contestPath, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, CENTRAL_CONFIGURATION, outcome, detail);
		this.contestPath = contestPath;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		if (AuditEventTypes.Delete.equals(eventType())) {
			builder.add("contestPath", contestPath.path());
		} else {
			builder.add("contestPath", contest.getElectionPath().path());
			builder.add("name", contest.getName());
			builder.add("penultimateRecount", contest.getPenultimateRecount());
			if (contest.getEndDateOfBirth() != null) {
				builder.add("endDateOfBirth", contest.getEndDateOfBirth().toString());
			}
			builder.add("maxCandidates", contest.getListProposalData().getMaxCandidates());
			builder.add("minCandidates", contest.getListProposalData().getMinCandidates());
			builder.add("maxWriteIn", contest.getListProposalData().getMaxWriteIn());
			builder.add("numberOfPositions", contest.getListProposalData().getNumberOfPositions());
			builder.add("maxRenumber", contest.getListProposalData().getMaxRenumber());
			builder.add("minProposersNewParty", contest.getListProposalData().getMinProposersNewParty());
			builder.add("minProposersOldParty", contest.getListProposalData().getMinProposersOldParty());
		}
		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Contest.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == Delete) {
			return new Class[] { ElectionPath.class };
		}
		return new Class[] { Contest.class };
	}
}
