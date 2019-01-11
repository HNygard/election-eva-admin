package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.LOCALE_ID;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.MUNICIPALITY_ID;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.MUNICIPALITY_NAME;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;

import org.testng.annotations.Test;

public class MunicipalityAuditEventTest extends AbstractAuditEventTest {

	private static final String STATUS_NAME = "LOCAL_CONFIGURATION";
	private static final String STATUS_NAME_2 = "CENTRAL_CONFIGURATION";
	private Municipality municipality = createMunicipality(1);
	private Municipality municipalityForDelete = createMunicipalityForDelete();

	private Municipality createMunicipalityForDelete() {
		return createMunicipality(0);
	}

	@Test
	public void toJson_forSuccessfulCreateMunicipality_isCorrect() {
		MunicipalityAuditEvent auditEvent = createMunicipalityAuditEvent(municipality, Create);
		checkEventJson(auditEvent, STATUS_NAME, 1);
	}

	@Test
	public void toJson_forSuccessfulDeleteMunicipality_isCorrect() {
		MunicipalityAuditEvent auditEvent = createDeleteMunicipalityAuditEvent(municipalityForDelete, Create);
		checkEventJson(auditEvent, STATUS_NAME_2, 0);
	}

	private void checkEventJson(MunicipalityAuditEvent auditEvent, String statusName, int value) {
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("id", MUNICIPALITY_ID))
				.assertThat("$", hasEntry("name", MUNICIPALITY_NAME))
				.assertThat("$", hasEntry("locale", LOCALE_ID))
				.assertThat("$", hasEntry("electronicMarkoffs", true))
				.assertThat("$", hasEntry("requiredProtocolCount", true))
				.assertThat("$", hasEntry("technicalPollingDistrictsAllowed", true))
				.assertThat("$.municipalityStatus", hasEntry("id", value))
				.assertThat("$.municipalityStatus", hasEntry("name", statusName));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return MunicipalityAuditEvent.class;
	}

	protected MunicipalityAuditEvent createMunicipalityAuditEvent(Municipality municipality, AuditEventTypes crudType) {
		MunicipalityAuditEventForLocalConfiguration detail = new MunicipalityAuditEventForLocalConfiguration(
				new AuditLogTestsObjectMother().createUserData(),
				municipality, crudType, Success, "detail");
		return detail;
	}

	protected MunicipalityAuditEvent createDeleteMunicipalityAuditEvent(Municipality municipality, AuditEventTypes crudType) {
		return new MunicipalityAuditEventForLocalConfiguration(new AuditLogTestsObjectMother().createUserData(), municipality, crudType, Success, "detail");
	}

	protected Municipality createMunicipality(int id) {
		Municipality municipality = new AuditLogTestsObjectMother().createMunicipality();
		municipality.setElectronicMarkoffs(true);
		municipality.setRequiredProtocolCount(true);
		municipality.setTechnicalPollingDistrictsAllowed(true);
		MunicipalityStatus municipalityStatus = new MunicipalityStatus();
		municipalityStatus.setId(id);
		municipality.setMunicipalityStatus(municipalityStatus);
		return municipality;
	}
}
