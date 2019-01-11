package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.annotations.Test;

public class VoterAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(VoterAuditEvent.class,
				VoterAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		VoterAuditEvent auditEvent = new VoterAuditEvent(objectMother.createUserData(),
				objectMother.createVoter(), AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Voter.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(VoterAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll)).isEqualTo(new Class[] { Voter.class });
	}

	@Test
	public void toJson_isCorrect() throws Exception {
		VoterAuditEvent auditEvent = new VoterAuditEvent(objectMother.createUserData(),
				objectMother.createVoter(), AuditEventTypes.SearchElectoralRoll, Outcome.Success, null);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("id", AuditLogTestsObjectMother.UID))
				.assertThat("$", hasEntry("nameLine", AuditLogTestsObjectMother.NAME_LINE))
				.assertThat("$", hasEntry("aarsakskode", AuditLogTestsObjectMother.AARSAKSKODE))
				.assertThat("$", hasEntry("addressLine1", AuditLogTestsObjectMother.ADDRESS_LINE_1));
	}

	@Test
	public void toJson_isCorrect_for_update_electoralRollEvents() throws Exception {
		VoterAuditEvent auditEvent = new VoterAuditEvent(objectMother.createUserData(),
			objectMother.createUpdateVoter(), AuditEventTypes.Update, Outcome.Success, null);
		with(auditEvent.toJson())
			.assertThat("$", hasEntry("nameLine", AuditLogTestsObjectMother.NAME_LINE))
			.assertThat("$", hasEntry("changeType", "E"));
	}

}
