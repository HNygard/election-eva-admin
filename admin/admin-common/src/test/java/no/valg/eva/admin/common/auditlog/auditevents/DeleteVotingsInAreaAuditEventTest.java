package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.testng.annotations.Test;

public class DeleteVotingsInAreaAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		MvElection mvElection = new MvElection();
		mvElection.setElectionPath(ElectionPath.from("150001.01").path());
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(AreaPath.from("150001.47.01").path());

		DeleteVotingsInAreaAuditEvent auditEvent = new DeleteVotingsInAreaAuditEvent(objectMother.createUserData(), mvElection, mvArea, 1,
				AuditEventTypes.DeletedAllInArea, Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(Voting.class);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("election", "150001.01"))
				.assertThat("$", hasEntry("area", "150001.47.01"))
				.assertThat("$", hasEntry("votingCategoryPk", 1));
	}

}
