package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.Settlement;

import org.testng.annotations.Test;

public class DeleteSettlementAuditEventTest {

	private static final ElectionPath ELECTION_PATH = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111.111111");
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		DeleteSettlementAuditEvent auditEvent = new DeleteSettlementAuditEvent(
				objectMother.createUserData(), ELECTION_PATH, AREA_PATH, AuditEventTypes.DeletedAllInArea, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(Settlement.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("electionPath", ELECTION_PATH.path()))
				.assertThat("$", hasEntry("areaPath", AREA_PATH.path()));
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(DeleteSettlementAuditEvent.objectClasses(AuditEventTypes.DeletedAllInArea)).isEqualTo(new Class[] { ElectionPath.class, AreaPath.class });
	}

}
