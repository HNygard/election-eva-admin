package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatorLoginAuditEventTest {
	private AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_forSuccessfulLogin_isCorrect() {
		UserData userData = objectMother.createUserData();
		OperatorLoginAuditEvent auditEvent = new OperatorLoginAuditEvent(userData, Outcome.Success);
		assertThat(auditEvent.toJson()).isEqualTo("{\"securityLevel\":3}");
	}
}
