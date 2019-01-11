package no.valg.eva.admin.common.auditlog.auditevents.electoralroll;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.testng.annotations.Test;

public class ElectoralRollAuditEventTest {

	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(ElectoralRollAuditEvent.class,
			ElectoralRollAuditEvent.objectClasses(AuditEventTypes.DeleteAll), AuditedObjectSource.Parameters)).isNotNull();
		assertThat(AuditEventFactory.getAuditEventConstructor(ElectoralRollAuditEvent.class,
			ElectoralRollAuditEvent.objectClasses(AuditEventTypes.FullElectoralImportStarted), AuditedObjectSource.Parameters)).isNotNull();
		assertThat(AuditEventFactory.getAuditEventConstructor(ElectoralRollAuditEvent.class,
			ElectoralRollAuditEvent.objectClasses(AuditEventTypes.GenererValgkortgrunnlagJobbStartet), AuditedObjectSource.Parameters)).isNotNull();
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		assertThat(new ElectoralRollAuditEvent(objectMother.createUserData(), AuditEventTypes.DeleteAll, Outcome.Success, null).objectType()).isEqualTo(Voter.class);
		assertThat(new ElectoralRollAuditEvent(objectMother.createUserData(), AuditEventTypes.DeletedAllWithoutArea,
			Outcome.Success, null).objectType()).isEqualTo(Voter.class);
		assertThat(new ElectoralRollAuditEvent(objectMother.createUserData(), new MvElection(), new MvArea(), AuditEventTypes.DeletedAllInArea,
			Outcome.Success, null).objectType()).isEqualTo(Voter.class);
		assertThat(new ElectoralRollAuditEvent(objectMother.createUserData(), AuditEventTypes.GenererValgkortgrunnlagJobbStartet,
			Outcome.Success, null).objectType()).isEqualTo(Voter.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ElectoralRollAuditEvent.objectClasses(AuditEventTypes.DeleteAll)).isEqualTo(new Class[]{});
		assertThat(ElectoralRollAuditEvent.objectClasses(AuditEventTypes.DeletedAllInArea)).isEqualTo(new Class[] { MvElection.class, MvArea.class });
		assertThat(ElectoralRollAuditEvent.objectClasses(AuditEventTypes.DeletedAllWithoutArea)).isEqualTo(new Class[] { });
		assertThat(ElectoralRollAuditEvent.objectClasses(AuditEventTypes.FullElectoralImportStarted)).isEqualTo(new Class[]{String.class});
		assertThat(ElectoralRollAuditEvent.objectClasses(AuditEventTypes.GenererValgkortgrunnlagJobbStartet)).isEqualTo(new Class[]{String.class});
	}

	@Test
	public void toJson_isCorrect_forDeleteAll() throws Exception {
		ElectoralRollAuditEvent auditEvent = new ElectoralRollAuditEvent(objectMother.createUserData(), AuditEventTypes.DeleteAll, Outcome.Success, null);
		with(auditEvent.toJson()).assertNotDefined("$.fileName");
	}

	@Test
	public void toJson_isCorrect_forTriggerFullElectoralRollImport() throws Exception {
		ElectoralRollAuditEvent auditEvent =
			new ElectoralRollAuditEvent(objectMother.createUserData(), "fileName.txt", AuditEventTypes.FullElectoralImportStarted, Outcome.Success, null);
		with(auditEvent.toJson()).assertThat("$", hasEntry("fileName", "fileName.txt"));
	}
}
