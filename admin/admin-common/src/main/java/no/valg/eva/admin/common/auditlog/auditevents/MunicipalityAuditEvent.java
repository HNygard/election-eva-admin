package no.valg.eva.admin.common.auditlog.auditevents;

import javax.json.Json;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;

import org.joda.time.DateTime;

public abstract class MunicipalityAuditEvent extends AuditEvent {
	private final Municipality municipality;

	public MunicipalityAuditEvent(UserData userData, Municipality municipality, AuditEventTypes auditEventType, Process process, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), auditEventType, process, outcome, detail);
		this.municipality = municipality;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Municipality.class };
	}

	@Override
	public String toJson() {
		JsonBuilder jsonBuilder = new JsonBuilder();
		addBasicData(jsonBuilder);
		addLocale(jsonBuilder);
		addFlags(jsonBuilder);
		addTranslatedStatusName(jsonBuilder);
		return jsonBuilder.toJson();
	}

	private void addLocale(JsonBuilder jsonBuilder) {
		jsonBuilder.add("locale", municipality.getLocale().getId());
	}

	private void addFlags(JsonBuilder jsonBuilder) {
		jsonBuilder.add("electronicMarkoffs", municipality.isElectronicMarkoffs());
		jsonBuilder.add("requiredProtocolCount", municipality.isRequiredProtocolCount());
		jsonBuilder.add("technicalPollingDistrictsAllowed", municipality.isTechnicalPollingDistrictsAllowed());
	}

	private void addBasicData(JsonBuilder jsonBuilder) {
		jsonBuilder.add("id", municipality.getId());
		jsonBuilder.add("name", municipality.getName());
	}

	private void addTranslatedStatusName(JsonBuilder jsonBuilder) {
		MunicipalityStatus municipalityStatus = municipality.getMunicipalityStatus();
		String translatedStatusName = MunicipalityStatusEnum.fromId(municipalityStatus.getId()).name();
		jsonBuilder
				.add("municipalityStatus", Json.createObjectBuilder()
						.add("id", municipalityStatus.getId())
						.add("name", translatedStatusName));
	}

	@Override
	public Class objectType() {
		return Municipality.class;
	}

}
