package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class CandidateAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_whenCreate_formatsCorrectly() {
		CandidateAuditEvent auditEvent = new CandidateAuditEvent(objectMother.createUserData(), createCandidate(true), AuditEventTypes.Create, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo("{\"contestName\":\"Kommunenavn\",\"partyId\":\"PRTY\",\"id\":\"16074826478\",\"firstName\":\"Ola\","
				+ "\"middleName\":null,\"lastName\":\"Nordmann\",\"dateOfBirth\":\"1948-07-16\",\"displayOrder\":0}");
	}

	@Test
	public void toJson_whenReorder_formatsCorrectly() {
		CandidateAuditEvent auditEvent = new CandidateAuditEvent(objectMother.createUserData(), createCandidate(true), 0, 1,
				AuditEventTypes.DisplayOrderChanged, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo("{\"contestName\":\"Kommunenavn\",\"partyId\":\"PRTY\",\"id\":\"16074826478\",\"firstName\":\"Ola\","
				+ "\"middleName\":null,\"lastName\":\"Nordmann\",\"dateOfBirth\":\"1948-07-16\",\"reorderFrom\":0,\"reorderTo\":1}");
	}

	@Test
	public void toJson_whenDelete_formatsCorrectly() {
		CandidateAuditEvent auditEvent = new CandidateAuditEvent(objectMother.createUserData(), createCandidate(true), AuditEventTypes.Delete, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo("{\"contestName\":\"Kommunenavn\",\"partyId\":\"PRTY\",\"id\":\"16074826478\",\"firstName\":\"Ola\","
				+ "\"middleName\":null,\"lastName\":\"Nordmann\",\"dateOfBirth\":\"1948-07-16\",\"displayOrder\":0}");
	}

	@Test
	public void toJson_whenDeleteAll_formatsCorrectly() {
		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(createCandidate(true));

		CandidateAuditEvent auditEvent = new CandidateAuditEvent(objectMother.createUserData(), candidateList, AuditEventTypes.DeleteAll, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo("[{\"contestName\":\"Kommunenavn\",\"partyId\":\"PRTY\",\"id\":\"16074826478\",\"firstName\":\"Ola\","
				+ "\"middleName\":null,\"lastName\":\"Nordmann\",\"dateOfBirth\":\"1948-07-16\",\"displayOrder\":0}]");
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		CandidateAuditEvent auditEvent = new CandidateAuditEvent(objectMother.createUserData(), createCandidate(true), AuditEventTypes.Create, Outcome.Success, null);

		assertThat(auditEvent.objectType()).isEqualTo(Candidate.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(CandidateAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[] { Candidate.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(
				AuditEventFactory.getAuditEventConstructor(CandidateAuditEvent.class, CandidateAuditEvent.objectClasses(AuditEventTypes.Delete),
						AuditedObjectSource.Parameters)).isNotNull();
	}

	private Candidate createCandidate(boolean withId) {
		Candidate candidate = new Candidate();
		if (withId) {
			candidate.setId("16074826478");
		}
		candidate.setFirstName("Ola");
		candidate.setLastName("Nordmann");
		
		candidate.setDateOfBirth(new LocalDate(1948, 7, 16));
		

		Ballot ballot = new Ballot();
		Contest contest = new Contest();
		contest.setName("Kommunenavn");
		ballot.setContest(contest);
		candidate.setBallot(ballot);

		Affiliation affiliation = new Affiliation();
		Party party = new Party();
		party.setId("PRTY");
		affiliation.setParty(party);
		affiliation.setBallot(ballot);
		candidate.setAffiliation(affiliation);

		return candidate;
	}

}
