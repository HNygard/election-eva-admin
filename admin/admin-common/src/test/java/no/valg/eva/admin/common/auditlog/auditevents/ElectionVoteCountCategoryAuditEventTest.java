package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class ElectionVoteCountCategoryAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_isCorrect() throws Exception {
		ElectionVoteCountCategoryAuditEvent auditEvent = new ElectionVoteCountCategoryAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()),
				objectMother.createElectionVoteCountCategories(), AuditEventTypes.Update, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("[{\"id\":\"VO\","
				+ "\"countingMode\":\"CENTRAL\","
				+ "\"countCategoryEditable\":true,"
				+ "\"countCategoryEnabled\":true,"
				+ "\"technicalPollingDistrictCountConfigurable\":true,"
				+ "\"specialCover\":true}]");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ElectionVoteCountCategoryAuditEvent.objectClasses(AuditEventTypes.Update)).isEqualTo(new Class[] { List.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws Exception {
		Assertions.assertThat(AuditEventFactory.getAuditEventConstructor(ElectionVoteCountCategoryAuditEvent.class,
				ElectionVoteCountCategoryAuditEvent.objectClasses(AuditEventTypes.Update), AuditedObjectSource.Parameters)).isNotNull();
	}

}
