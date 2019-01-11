package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class ElectionEventAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_isCorrect() {
		ElectionEventAuditEvent auditEvent = new ElectionEventAuditEvent(objectMother.createUserData(objectMother.createOperatorRole()),
				objectMother.createElectionEvent(), objectMother.createElectionEvent(), objectMother.createLocales(), AuditEventTypes.Create, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"id\":\"950000\",\"name\":null,\"locales\":[{\"id\":\"nn-NO\"},{\"id\":\"nb-NO\"}],\"status\":\"CENTRAL_CONFIGURATION\","
				+ "\"theme\":null,\"demoElection\":false,\"electoralRollCutOffDate\":null,\"votingCardElectoralRollDate\":null,\"votingCardDeadline\":null,"
				+ "\"voterNumbersAssignedDate\":null,\"electoralRollLinesPerPage\":null,\"voterImportDirName\":null,\"voterImportMunicipality\":false}");
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() {
		ElectionEventAuditEvent auditEvent = new ElectionEventAuditEvent(objectMother.createUserData(objectMother.createOperatorRole()),
				objectMother.createElectionEvent(), objectMother.createElectionEvent(), Collections.<Locale> emptySet(), AuditEventTypes.Create, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(ElectionEvent.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ElectionEventAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[] { ElectionEvent.class, ElectionEvent.class, Set.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws Exception {
		Assertions.assertThat(
				AuditEventFactory.getAuditEventConstructor(ElectionEventAuditEvent.class, ElectionEventAuditEvent.objectClasses(AuditEventTypes.Create),
						AuditedObjectSource.Parameters))
				.isNotNull();
		assertThat(
				AuditEventFactory.getAuditEventConstructor(ElectionEventAuditEvent.class, ElectionEventAuditEvent.objectClasses(AuditEventTypes.Update),
						AuditedObjectSource.Parameters))
				.isNotNull();
	}
}
