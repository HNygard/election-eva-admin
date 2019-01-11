package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlement;

import org.testng.annotations.Test;

public class DistributeLevelingSeatsAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		DistributeLevelingSeatsAuditEvent auditEvent = new DistributeLevelingSeatsAuditEvent(
				objectMother.createUserData(), AuditEventTypes.Create, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(LevelingSeatSettlement.class);
		assertThat(auditEvent.toJson()).isEqualTo("{}");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(DistributeLevelingSeatsAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[0]);
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(DistributeLevelingSeatsAuditEvent.class,
				DistributeLevelingSeatsAuditEvent.objectClasses(AuditEventTypes.Create), AuditedObjectSource.Parameters)).isNotNull();
	}
}
