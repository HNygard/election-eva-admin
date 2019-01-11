package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.finalVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.voteCount;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;

public class FinalCountAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		VoteCount voteCount = voteCount(finalVoteCountConfig(SAVED, false, false));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(FINAL, new VoteCountAuditDetails(voteCount, false, false));
		FinalCountAuditEvent auditEvent = new FinalCountAuditEvent(objectMother.createUserData(), AuditEventTypes.SaveCount, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
		assertThat(auditEvent.toJson()).contains("\"contestReport\":{");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(FinalCountAuditEvent.objectClasses(AuditEventTypes.SaveCount)).isEqualTo(new Class[0]);
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(FinalCountAuditEvent.class,
				FinalCountAuditEvent.objectClasses(AuditEventTypes.SaveCount), AuditedObjectSource.Parameters)).isNotNull();
	}
}
