package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Party;

import org.testng.annotations.Test;

public class BallotAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_whenDelete_isCorrect() throws Exception {
		BallotAuditEvent auditEvent = new BallotAuditEvent(objectMother.createUserData(), createBallot(), AuditEventTypes.Delete, Outcome.Success, null);
		String json = auditEvent.toJson();
		assertThat(json).isEqualTo("{\"partyId\":\"Test\",\"contestName\":\"Kommune- og fylkestingsvalget 2015\",\"status\":\"APPROVED\"}");
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		BallotAuditEvent auditEvent = new BallotAuditEvent(objectMother.createUserData(), createBallot(), AuditEventTypes.Delete, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Ballot.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(BallotAuditEvent.objectClasses(AuditEventTypes.Delete)).isEqualTo(new Class[] { Ballot.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(BallotAuditEvent.class, BallotAuditEvent.objectClasses(AuditEventTypes.Delete),
						AuditedObjectSource.Parameters)).isNotNull();
	}

	private Ballot createBallot() {
		Ballot ballot = new Ballot();

		BallotStatus ballotStatus = new BallotStatus();
		ballotStatus.setId(BallotStatus.BallotStatusValue.APPROVED.getId());
		ballot.setBallotStatus(ballotStatus);

		ballot.setContest(objectMother.createContest());

		Affiliation affiliation = new Affiliation();
		ballot.setAffiliation(affiliation);

		Party party = new Party();
		party.setId("Test");
		affiliation.setParty(party);

		return ballot;
	}

}
