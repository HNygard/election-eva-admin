package no.valg.eva.admin.common.auditlog.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class ProposerAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test
	public void toJson_whenCreate_formatsCorrectly() {
		ProposerAuditEvent auditEvent = new ProposerAuditEvent(objectMother.createUserData(), createProposer(), AuditEventTypes.Create, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo("{\"contestName\":\"Kommunenavn\",\"partyId\":\"PRTY\",\"id\":\"16074826478\",\"function\":\"aRoleId\","
				+ "\"firstName\":\"Ola\",\"lastName\":\"Nordmann\",\"dateOfBirth\":\"1948-07-16\",\"addressLine1\":\"Nyveien 2\","
				+ "\"postalCode\":\"0042\",\"postTown\":\"Oslo\",\"displayOrder\":0}");
	}

	@Test
	public void objectType_mustReturnClassOfAuditedObject() throws Exception {
		ProposerAuditEvent auditEvent = new ProposerAuditEvent(objectMother.createUserData(), createProposer(), AuditEventTypes.Create, Outcome.Success, null);
		assertThat(auditEvent.objectType()).isEqualTo(Proposer.class);
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ProposerAuditEvent.objectClasses(AuditEventTypes.Create)).isEqualTo(new Class[] { Proposer.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(
				AuditEventFactory.getAuditEventConstructor(ProposerAuditEvent.class, ProposerAuditEvent.objectClasses(AuditEventTypes.Create),
						AuditedObjectSource.Parameters)).isNotNull();
		assertThat(
				AuditEventFactory.getAuditEventConstructor(ProposerAuditEvent.class, ProposerAuditEvent.objectClasses(AuditEventTypes.Create),
						AuditedObjectSource.Parameters)).isNotNull();
		assertThat(
				AuditEventFactory.getAuditEventConstructor(ProposerAuditEvent.class, ProposerAuditEvent.objectClasses(AuditEventTypes.DisplayOrderChanged),
						AuditedObjectSource.Parameters)).isNotNull();
	}

	private Proposer createProposer() {
		Proposer proposer = new Proposer();

		proposer.setId("16074826478");
		proposer.setFirstName("Ola");
		proposer.setLastName("Nordmann");
		
		proposer.setDateOfBirth(new LocalDate(1948, 7, 16));
		
		proposer.setAddressLine1("Nyveien 2");
		proposer.setPostalCode("0042");
		proposer.setPostTown("Oslo");

		ProposerRole proposerRole = new ProposerRole();
		proposerRole.setId("aRoleId");
		proposer.setProposerRole(proposerRole);

		Ballot ballot = new Ballot();
		Contest contest = new Contest();
		contest.setName("Kommunenavn");
		ballot.setContest(contest);
		proposer.setBallot(ballot);

		Affiliation affiliation = new Affiliation();
		Party party = new Party();
		party.setId("PRTY");
		affiliation.setParty(party);
		affiliation.setBallot(ballot);
		ballot.setAffiliation(affiliation);

		return proposer;
	}

}
