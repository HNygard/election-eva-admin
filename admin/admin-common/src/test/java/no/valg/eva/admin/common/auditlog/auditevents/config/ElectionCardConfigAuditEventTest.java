package no.valg.eva.admin.common.auditlog.auditevents.config;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ReportingUnit;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ElectionCardConfigAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ElectionCardConfigAuditEvent event = event(AuditEventTypes.Save, electionCard());

		assertThat(event.objectType()).isSameAs(ElectionCardConfig.class);
	}

	@Test
	public void toJson_withSave_verifyJson() throws Exception {
		ElectionCardConfig card = electionCard();
		ElectionCardConfigAuditEvent event = event(AuditEventTypes.Save, card);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("infoText", card.getInfoText()))
				.assertThat("reportingUnit", hasEntry("pk", Integer.valueOf(10)))
				.assertThat("reportingUnit", hasEntry("areaPath", card.getReportingUnit().getAreaPath().path()))
				.assertThat("reportingUnit", hasEntry("type", card.getReportingUnit().getType().name()))
				.assertThat("reportingUnit", hasEntry("address", card.getReportingUnit().getAddress()))
				.assertThat("reportingUnit", hasEntry("postalCode", card.getReportingUnit().getPostalCode()))
				.assertThat("reportingUnit", hasEntry("postTown", card.getReportingUnit().getPostTown()))
				.assertThat("$.places[*]", collectionWithSize(equalTo(1)))
				.assertThat("$.places[*].pk", containsInAnyOrder(Integer.valueOf(11)))
				.assertThat("$.places[*].id", containsInAnyOrder("1000"))
				.assertThat("$.places[*].infoText", containsInAnyOrder("Info text"));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ResponsibleOfficerAuditEvent.class;
	}

	private ElectionCardConfig electionCard() {
		ReportingUnit unit = new ReportingUnit(AreaPath.from("111111.22.33.4444"), ReportingUnitTypeId.VALGSTYRET, 0);
		unit.setPk(10L);
		unit.setAddress("Address");
		unit.setPostalCode("1234");
		unit.setPostTown("By");
		ElectionCardConfig result = new ElectionCardConfig(unit, 0);
		result.setInfoText("Info text");
		result.getPlaces().add(place());
		return result;
	}

	private ElectionDayPollingPlace place() {
		ElectionDayPollingPlace result = new ElectionDayPollingPlace(AreaPath.from("111111.22.33.4444.444400.1000"));
		result.setPk(11L);
		result.setId("1000");
		result.setInfoText("Info text");
		return result;
	}

	private ElectionCardConfigAuditEvent event(AuditEventTypes eventType, ElectionCardConfig electionCard) {
		return new ElectionCardConfigAuditEvent(createMock(UserData.class), electionCard, eventType, Outcome.Success, "");
	}

}

