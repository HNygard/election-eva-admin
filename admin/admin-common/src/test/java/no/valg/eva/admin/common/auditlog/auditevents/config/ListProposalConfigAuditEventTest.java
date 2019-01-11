package no.valg.eva.admin.common.auditlog.auditevents.config;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ListProposalConfigAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ContestListProposalData data = getContestListProposalData();
		ListProposalConfig config = new ListProposalConfig(AreaPath.from("111111.22.33"), 1L, "Name", true, true, data, 0);
		ListProposalConfigAuditEvent event = event(AuditEventTypes.Save, config);

		assertThat(event.objectType()).isSameAs(ListProposalConfig.class);
	}

	@Test
	public void toJson() throws Exception {
		ContestListProposalData data = getContestListProposalData();

		ListProposalConfig config = new ListProposalConfig(AreaPath.from("111111.22.33"), 1L, "Name", true, true, data, 0);
		config.getChildren().add(new ListProposalConfig(AreaPath.from("111111.22.33.4444"), 1L, "Name", true, true, data, 0));

		ListProposalConfigAuditEvent event = event(AuditEventTypes.Save, config);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", config.getAreaPath().path()))
				.assertThat("$", hasEntry("contestName", config.getContestName()))
				.assertThat("$", hasEntry("maxCandidates", config.getContestListProposalData().getMaxCandidates()))
				.assertThat("$", hasEntry("minCandidates", config.getContestListProposalData().getMinCandidates()))
				.assertThat("$", hasEntry("maxWriteIn", config.getContestListProposalData().getMaxWriteIn()))
				.assertThat("$", hasEntry("numberOfPositions", config.getContestListProposalData().getNumberOfPositions()))
				.assertThat("$", hasEntry("maxRenumber", config.getContestListProposalData().getMaxRenumber()))
				.assertThat("$", hasEntry("minProposersNewParty", config.getContestListProposalData().getMinProposersNewParty()))
				.assertThat("$", hasEntry("minProposersOldParty", config.getContestListProposalData().getMinProposersOldParty()))
				.assertThat("$.children[*]", collectionWithSize(equalTo(1)))
				.assertThat("$.children[*].path", containsInAnyOrder(config.getChildren().get(0).getAreaPath().path()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ListProposalConfigAuditEvent.class;
	}

	private ListProposalConfigAuditEvent event(AuditEventTypes eventType, ListProposalConfig config) {
		return new ListProposalConfigAuditEvent(createMock(UserData.class), config, eventType, Outcome.Success, "");
	}

	private ContestListProposalData getContestListProposalData() {
		return new ContestListProposalData(new Election(ElectionPath.from("111111.22.33")), 1, 2, 3, 4, 5, 6, 7);
	}

}

