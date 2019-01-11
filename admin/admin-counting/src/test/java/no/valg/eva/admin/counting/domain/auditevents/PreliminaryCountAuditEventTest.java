package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.preliminaryVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.voteCount;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PreliminaryCountAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@BeforeMethod
	public void setUp() {
	}

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		VoteCount voteCount = voteCount(preliminaryVoteCountConfig(VO));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PRELIMINARY, new VoteCountAuditDetails(voteCount, false, false));
		PreliminaryCountAuditEvent auditEvent = new PreliminaryCountAuditEvent(objectMother.createUserData(), null, null, AuditEventTypes.ApproveCount,
				Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
		assertThat(auditEvent.toJson()).contains("\"contestReport\":{");

	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(PreliminaryCountAuditEvent.objectClasses(AuditEventTypes.ApproveCount))
				.isEqualTo(new Class[] { CountContext.class, PreliminaryCount.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(PreliminaryCountAuditEvent.class,
				PreliminaryCountAuditEvent.objectClasses(AuditEventTypes.ApproveCount), AuditedObjectSource.Parameters)).isNotNull();
	}

}
