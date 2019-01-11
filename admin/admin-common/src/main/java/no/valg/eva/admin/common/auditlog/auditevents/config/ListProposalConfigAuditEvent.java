package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;

import org.joda.time.DateTime;

public class ListProposalConfigAuditEvent extends AuditEvent {

	private final ListProposalConfig listProposalConfig;

	public ListProposalConfigAuditEvent(UserData userData, ListProposalConfig listProposalConfig, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.listProposalConfig = listProposalConfig;
	}

	@Override
	public Class objectType() {
		return ListProposalConfig.class;
	}

	@Override
	public String toJson() {
		JsonBuilder objectBuilder = new JsonBuilder();
		add(objectBuilder, listProposalConfig);
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (ListProposalConfig child : listProposalConfig.getChildren()) {
			JsonBuilder childBuilder = new JsonBuilder();
			add(childBuilder, child);
			arrayBuilder.add(childBuilder.asJsonObject());
		}
		objectBuilder.add("children", arrayBuilder.build());
		return objectBuilder.toJson();
	}

	private void add(JsonBuilder builder, ListProposalConfig config) {
		builder.add("path", config.getAreaPath().path());
		builder.add("contestPk", config.getContestPk());
		builder.add("contestName", config.getContestName());
		builder.add("maxCandidates", config.getContestListProposalData().getMaxCandidates());
		builder.add("minCandidates", config.getContestListProposalData().getMinCandidates());
		builder.add("maxWriteIn", config.getContestListProposalData().getMaxWriteIn());
		builder.add("numberOfPositions", config.getContestListProposalData().getNumberOfPositions());
		builder.add("maxRenumber", config.getContestListProposalData().getMaxRenumber());
		builder.add("minProposersNewParty", config.getContestListProposalData().getMinProposersNewParty());
		builder.add("minProposersOldParty", config.getContestListProposalData().getMinProposersOldParty());
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ListProposalConfig.class };
	}
}
