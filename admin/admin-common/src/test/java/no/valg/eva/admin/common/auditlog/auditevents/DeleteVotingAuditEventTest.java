package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.testng.annotations.Test;

public class DeleteVotingAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		DeleteVotingAuditEvent auditEvent = new DeleteVotingAuditEvent(objectMother.createUserData(), 1L, AuditEventTypes.Delete, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("votingPk", 1));
	}
}
