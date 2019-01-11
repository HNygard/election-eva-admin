package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.annotations.Test;

public class SearchByIdAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(SearchByIdAuditEvent.class,
				SearchByIdAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		assertThat(createAuditEvent().objectType()).isEqualTo(Voter.class);
	}

	private SearchByIdAuditEvent createAuditEvent() {
		return new SearchByIdAuditEvent(objectMother.createUserData(),
				AuditLogTestsObjectMother.UID, AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(SearchByIdAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll)).isEqualTo(new Class[] { String.class });
	}

	@Test
	public void toJson_isCorrect() throws Exception {
		String json = createAuditEvent().toJson();
		assertThat(json).isEqualTo("{\"id\":\"" + objectMother.UID + "\"}");
	}
}
