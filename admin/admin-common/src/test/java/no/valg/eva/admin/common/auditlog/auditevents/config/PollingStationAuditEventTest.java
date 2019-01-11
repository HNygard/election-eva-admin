package no.valg.eva.admin.common.auditlog.auditevents.config;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.Rode;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class PollingStationAuditEventTest extends AbstractAuditEventTest {

	private static final AreaPath POLLING_PLACE_AREA_PATH = AreaPath.from("111111.22.33.4444.444400.0001");

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		PollingStationAuditEvent event = event(AuditEventTypes.Create, POLLING_PLACE_AREA_PATH, new ArrayList<>());

		assertThat(event.objectType()).isSameAs(Rode.class);
	}

	@Test
	public void toJson() throws Exception {
		PollingStationAuditEvent event = event(AuditEventTypes.Update, POLLING_PLACE_AREA_PATH,
				Arrays.asList(
						rodefordeling("0001", "a", "g", 100),
						rodefordeling("0002", "h", "m", 101),
						rodefordeling("0003", "n", "å", 200)));
		System.out.println(event.toJson());
		JsonAssert
				.with(event.toJson())
				.assertThat("$", hasEntry("path", POLLING_PLACE_AREA_PATH.path()))
				.assertThat("$.pollingStations[*]", collectionWithSize(equalTo(3)))
				.assertThat("$.pollingStations[*].id", containsInAnyOrder("0001", "0002", "0003"))
				.assertThat("$.pollingStations[*].fra", containsInAnyOrder("A", "H", "N"))
				.assertThat("$.pollingStations[*].til", containsInAnyOrder("G", "M", "Å"))
				.assertThat("$.pollingStations[*].antallVelgere", containsInAnyOrder(100, 101, 200));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return PollingStationAuditEvent.class;
	}

	private PollingStationAuditEvent event(AuditEventTypes eventType, AreaPath areaPath, List<Rode> divisionList) {
		return new PollingStationAuditEvent(createMock(UserData.class), areaPath, divisionList, eventType, Outcome.Success, "");
	}

	private Rode rodefordeling(String id, String fra, String til, int antallVelgere) {
		return new Rode(id, fra, til, antallVelgere);
	}
}

