package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.*;
import static no.valg.eva.admin.common.auditlog.Outcome.GenericError;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;

import org.testng.annotations.Test;

public class AffiliationAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		AffiliationAuditEvent auditEvent = new AffiliationAuditEvent(objectMother.createUserData(), new Affiliation(), Create, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Affiliation.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(AffiliationAuditEvent.objectClasses(Create)).isEqualTo(new Class[] { Affiliation.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(
				AuditEventFactory.getAuditEventConstructor(AffiliationAuditEvent.class, AffiliationAuditEvent.objectClasses(Delete),
						AuditedObjectSource.Parameters)).isNotNull();
	}
	
	@Test
	public void toJson_withNullAffiliation_stillPrintsOutData() throws NoSuchMethodException {
		AffiliationAuditEvent auditEvent = new AffiliationAuditEvent(objectMother.createUserData(), null, Create, GenericError, null);
		String expectedOutput = "{\"partyId\":null,\"contestName\":null,\"displayOrder\":null}";
		
		assertThat(auditEvent.toJson()).isEqualTo(expectedOutput);
	}
}
