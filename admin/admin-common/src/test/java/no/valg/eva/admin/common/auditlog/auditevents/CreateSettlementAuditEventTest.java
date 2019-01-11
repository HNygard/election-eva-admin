package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.Settlement;

import org.testng.annotations.Test;

public class CreateSettlementAuditEventTest {
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("750401.01.02.030301");
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		CreateSettlementAuditEvent auditEvent = new CreateSettlementAuditEvent(
				objectMother.createUserData(), CONTEST_PATH, AuditEventTypes.Create, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(Settlement.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("contestPath", CONTEST_PATH.path()));
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(CreateSettlementAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[] { ElectionPath.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(CreateSettlementAuditEvent.class,
				CreateSettlementAuditEvent.objectClasses(AuditEventTypes.Create), AuditedObjectSource.Parameters)).isNotNull();
	}
}
