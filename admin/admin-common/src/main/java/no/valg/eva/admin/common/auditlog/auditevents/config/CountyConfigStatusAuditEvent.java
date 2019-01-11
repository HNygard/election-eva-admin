package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;

import org.joda.time.DateTime;

public class CountyConfigStatusAuditEvent extends AuditEvent {

	private final CountyConfigStatus countyConfigStatus;

	public CountyConfigStatusAuditEvent(UserData userData, CountyConfigStatus countyConfigStatus, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.countyConfigStatus = countyConfigStatus;
	}

	@Override
	public Class objectType() {
		return CountyConfigStatus.class;
	}

	@Override
	public String toJson() {
		return jonBuilder().toJson();
	}

	protected JsonBuilder jonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", countyConfigStatus.getCountyPath().path());
		objectBuilder.add("localeId", countyConfigStatus.getLocaleId().getId());
		objectBuilder.add("reportingUnitFylkesvalgstyre", countyConfigStatus.isReportingUnitFylkesvalgstyre());
		objectBuilder.add("language", countyConfigStatus.isLanguage());
		objectBuilder.add("listProposals", countyConfigStatus.isListProposals());
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { CountyConfigStatus.class };
	}
}
