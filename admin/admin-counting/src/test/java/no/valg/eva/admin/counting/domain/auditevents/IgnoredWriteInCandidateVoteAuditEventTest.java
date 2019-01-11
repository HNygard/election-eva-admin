package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.auditevents.counting.IgnoredWriteInCandidateVoteAuditEvent;
import no.valg.eva.admin.configuration.domain.model.Candidate;

import org.testng.annotations.Test;

public class IgnoredWriteInCandidateVoteAuditEventTest {
	private static final String CANDIDATE_ID = "12345678912";
	private static final String MESSAGE = "MESSAGE";
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_givenAuditEvent_isCorrect() throws Exception {
		IgnoredWriteInCandidateVoteAuditEvent auditEvent =
				new IgnoredWriteInCandidateVoteAuditEvent(objectMother.createUserData(), CANDIDATE_ID, MESSAGE);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"candidateId\":\"12345678912\"}");
	}

	@Test
	public void objectType_mustReturnCandidateClass() throws Exception {
		IgnoredWriteInCandidateVoteAuditEvent auditEvent =
				new IgnoredWriteInCandidateVoteAuditEvent(objectMother.createUserData(), CANDIDATE_ID, MESSAGE);
		assertThat(auditEvent.objectType()).isEqualTo(Candidate.class);
	}
}
