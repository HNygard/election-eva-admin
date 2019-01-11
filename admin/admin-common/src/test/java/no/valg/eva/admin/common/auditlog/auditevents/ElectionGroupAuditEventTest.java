package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class ElectionGroupAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withPartialUpdate_isCorrect() throws Exception {
		ElectionGroupAuditEvent auditEvent = new ElectionGroupAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()), 1L, true, AuditEventTypes.PartialUpdate, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"isRequiredProtocolCount\":true,"
				+ "\"electionEventPk\":1}");
	}

	@Test
	public void toJson_forElectionGroupCreateOrUpdate_isCorrect() throws Exception {
		ElectionGroup electionGroup = objectMother.createElectionGroup();
		ElectionGroupAuditEvent auditEvent = new ElectionGroupAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()), electionGroup, AuditEventTypes.Save, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"parentElectionPath\":\"111111\",\"id\":\"01\",\"name\":\"Kommune- og fylkestingsvalg\""
				+ ",\"electronicMarkoffs\":false,\"advanceVoteInBallotBox\":false}");
	}

	@Test
	public void toJson_withDelete_isCorrect() throws Exception {
		ElectionGroupAuditEvent auditEvent = new ElectionGroupAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()), ElectionPath.from("111111.22"), AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"electionGroupPath\":\"111111.22\"}");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ElectionGroupAuditEvent.objectClasses(AuditEventTypes.PartialUpdate)).isEqualTo(new Class[] { Long.class, Boolean.class, });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws Exception {
		Assertions
				.assertThat(AuditEventFactory.getAuditEventConstructor(ElectionGroupAuditEvent.class,
						ElectionGroupAuditEvent.objectClasses(AuditEventTypes.PartialUpdate), AuditedObjectSource.Parameters))
				.isNotNull();
	}

}
