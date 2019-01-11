package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.rbac.service.ContactInfo;

import org.testng.annotations.Test;

public class ContactInfoChangedAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(ContactInfoChangedAuditEvent.class,
				ContactInfoChangedAuditEvent.objectClasses(AuditEventTypes.ContactInfoChanged), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		ContactInfoChangedAuditEvent auditEvent = new ContactInfoChangedAuditEvent(objectMother.createUserData(),
				objectMother.createContactInfo(), AuditEventTypes.ContactInfoChanged, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(ContactInfo.class);
	}

	@Test
	public void objectClasses_whenUpdate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ContactInfoChangedAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(
				new Class[] { ContactInfo.class });
	}

	@Test
	public void toJson_isCorrect() throws Exception {
		ContactInfoChangedAuditEvent auditEvent = new ContactInfoChangedAuditEvent(objectMother.createUserData(),
				objectMother.createContactInfo(), AuditEventTypes.ContactInfoChanged, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"phone\":\"99999999\",\"email\":\"test@jpro.no\"}");
	}
}
