package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.annotations.Test;

public class SearchByVoterNumberAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(SearchByVoterNumberAuditEvent.class,
				SearchByVoterNumberAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		SearchByVoterNumberAuditEvent auditEvent = new SearchByVoterNumberAuditEvent(objectMother.createUserData(),
				AuditLogTestsObjectMother.MANNTALLSNUMMER, AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Voter.class);
	}

	@Test
	public void objectClasses_whenRead_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(SearchByVoterNumberAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll)).isEqualTo(new Class[] { Manntallsnummer.class });
	}

	@Test
	public void toJson_whenRead_isCorrect() throws Exception {
		SearchByVoterNumberAuditEvent auditEvent = new SearchByVoterNumberAuditEvent(objectMother.createUserData(),
				AuditLogTestsObjectMother.MANNTALLSNUMMER, AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).contains("manntallsnummer\":" + AuditLogTestsObjectMother.MANNTALLSNUMMER.getManntallsnummer());
	}

}
