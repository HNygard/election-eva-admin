package no.valg.eva.admin.common.auditlog.auditevents.counting;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

import org.joda.time.DateTime;

public class SaveUploadedCountAuditEvent extends AuditEvent {
	private final String fileName;
	private final int fileLength;
	private final String accessPath;

	public SaveUploadedCountAuditEvent(
			UserData userData, byte[] file, String fileName, Jobbkategori category, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.COUNTING, outcome, detail);
		this.fileName = fileName;
		this.fileLength = file.length;
		this.accessPath = category.toAccessPath();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { byte[].class, String.class, Jobbkategori.class };
	}

	@Override
	public Class objectType() {
		return null;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("fileName", fileName);
		builder.add("fileLength", fileLength);
		builder.add("accessPath", accessPath);
		return builder.toJson();
	}
}
