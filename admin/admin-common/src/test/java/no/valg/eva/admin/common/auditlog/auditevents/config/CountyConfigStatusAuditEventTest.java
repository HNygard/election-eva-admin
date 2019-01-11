package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class CountyConfigStatusAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		CountyConfigStatusAuditEvent event = event(AuditEventTypes.Save, new CountyConfigStatus(AreaPath.from("111111.22.33"), "County"));

		assertThat(event.objectType()).isSameAs(CountyConfigStatus.class);
	}

	@Test
	public void toJson() throws Exception {
		CountyConfigStatus countyConfigStatus = new CountyConfigStatus(AreaPath.from("111111.22.33"), "County");
		countyConfigStatus.setLocaleId(new LocaleId("nb_NO"));
		countyConfigStatus.setReportingUnitFylkesvalgstyre(true);
		CountyConfigStatusAuditEvent event = event(AuditEventTypes.Save, countyConfigStatus);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", countyConfigStatus.getCountyPath().path()))
				.assertThat("$", hasEntry("localeId", countyConfigStatus.getLocaleId().getId()))
				.assertThat("$", hasEntry("reportingUnitFylkesvalgstyre", countyConfigStatus.isReportingUnitFylkesvalgstyre()))
				.assertThat("$", hasEntry("language", countyConfigStatus.isLanguage()))
				.assertThat("$", hasEntry("listProposals", countyConfigStatus.isListProposals()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return CountyConfigStatusAuditEvent.class;
	}

	private CountyConfigStatusAuditEvent event(AuditEventTypes eventType, CountyConfigStatus countyConfigStatus) {
		return new CountyConfigStatusAuditEvent(createMock(UserData.class), countyConfigStatus, eventType, Outcome.Success, "");
	}
}
