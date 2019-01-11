package no.valg.eva.admin.common.auditlog.auditevents;

import javax.json.Json;

import org.joda.time.DateTime;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;

public abstract class CountyAuditEvent extends AuditEvent {
	private final County county;

	public CountyAuditEvent(UserData userData, County county, AuditEventTypes auditEventType, Process process, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, process, outcome, detail);
		this.county = county;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { County.class };
	}

	@Override
	public String toJson() {
		JsonBuilder jsonBuilder = new JsonBuilder();
		addBasicData(jsonBuilder);
		addLocale(jsonBuilder);
		addTranslatedStatusName(jsonBuilder);
		return jsonBuilder.toJson();
	}

	private void addLocale(JsonBuilder jsonBuilder) {
		jsonBuilder.add("locale", county.getLocale().getId());
	}

	private void addBasicData(JsonBuilder jsonBuilder) {
		jsonBuilder.add("path", county.areaPath().path());
		jsonBuilder.add("name", county.getName());
	}

	private void addTranslatedStatusName(JsonBuilder jsonBuilder) {
		CountyStatus countyStatus = county.getCountyStatus();
		String statusName = CountyStatusEnum.fromId(countyStatus.getId()).name();
		jsonBuilder
				.add("countyStatus", Json.createObjectBuilder()
						.add("id", countyStatus.getId())
						.add("name", statusName));
	}

	@Override
	public Class objectType() {
		return County.class;
	}

}
