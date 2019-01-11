package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlement;

import org.testng.annotations.Test;

public class DeleteLevelingSeatSettlementAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		DeleteLevelingSeatSettlementAuditEvent auditEvent = new DeleteLevelingSeatSettlementAuditEvent(
				objectMother.createUserData(), AuditEventTypes.DeletedAllInArea, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(LevelingSeatSettlement.class);
		assertThat(auditEvent.toJson()).isEqualTo("{}");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(DeleteLevelingSeatSettlementAuditEvent.objectClasses(AuditEventTypes.Delete)).isEqualTo(new Class[0]);
	}

}
