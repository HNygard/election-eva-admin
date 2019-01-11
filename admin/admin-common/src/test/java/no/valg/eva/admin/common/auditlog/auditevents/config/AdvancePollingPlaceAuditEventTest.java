package no.valg.eva.admin.common.auditlog.auditevents.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;

public class AdvancePollingPlaceAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		AdvancePollingPlaceAuditEvent event = event(AuditEventTypes.Save, new AdvancePollingPlace(AreaPath.from("111111.22.33.4444")));

		assertThat(event.objectType()).isSameAs(AdvancePollingPlace.class);
	}

	@Test
	public void toJson() throws Exception {
		AdvancePollingPlace advancePollingPlace = new AdvancePollingPlace(AreaPath.from("111111.22.33.4444"));
		advancePollingPlace.setName("My Polling place");
		advancePollingPlace.setAdvanceVoteInBallotBox(true);
		advancePollingPlace.setPostalCode("0101");
		AdvancePollingPlaceAuditEvent event = event(AuditEventTypes.Save, advancePollingPlace);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", advancePollingPlace.getPath().path()))
				.assertThat("$", hasEntry("name", advancePollingPlace.getName()))
				.assertThat("$", hasEntry("advanceVoteInBallotBox", advancePollingPlace.isAdvanceVoteInBallotBox()))
				.assertThat("$", hasEntry("address", advancePollingPlace.getAddress()))
				.assertThat("$", hasEntry("postalCode", advancePollingPlace.getPostalCode()))
				.assertThat("$", hasEntry("postTown", advancePollingPlace.getPostTown()))
				.assertThat("$", hasEntry("gpsCoordinates", advancePollingPlace.getGpsCoordinates()))
				.assertThat("$", hasEntry("publicPlace", advancePollingPlace.isPublicPlace()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return AdvancePollingPlaceAuditEvent.class;
	}

	private AdvancePollingPlaceAuditEvent event(AuditEventTypes eventType, AdvancePollingPlace advancePollingPlace) {
		return new AdvancePollingPlaceAuditEvent(createMock(UserData.class), advancePollingPlace, eventType, Outcome.Success, "");
	}
}
