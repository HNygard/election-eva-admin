package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.Contest;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class ContestAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_whenSave_isCorrect() throws Exception {
		ContestAuditEvent auditEvent = new ContestAuditEvent(objectMother.createUserData(),
				objectMother.createCommonContest(), AuditEventTypes.Save, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"contestPath\":\"111111.22.33\",\"name\":\"Kommune- og fylkestingsvalget 2015\",\"penultimateRecount\":null,\""
				+ "maxCandidates\":31,\"minCandidates\":2,\"maxWriteIn\":6,\"numberOfPositions\":25,\"maxRenumber\":3,\"minProposersNewParty\":2,\""
				+ "minProposersOldParty\":1}");
	}

	@Test
	public void toJson_whenDelete_isCorrect() throws Exception {
		ContestAuditEvent auditEvent = new ContestAuditEvent(objectMother.createUserData(),
			ElectionPath.from("111111.22.33.444444"), AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"contestPath\":\"111111.22.33.444444\"}");
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		Assertions.assertThat(AuditEventFactory.getAuditEventConstructor(ContestAuditEvent.class,
				ContestAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		ContestAuditEvent auditEvent = new ContestAuditEvent(objectMother.createUserData(),
				objectMother.createCommonContest(), AuditEventTypes.Save, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Contest.class);
	}

	@Test
	public void objectClasses_whenDelete_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ContestAuditEvent.objectClasses(AuditEventTypes.Delete)).isEqualTo(new Class[] { ElectionPath.class });
	}
}
