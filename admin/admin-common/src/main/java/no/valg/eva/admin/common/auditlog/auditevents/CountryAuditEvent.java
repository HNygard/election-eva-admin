package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.CENTRAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Country;

import org.joda.time.DateTime;

public class CountryAuditEvent extends AuditEvent {
	private final Country country;

	public CountryAuditEvent(UserData userData, Country country, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, CENTRAL_CONFIGURATION, outcome, detail);
		this.country = country;
	}

	@Override
	public Class objectType() {
		return Country.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("path", country.areaPath().path());
		builder.add("name", country.getName());
		return builder.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Country.class };
	}

}
