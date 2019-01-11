package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.rbac.Operator;

import org.testng.annotations.Test;

public class DeleteOperatorAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(DeleteOperatorAuditEvent.class,
				DeleteOperatorAuditEvent.objectClasses(AuditEventTypes.Delete), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		DeleteOperatorAuditEvent auditEvent = new DeleteOperatorAuditEvent(objectMother.createUserData(),
				objectMother.createOperator(), AuditEventTypes.Delete, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Operator.class);
	}

	@Test
	public void objectClasses_whenDelete_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(DeleteOperatorAuditEvent.objectClasses(AuditEventTypes.Delete)).isEqualTo(new Class[] { Operator.class });
	}

	@Test
	public void toJson_whenDelete_isCorrect() throws Exception {
		DeleteOperatorAuditEvent auditEvent = new DeleteOperatorAuditEvent(objectMother.createUserData(),
				objectMother.createOperator(), AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"personId\":\"" + objectMother.UID + "\"}");
	}

}
