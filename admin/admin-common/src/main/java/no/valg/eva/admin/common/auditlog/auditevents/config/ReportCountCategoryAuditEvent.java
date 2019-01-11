package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;

import org.joda.time.DateTime;

public class ReportCountCategoryAuditEvent extends AuditEvent {

	private final AreaPath areaPath;
	private final List<ReportCountCategory> categories;

	public ReportCountCategoryAuditEvent(UserData userData, AreaPath areaPath, List<ReportCountCategory> categories, AuditEventTypes crudType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.areaPath = areaPath;
		this.categories = categories;
	}

	@Override
	public String toJson() {
		JsonBuilder jsonBuilder = new JsonBuilder();
		jsonBuilder.add("path", areaPath.path());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (ReportCountCategory category : categories) {
			JsonBuilder rccBuilder = new JsonBuilder();
			rccBuilder.add("voteCountCategoryId", category.getCategory().getId());
			rccBuilder.add("countingMode", category.getCountingMode().name());
			arrayBuilder.add(rccBuilder.asJsonObject());
		}
		jsonBuilder.add("reportCountCategories", arrayBuilder.build());

		return jsonBuilder.toJson();
	}

	@Override
	public Class objectType() {
		return AreaPath.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { AreaPath.class, List.class };
	}
}
