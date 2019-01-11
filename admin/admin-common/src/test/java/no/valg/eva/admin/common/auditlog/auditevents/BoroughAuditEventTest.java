package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.Borough;

import org.testng.annotations.Test;


public class BoroughAuditEventTest extends AbstractAuditEventTest {

	private final Borough borough = createBorough();

	private Borough createBorough() {
		Borough borough = new AuditLogTestsObjectMother().createBorough();
		borough.setMunicipality1(true);
		return borough;
	}

	@Test
	public void toJson_forSuccessfulCreateBorough_isCorrect() {
		BoroughAuditEvent auditEvent = createBoroughAuditEvent(borough, Create);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("path", borough.areaPath().path()))
				.assertThat("$", hasEntry("name", borough.getName()))
				.assertThat("$", hasEntry("representsWholeMunicipality", true));
	}

	protected BoroughAuditEvent createBoroughAuditEvent(Borough borough, AuditEventTypes crudType) {
		return new BoroughAuditEvent(new AuditLogTestsObjectMother().createUserData(), borough, crudType, Success, "detail");
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return BoroughAuditEvent.class;
	}
}
