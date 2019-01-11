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
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class MunicipalityConfigStatusAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		MunicipalityConfigStatusAuditEvent event = event(AuditEventTypes.Save,
				new MunicipalityConfigStatus(AreaPath.from("111111.22.33.4444"), "Municipality"));

		assertThat(event.objectType()).isSameAs(MunicipalityConfigStatus.class);
	}

	@Test
	public void toJson() throws Exception {
		MunicipalityConfigStatus municipalityConfigStatus = new MunicipalityConfigStatus(AreaPath.from("111111.22.33.4444"), "Municipality");
		municipalityConfigStatus.setLocaleId(new LocaleId("nb_NO"));
		municipalityConfigStatus.setAdvancePollingPlaces(true);
		municipalityConfigStatus.setLanguage(true);
		municipalityConfigStatus.setReportingUnitStemmestyre(true);
		MunicipalityConfigStatusAuditEvent event = event(AuditEventTypes.Save, municipalityConfigStatus);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", municipalityConfigStatus.getMunicipalityPath().path()))
				.assertThat("$", hasEntry("localeId", municipalityConfigStatus.getLocaleId().getId()))
				.assertThat("$", hasEntry("useElectronicMarkoffs", municipalityConfigStatus.isUseElectronicMarkoffs()))
				.assertThat("$", hasEntry("listProposals", municipalityConfigStatus.isListProposals()))
				.assertThat("$", hasEntry("language", municipalityConfigStatus.isLanguage()))
				.assertThat("$", hasEntry("advancePollingPlaces", municipalityConfigStatus.isAdvancePollingPlaces()))
				.assertThat("$", hasEntry("countCategories", municipalityConfigStatus.isCountCategories()))
				.assertThat("$", hasEntry("electionCard", municipalityConfigStatus.isElectionCard()))
				.assertThat("$", hasEntry("electionPollingPlaces", municipalityConfigStatus.isElectionPollingPlaces()))
				.assertThat("$", hasEntry("reportingUnitStemmestyre", municipalityConfigStatus.isReportingUnitStemmestyre()))
				.assertThat("$", hasEntry("reportingUnitValgstyre", municipalityConfigStatus.isReportingUnitValgstyre()))
				.assertThat("$", hasEntry("pollingDistricts", municipalityConfigStatus.isPollingDistricts()))
				.assertThat("$", hasEntry("techPollingDistricts", municipalityConfigStatus.isTechPollingDistricts()))
				.assertThat("$", hasEntry("electronicMarkoffs", municipalityConfigStatus.isElectronicMarkoffs()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return MunicipalityConfigStatusAuditEvent.class;
	}

	private MunicipalityConfigStatusAuditEvent event(AuditEventTypes eventType, MunicipalityConfigStatus countyConfigStatus) {
		return new MunicipalityConfigStatusAuditEvent(createMock(UserData.class), countyConfigStatus, eventType, Outcome.Success, "");
	}
}
