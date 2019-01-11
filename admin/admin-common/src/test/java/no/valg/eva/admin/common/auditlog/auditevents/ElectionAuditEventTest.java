package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;

import org.testng.annotations.Test;

public class ElectionAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withSave_isCorrect() throws Exception {
		Election election = objectMother.createElection();
		election.setGenericElectionType(GenericElectionType.F);
		ElectionAuditEvent auditEvent = new ElectionAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()), election, AuditEventTypes.Save, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"id\":\"01\",\"name\":\"Kommunestyrevalg\",\"valgtype\":\"STORTINGSVALG\",\"electionType\":\"F\","
				+ "\"endDateOfBirth\":\"2015-07-01\",\"singleArea\":false,\"penultimateRecount\":false,\"renumber\":false,\"renumberLimit\":false,"
				+ "\"writein\":false,\"writeinLocalOverride\":false,\"strikeout\":false,\"personal\":false,\"candidatesInContestArea\":false,"
				+ "\"maxCandidateNameLength\":0,\"maxCandidateResidenceProfessionLength\":0,\"maxCandidatesAddition\":null,"
				+ "\"maxCandidates\":null,\"minCandidatesAddition\":null,\"minCandidates\":null,\"levelingSeats\":0}");
	}

	@Test
	public void toJson_withDelete_isCorrect() throws Exception {
		ElectionAuditEvent auditEvent = new ElectionAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()), ElectionPath.from("111111.22.33"), AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"electionPath\":\"111111.22.33\"}");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ElectionAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[] { Election.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws Exception {
		assertThat(
				AuditEventFactory.getAuditEventConstructor(ElectionAuditEvent.class, ElectionAuditEvent.objectClasses(AuditEventTypes.Create),
						AuditedObjectSource.Parameters)).isNotNull();
	}

}
