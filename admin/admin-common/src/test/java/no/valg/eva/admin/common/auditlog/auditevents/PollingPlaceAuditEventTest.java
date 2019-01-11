package no.valg.eva.admin.common.auditlog.auditevents;

import static com.jayway.jsonassert.JsonAssert.with;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.ADDRESS_LINE_1;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.ADDRESS_LINE_2;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.ADDRESS_LINE_3;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.GPS_COORDINATES;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.POLLING_PLACE_INFO_TEXT;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.POSTAL_CODE;
import static no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother.POST_TOWN;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.hasEntry;

import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAsserter;


public class PollingPlaceAuditEventTest extends AbstractAuditEventTest {

	private final PollingPlace pollingPlace = new AuditLogTestsObjectMother().createPollingPlace();

	@BeforeMethod
	public void setUp() {
		pollingPlace.setElectionDayVoting(true);
		pollingPlace.setAdvanceVoteInBallotBox(true);
		pollingPlace.setUsingPollingStations(true);
	}

	@Test
	public void toJson_forSuccessfulCreatePollingPlace_isCorrect() {
		AuditEvent auditEvent = createAuditEvent(Create);
		assertPollingPlace(auditEvent)
				.assertThat("$", hasEntry("electionDayVoting", true))
				.assertThat("$", hasEntry("advanceVoteInBallotBox", true))
				.assertThat("$", hasEntry("usingPollingStations", true))
				.assertThat("$", hasEntry("addressLine1", ADDRESS_LINE_1))
				.assertThat("$", hasEntry("addressLine2", ADDRESS_LINE_2))
				.assertThat("$", hasEntry("addressLine3", ADDRESS_LINE_3))
				.assertThat("$", hasEntry("gpsCoordinates", GPS_COORDINATES))
				.assertThat("$", hasEntry("infoText", POLLING_PLACE_INFO_TEXT))
				.assertThat("$", hasEntry("postalCode", POSTAL_CODE))
				.assertThat("$", hasEntry("postTown", POST_TOWN));
	}

	@Test
	public void toJson_forSuccessfulDeletePollingPlace_isCorrect() {
		AuditEvent auditEvent = createAuditEvent(Delete);
		assertPollingPlace(auditEvent);
	}

	public JsonAsserter assertPollingPlace(AuditEvent auditEvent) {
		return with(auditEvent.toJson())
				.assertThat("$", hasEntry("path", pollingPlace.areaPath().path()))
				.assertThat("$", hasEntry("name", pollingPlace.getName()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return PollingPlaceAuditEvent.class;
	}

	private AuditEvent createAuditEvent(AuditEventTypes auditEventType) {
		return new PollingPlaceAuditEvent(
				new AuditLogTestsObjectMother().createUserData(),
				pollingPlace, auditEventType, Success, "detail");
	}

}
