package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class UpdateAdvanceVotingsApprovedAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	
	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		LocalDate startDate = new LocalDate(2000, 12, 24);
		LocalDate endDate = startDate.plusWeeks(1);

		UpdateAdvanceVotingsApprovedAuditEvent auditEvent = new UpdateAdvanceVotingsApprovedAuditEvent(objectMother.createUserData(), 1L, 1L, 1L, startDate, endDate,
				0, 1, 1, AuditEventTypes.UpdateAll, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("pollingPlacePk", 1))
				.assertThat("$", hasEntry("municipalityPk", 1))
				.assertThat("$", hasEntry("electionGroupPk", 1))
				.assertThat("$", hasEntry("startDate", "2000-12-24"))
				.assertThat("$", hasEntry("endDate", "2000-12-31"))
				.assertThat("$", hasEntry("votingNumberStart", 0))
				.assertThat("$", hasEntry("votingNumberEnd", 1))
				.assertThat("$", hasEntry("numberUpdated", 1));
	}
	

}
