package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import org.joda.time.DateTime;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

public class ElectionGroupAuditEvent extends AuditEvent {

	private static final Process PROCESS = Process.CENTRAL_CONFIGURATION;
	private Boolean isRequiredProtocolCount;
	private long electionEventPk;
	private ElectionGroup electionGroup;
	private ElectionPath electionGroupPath;

	public ElectionGroupAuditEvent(UserData userData, Long electionEventPk, Boolean isRequiredProtocolCount, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, PROCESS, outcome, detail);
		this.isRequiredProtocolCount = isRequiredProtocolCount;
		this.electionEventPk = electionEventPk;
	}

	public ElectionGroupAuditEvent(UserData userData, ElectionGroup electionGroup, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(),
				Save.equals(auditEventType) ? (electionGroup.getElectionGroupRef() == null ? Create : Update) : auditEventType,
				PROCESS, outcome, detail);
		this.electionGroup = electionGroup;
	}

	public ElectionGroupAuditEvent(UserData userData, ElectionPath electionGroupPath, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, PROCESS, outcome, detail);
		this.electionGroupPath = electionGroupPath;
	}

	@Override
	public Class objectType() {
		return ElectionGroup.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		if (AuditEventTypes.PartialUpdate.equals(eventType())) {
			builder.add("isRequiredProtocolCount", isRequiredProtocolCount);
			builder.add("electionEventPk", electionEventPk);
		} else if (AuditEventTypes.Create.equals(eventType()) || AuditEventTypes.Update.equals(eventType())) {
			builder.add("parentElectionPath", electionGroup.getParentElectionPath().path());
			builder.add("id", electionGroup.getId());
			builder.add("name", electionGroup.getName());
			builder.add("electronicMarkoffs", electionGroup.isElectronicMarkoffs());
			builder.add("advanceVoteInBallotBox", electionGroup.isAdvanceVoteInBallotBox());
		} else if (AuditEventTypes.Delete.equals(eventType())) {
			builder.add("electionGroupPath", electionGroupPath.path());
		}

		return builder.toJson();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.PartialUpdate.equals(auditEventType)) {
			return new Class[] { Long.class, Boolean.class };
		} else if (AuditEventTypes.Delete.equals(auditEventType)) {
			return new Class[] { ElectionPath.class };
		}
		return new Class[] { ElectionGroup.class };
	}

}
