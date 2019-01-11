package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ElectoralRollSearchAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectClasses_matchesConstructorAndApplicationServiceParameters() {
		assertThat(ElectoralRollSearchAuditEvent.objectClasses(AuditEventTypes.SearchElectoralRoll)).isEqualTo(new Class[] { AreaPath.class, ElectoralRollSearch.class });
	}
	
	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ElectoralRollSearchAuditEvent event = event(AuditEventTypes.SearchElectoralRoll, AreaPath.from("111111"), new ElectoralRollSearch());

		assertThat(event.objectType()).isSameAs(ElectoralRollSearch.class);
	}

	@Test
	public void toJson_withSsn_verifyJson() throws Exception {
		ElectoralRollSearch electoralRollSearch = new ElectoralRollSearch();
		electoralRollSearch.setSsn("24036518886");
		ElectoralRollSearchAuditEvent event = event(AuditEventTypes.SearchElectoralRoll, AreaPath.from("111111"), electoralRollSearch);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("areaPath", "111111"))
				.assertThat("$", hasEntry("ssn", electoralRollSearch.getSsn()));
	}

	@Test
	public void toJson_withBirthDateAndName_verifyJson() throws Exception {
		ElectoralRollSearch electoralRollSearch = new ElectoralRollSearch();
		electoralRollSearch.setBirthDate(new LocalDate(2000, 1, 1));
		electoralRollSearch.setName("name");
		ElectoralRollSearchAuditEvent event = event(AuditEventTypes.SearchElectoralRoll, AreaPath.from("111111"), electoralRollSearch);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("areaPath", "111111"))
				.assertThat("$", hasEntry("birthDate", "01.01.2000"))
				.assertThat("$", hasEntry("name", "name"));
	}

	@Test
	public void toJson_withName_verifyJson() throws Exception {
		ElectoralRollSearch electoralRollSearch = new ElectoralRollSearch();
		electoralRollSearch.setName("name");
		ElectoralRollSearchAuditEvent event = event(AuditEventTypes.SearchElectoralRoll, AreaPath.from("111111"), electoralRollSearch);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("areaPath", "111111"))
				.assertThat("$", hasEntry("name", "name"));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ElectoralRollSearchAuditEvent.class;
	}

	private ElectoralRollSearchAuditEvent event(AuditEventTypes eventType, AreaPath areaPath, ElectoralRollSearch electoralRollSearch) {
		return new ElectoralRollSearchAuditEvent(createMock(UserData.class), areaPath, electoralRollSearch, eventType, Outcome.Success, "");
	}

}

