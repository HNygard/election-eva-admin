package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.auditlog.Outcome.GenericError;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.reporting.model.ReportExecution;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class ReportAuditEventTest extends AbstractAuditEventTest {
	private static final String REPORT_NAME = "Report Name";
	private static final String FILE_NAME = "Report File Name";
	public static final String FORMAT = "pdf";
	public static final int SIZE = 10;
	private static final ReportExecution REPORT_CONTENT = new ReportExecution(
			new byte[SIZE], REPORT_NAME, FILE_NAME, FORMAT, ImmutableMap.of("param1", "value1"), ImmutableMap.of("param1", "label1"));
	private static final String DETAIL = "detail";
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void checkCorrectJson() {
		ReportAuditEvent auditEvent = new ReportAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()),
				REPORT_CONTENT,
				Update, Success, DETAIL);

		String json = auditEvent.toJson();
		with(json)
				.assertThat("$", hasEntry("report", REPORT_NAME))
				.assertThat("$", hasEntry("format", FORMAT))
				.assertThat("$", hasEntry("size", SIZE))
				.assertThat("$.arguments", hasEntry("label1", "value1"));
	}

	@Test
	public void checkCorrectJson_hvisKallFeiler_skriverObjektMedTomRapport() {
		ReportAuditEvent auditEvent = new ReportAuditEvent(
				objectMother.createUserData(objectMother.createOperatorRole()),
				null,
				Update, GenericError, DETAIL);

		String json = auditEvent.toJson();
		with(json).assertThat("$", hasEntry("report", ReportAuditEvent.INTET_RAPPORTRESULTAT));
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		AuditEvent auditEvent = createAuditEvent(Create);
		assertThat(auditEvent.objectType()).isEqualTo(ReportExecution.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ReportAuditEvent.objectClasses(AuditEventTypes.GenerateReport)).isEqualTo(new Class[] { ReportExecution.class });
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ReportAuditEvent.class;
	}

	private AuditEvent createAuditEvent(AuditEventTypes auditEventType) {
		return new ReportAuditEvent(objectMother.createUserData(), REPORT_CONTENT, auditEventType, Success, null);
	}
}
