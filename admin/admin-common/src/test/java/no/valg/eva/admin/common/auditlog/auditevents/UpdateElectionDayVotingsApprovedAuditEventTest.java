package no.valg.eva.admin.common.auditlog.auditevents;


import static com.jayway.jsonassert.JsonAssert.emptyCollection;
import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.testng.annotations.Test;

public class UpdateElectionDayVotingsApprovedAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		UpdateElectionDayVotingsApprovedAuditEvent auditEvent = buildAuditEvent(new String[]{});

		assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("municipalityPk", 1))
				.assertThat("$", hasEntry("electionGroupPk", 1))
				.assertThat("$", hasEntry("votingNumberStart", 0))
				.assertThat("$", hasEntry("votingNumberEnd", 1))
				.assertThat("$", hasEntry("numberUpdated", 1));
	}

	@Test
	public void toJson_withNoVotingCategories_rendersEmptyArray() throws Exception {
		UpdateElectionDayVotingsApprovedAuditEvent auditEvent = buildAuditEvent(new String[]{});

		with(auditEvent.toJson()).assertThat("$.votingCategories", emptyCollection());
	}

	@Test
	public void toJson_withVotingCategories_rendersArray() throws Exception {
		UpdateElectionDayVotingsApprovedAuditEvent auditEvent = buildAuditEvent(new String[]{"FI", "FE"});

		with(auditEvent.toJson()).assertThat("$.votingCategories", contains("FI", "FE"));
	}

	private UpdateElectionDayVotingsApprovedAuditEvent buildAuditEvent(String[] votingCategories) {
		return new UpdateElectionDayVotingsApprovedAuditEvent(objectMother.createUserData(), 1L, 1L, 0, 1, votingCategories, 1,
				AuditEventTypes.UpdateAll, Outcome.Success, "details, details");
	}

}
