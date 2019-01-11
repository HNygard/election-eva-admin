package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;

import org.joda.time.DateTime;

public class ElectionVoteCountCategoryAuditEvent extends AuditEvent {

	private static final Process PROCESS = Process.CENTRAL_CONFIGURATION;
	private final List<ElectionVoteCountCategory> electionVoteCountCategories;

	public ElectionVoteCountCategoryAuditEvent(UserData userData, List<ElectionVoteCountCategory> electionVoteCountCategories, AuditEventTypes auditEventTypes,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventTypes, PROCESS, outcome, detail);
		this.electionVoteCountCategories = electionVoteCountCategories;
	}

	@Override
	public Class objectType() {
		return ElectionVoteCountCategory.class;
	}

	@Override
	public String toJson() {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		for (ElectionVoteCountCategory electionVoteCountCategory : electionVoteCountCategories) {
			JsonBuilder builder = new JsonBuilder();
			builder.add("id", electionVoteCountCategory.getVoteCountCategory().getId());
			builder.add("countingMode", electionVoteCountCategory.getCountingMode().toString());
			builder.add("countCategoryEditable", electionVoteCountCategory.isCountCategoryEditable());
			builder.add("countCategoryEnabled", electionVoteCountCategory.isCountCategoryEnabled());
			builder.add("technicalPollingDistrictCountConfigurable", electionVoteCountCategory.isTechnicalPollingDistrictCountConfigurable());
			builder.add("specialCover", electionVoteCountCategory.isSpecialCover());
			arrayBuilder.add(builder.asJsonObject());
		}

		return arrayBuilder.build().toString();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { List.class };
	}
}
