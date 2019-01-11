package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class ImportUploadedCountAuditEventTest {
	private static final Jobbkategori BATCH_CATEGORY = COUNT_UPLOAD;
	private static final int BATCH_ID = 1;
	private static final Long ELECITON_EVENT_PK = 2L;
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_whenUpdate_isCorrect() throws Exception {
		ImportUploadedCountAuditEvent auditEvent = new ImportUploadedCountAuditEvent(
				objectMother.createUserData(), BATCH_ID, ELECITON_EVENT_PK, BATCH_CATEGORY, AuditEventTypes.ImportUploadedCount, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"batchId\":1,\"electionEventPk\":2,\"accessPath\":\"e.count.upload\"}");
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		Assertions.assertThat(AuditEventFactory.getAuditEventConstructor(ImportUploadedCountAuditEvent.class,
				ImportUploadedCountAuditEvent.objectClasses(AuditEventTypes.ImportUploadedCount), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnVoteCountClass() throws Exception {
		ImportUploadedCountAuditEvent auditEvent = new ImportUploadedCountAuditEvent(
				objectMother.createUserData(), BATCH_ID, ELECITON_EVENT_PK, BATCH_CATEGORY, AuditEventTypes.SaveUploadedCount, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
	}

	@Test
	public void objectClasses_whenCreate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ImportUploadedCountAuditEvent.objectClasses(AuditEventTypes.ImportUploadedCount)).containsExactly(int.class, Long.class, Jobbkategori.class);
	}
}
