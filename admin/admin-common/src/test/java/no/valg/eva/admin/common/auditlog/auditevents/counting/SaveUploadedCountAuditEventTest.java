package no.valg.eva.admin.common.auditlog.auditevents.counting;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class SaveUploadedCountAuditEventTest {
	private static final AuditLogTestsObjectMother OBJECT_MOTHER = new AuditLogTestsObjectMother();
	private static final byte[] FILE = OBJECT_MOTHER.createByteArray(256);
	private static final String FILE_NAME = "fileName.ext";
	private static final Jobbkategori BATCH_CATEGORY = COUNT_UPLOAD;

	@Test
	public void toJson_whenUpdate_isCorrect() throws Exception {
		SaveUploadedCountAuditEvent auditEvent = new SaveUploadedCountAuditEvent(
				OBJECT_MOTHER.createUserData(), FILE, FILE_NAME, BATCH_CATEGORY, AuditEventTypes.SaveUploadedCount, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"fileName\":\"fileName.ext\",\"fileLength\":" + FILE.length + ",\"accessPath\":\"e.count.upload\"}");
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		Assertions.assertThat(AuditEventFactory.getAuditEventConstructor(SaveUploadedCountAuditEvent.class,
				SaveUploadedCountAuditEvent.objectClasses(AuditEventTypes.SaveUploadedCount), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnNull() throws Exception {
		SaveUploadedCountAuditEvent auditEvent = new SaveUploadedCountAuditEvent(
				OBJECT_MOTHER.createUserData(), FILE, FILE_NAME, BATCH_CATEGORY, AuditEventTypes.SaveUploadedCount, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isNull();
	}

	@Test
	public void objectClasses_whenCreate_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(SaveUploadedCountAuditEvent.objectClasses(AuditEventTypes.SaveUploadedCount)).containsExactly(byte[].class, String.class, Jobbkategori.class);
	}
}
