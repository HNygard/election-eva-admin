package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.preliminaryVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.protocolVoteCountConfig;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.voteCount;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolAndPreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;

public class ProtocolAndPreliminaryCountAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		VoteCount protocolVoteCount = voteCount(protocolVoteCountConfig(1, null, null));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PROTOCOL, new VoteCountAuditDetails(protocolVoteCount, false, false));
		VoteCount preliminaryVoteCount = voteCount(preliminaryVoteCountConfig(VO));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PRELIMINARY, new VoteCountAuditDetails(preliminaryVoteCount, false, false));
		ProtocolAndPreliminaryCountAuditEvent auditEvent = new ProtocolAndPreliminaryCountAuditEvent(objectMother.createUserData(), null, null,
				AuditEventTypes.ApproveCount, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
		String json = auditEvent.toJson();
		assertThat(json).contains("\"protocolVoteCount\":{");
		assertThat(json).contains("\"preliminaryVoteCount\":{");

	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ProtocolAndPreliminaryCountAuditEvent.objectClasses(AuditEventTypes.ApproveCount)).isEqualTo(
				new Class[] { CountContext.class, ProtocolAndPreliminaryCount.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(ProtocolAndPreliminaryCountAuditEvent.class,
				ProtocolAndPreliminaryCountAuditEvent.objectClasses(AuditEventTypes.ApproveCount), AuditedObjectSource.Parameters)).isNotNull();
	}
}
