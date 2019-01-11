package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;

import org.joda.time.DateTime;

public class MunicipalityConfigStatusAuditEvent extends AuditEvent {

	private final MunicipalityConfigStatus municipalityConfigStatus;

	public MunicipalityConfigStatusAuditEvent(UserData userData, MunicipalityConfigStatus municipalityConfigStatus, AuditEventTypes crudType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.municipalityConfigStatus = municipalityConfigStatus;
	}

	@Override
	public Class objectType() {
		return MunicipalityConfigStatus.class;
	}

	@Override
	public String toJson() {
		return jsonBuilder().toJson();
	}

	protected JsonBuilder jsonBuilder() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("path", municipalityConfigStatus.getMunicipalityPath().path());
		objectBuilder.add("localeId", municipalityConfigStatus.getLocaleId().getId());
		objectBuilder.add("listProposals", municipalityConfigStatus.isListProposals());
		objectBuilder.add("language", municipalityConfigStatus.isLanguage());
		objectBuilder.add("advancePollingPlaces", municipalityConfigStatus.isAdvancePollingPlaces());
		objectBuilder.add("countCategories", municipalityConfigStatus.isCountCategories());
		objectBuilder.add("electionCard", municipalityConfigStatus.isElectionCard());
		objectBuilder.add("electionPollingPlaces", municipalityConfigStatus.isElectionPollingPlaces());
		objectBuilder.add("reportingUnitStemmestyre", municipalityConfigStatus.isReportingUnitStemmestyre());
		objectBuilder.add("reportingUnitValgstyre", municipalityConfigStatus.isReportingUnitValgstyre());
		objectBuilder.add("pollingDistricts", municipalityConfigStatus.isPollingDistricts());
		objectBuilder.add("techPollingDistricts", municipalityConfigStatus.isTechPollingDistricts());
		objectBuilder.add("electronicMarkoffs", municipalityConfigStatus.isElectronicMarkoffs());
		objectBuilder.add("useElectronicMarkoffs", municipalityConfigStatus.isUseElectronicMarkoffs());
		return objectBuilder;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { MunicipalityConfigStatus.class };
	}
}
