package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.annotations.Test;

public class SearchByMunicipalityAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(SearchByMunicipalityAuditEvent.class,
				SearchByMunicipalityAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		SearchByMunicipalityAuditEvent auditEvent = new SearchByMunicipalityAuditEvent(objectMother.createUserData(),
				AuditLogTestsObjectMother.MUNICIPALITY_ID, AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Voter.class);
	}

	@Test
	public void objectClasses_whenRead_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(SearchByMunicipalityAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll)).isEqualTo(new Class[] { String.class });
	}

	@Test
	public void toJson_whenRead_isCorrect() throws Exception {
		SearchByMunicipalityAuditEvent auditEvent = new SearchByMunicipalityAuditEvent(objectMother.createUserData(),
				AuditLogTestsObjectMother.MUNICIPALITY_ID, AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).contains("{\"municipalityId\":\"" + objectMother.MUNICIPALITY_ID);
	}
}
