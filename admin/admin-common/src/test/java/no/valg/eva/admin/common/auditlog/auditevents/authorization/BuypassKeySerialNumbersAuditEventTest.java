package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.rbac.BuypassOperator;

import org.testng.annotations.Test;

public class BuypassKeySerialNumbersAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(BuypassKeySerialNumbersAuditEvent.class,
				BuypassKeySerialNumbersAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		BuypassKeySerialNumbersAuditEvent auditEvent = new BuypassKeySerialNumbersAuditEvent(objectMother.createUserData(),
				objectMother.createBuypassOperatorList(), AuditEventTypes.Update, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(BuypassOperator.class);
	}

	@Test
	public void objectClasses_whenUpdate_returnsClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(BuypassKeySerialNumbersAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(
				new Class[] { List.class });
	}

	@Test
	public void toJson_isCorrect() throws Exception {
		BuypassKeySerialNumbersAuditEvent auditEvent = new BuypassKeySerialNumbersAuditEvent(objectMother.createUserData(),
				objectMother.createBuypassOperatorList(), AuditEventTypes.Update, Outcome.Success, null);
		String json = auditEvent.toJson();

		assertThat(json).isEqualTo("[{\"fnr\":\"12345678901\",\"buypass-nummer\":\"9578-4050-000000000\"}]");
	}

}
