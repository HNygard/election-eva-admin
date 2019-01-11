package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.Country;

import org.testng.annotations.Test;


public class CountryAuditEventTest extends AbstractAuditEventTest {

	private final Country country = new AuditLogTestsObjectMother().createCountry();

	@Test
	public void toJson_forSuccessfulCreateCountry_isCorrect() {
		CountryAuditEvent auditEvent = createCountryAuditEvent(country, Create);
		with(auditEvent.toJson())
				.assertThat("$", hasEntry("path", country.areaPath().path()))
				.assertThat("$", hasEntry("name", country.getName()));
	}

	protected CountryAuditEvent createCountryAuditEvent(Country country, AuditEventTypes crudType) {
		return new CountryAuditEvent(new AuditLogTestsObjectMother().createUserData(), country, crudType, Success, "detail");
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return CountryAuditEvent.class;
	}
}
