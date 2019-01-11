package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.DeleteAll;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.DeletedAllInArea;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.DeletedAllWithoutArea;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.FullElectoralImportStarted;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.GenererValgkortgrunnlagJobbFerdig;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.GenererValgkortgrunnlagJobbStartet;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.DateTime;

public class ElectoralRollAuditEvent extends AuditEvent {

	private String fileName;
	private MvArea mvArea;
	private MvElection mvElection;

	public ElectoralRollAuditEvent(UserData userData, String fileName, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.ELECTORAL_ROLL, outcome, detail);
		this.fileName = fileName;
	}

	public ElectoralRollAuditEvent(UserData userData, MvElection mvElection, MvArea mvArea, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.ELECTORAL_ROLL, outcome, detail);
		this.mvArea = mvArea;
		this.mvElection = mvElection;
	}

	public ElectoralRollAuditEvent(UserData userData, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.ELECTORAL_ROLL, outcome, detail);
		this.fileName = null;
	}

	@Override
	public Class objectType() {
		return Voter.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		if (fileName != null) {
			builder.add("fileName", fileName);
		}
		if (mvArea != null) {
			builder.add("areaPath", mvArea.getAreaPath());
		}
		if (mvElection != null) {
			builder.add("electionPath", mvElection.getElectionPath());
		}
		
		return builder.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType.equals(DeleteAll) || auditEventType.equals(DeletedAllWithoutArea)) {
			return new Class[]{};
		} else if (auditEventType.equals(DeletedAllInArea)) {
			return new Class[] { MvElection.class, MvArea.class };
		} else if (auditEventType.equals(FullElectoralImportStarted)
			|| auditEventType.equals(GenererValgkortgrunnlagJobbStartet)
			|| auditEventType.equals(GenererValgkortgrunnlagJobbFerdig)) {
			return new Class[] { String.class };
		}
		return new Class[]{};
	}
}
